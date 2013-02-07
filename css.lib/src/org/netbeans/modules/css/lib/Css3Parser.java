// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-02-07 15:30:47

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'", "'...'", "'@rest...'"
    };
    public static final int EOF=-1;
    public static final int T__122=122;
    public static final int T__123=123;
    public static final int T__124=124;
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
    public static final int LESS_AND=59;
    public static final int OPEQ=60;
    public static final int INCLUDES=61;
    public static final int DASHMATCH=62;
    public static final int BEGINS=63;
    public static final int ENDS=64;
    public static final int CONTAINS=65;
    public static final int RBRACKET=66;
    public static final int LPAREN=67;
    public static final int RPAREN=68;
    public static final int IMPORTANT_SYM=69;
    public static final int NUMBER=70;
    public static final int LENGTH=71;
    public static final int EMS=72;
    public static final int REM=73;
    public static final int EXS=74;
    public static final int ANGLE=75;
    public static final int TIME=76;
    public static final int FREQ=77;
    public static final int RESOLUTION=78;
    public static final int DIMENSION=79;
    public static final int NL=80;
    public static final int COMMENT=81;
    public static final int LESS_WHEN=82;
    public static final int GREATER_OR_EQ=83;
    public static final int LESS=84;
    public static final int LESS_OR_EQ=85;
    public static final int HEXCHAR=86;
    public static final int NONASCII=87;
    public static final int UNICODE=88;
    public static final int ESCAPE=89;
    public static final int NMSTART=90;
    public static final int NMCHAR=91;
    public static final int URL=92;
    public static final int A=93;
    public static final int B=94;
    public static final int C=95;
    public static final int D=96;
    public static final int E=97;
    public static final int F=98;
    public static final int G=99;
    public static final int H=100;
    public static final int I=101;
    public static final int J=102;
    public static final int K=103;
    public static final int L=104;
    public static final int M=105;
    public static final int N=106;
    public static final int O=107;
    public static final int P=108;
    public static final int Q=109;
    public static final int R=110;
    public static final int S=111;
    public static final int T=112;
    public static final int U=113;
    public static final int V=114;
    public static final int W=115;
    public static final int X=116;
    public static final int Y=117;
    public static final int Z=118;
    public static final int CDO=119;
    public static final int CDC=120;
    public static final int INVALID=121;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "imports", "elementName", "namespacePrefixName", 
        "less_expression", "combinator", "less_mixin_call", "less_function_in_condition", 
        "prio", "less_variable", "vendorAtRule", "page", "declarations", 
        "elementSubsequent", "expression", "syncToDeclarationsRule", "webkitKeyframesBlock", 
        "term", "generic_at_rule", "declarationPredicate", "body", "slAttributeName", 
        "slAttribute", "mediaExpression", "mediaType", "typeSelector", "atRuleId", 
        "pseudoPage", "charSetValue", "operator", "less_mixin_declaration", 
        "slAttributeValue", "mediaQueryList", "webkitKeyframeSelectors", 
        "namespaces", "hexColor", "less_function", "less_args_list", "nsPred", 
        "synpred2_Css3", "less_variable_declaration", "syncTo_RBRACE", "synpred3_Css3", 
        "margin_sym", "synpred4_Css3", "less_fn_name", "ws", "moz_document_function", 
        "synpred5_Css3", "less_mixin_call_args", "charSet", "syncToFollow", 
        "declaration", "media", "less_mixin_guarded", "moz_document", "fnAttribute", 
        "importItem", "resourceIdentifier", "namespace", "function", "bodyItem", 
        "rulePredicate", "functionName", "selector", "fontFace", "fnAttributeValue", 
        "styleSheet", "unaryOperator", "mediaFeature", "mediaQuery", "less_arg", 
        "pseudo", "rule", "selectorsGroup", "fnAttributeName", "lastDeclarationPredicate", 
        "simpleSelectorSequence", "esPred", "cssId", "synpred1_Css3", "less_condition_operator", 
        "cssClass", "less_condition", "counterStyle", "mediaQueryOperator", 
        "propertyValue", "less_expression_operator", "webkitKeyframes", 
        "property", "namespacePrefix", "margin"
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
            false, false, false, false, false, false, false, false, true, 
            false, false, false, false, false, false, false, false, false, 
            true, false, true, false, true, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
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

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=AT_IDENT)||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==LESS_AND||LA6_0==122) ) {
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

                if ( (LA23_0==IDENT||(LA23_0>=GEN && LA23_0<=AT_IDENT)||LA23_0==MOZ_DOCUMENT_SYM||LA23_0==WEBKIT_KEYFRAMES_SYM||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==LESS_AND||LA23_0==122) ) {
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
            	    case LESS_AND:
            	    case 122:
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

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=AT_IDENT)||LA41_0==MOZ_DOCUMENT_SYM||LA41_0==WEBKIT_KEYFRAMES_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==LESS_AND||LA41_0==122) ) {
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

            if ( (LA53_0==IDENT||LA53_0==MEDIA_SYM||(LA53_0>=GEN && LA53_0<=AT_IDENT)||LA53_0==MOZ_DOCUMENT_SYM||LA53_0==WEBKIT_KEYFRAMES_SYM||(LA53_0>=PAGE_SYM && LA53_0<=FONT_FACE_SYM)||LA53_0==COLON||(LA53_0>=HASH && LA53_0<=PIPE)||LA53_0==LESS_AND||LA53_0==122) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
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

            dbg.location(440,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock994); if (state.failed) return ;
            dbg.location(440,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:10: ws
                    {
                    dbg.location(440,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock997);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(440,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1000);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1004);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(442,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1007); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(443, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(447,2);
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

            dbg.location(447,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(447,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:27: ws
            	            {
            	            dbg.location(447,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1034);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(447,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1037); if (state.failed) return ;
            	    dbg.location(447,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:37: ws
            	            {
            	            dbg.location(447,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1039);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(62);}

            	    dbg.location(447,41);
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
        dbg.location(448, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(450, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(451,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1071); if (state.failed) return ;
            dbg.location(451,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:16: ws
                    {
                    dbg.location(451,16);
                    pushFollow(FOLLOW_ws_in_page1073);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(451,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:20: ( IDENT ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:22: IDENT ( ws )?
                    {
                    dbg.location(451,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1078); if (state.failed) return ;
                    dbg.location(451,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:28: ws
                            {
                            dbg.location(451,28);
                            pushFollow(FOLLOW_ws_in_page1080);
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

            dbg.location(451,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:35: ( pseudoPage ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:36: pseudoPage ( ws )?
                    {
                    dbg.location(451,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1087);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(451,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:47: ws
                            {
                            dbg.location(451,47);
                            pushFollow(FOLLOW_ws_in_page1089);
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

            dbg.location(452,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1102); if (state.failed) return ;
            dbg.location(452,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:16: ws
                    {
                    dbg.location(452,16);
                    pushFollow(FOLLOW_ws_in_page1104);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(456,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:13: ( declaration | margin ( ws )? )?
            int alt71=3;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT||LA71_0==MEDIA_SYM||(LA71_0>=GEN && LA71_0<=AT_IDENT)||LA71_0==STAR) ) {
                alt71=1;
            }
            else if ( ((LA71_0>=TOPLEFTCORNER_SYM && LA71_0<=RIGHTBOTTOM_SYM)) ) {
                alt71=2;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:14: declaration
                    {
                    dbg.location(456,14);
                    pushFollow(FOLLOW_declaration_in_page1159);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:26: margin ( ws )?
                    {
                    dbg.location(456,26);
                    pushFollow(FOLLOW_margin_in_page1161);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(456,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:33: ws
                            {
                            dbg.location(456,33);
                            pushFollow(FOLLOW_ws_in_page1163);
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

            dbg.location(456,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(456,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1169); if (state.failed) return ;
            	    dbg.location(456,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:45: ws
            	            {
            	            dbg.location(456,45);
            	            pushFollow(FOLLOW_ws_in_page1171);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(456,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:49: ( declaration | margin ( ws )? )?
            	    int alt74=3;
            	    try { dbg.enterSubRule(74);
            	    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            	    int LA74_0 = input.LA(1);

            	    if ( (LA74_0==IDENT||LA74_0==MEDIA_SYM||(LA74_0>=GEN && LA74_0<=AT_IDENT)||LA74_0==STAR) ) {
            	        alt74=1;
            	    }
            	    else if ( ((LA74_0>=TOPLEFTCORNER_SYM && LA74_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt74=2;
            	    }
            	    } finally {dbg.exitDecision(74);}

            	    switch (alt74) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:50: declaration
            	            {
            	            dbg.location(456,50);
            	            pushFollow(FOLLOW_declaration_in_page1175);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:62: margin ( ws )?
            	            {
            	            dbg.location(456,62);
            	            pushFollow(FOLLOW_margin_in_page1177);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(456,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:69: ws
            	                    {
            	                    dbg.location(456,69);
            	                    pushFollow(FOLLOW_ws_in_page1179);
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

            dbg.location(457,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1194); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(458, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(460, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(461,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1215); if (state.failed) return ;
            dbg.location(461,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:25: ws
                    {
                    dbg.location(461,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1217);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(461,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1220); if (state.failed) return ;
            dbg.location(461,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:35: ws
                    {
                    dbg.location(461,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1222);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(462,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1233); if (state.failed) return ;
            dbg.location(462,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:16: ws
                    {
                    dbg.location(462,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1235);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(462,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1238);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(463,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1242);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(464,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1252); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(465, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(467, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(468,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1273); if (state.failed) return ;
            dbg.location(468,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:21: ws
                    {
                    dbg.location(468,21);
                    pushFollow(FOLLOW_ws_in_fontFace1275);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(469,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1286); if (state.failed) return ;
            dbg.location(469,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:16: ws
                    {
                    dbg.location(469,16);
                    pushFollow(FOLLOW_ws_in_fontFace1288);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(469,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1291);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(470,3);
            pushFollow(FOLLOW_declarations_in_fontFace1295);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(471,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1305); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(472, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(474, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(475,4);
            pushFollow(FOLLOW_margin_sym_in_margin1320);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(475,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:15: ws
                    {
                    dbg.location(475,15);
                    pushFollow(FOLLOW_ws_in_margin1322);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(475,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1325); if (state.failed) return ;
            dbg.location(475,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:26: ws
                    {
                    dbg.location(475,26);
                    pushFollow(FOLLOW_ws_in_margin1327);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(475,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1330);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(475,53);
            pushFollow(FOLLOW_declarations_in_margin1332);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(475,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1334); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(476, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(478, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(479,2);
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
        dbg.location(496, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(498, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:7: COLON IDENT
            {
            dbg.location(499,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1563); if (state.failed) return ;
            dbg.location(499,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1565); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(500, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(502, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(503,5);
            if ( input.LA(1)==COMMA||input.LA(1)==SOLIDUS ) {
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
            int alt86=4;
            try { dbg.enterDecision(86, decisionCanBacktrack[86]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt86=1;
                }
                break;
            case GREATER:
                {
                alt86=2;
                }
                break;
            case TILDE:
                {
                alt86=3;
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
            case LESS_AND:
            case 122:
                {
                alt86=4;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:7: PLUS ( ws )?
                    {
                    dbg.location(508,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1615); if (state.failed) return ;
                    dbg.location(508,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:12: ws
                            {
                            dbg.location(508,12);
                            pushFollow(FOLLOW_ws_in_combinator1617);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:7: GREATER ( ws )?
                    {
                    dbg.location(509,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1626); if (state.failed) return ;
                    dbg.location(509,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:15: ws
                            {
                            dbg.location(509,15);
                            pushFollow(FOLLOW_ws_in_combinator1628);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:7: TILDE ( ws )?
                    {
                    dbg.location(510,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1637); if (state.failed) return ;
                    dbg.location(510,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:13: ( ws )?
                    int alt85=2;
                    try { dbg.enterSubRule(85);
                    try { dbg.enterDecision(85, decisionCanBacktrack[85]);

                    int LA85_0 = input.LA(1);

                    if ( (LA85_0==WS||(LA85_0>=NL && LA85_0<=COMMENT)) ) {
                        alt85=1;
                    }
                    } finally {dbg.exitDecision(85);}

                    switch (alt85) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:13: ws
                            {
                            dbg.location(510,13);
                            pushFollow(FOLLOW_ws_in_combinator1639);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(85);}


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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:519:1: property : ( IDENT | GEN | less_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(519, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:5: ( ( IDENT | GEN | less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:7: ( IDENT | GEN | less_variable ) ( ws )?
            {
            dbg.location(520,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:7: ( IDENT | GEN | less_variable )
            int alt87=3;
            try { dbg.enterSubRule(87);
            try { dbg.enterDecision(87, decisionCanBacktrack[87]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt87=1;
                }
                break;
            case GEN:
                {
                alt87=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
                {
                alt87=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(87);}

            switch (alt87) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:8: IDENT
                    {
                    dbg.location(520,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property1700); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:16: GEN
                    {
                    dbg.location(520,16);
                    match(input,GEN,FOLLOW_GEN_in_property1704); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:22: less_variable
                    {
                    dbg.location(520,22);
                    pushFollow(FOLLOW_less_variable_in_property1708);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(520,37);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:37: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:37: ws
                    {
                    dbg.location(520,37);
                    pushFollow(FOLLOW_ws_in_property1711);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(88);}


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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:1: rule : ( selectorsGroup | less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(523, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:5: ( ( selectorsGroup | less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:9: ( selectorsGroup | less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(524,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:9: ( selectorsGroup | less_mixin_declaration )
            int alt89=2;
            try { dbg.enterSubRule(89);
            try { dbg.enterDecision(89, decisionCanBacktrack[89]);

            try {
                isCyclicDecision = true;
                alt89 = dfa89.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(89);}

            switch (alt89) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:11: selectorsGroup
                    {
                    dbg.location(524,11);
                    pushFollow(FOLLOW_selectorsGroup_in_rule1738);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:28: less_mixin_declaration
                    {
                    dbg.location(524,28);
                    pushFollow(FOLLOW_less_mixin_declaration_in_rule1742);
                    less_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(526,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1755); if (state.failed) return ;
            dbg.location(526,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:16: ws
                    {
                    dbg.location(526,16);
                    pushFollow(FOLLOW_ws_in_rule1757);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(526,20);
            pushFollow(FOLLOW_syncToFollow_in_rule1760);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(527,13);
            pushFollow(FOLLOW_declarations_in_rule1774);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(528,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1784); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(529, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:1: declarations : ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call ) ( ws )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(537, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:5: ( ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call ) ( ws )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:10: ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call ) ( ws )? )*
            {
            dbg.location(539,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:10: ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call ) ( ws )? )*
            try { dbg.enterSubRule(93);

            loop93:
            do {
                int alt93=2;
                try { dbg.enterDecision(93, decisionCanBacktrack[93]);

                int LA93_0 = input.LA(1);

                if ( (LA93_0==IDENT||LA93_0==MEDIA_SYM||(LA93_0>=GEN && LA93_0<=AT_IDENT)||LA93_0==COLON||(LA93_0>=HASH && LA93_0<=PIPE)||LA93_0==LESS_AND||LA93_0==122) ) {
                    alt93=1;
                }


                } finally {dbg.exitDecision(93);}

                switch (alt93) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call ) ( ws )?
            	    {
            	    dbg.location(540,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call )
            	    int alt91=3;
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:17: ( rulePredicate )=> rule
            	            {
            	            dbg.location(541,34);
            	            pushFollow(FOLLOW_rule_in_declarations1862);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:17: ( ( declarationPredicate )=> declaration SEMI )
            	            {
            	            dbg.location(543,17);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:17: ( ( declarationPredicate )=> declaration SEMI )
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:19: ( declarationPredicate )=> declaration SEMI
            	            {
            	            dbg.location(543,43);
            	            pushFollow(FOLLOW_declaration_in_declarations1905);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(543,55);
            	            match(input,SEMI,FOLLOW_SEMI_in_declarations1907); if (state.failed) return ;

            	            }


            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:17: less_mixin_call
            	            {
            	            dbg.location(545,17);
            	            pushFollow(FOLLOW_less_mixin_call_in_declarations1945);
            	            less_mixin_call();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(91);}

            	    dbg.location(547,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:13: ( ws )?
            	    int alt92=2;
            	    try { dbg.enterSubRule(92);
            	    try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            	    int LA92_0 = input.LA(1);

            	    if ( (LA92_0==WS||(LA92_0>=NL && LA92_0<=COMMENT)) ) {
            	        alt92=1;
            	    }
            	    } finally {dbg.exitDecision(92);}

            	    switch (alt92) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:13: ws
            	            {
            	            dbg.location(547,13);
            	            pushFollow(FOLLOW_ws_in_declarations1985);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(92);}


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
        dbg.location(551, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarations"


    // $ANTLR start "rulePredicate"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:1: rulePredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE ;
    public final void rulePredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rulePredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(553, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:5: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
            {
            dbg.location(557,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt94=0;
            try { dbg.enterSubRule(94);

            loop94:
            do {
                int alt94=2;
                try { dbg.enterDecision(94, decisionCanBacktrack[94]);

                int LA94_0 = input.LA(1);

                if ( ((LA94_0>=NAMESPACE_SYM && LA94_0<=CHARSET_SYM)||(LA94_0>=IMPORT_SYM && LA94_0<=MEDIA_SYM)||(LA94_0>=COMMA && LA94_0<=124)) ) {
                    alt94=1;
                }


                } finally {dbg.exitDecision(94);}

                switch (alt94) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(557,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=124) ) {
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
            	    if ( cnt94 >= 1 ) break loop94;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(94, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt94++;
            } while (true);
            } finally {dbg.exitSubRule(94);}

            dbg.location(557,37);
            match(input,LBRACE,FOLLOW_LBRACE_in_rulePredicate2067); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(558, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "rulePredicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "rulePredicate"


    // $ANTLR start "declarationPredicate"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:1: declarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI ;
    public final void declarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(560, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:5: (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI
            {
            dbg.location(564,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt95=0;
            try { dbg.enterSubRule(95);

            loop95:
            do {
                int alt95=2;
                try { dbg.enterDecision(95, decisionCanBacktrack[95]);

                int LA95_0 = input.LA(1);

                if ( ((LA95_0>=NAMESPACE_SYM && LA95_0<=CHARSET_SYM)||(LA95_0>=IMPORT_SYM && LA95_0<=MEDIA_SYM)||(LA95_0>=COMMA && LA95_0<=124)) ) {
                    alt95=1;
                }


                } finally {dbg.exitDecision(95);}

                switch (alt95) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(564,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=124) ) {
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
            	    if ( cnt95 >= 1 ) break loop95;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(95, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt95++;
            } while (true);
            } finally {dbg.exitSubRule(95);}

            dbg.location(564,37);
            match(input,SEMI,FOLLOW_SEMI_in_declarationPredicate2127); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(565, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declarationPredicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarationPredicate"


    // $ANTLR start "lastDeclarationPredicate"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:1: lastDeclarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE ;
    public final void lastDeclarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "lastDeclarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(567, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:5: (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE
            {
            dbg.location(571,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt96=0;
            try { dbg.enterSubRule(96);

            loop96:
            do {
                int alt96=2;
                try { dbg.enterDecision(96, decisionCanBacktrack[96]);

                int LA96_0 = input.LA(1);

                if ( ((LA96_0>=NAMESPACE_SYM && LA96_0<=CHARSET_SYM)||(LA96_0>=IMPORT_SYM && LA96_0<=MEDIA_SYM)||(LA96_0>=COMMA && LA96_0<=124)) ) {
                    alt96=1;
                }


                } finally {dbg.exitDecision(96);}

                switch (alt96) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(571,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=124) ) {
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
            	    if ( cnt96 >= 1 ) break loop96;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(96, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt96++;
            } while (true);
            } finally {dbg.exitSubRule(96);}

            dbg.location(571,37);
            match(input,RBRACE,FOLLOW_RBRACE_in_lastDeclarationPredicate2187); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(572, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "lastDeclarationPredicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "lastDeclarationPredicate"


    // $ANTLR start "selectorsGroup"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(574, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(575,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup2208);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(575,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(98);

            loop98:
            do {
                int alt98=2;
                try { dbg.enterDecision(98, decisionCanBacktrack[98]);

                int LA98_0 = input.LA(1);

                if ( (LA98_0==COMMA) ) {
                    alt98=1;
                }


                } finally {dbg.exitDecision(98);}

                switch (alt98) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(575,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2211); if (state.failed) return ;
            	    dbg.location(575,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:23: ( ws )?
            	    int alt97=2;
            	    try { dbg.enterSubRule(97);
            	    try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            	    int LA97_0 = input.LA(1);

            	    if ( (LA97_0==WS||(LA97_0>=NL && LA97_0<=COMMENT)) ) {
            	        alt97=1;
            	    }
            	    } finally {dbg.exitDecision(97);}

            	    switch (alt97) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:23: ws
            	            {
            	            dbg.location(575,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup2213);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(97);}

            	    dbg.location(575,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup2216);
            	    selector();

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
        dbg.location(576, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(578, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(579,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2239);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(579,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(99);

            loop99:
            do {
                int alt99=2;
                try { dbg.enterDecision(99, decisionCanBacktrack[99]);

                int LA99_0 = input.LA(1);

                if ( (LA99_0==IDENT||LA99_0==GEN||LA99_0==COLON||(LA99_0>=PLUS && LA99_0<=TILDE)||(LA99_0>=HASH && LA99_0<=PIPE)||LA99_0==LESS_AND||LA99_0==122) ) {
                    alt99=1;
                }


                } finally {dbg.exitDecision(99);}

                switch (alt99) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(579,31);
            	    pushFollow(FOLLOW_combinator_in_selector2242);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(579,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2244);
            	    simpleSelectorSequence();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(580, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(583, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt104=2;
            try { dbg.enterDecision(104, decisionCanBacktrack[104]);

            int LA104_0 = input.LA(1);

            if ( (LA104_0==IDENT||LA104_0==GEN||(LA104_0>=STAR && LA104_0<=PIPE)||LA104_0==LESS_AND) ) {
                alt104=1;
            }
            else if ( (LA104_0==COLON||(LA104_0>=HASH && LA104_0<=DCOLON)||LA104_0==122) ) {
                alt104=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 104, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(104);}

            switch (alt104) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(586,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(586,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2277);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(586,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(101);

                    loop101:
                    do {
                        int alt101=2;
                        try { dbg.enterDecision(101, decisionCanBacktrack[101]);

                        try {
                            isCyclicDecision = true;
                            alt101 = dfa101.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(101);}

                        switch (alt101) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(586,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2284);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(586,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:46: ( ws )?
                    	    int alt100=2;
                    	    try { dbg.enterSubRule(100);
                    	    try { dbg.enterDecision(100, decisionCanBacktrack[100]);

                    	    int LA100_0 = input.LA(1);

                    	    if ( (LA100_0==WS||(LA100_0>=NL && LA100_0<=COMMENT)) ) {
                    	        alt100=1;
                    	    }
                    	    } finally {dbg.exitDecision(100);}

                    	    switch (alt100) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:46: ws
                    	            {
                    	            dbg.location(586,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2286);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(100);}


                    	    }
                    	    break;

                    	default :
                    	    break loop101;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(101);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(588,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(588,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt103=0;
                    try { dbg.enterSubRule(103);

                    loop103:
                    do {
                        int alt103=2;
                        try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA103_2 = input.LA(2);

                            if ( (synpred4_Css3()) ) {
                                alt103=1;
                            }


                            }
                            break;
                        case 122:
                            {
                            int LA103_3 = input.LA(2);

                            if ( (synpred4_Css3()) ) {
                                alt103=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA103_4 = input.LA(2);

                            if ( (synpred4_Css3()) ) {
                                alt103=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA103_5 = input.LA(2);

                            if ( (synpred4_Css3()) ) {
                                alt103=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA103_6 = input.LA(2);

                            if ( (synpred4_Css3()) ) {
                                alt103=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(103);}

                        switch (alt103) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(588,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2305);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(588,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:33: ws
                    	            {
                    	            dbg.location(588,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2307);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(102);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt103 >= 1 ) break loop103;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(103, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt103++;
                    } while (true);
                    } finally {dbg.exitSubRule(103);}


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
        dbg.location(589, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(596, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(597,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==122 ) {
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
        dbg.location(598, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(602,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:6: ( ( nsPred )=> namespacePrefix )?
            int alt105=2;
            try { dbg.enterSubRule(105);
            try { dbg.enterDecision(105, decisionCanBacktrack[105]);

            int LA105_0 = input.LA(1);

            if ( (LA105_0==IDENT) ) {
                int LA105_1 = input.LA(2);

                if ( (synpred5_Css3()) ) {
                    alt105=1;
                }
            }
            else if ( (LA105_0==STAR) ) {
                int LA105_2 = input.LA(2);

                if ( (synpred5_Css3()) ) {
                    alt105=1;
                }
            }
            else if ( (LA105_0==PIPE) && (synpred5_Css3())) {
                alt105=1;
            }
            } finally {dbg.exitDecision(105);}

            switch (alt105) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(602,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2410);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(602,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:37: elementName ( ws )?
            {
            dbg.location(602,37);
            pushFollow(FOLLOW_elementName_in_typeSelector2416);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(602,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:49: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:49: ws
                    {
                    dbg.location(602,49);
                    pushFollow(FOLLOW_ws_in_typeSelector2418);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(604, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(607, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(609,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:3: ( IDENT | STAR )?
            int alt107=2;
            try { dbg.enterSubRule(107);
            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

            int LA107_0 = input.LA(1);

            if ( (LA107_0==IDENT||LA107_0==STAR) ) {
                alt107=1;
            }
            } finally {dbg.exitDecision(107);}

            switch (alt107) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(609,3);
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
            } finally {dbg.exitSubRule(107);}

            dbg.location(609,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred2448); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(610, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(612, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(613,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:5: ( namespacePrefixName | STAR )?
            int alt108=3;
            try { dbg.enterSubRule(108);
            try { dbg.enterDecision(108, decisionCanBacktrack[108]);

            int LA108_0 = input.LA(1);

            if ( (LA108_0==IDENT) ) {
                alt108=1;
            }
            else if ( (LA108_0==STAR) ) {
                alt108=2;
            }
            } finally {dbg.exitDecision(108);}

            switch (alt108) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:7: namespacePrefixName
                    {
                    dbg.location(613,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2463);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:29: STAR
                    {
                    dbg.location(613,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2467); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(108);}

            dbg.location(613,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2471); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(614, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(617, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(619,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt109=4;
            try { dbg.enterSubRule(109);
            try { dbg.enterDecision(109, decisionCanBacktrack[109]);

            switch ( input.LA(1) ) {
            case HASH:
            case 122:
                {
                alt109=1;
                }
                break;
            case DOT:
                {
                alt109=2;
                }
                break;
            case LBRACKET:
                {
                alt109=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt109=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 109, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(109);}

            switch (alt109) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:6: cssId
                    {
                    dbg.location(620,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2505);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:8: cssClass
                    {
                    dbg.location(621,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2514);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:11: slAttribute
                    {
                    dbg.location(622,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2526);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:11: pseudo
                    {
                    dbg.location(623,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2538);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(109);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(625, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(628, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:5: ( HASH | ( '#' NAME ) )
            int alt110=2;
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            int LA110_0 = input.LA(1);

            if ( (LA110_0==HASH) ) {
                alt110=1;
            }
            else if ( (LA110_0==122) ) {
                alt110=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 110, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(110);}

            switch (alt110) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:7: HASH
                    {
                    dbg.location(629,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2566); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:14: ( '#' NAME )
                    {
                    dbg.location(629,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:16: '#' NAME
                    {
                    dbg.location(629,16);
                    match(input,122,FOLLOW_122_in_cssId2572); if (state.failed) return ;
                    dbg.location(629,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2574); if (state.failed) return ;

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
        dbg.location(630, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(636, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:7: DOT ( IDENT | GEN )
            {
            dbg.location(637,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2602); if (state.failed) return ;
            dbg.location(637,11);
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
        dbg.location(638, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:1: elementName : ( ( IDENT | GEN | LESS_AND ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(645, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:5: ( ( IDENT | GEN | LESS_AND ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(646,5);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN||input.LA(1)==STAR||input.LA(1)==LESS_AND ) {
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
        dbg.location(647, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(649, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(650,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2676); if (state.failed) return ;
            dbg.location(651,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:6: ( namespacePrefix )?
            int alt111=2;
            try { dbg.enterSubRule(111);
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( (LA111_0==IDENT) ) {
                int LA111_1 = input.LA(2);

                if ( (LA111_1==PIPE) ) {
                    alt111=1;
                }
            }
            else if ( ((LA111_0>=STAR && LA111_0<=PIPE)) ) {
                alt111=1;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:6: namespacePrefix
                    {
                    dbg.location(651,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2683);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(651,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:23: ws
                    {
                    dbg.location(651,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2686);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}

            dbg.location(652,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2697);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(652,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:25: ws
                    {
                    dbg.location(652,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2699);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}

            dbg.location(654,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt116=2;
            try { dbg.enterSubRule(116);
            try { dbg.enterDecision(116, decisionCanBacktrack[116]);

            int LA116_0 = input.LA(1);

            if ( ((LA116_0>=OPEQ && LA116_0<=CONTAINS)) ) {
                alt116=1;
            }
            } finally {dbg.exitDecision(116);}

            switch (alt116) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(655,17);
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

                    dbg.location(663,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:17: ws
                            {
                            dbg.location(663,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2921);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(114);}

                    dbg.location(664,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2940);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(665,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:17: ws
                            {
                            dbg.location(665,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2958);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(115);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(116);}

            dbg.location(668,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2987); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(669, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(676, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:4: IDENT
            {
            dbg.location(677,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3003); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(678, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(680, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:2: ( IDENT | STRING )
            {
            dbg.location(682,2);
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
        dbg.location(686, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(688, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(689,7);
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

            dbg.location(690,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt124=2;
            try { dbg.enterSubRule(124);
            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

            int LA124_0 = input.LA(1);

            if ( (LA124_0==IDENT||LA124_0==GEN) ) {
                alt124=1;
            }
            else if ( (LA124_0==NOT) ) {
                alt124=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 124, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(124);}

            switch (alt124) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(691,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(692,21);
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

                    dbg.location(693,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    int alt120=2;
                    try { dbg.enterSubRule(120);
                    try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                    try {
                        isCyclicDecision = true;
                        alt120 = dfa120.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(120);}

                    switch (alt120) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(694,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:25: ( ws )?
                            int alt117=2;
                            try { dbg.enterSubRule(117);
                            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

                            int LA117_0 = input.LA(1);

                            if ( (LA117_0==WS||(LA117_0>=NL && LA117_0<=COMMENT)) ) {
                                alt117=1;
                            }
                            } finally {dbg.exitDecision(117);}

                            switch (alt117) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:25: ws
                                    {
                                    dbg.location(694,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3198);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(117);}

                            dbg.location(694,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3201); if (state.failed) return ;
                            dbg.location(694,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:36: ( ws )?
                            int alt118=2;
                            try { dbg.enterSubRule(118);
                            try { dbg.enterDecision(118, decisionCanBacktrack[118]);

                            int LA118_0 = input.LA(1);

                            if ( (LA118_0==WS||(LA118_0>=NL && LA118_0<=COMMENT)) ) {
                                alt118=1;
                            }
                            } finally {dbg.exitDecision(118);}

                            switch (alt118) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:36: ws
                                    {
                                    dbg.location(694,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3203);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(118);}

                            dbg.location(694,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:40: ( expression | '*' )?
                            int alt119=3;
                            try { dbg.enterSubRule(119);
                            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

                            int LA119_0 = input.LA(1);

                            if ( ((LA119_0>=IDENT && LA119_0<=URI)||LA119_0==MEDIA_SYM||(LA119_0>=GEN && LA119_0<=AT_IDENT)||LA119_0==PERCENTAGE||LA119_0==PLUS||(LA119_0>=MINUS && LA119_0<=HASH)||(LA119_0>=NUMBER && LA119_0<=DIMENSION)) ) {
                                alt119=1;
                            }
                            else if ( (LA119_0==STAR) ) {
                                alt119=2;
                            }
                            } finally {dbg.exitDecision(119);}

                            switch (alt119) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:42: expression
                                    {
                                    dbg.location(694,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3208);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:55: '*'
                                    {
                                    dbg.location(694,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3212); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(119);}

                            dbg.location(694,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3217); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(698,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(698,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3296); if (state.failed) return ;
                    dbg.location(698,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:23: ws
                            {
                            dbg.location(698,23);
                            pushFollow(FOLLOW_ws_in_pseudo3298);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}

                    dbg.location(698,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3301); if (state.failed) return ;
                    dbg.location(698,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:34: ws
                            {
                            dbg.location(698,34);
                            pushFollow(FOLLOW_ws_in_pseudo3303);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(122);}

                    dbg.location(698,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:38: ( simpleSelectorSequence )?
                    int alt123=2;
                    try { dbg.enterSubRule(123);
                    try { dbg.enterDecision(123, decisionCanBacktrack[123]);

                    int LA123_0 = input.LA(1);

                    if ( (LA123_0==IDENT||LA123_0==GEN||LA123_0==COLON||(LA123_0>=HASH && LA123_0<=PIPE)||LA123_0==LESS_AND||LA123_0==122) ) {
                        alt123=1;
                    }
                    } finally {dbg.exitDecision(123);}

                    switch (alt123) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:38: simpleSelectorSequence
                            {
                            dbg.location(698,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3306);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(123);}

                    dbg.location(698,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3309); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(124);}


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
            dbg.exitRule(getGrammarFileName(), "pseudo");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudo"


    // $ANTLR start "declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(702, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:5: ( STAR )? property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(705,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:5: ( STAR )?
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            int LA125_0 = input.LA(1);

            if ( (LA125_0==STAR) ) {
                alt125=1;
            }
            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:5: STAR
                    {
                    dbg.location(705,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3353); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(705,11);
            pushFollow(FOLLOW_property_in_declaration3356);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(705,20);
            match(input,COLON,FOLLOW_COLON_in_declaration3358); if (state.failed) return ;
            dbg.location(705,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:26: ws
                    {
                    dbg.location(705,26);
                    pushFollow(FOLLOW_ws_in_declaration3360);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(705,30);
            pushFollow(FOLLOW_propertyValue_in_declaration3363);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(705,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:44: ( prio )?
            int alt127=2;
            try { dbg.enterSubRule(127);
            try { dbg.enterDecision(127, decisionCanBacktrack[127]);

            int LA127_0 = input.LA(1);

            if ( (LA127_0==IMPORTANT_SYM) ) {
                alt127=1;
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:44: prio
                    {
                    dbg.location(705,44);
                    pushFollow(FOLLOW_prio_in_declaration3365);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}


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
        dbg.location(706, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:1: propertyValue : ( expression | less_function );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(714, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:2: ( expression | less_function )
            int alt128=2;
            try { dbg.enterDecision(128, decisionCanBacktrack[128]);

            int LA128_0 = input.LA(1);

            if ( ((LA128_0>=IDENT && LA128_0<=URI)||LA128_0==MEDIA_SYM||(LA128_0>=GEN && LA128_0<=AT_IDENT)||LA128_0==PERCENTAGE||LA128_0==PLUS||(LA128_0>=MINUS && LA128_0<=HASH)||(LA128_0>=NUMBER && LA128_0<=DIMENSION)) ) {
                alt128=1;
            }
            else if ( (LA128_0==LPAREN) ) {
                alt128=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 128, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(128);}

            switch (alt128) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:4: expression
                    {
                    dbg.location(715,4);
                    pushFollow(FOLLOW_expression_in_propertyValue3389);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:17: less_function
                    {
                    dbg.location(715,17);
                    pushFollow(FOLLOW_less_function_in_propertyValue3393);
                    less_function();

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
        dbg.location(716, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(720, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:6: 
            {
            }

        }
        finally {
        }
        dbg.location(726, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:728:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(728, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:6: 
            {
            }

        }
        finally {
        }
        dbg.location(733, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(736, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:6: 
            {
            }

        }
        finally {
        }
        dbg.location(741, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(743, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:7: IMPORTANT_SYM
            {
            dbg.location(744,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3505); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(745, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(747, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(748,7);
            pushFollow(FOLLOW_term_in_expression3526);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(748,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(131);

            loop131:
            do {
                int alt131=2;
                try { dbg.enterDecision(131, decisionCanBacktrack[131]);

                try {
                    isCyclicDecision = true;
                    alt131 = dfa131.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(131);}

                switch (alt131) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(748,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:14: ( operator ( ws )? )?
            	    int alt130=2;
            	    try { dbg.enterSubRule(130);
            	    try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            	    int LA130_0 = input.LA(1);

            	    if ( (LA130_0==COMMA||LA130_0==SOLIDUS) ) {
            	        alt130=1;
            	    }
            	    } finally {dbg.exitDecision(130);}

            	    switch (alt130) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:15: operator ( ws )?
            	            {
            	            dbg.location(748,15);
            	            pushFollow(FOLLOW_operator_in_expression3531);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(748,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:24: ws
            	                    {
            	                    dbg.location(748,24);
            	                    pushFollow(FOLLOW_ws_in_expression3533);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(129);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(130);}

            	    dbg.location(748,30);
            	    pushFollow(FOLLOW_term_in_expression3538);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop131;
                }
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
        dbg.location(749, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(751, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )?
            {
            dbg.location(752,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:7: ( unaryOperator ( ws )? )?
            int alt133=2;
            try { dbg.enterSubRule(133);
            try { dbg.enterDecision(133, decisionCanBacktrack[133]);

            int LA133_0 = input.LA(1);

            if ( (LA133_0==PLUS||LA133_0==MINUS) ) {
                alt133=1;
            }
            } finally {dbg.exitDecision(133);}

            switch (alt133) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:9: unaryOperator ( ws )?
                    {
                    dbg.location(752,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3563);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(752,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:23: ws
                            {
                            dbg.location(752,23);
                            pushFollow(FOLLOW_ws_in_term3565);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(132);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(133);}

            dbg.location(753,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )
            int alt134=8;
            try { dbg.enterSubRule(134);
            try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            try {
                isCyclicDecision = true;
                alt134 = dfa134.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(134);}

            switch (alt134) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(754,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:7: STRING
                    {
                    dbg.location(767,7);
                    match(input,STRING,FOLLOW_STRING_in_term3789); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:7: IDENT
                    {
                    dbg.location(768,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3797); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:7: GEN
                    {
                    dbg.location(769,7);
                    match(input,GEN,FOLLOW_GEN_in_term3805); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:7: URI
                    {
                    dbg.location(770,7);
                    match(input,URI,FOLLOW_URI_in_term3813); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:7: hexColor
                    {
                    dbg.location(771,7);
                    pushFollow(FOLLOW_hexColor_in_term3821);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:7: function
                    {
                    dbg.location(772,7);
                    pushFollow(FOLLOW_function_in_term3829);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:7: less_variable
                    {
                    dbg.location(773,7);
                    pushFollow(FOLLOW_less_variable_in_term3837);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(134);}

            dbg.location(775,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:5: ( ws )?
            int alt135=2;
            try { dbg.enterSubRule(135);
            try { dbg.enterDecision(135, decisionCanBacktrack[135]);

            int LA135_0 = input.LA(1);

            if ( (LA135_0==WS||(LA135_0>=NL && LA135_0<=COMMENT)) ) {
                alt135=1;
            }
            } finally {dbg.exitDecision(135);}

            switch (alt135) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:5: ws
                    {
                    dbg.location(775,5);
                    pushFollow(FOLLOW_ws_in_term3849);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(135);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(778, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(779,5);
            pushFollow(FOLLOW_functionName_in_function3865);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(779,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:18: ( ws )?
            int alt136=2;
            try { dbg.enterSubRule(136);
            try { dbg.enterDecision(136, decisionCanBacktrack[136]);

            int LA136_0 = input.LA(1);

            if ( (LA136_0==WS||(LA136_0>=NL && LA136_0<=COMMENT)) ) {
                alt136=1;
            }
            } finally {dbg.exitDecision(136);}

            switch (alt136) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:18: ws
                    {
                    dbg.location(779,18);
                    pushFollow(FOLLOW_ws_in_function3867);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}

            dbg.location(780,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3872); if (state.failed) return ;
            dbg.location(780,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:10: ( ws )?
            int alt137=2;
            try { dbg.enterSubRule(137);
            try { dbg.enterDecision(137, decisionCanBacktrack[137]);

            int LA137_0 = input.LA(1);

            if ( (LA137_0==WS||(LA137_0>=NL && LA137_0<=COMMENT)) ) {
                alt137=1;
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:10: ws
                    {
                    dbg.location(780,10);
                    pushFollow(FOLLOW_ws_in_function3874);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(781,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt140=2;
            try { dbg.enterSubRule(140);
            try { dbg.enterDecision(140, decisionCanBacktrack[140]);

            try {
                isCyclicDecision = true;
                alt140 = dfa140.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(140);}

            switch (alt140) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:4: expression
                    {
                    dbg.location(782,4);
                    pushFollow(FOLLOW_expression_in_function3884);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(784,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(785,5);
                    pushFollow(FOLLOW_fnAttribute_in_function3902);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(785,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(139);

                    loop139:
                    do {
                        int alt139=2;
                        try { dbg.enterDecision(139, decisionCanBacktrack[139]);

                        int LA139_0 = input.LA(1);

                        if ( (LA139_0==COMMA) ) {
                            alt139=1;
                        }


                        } finally {dbg.exitDecision(139);}

                        switch (alt139) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(785,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3905); if (state.failed) return ;
                    	    dbg.location(785,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:24: ( ws )?
                    	    int alt138=2;
                    	    try { dbg.enterSubRule(138);
                    	    try { dbg.enterDecision(138, decisionCanBacktrack[138]);

                    	    int LA138_0 = input.LA(1);

                    	    if ( (LA138_0==WS||(LA138_0>=NL && LA138_0<=COMMENT)) ) {
                    	        alt138=1;
                    	    }
                    	    } finally {dbg.exitDecision(138);}

                    	    switch (alt138) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:24: ws
                    	            {
                    	            dbg.location(785,24);
                    	            pushFollow(FOLLOW_ws_in_function3907);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(138);}

                    	    dbg.location(785,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function3910);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop139;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(139);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(788,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3931); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(789, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:1: functionName : IDENT ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(795, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:4: IDENT
            {
            dbg.location(800,4);
            match(input,IDENT,FOLLOW_IDENT_in_functionName3980); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(801, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(803, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(804,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4000);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(804,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:20: ( ws )?
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            int LA141_0 = input.LA(1);

            if ( (LA141_0==WS||(LA141_0>=NL && LA141_0<=COMMENT)) ) {
                alt141=1;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:20: ws
                    {
                    dbg.location(804,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4002);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(804,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4005); if (state.failed) return ;
            dbg.location(804,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:29: ( ws )?
            int alt142=2;
            try { dbg.enterSubRule(142);
            try { dbg.enterDecision(142, decisionCanBacktrack[142]);

            int LA142_0 = input.LA(1);

            if ( (LA142_0==WS||(LA142_0>=NL && LA142_0<=COMMENT)) ) {
                alt142=1;
            }
            } finally {dbg.exitDecision(142);}

            switch (alt142) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:29: ws
                    {
                    dbg.location(804,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4007);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(142);}

            dbg.location(804,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4010);
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
        dbg.location(805, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(807, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:4: IDENT ( DOT IDENT )*
            {
            dbg.location(808,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4025); if (state.failed) return ;
            dbg.location(808,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:10: ( DOT IDENT )*
            try { dbg.enterSubRule(143);

            loop143:
            do {
                int alt143=2;
                try { dbg.enterDecision(143, decisionCanBacktrack[143]);

                int LA143_0 = input.LA(1);

                if ( (LA143_0==DOT) ) {
                    alt143=1;
                }


                } finally {dbg.exitDecision(143);}

                switch (alt143) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:11: DOT IDENT
            	    {
            	    dbg.location(808,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4028); if (state.failed) return ;
            	    dbg.location(808,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4030); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop143;
                }
            } while (true);
            } finally {dbg.exitSubRule(143);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(809, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(811, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:4: expression
            {
            dbg.location(812,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4044);
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
        dbg.location(813, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(815, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:7: HASH
            {
            dbg.location(816,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4062); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(817, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(819, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:820:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:820:7: ( WS | NL | COMMENT )+
            {
            dbg.location(820,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:820:7: ( WS | NL | COMMENT )+
            int cnt144=0;
            try { dbg.enterSubRule(144);

            loop144:
            do {
                int alt144=2;
                try { dbg.enterDecision(144, decisionCanBacktrack[144]);

                int LA144_0 = input.LA(1);

                if ( (LA144_0==WS||(LA144_0>=NL && LA144_0<=COMMENT)) ) {
                    alt144=1;
                }


                } finally {dbg.exitDecision(144);}

                switch (alt144) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(820,7);
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
            	    if ( cnt144 >= 1 ) break loop144;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(144, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt144++;
            } while (true);
            } finally {dbg.exitSubRule(144);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(821, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:1: less_variable_declaration : less_variable ( ws )? COLON ( ws )? less_expression SEMI ;
    public final void less_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(826, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:5: ( less_variable ( ws )? COLON ( ws )? less_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:7: less_variable ( ws )? COLON ( ws )? less_expression SEMI
            {
            dbg.location(827,7);
            pushFollow(FOLLOW_less_variable_in_less_variable_declaration4120);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(827,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:21: ( ws )?
            int alt145=2;
            try { dbg.enterSubRule(145);
            try { dbg.enterDecision(145, decisionCanBacktrack[145]);

            int LA145_0 = input.LA(1);

            if ( (LA145_0==WS||(LA145_0>=NL && LA145_0<=COMMENT)) ) {
                alt145=1;
            }
            } finally {dbg.exitDecision(145);}

            switch (alt145) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:21: ws
                    {
                    dbg.location(827,21);
                    pushFollow(FOLLOW_ws_in_less_variable_declaration4122);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(145);}

            dbg.location(827,25);
            match(input,COLON,FOLLOW_COLON_in_less_variable_declaration4125); if (state.failed) return ;
            dbg.location(827,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:31: ( ws )?
            int alt146=2;
            try { dbg.enterSubRule(146);
            try { dbg.enterDecision(146, decisionCanBacktrack[146]);

            int LA146_0 = input.LA(1);

            if ( (LA146_0==WS||(LA146_0>=NL && LA146_0<=COMMENT)) ) {
                alt146=1;
            }
            } finally {dbg.exitDecision(146);}

            switch (alt146) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:31: ws
                    {
                    dbg.location(827,31);
                    pushFollow(FOLLOW_ws_in_less_variable_declaration4127);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(146);}

            dbg.location(827,35);
            pushFollow(FOLLOW_less_expression_in_less_variable_declaration4130);
            less_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(827,51);
            match(input,SEMI,FOLLOW_SEMI_in_less_variable_declaration4132); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(828, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:1: less_variable : ( AT_IDENT | MEDIA_SYM );
    public final void less_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(830, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:5: ( AT_IDENT | MEDIA_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(831,5);
            if ( input.LA(1)==MEDIA_SYM||input.LA(1)==AT_IDENT ) {
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
        dbg.location(832, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_variable"


    // $ANTLR start "less_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:1: less_function : LPAREN ( ws )? less_expression RPAREN ;
    public final void less_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(834, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:5: ( LPAREN ( ws )? less_expression RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:7: LPAREN ( ws )? less_expression RPAREN
            {
            dbg.location(835,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function4175); if (state.failed) return ;
            dbg.location(835,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:14: ( ws )?
            int alt147=2;
            try { dbg.enterSubRule(147);
            try { dbg.enterDecision(147, decisionCanBacktrack[147]);

            int LA147_0 = input.LA(1);

            if ( (LA147_0==WS||(LA147_0>=NL && LA147_0<=COMMENT)) ) {
                alt147=1;
            }
            } finally {dbg.exitDecision(147);}

            switch (alt147) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:14: ws
                    {
                    dbg.location(835,14);
                    pushFollow(FOLLOW_ws_in_less_function4177);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(147);}

            dbg.location(835,18);
            pushFollow(FOLLOW_less_expression_in_less_function4180);
            less_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(835,35);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function4183); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(836, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function"


    // $ANTLR start "less_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:1: less_expression : term ( less_expression_operator ( ws )? term )* ;
    public final void less_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(838, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:5: ( term ( less_expression_operator ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:7: term ( less_expression_operator ( ws )? term )*
            {
            dbg.location(839,7);
            pushFollow(FOLLOW_term_in_less_expression4200);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(839,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:12: ( less_expression_operator ( ws )? term )*
            try { dbg.enterSubRule(149);

            loop149:
            do {
                int alt149=2;
                try { dbg.enterDecision(149, decisionCanBacktrack[149]);

                int LA149_0 = input.LA(1);

                if ( ((LA149_0>=SOLIDUS && LA149_0<=PLUS)||LA149_0==MINUS||LA149_0==STAR) ) {
                    alt149=1;
                }


                } finally {dbg.exitDecision(149);}

                switch (alt149) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:13: less_expression_operator ( ws )? term
            	    {
            	    dbg.location(839,13);
            	    pushFollow(FOLLOW_less_expression_operator_in_less_expression4203);
            	    less_expression_operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(839,38);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:38: ( ws )?
            	    int alt148=2;
            	    try { dbg.enterSubRule(148);
            	    try { dbg.enterDecision(148, decisionCanBacktrack[148]);

            	    int LA148_0 = input.LA(1);

            	    if ( (LA148_0==WS||(LA148_0>=NL && LA148_0<=COMMENT)) ) {
            	        alt148=1;
            	    }
            	    } finally {dbg.exitDecision(148);}

            	    switch (alt148) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:38: ws
            	            {
            	            dbg.location(839,38);
            	            pushFollow(FOLLOW_ws_in_less_expression4205);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(148);}

            	    dbg.location(839,42);
            	    pushFollow(FOLLOW_term_in_less_expression4208);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop149;
                }
            } while (true);
            } finally {dbg.exitSubRule(149);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(840, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_expression"


    // $ANTLR start "less_expression_operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:1: less_expression_operator : ( SOLIDUS | STAR | PLUS | MINUS ) ;
    public final void less_expression_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_expression_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(842, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:5: ( ( SOLIDUS | STAR | PLUS | MINUS ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:7: ( SOLIDUS | STAR | PLUS | MINUS )
            {
            dbg.location(843,7);
            if ( (input.LA(1)>=SOLIDUS && input.LA(1)<=PLUS)||input.LA(1)==MINUS||input.LA(1)==STAR ) {
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
        dbg.location(850, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_expression_operator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_expression_operator"


    // $ANTLR start "less_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:1: less_mixin_declaration : cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? ;
    public final void less_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(857, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:5: ( cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:5: cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
            {
            dbg.location(859,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_declaration4318);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(859,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:14: ( ws )?
            int alt150=2;
            try { dbg.enterSubRule(150);
            try { dbg.enterDecision(150, decisionCanBacktrack[150]);

            int LA150_0 = input.LA(1);

            if ( (LA150_0==WS||(LA150_0>=NL && LA150_0<=COMMENT)) ) {
                alt150=1;
            }
            } finally {dbg.exitDecision(150);}

            switch (alt150) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:14: ws
                    {
                    dbg.location(859,14);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4320);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}

            dbg.location(859,18);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_declaration4323); if (state.failed) return ;
            dbg.location(859,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:25: ( less_args_list )?
            int alt151=2;
            try { dbg.enterSubRule(151);
            try { dbg.enterDecision(151, decisionCanBacktrack[151]);

            int LA151_0 = input.LA(1);

            if ( (LA151_0==MEDIA_SYM||LA151_0==AT_IDENT||(LA151_0>=123 && LA151_0<=124)) ) {
                alt151=1;
            }
            } finally {dbg.exitDecision(151);}

            switch (alt151) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:25: less_args_list
                    {
                    dbg.location(859,25);
                    pushFollow(FOLLOW_less_args_list_in_less_mixin_declaration4325);
                    less_args_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(859,41);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_declaration4328); if (state.failed) return ;
            dbg.location(859,48);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:48: ( ws )?
            int alt152=2;
            try { dbg.enterSubRule(152);
            try { dbg.enterDecision(152, decisionCanBacktrack[152]);

            int LA152_0 = input.LA(1);

            if ( (LA152_0==WS||(LA152_0>=NL && LA152_0<=COMMENT)) ) {
                alt152=1;
            }
            } finally {dbg.exitDecision(152);}

            switch (alt152) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:48: ws
                    {
                    dbg.location(859,48);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4330);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(152);}

            dbg.location(859,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:52: ( less_mixin_guarded ( ws )? )?
            int alt154=2;
            try { dbg.enterSubRule(154);
            try { dbg.enterDecision(154, decisionCanBacktrack[154]);

            int LA154_0 = input.LA(1);

            if ( (LA154_0==LESS_WHEN) ) {
                alt154=1;
            }
            } finally {dbg.exitDecision(154);}

            switch (alt154) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:53: less_mixin_guarded ( ws )?
                    {
                    dbg.location(859,53);
                    pushFollow(FOLLOW_less_mixin_guarded_in_less_mixin_declaration4334);
                    less_mixin_guarded();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(859,72);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:72: ( ws )?
                    int alt153=2;
                    try { dbg.enterSubRule(153);
                    try { dbg.enterDecision(153, decisionCanBacktrack[153]);

                    int LA153_0 = input.LA(1);

                    if ( (LA153_0==WS||(LA153_0>=NL && LA153_0<=COMMENT)) ) {
                        alt153=1;
                    }
                    } finally {dbg.exitDecision(153);}

                    switch (alt153) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:72: ws
                            {
                            dbg.location(859,72);
                            pushFollow(FOLLOW_ws_in_less_mixin_declaration4336);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(153);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(154);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(860, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_mixin_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_declaration"


    // $ANTLR start "less_mixin_call"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:1: less_mixin_call : cssClass ( ws )? ( LPAREN ( less_mixin_call_args )? RPAREN )? SEMI ;
    public final void less_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(863, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:5: ( cssClass ( ws )? ( LPAREN ( less_mixin_call_args )? RPAREN )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:5: cssClass ( ws )? ( LPAREN ( less_mixin_call_args )? RPAREN )? SEMI
            {
            dbg.location(865,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_call4361);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(865,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:14: ( ws )?
            int alt155=2;
            try { dbg.enterSubRule(155);
            try { dbg.enterDecision(155, decisionCanBacktrack[155]);

            int LA155_0 = input.LA(1);

            if ( (LA155_0==WS||(LA155_0>=NL && LA155_0<=COMMENT)) ) {
                alt155=1;
            }
            } finally {dbg.exitDecision(155);}

            switch (alt155) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:14: ws
                    {
                    dbg.location(865,14);
                    pushFollow(FOLLOW_ws_in_less_mixin_call4363);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(155);}

            dbg.location(865,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:18: ( LPAREN ( less_mixin_call_args )? RPAREN )?
            int alt157=2;
            try { dbg.enterSubRule(157);
            try { dbg.enterDecision(157, decisionCanBacktrack[157]);

            int LA157_0 = input.LA(1);

            if ( (LA157_0==LPAREN) ) {
                alt157=1;
            }
            } finally {dbg.exitDecision(157);}

            switch (alt157) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:19: LPAREN ( less_mixin_call_args )? RPAREN
                    {
                    dbg.location(865,19);
                    match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_call4367); if (state.failed) return ;
                    dbg.location(865,26);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:26: ( less_mixin_call_args )?
                    int alt156=2;
                    try { dbg.enterSubRule(156);
                    try { dbg.enterDecision(156, decisionCanBacktrack[156]);

                    int LA156_0 = input.LA(1);

                    if ( ((LA156_0>=IDENT && LA156_0<=URI)||LA156_0==MEDIA_SYM||(LA156_0>=GEN && LA156_0<=AT_IDENT)||LA156_0==PERCENTAGE||LA156_0==PLUS||(LA156_0>=MINUS && LA156_0<=HASH)||(LA156_0>=NUMBER && LA156_0<=DIMENSION)) ) {
                        alt156=1;
                    }
                    } finally {dbg.exitDecision(156);}

                    switch (alt156) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:26: less_mixin_call_args
                            {
                            dbg.location(865,26);
                            pushFollow(FOLLOW_less_mixin_call_args_in_less_mixin_call4369);
                            less_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(156);}

                    dbg.location(865,48);
                    match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_call4372); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(157);}

            dbg.location(865,57);
            match(input,SEMI,FOLLOW_SEMI_in_less_mixin_call4376); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(866, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_mixin_call");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_call"


    // $ANTLR start "less_mixin_call_args"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:1: less_mixin_call_args : term ( COMMA ( ws )? term )* ;
    public final void less_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(868, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:5: ( term ( COMMA ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:5: term ( COMMA ( ws )? term )*
            {
            dbg.location(870,5);
            pushFollow(FOLLOW_term_in_less_mixin_call_args4402);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(870,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:10: ( COMMA ( ws )? term )*
            try { dbg.enterSubRule(159);

            loop159:
            do {
                int alt159=2;
                try { dbg.enterDecision(159, decisionCanBacktrack[159]);

                int LA159_0 = input.LA(1);

                if ( (LA159_0==COMMA) ) {
                    alt159=1;
                }


                } finally {dbg.exitDecision(159);}

                switch (alt159) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:12: COMMA ( ws )? term
            	    {
            	    dbg.location(870,12);
            	    match(input,COMMA,FOLLOW_COMMA_in_less_mixin_call_args4406); if (state.failed) return ;
            	    dbg.location(870,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:18: ( ws )?
            	    int alt158=2;
            	    try { dbg.enterSubRule(158);
            	    try { dbg.enterDecision(158, decisionCanBacktrack[158]);

            	    int LA158_0 = input.LA(1);

            	    if ( (LA158_0==WS||(LA158_0>=NL && LA158_0<=COMMENT)) ) {
            	        alt158=1;
            	    }
            	    } finally {dbg.exitDecision(158);}

            	    switch (alt158) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:18: ws
            	            {
            	            dbg.location(870,18);
            	            pushFollow(FOLLOW_ws_in_less_mixin_call_args4408);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(158);}

            	    dbg.location(870,22);
            	    pushFollow(FOLLOW_term_in_less_mixin_call_args4411);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop159;
                }
            } while (true);
            } finally {dbg.exitSubRule(159);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(871, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_call_args"


    // $ANTLR start "less_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:1: less_args_list : ( ( less_arg ( COMMA ( ws )? less_arg )* ( COMMA ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(874, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:5: ( ( less_arg ( COMMA ( ws )? less_arg )* ( COMMA ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) )
            int alt164=2;
            try { dbg.enterDecision(164, decisionCanBacktrack[164]);

            int LA164_0 = input.LA(1);

            if ( (LA164_0==MEDIA_SYM||LA164_0==AT_IDENT) ) {
                alt164=1;
            }
            else if ( ((LA164_0>=123 && LA164_0<=124)) ) {
                alt164=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 164, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(164);}

            switch (alt164) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:5: ( less_arg ( COMMA ( ws )? less_arg )* ( COMMA ( ws )? ( '...' | '@rest...' ) )? )
                    {
                    dbg.location(876,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:5: ( less_arg ( COMMA ( ws )? less_arg )* ( COMMA ( ws )? ( '...' | '@rest...' ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:7: less_arg ( COMMA ( ws )? less_arg )* ( COMMA ( ws )? ( '...' | '@rest...' ) )?
                    {
                    dbg.location(876,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list4443);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(876,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:16: ( COMMA ( ws )? less_arg )*
                    try { dbg.enterSubRule(161);

                    loop161:
                    do {
                        int alt161=2;
                        try { dbg.enterDecision(161, decisionCanBacktrack[161]);

                        try {
                            isCyclicDecision = true;
                            alt161 = dfa161.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(161);}

                        switch (alt161) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:18: COMMA ( ws )? less_arg
                    	    {
                    	    dbg.location(876,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_less_args_list4447); if (state.failed) return ;
                    	    dbg.location(876,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:24: ( ws )?
                    	    int alt160=2;
                    	    try { dbg.enterSubRule(160);
                    	    try { dbg.enterDecision(160, decisionCanBacktrack[160]);

                    	    int LA160_0 = input.LA(1);

                    	    if ( (LA160_0==WS||(LA160_0>=NL && LA160_0<=COMMENT)) ) {
                    	        alt160=1;
                    	    }
                    	    } finally {dbg.exitDecision(160);}

                    	    switch (alt160) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:24: ws
                    	            {
                    	            dbg.location(876,24);
                    	            pushFollow(FOLLOW_ws_in_less_args_list4449);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(160);}

                    	    dbg.location(876,28);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list4452);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop161;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(161);}

                    dbg.location(876,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:39: ( COMMA ( ws )? ( '...' | '@rest...' ) )?
                    int alt163=2;
                    try { dbg.enterSubRule(163);
                    try { dbg.enterDecision(163, decisionCanBacktrack[163]);

                    int LA163_0 = input.LA(1);

                    if ( (LA163_0==COMMA) ) {
                        alt163=1;
                    }
                    } finally {dbg.exitDecision(163);}

                    switch (alt163) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:40: COMMA ( ws )? ( '...' | '@rest...' )
                            {
                            dbg.location(876,40);
                            match(input,COMMA,FOLLOW_COMMA_in_less_args_list4457); if (state.failed) return ;
                            dbg.location(876,46);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:46: ( ws )?
                            int alt162=2;
                            try { dbg.enterSubRule(162);
                            try { dbg.enterDecision(162, decisionCanBacktrack[162]);

                            int LA162_0 = input.LA(1);

                            if ( (LA162_0==WS||(LA162_0>=NL && LA162_0<=COMMENT)) ) {
                                alt162=1;
                            }
                            } finally {dbg.exitDecision(162);}

                            switch (alt162) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:46: ws
                                    {
                                    dbg.location(876,46);
                                    pushFollow(FOLLOW_ws_in_less_args_list4459);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(162);}

                            dbg.location(876,50);
                            if ( (input.LA(1)>=123 && input.LA(1)<=124) ) {
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
                    } finally {dbg.exitSubRule(163);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:5: ( '...' | '@rest...' )
                    {
                    dbg.location(878,5);
                    if ( (input.LA(1)>=123 && input.LA(1)<=124) ) {
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
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(879, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_args_list");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_args_list"


    // $ANTLR start "less_arg"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:1: less_arg : less_variable ( COLON ( ws )? less_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(882, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:5: ( less_variable ( COLON ( ws )? less_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:5: less_variable ( COLON ( ws )? less_expression )?
            {
            dbg.location(884,5);
            pushFollow(FOLLOW_less_variable_in_less_arg4516);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(884,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:19: ( COLON ( ws )? less_expression )?
            int alt166=2;
            try { dbg.enterSubRule(166);
            try { dbg.enterDecision(166, decisionCanBacktrack[166]);

            int LA166_0 = input.LA(1);

            if ( (LA166_0==COLON) ) {
                alt166=1;
            }
            } finally {dbg.exitDecision(166);}

            switch (alt166) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:21: COLON ( ws )? less_expression
                    {
                    dbg.location(884,21);
                    match(input,COLON,FOLLOW_COLON_in_less_arg4520); if (state.failed) return ;
                    dbg.location(884,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:27: ( ws )?
                    int alt165=2;
                    try { dbg.enterSubRule(165);
                    try { dbg.enterDecision(165, decisionCanBacktrack[165]);

                    int LA165_0 = input.LA(1);

                    if ( (LA165_0==WS||(LA165_0>=NL && LA165_0<=COMMENT)) ) {
                        alt165=1;
                    }
                    } finally {dbg.exitDecision(165);}

                    switch (alt165) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:27: ws
                            {
                            dbg.location(884,27);
                            pushFollow(FOLLOW_ws_in_less_arg4522);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(165);}

                    dbg.location(884,31);
                    pushFollow(FOLLOW_less_expression_in_less_arg4525);
                    less_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(166);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(885, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_arg");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_arg"


    // $ANTLR start "less_mixin_guarded"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(889, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(891,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded4551); if (state.failed) return ;
            dbg.location(891,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:15: ( ws )?
            int alt167=2;
            try { dbg.enterSubRule(167);
            try { dbg.enterDecision(167, decisionCanBacktrack[167]);

            int LA167_0 = input.LA(1);

            if ( (LA167_0==WS||(LA167_0>=NL && LA167_0<=COMMENT)) ) {
                alt167=1;
            }
            } finally {dbg.exitDecision(167);}

            switch (alt167) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:15: ws
                    {
                    dbg.location(891,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded4553);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(167);}

            dbg.location(891,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded4556);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(891,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(169);

            loop169:
            do {
                int alt169=2;
                try { dbg.enterDecision(169, decisionCanBacktrack[169]);

                int LA169_0 = input.LA(1);

                if ( ((LA169_0>=COMMA && LA169_0<=AND)) ) {
                    alt169=1;
                }


                } finally {dbg.exitDecision(169);}

                switch (alt169) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(891,36);
            	    if ( (input.LA(1)>=COMMA && input.LA(1)<=AND) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(891,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:50: ( ws )?
            	    int alt168=2;
            	    try { dbg.enterSubRule(168);
            	    try { dbg.enterDecision(168, decisionCanBacktrack[168]);

            	    int LA168_0 = input.LA(1);

            	    if ( (LA168_0==WS||(LA168_0>=NL && LA168_0<=COMMENT)) ) {
            	        alt168=1;
            	    }
            	    } finally {dbg.exitDecision(168);}

            	    switch (alt168) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:50: ws
            	            {
            	            dbg.location(891,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded4568);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(168);}

            	    dbg.location(891,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded4571);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop169;
                }
            } while (true);
            } finally {dbg.exitSubRule(169);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(892, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_mixin_guarded");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_guarded"


    // $ANTLR start "less_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(896, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN
            {
            dbg.location(898,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:5: ( NOT ( ws )? )?
            int alt171=2;
            try { dbg.enterSubRule(171);
            try { dbg.enterDecision(171, decisionCanBacktrack[171]);

            int LA171_0 = input.LA(1);

            if ( (LA171_0==NOT) ) {
                alt171=1;
            }
            } finally {dbg.exitDecision(171);}

            switch (alt171) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:6: NOT ( ws )?
                    {
                    dbg.location(898,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition4601); if (state.failed) return ;
                    dbg.location(898,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:10: ( ws )?
                    int alt170=2;
                    try { dbg.enterSubRule(170);
                    try { dbg.enterDecision(170, decisionCanBacktrack[170]);

                    int LA170_0 = input.LA(1);

                    if ( (LA170_0==WS||(LA170_0>=NL && LA170_0<=COMMENT)) ) {
                        alt170=1;
                    }
                    } finally {dbg.exitDecision(170);}

                    switch (alt170) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:10: ws
                            {
                            dbg.location(898,10);
                            pushFollow(FOLLOW_ws_in_less_condition4603);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(170);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(171);}

            dbg.location(899,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition4612); if (state.failed) return ;
            dbg.location(899,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:12: ( ws )?
            int alt172=2;
            try { dbg.enterSubRule(172);
            try { dbg.enterDecision(172, decisionCanBacktrack[172]);

            int LA172_0 = input.LA(1);

            if ( (LA172_0==WS||(LA172_0>=NL && LA172_0<=COMMENT)) ) {
                alt172=1;
            }
            } finally {dbg.exitDecision(172);}

            switch (alt172) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:12: ws
                    {
                    dbg.location(899,12);
                    pushFollow(FOLLOW_ws_in_less_condition4614);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(172);}

            dbg.location(900,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:9: ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) )
            int alt177=2;
            try { dbg.enterSubRule(177);
            try { dbg.enterDecision(177, decisionCanBacktrack[177]);

            int LA177_0 = input.LA(1);

            if ( (LA177_0==IDENT) ) {
                alt177=1;
            }
            else if ( (LA177_0==MEDIA_SYM||LA177_0==AT_IDENT) ) {
                alt177=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 177, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(177);}

            switch (alt177) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(901,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition4640);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(901,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:40: ( ws )?
                    int alt173=2;
                    try { dbg.enterSubRule(173);
                    try { dbg.enterDecision(173, decisionCanBacktrack[173]);

                    int LA173_0 = input.LA(1);

                    if ( (LA173_0==WS||(LA173_0>=NL && LA173_0<=COMMENT)) ) {
                        alt173=1;
                    }
                    } finally {dbg.exitDecision(173);}

                    switch (alt173) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:40: ws
                            {
                            dbg.location(901,40);
                            pushFollow(FOLLOW_ws_in_less_condition4642);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(173);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:13: ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? )
                    {
                    dbg.location(903,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:13: ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:15: less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )?
                    {
                    dbg.location(903,15);
                    pushFollow(FOLLOW_less_variable_in_less_condition4673);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(903,29);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:29: ( ( ws )? less_condition_operator ( ws )? less_expression )?
                    int alt176=2;
                    try { dbg.enterSubRule(176);
                    try { dbg.enterDecision(176, decisionCanBacktrack[176]);

                    int LA176_0 = input.LA(1);

                    if ( (LA176_0==WS||LA176_0==GREATER||LA176_0==OPEQ||(LA176_0>=NL && LA176_0<=COMMENT)||(LA176_0>=GREATER_OR_EQ && LA176_0<=LESS_OR_EQ)) ) {
                        alt176=1;
                    }
                    } finally {dbg.exitDecision(176);}

                    switch (alt176) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:30: ( ws )? less_condition_operator ( ws )? less_expression
                            {
                            dbg.location(903,30);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:30: ( ws )?
                            int alt174=2;
                            try { dbg.enterSubRule(174);
                            try { dbg.enterDecision(174, decisionCanBacktrack[174]);

                            int LA174_0 = input.LA(1);

                            if ( (LA174_0==WS||(LA174_0>=NL && LA174_0<=COMMENT)) ) {
                                alt174=1;
                            }
                            } finally {dbg.exitDecision(174);}

                            switch (alt174) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:30: ws
                                    {
                                    dbg.location(903,30);
                                    pushFollow(FOLLOW_ws_in_less_condition4676);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(174);}

                            dbg.location(903,34);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition4679);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(903,58);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:58: ( ws )?
                            int alt175=2;
                            try { dbg.enterSubRule(175);
                            try { dbg.enterDecision(175, decisionCanBacktrack[175]);

                            int LA175_0 = input.LA(1);

                            if ( (LA175_0==WS||(LA175_0>=NL && LA175_0<=COMMENT)) ) {
                                alt175=1;
                            }
                            } finally {dbg.exitDecision(175);}

                            switch (alt175) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:58: ws
                                    {
                                    dbg.location(903,58);
                                    pushFollow(FOLLOW_ws_in_less_condition4681);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(175);}

                            dbg.location(903,62);
                            pushFollow(FOLLOW_less_expression_in_less_condition4684);
                            less_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(176);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(177);}

            dbg.location(905,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition4713); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(906, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition"


    // $ANTLR start "less_function_in_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(909, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:5: ( less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:5: less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN
            {
            dbg.location(911,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition4739);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(911,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:18: ( ws )?
            int alt178=2;
            try { dbg.enterSubRule(178);
            try { dbg.enterDecision(178, decisionCanBacktrack[178]);

            int LA178_0 = input.LA(1);

            if ( (LA178_0==WS||(LA178_0>=NL && LA178_0<=COMMENT)) ) {
                alt178=1;
            }
            } finally {dbg.exitDecision(178);}

            switch (alt178) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:18: ws
                    {
                    dbg.location(911,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition4741);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(178);}

            dbg.location(911,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition4744); if (state.failed) return ;
            dbg.location(911,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:29: ( ws )?
            int alt179=2;
            try { dbg.enterSubRule(179);
            try { dbg.enterDecision(179, decisionCanBacktrack[179]);

            int LA179_0 = input.LA(1);

            if ( (LA179_0==WS||(LA179_0>=NL && LA179_0<=COMMENT)) ) {
                alt179=1;
            }
            } finally {dbg.exitDecision(179);}

            switch (alt179) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:29: ws
                    {
                    dbg.location(911,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition4746);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(179);}

            dbg.location(911,33);
            pushFollow(FOLLOW_less_variable_in_less_function_in_condition4749);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(911,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:47: ( ws )?
            int alt180=2;
            try { dbg.enterSubRule(180);
            try { dbg.enterDecision(180, decisionCanBacktrack[180]);

            int LA180_0 = input.LA(1);

            if ( (LA180_0==WS||(LA180_0>=NL && LA180_0<=COMMENT)) ) {
                alt180=1;
            }
            } finally {dbg.exitDecision(180);}

            switch (alt180) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:47: ws
                    {
                    dbg.location(911,47);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition4751);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(180);}

            dbg.location(911,51);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition4754); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(912, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_function_in_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function_in_condition"


    // $ANTLR start "less_fn_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(915, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:5: IDENT
            {
            dbg.location(917,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name4776); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(918, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_fn_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_fn_name"


    // $ANTLR start "less_condition_operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(920, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(921,5);
            if ( input.LA(1)==GREATER||input.LA(1)==OPEQ||(input.LA(1)>=GREATER_OR_EQ && input.LA(1)<=LESS_OR_EQ) ) {
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
        dbg.location(923, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_condition_operator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition_operator"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:17: ( rulePredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:18: rulePredicate
        {
        dbg.location(541,18);
        pushFollow(FOLLOW_rulePredicate_in_synpred1_Css31859);
        rulePredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:19: ( declarationPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:20: declarationPredicate
        {
        dbg.location(543,20);
        pushFollow(FOLLOW_declarationPredicate_in_synpred2_Css31902);
        declarationPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:19: esPred
        {
        dbg.location(586,19);
        pushFollow(FOLLOW_esPred_in_synpred3_Css32281);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:6: esPred
        {
        dbg.location(588,6);
        pushFollow(FOLLOW_esPred_in_synpred4_Css32302);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:8: nsPred
        {
        dbg.location(602,8);
        pushFollow(FOLLOW_nsPred_in_synpred5_Css32407);
        nsPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // Delegated rules

    public final boolean synpred5_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred5_Css3_fragment(); // can never throw exception
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
    public final boolean synpred4_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred4_Css3_fragment(); // can never throw exception
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
    protected DFA89 dfa89 = new DFA89(this);
    protected DFA91 dfa91 = new DFA91(this);
    protected DFA101 dfa101 = new DFA101(this);
    protected DFA120 dfa120 = new DFA120(this);
    protected DFA131 dfa131 = new DFA131(this);
    protected DFA134 dfa134 = new DFA134(this);
    protected DFA140 dfa140 = new DFA140(this);
    protected DFA161 dfa161 = new DFA161(this);
    static final String DFA42_eotS =
        "\14\uffff";
    static final String DFA42_eofS =
        "\14\uffff";
    static final String DFA42_minS =
        "\1\5\1\uffff\1\5\4\uffff\2\5\2\uffff\1\5";
    static final String DFA42_maxS =
        "\1\172\1\uffff\1\121\4\uffff\2\121\2\uffff\1\121";
    static final String DFA42_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\2\uffff\1\2\1\7\1\uffff";
    static final String DFA42_specialS =
        "\14\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\1\5\uffff\1\2\6\uffff\1\1\1\7\1\uffff\1\6\3\uffff\1\6\1\uffff"+
            "\1\3\1\4\1\5\20\uffff\1\1\5\uffff\6\1\1\uffff\1\1\76\uffff\1"+
            "\1",
            "",
            "\1\11\6\uffff\1\11\3\uffff\3\11\1\uffff\1\10\31\uffff\1\12"+
            "\24\uffff\1\11\14\uffff\2\10",
            "",
            "",
            "",
            "",
            "\2\6\5\uffff\1\6\7\uffff\1\13\31\uffff\1\12\41\uffff\2\12",
            "\1\11\6\uffff\1\11\3\uffff\3\11\1\uffff\1\10\31\uffff\1\12"+
            "\24\uffff\1\11\14\uffff\2\10",
            "",
            "",
            "\2\6\5\uffff\1\6\7\uffff\1\13\31\uffff\1\12\41\uffff\2\12"
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
        "\2\121\2\uffff";
    static final String DFA63_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA63_specialS =
        "\4\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\2\1\uffff\1\3\5\uffff\1\1\73\uffff\2\1",
            "\1\2\1\uffff\1\3\5\uffff\1\1\73\uffff\2\1",
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
            return "()* loopback of 447:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA89_eotS =
        "\6\uffff";
    static final String DFA89_eofS =
        "\6\uffff";
    static final String DFA89_minS =
        "\1\5\1\uffff\3\5\1\uffff";
    static final String DFA89_maxS =
        "\1\172\1\uffff\1\22\2\172\1\uffff";
    static final String DFA89_acceptS =
        "\1\uffff\1\1\3\uffff\1\2";
    static final String DFA89_specialS =
        "\6\uffff}>";
    static final String[] DFA89_transitionS = {
            "\1\1\14\uffff\1\1\33\uffff\1\1\5\uffff\1\1\1\2\4\1\1\uffff\1"+
            "\1\76\uffff\1\1",
            "",
            "\1\3\14\uffff\1\3",
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\1\4\31\uffff"+
            "\1\1\1\uffff\3\1\1\uffff\6\1\1\uffff\1\1\7\uffff\1\5\14\uffff"+
            "\2\4\50\uffff\1\1",
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\1\4\31\uffff"+
            "\1\1\1\uffff\3\1\1\uffff\6\1\1\uffff\1\1\7\uffff\1\5\14\uffff"+
            "\2\4\50\uffff\1\1",
            ""
    };

    static final short[] DFA89_eot = DFA.unpackEncodedString(DFA89_eotS);
    static final short[] DFA89_eof = DFA.unpackEncodedString(DFA89_eofS);
    static final char[] DFA89_min = DFA.unpackEncodedStringToUnsignedChars(DFA89_minS);
    static final char[] DFA89_max = DFA.unpackEncodedStringToUnsignedChars(DFA89_maxS);
    static final short[] DFA89_accept = DFA.unpackEncodedString(DFA89_acceptS);
    static final short[] DFA89_special = DFA.unpackEncodedString(DFA89_specialS);
    static final short[][] DFA89_transition;

    static {
        int numStates = DFA89_transitionS.length;
        DFA89_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA89_transition[i] = DFA.unpackEncodedString(DFA89_transitionS[i]);
        }
    }

    class DFA89 extends DFA {

        public DFA89(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 89;
            this.eot = DFA89_eot;
            this.eof = DFA89_eof;
            this.min = DFA89_min;
            this.max = DFA89_max;
            this.accept = DFA89_accept;
            this.special = DFA89_special;
            this.transition = DFA89_transition;
        }
        public String getDescription() {
            return "524:9: ( selectorsGroup | less_mixin_declaration )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA91_eotS =
        "\15\uffff";
    static final String DFA91_eofS =
        "\15\uffff";
    static final String DFA91_minS =
        "\1\5\2\0\1\uffff\1\0\2\uffff\1\0\5\uffff";
    static final String DFA91_maxS =
        "\1\172\2\0\1\uffff\1\0\2\uffff\1\0\5\uffff";
    static final String DFA91_acceptS =
        "\3\uffff\1\1\1\uffff\2\1\1\uffff\3\1\1\2\1\3";
    static final String DFA91_specialS =
        "\1\0\1\1\1\2\1\uffff\1\3\2\uffff\1\4\5\uffff}>";
    static final String[] DFA91_transitionS = {
            "\1\1\5\uffff\1\13\6\uffff\1\4\1\13\32\uffff\1\11\5\uffff\1\5"+
            "\1\7\1\10\1\11\1\2\1\3\1\uffff\1\12\76\uffff\1\6",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
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
            return "540:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | less_mixin_call )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA91_0 = input.LA(1);

                         
                        int index91_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA91_0==IDENT) ) {s = 1;}

                        else if ( (LA91_0==STAR) ) {s = 2;}

                        else if ( (LA91_0==PIPE) && (synpred1_Css3())) {s = 3;}

                        else if ( (LA91_0==GEN) ) {s = 4;}

                        else if ( (LA91_0==HASH) && (synpred1_Css3())) {s = 5;}

                        else if ( (LA91_0==122) && (synpred1_Css3())) {s = 6;}

                        else if ( (LA91_0==DOT) ) {s = 7;}

                        else if ( (LA91_0==LBRACKET) && (synpred1_Css3())) {s = 8;}

                        else if ( (LA91_0==COLON||LA91_0==DCOLON) && (synpred1_Css3())) {s = 9;}

                        else if ( (LA91_0==LESS_AND) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA91_0==MEDIA_SYM||LA91_0==AT_IDENT) && (synpred2_Css3())) {s = 11;}

                         
                        input.seek(index91_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA91_1 = input.LA(1);

                         
                        int index91_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 10;}

                        else if ( (synpred2_Css3()) ) {s = 11;}

                         
                        input.seek(index91_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA91_2 = input.LA(1);

                         
                        int index91_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 10;}

                        else if ( (synpred2_Css3()) ) {s = 11;}

                         
                        input.seek(index91_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA91_4 = input.LA(1);

                         
                        int index91_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 10;}

                        else if ( (synpred2_Css3()) ) {s = 11;}

                         
                        input.seek(index91_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA91_7 = input.LA(1);

                         
                        int index91_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index91_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 91, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA101_eotS =
        "\21\uffff";
    static final String DFA101_eofS =
        "\21\uffff";
    static final String DFA101_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA101_maxS =
        "\1\172\7\uffff\5\0\4\uffff";
    static final String DFA101_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA101_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA101_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\33\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\1\10\1\12\1\13\1\14\2\1\1\uffff\1\1\10\uffff\1"+
            "\1\65\uffff\1\11",
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

    static final short[] DFA101_eot = DFA.unpackEncodedString(DFA101_eotS);
    static final short[] DFA101_eof = DFA.unpackEncodedString(DFA101_eofS);
    static final char[] DFA101_min = DFA.unpackEncodedStringToUnsignedChars(DFA101_minS);
    static final char[] DFA101_max = DFA.unpackEncodedStringToUnsignedChars(DFA101_maxS);
    static final short[] DFA101_accept = DFA.unpackEncodedString(DFA101_acceptS);
    static final short[] DFA101_special = DFA.unpackEncodedString(DFA101_specialS);
    static final short[][] DFA101_transition;

    static {
        int numStates = DFA101_transitionS.length;
        DFA101_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA101_transition[i] = DFA.unpackEncodedString(DFA101_transitionS[i]);
        }
    }

    class DFA101 extends DFA {

        public DFA101(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 101;
            this.eot = DFA101_eot;
            this.eof = DFA101_eof;
            this.min = DFA101_min;
            this.max = DFA101_max;
            this.accept = DFA101_accept;
            this.special = DFA101_special;
            this.transition = DFA101_transition;
        }
        public String getDescription() {
            return "()* loopback of 586:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA101_8 = input.LA(1);

                         
                        int index101_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index101_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA101_9 = input.LA(1);

                         
                        int index101_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index101_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA101_10 = input.LA(1);

                         
                        int index101_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index101_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA101_11 = input.LA(1);

                         
                        int index101_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index101_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA101_12 = input.LA(1);

                         
                        int index101_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index101_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 101, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA120_eotS =
        "\4\uffff";
    static final String DFA120_eofS =
        "\4\uffff";
    static final String DFA120_minS =
        "\2\5\2\uffff";
    static final String DFA120_maxS =
        "\2\172\2\uffff";
    static final String DFA120_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA120_specialS =
        "\4\uffff}>";
    static final String[] DFA120_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\1\uffff\1\3\7\uffff\1\2\1\3\13"+
            "\uffff\2\1\50\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\1\uffff\1\3\7\uffff\1\2\1\3\13"+
            "\uffff\2\1\50\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA120_eot = DFA.unpackEncodedString(DFA120_eotS);
    static final short[] DFA120_eof = DFA.unpackEncodedString(DFA120_eofS);
    static final char[] DFA120_min = DFA.unpackEncodedStringToUnsignedChars(DFA120_minS);
    static final char[] DFA120_max = DFA.unpackEncodedStringToUnsignedChars(DFA120_maxS);
    static final short[] DFA120_accept = DFA.unpackEncodedString(DFA120_acceptS);
    static final short[] DFA120_special = DFA.unpackEncodedString(DFA120_specialS);
    static final short[][] DFA120_transition;

    static {
        int numStates = DFA120_transitionS.length;
        DFA120_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA120_transition[i] = DFA.unpackEncodedString(DFA120_transitionS[i]);
        }
    }

    class DFA120 extends DFA {

        public DFA120(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 120;
            this.eot = DFA120_eot;
            this.eof = DFA120_eof;
            this.min = DFA120_min;
            this.max = DFA120_max;
            this.accept = DFA120_accept;
            this.special = DFA120_special;
            this.transition = DFA120_transition;
        }
        public String getDescription() {
            return "693:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA131_eotS =
        "\7\uffff";
    static final String DFA131_eofS =
        "\7\uffff";
    static final String DFA131_minS =
        "\1\5\1\uffff\1\5\1\uffff\3\5";
    static final String DFA131_maxS =
        "\1\117\1\uffff\1\121\1\uffff\3\121";
    static final String DFA131_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\3\uffff";
    static final String DFA131_specialS =
        "\7\uffff}>";
    static final String[] DFA131_transitionS = {
            "\3\3\1\uffff\1\1\1\uffff\1\3\1\uffff\1\1\1\2\3\uffff\2\3\6\uffff"+
            "\1\3\24\uffff\2\3\2\uffff\2\3\17\uffff\2\1\12\3",
            "",
            "\1\5\2\3\3\uffff\1\3\6\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1"+
            "\3\2\uffff\2\3\21\uffff\12\3\2\4",
            "",
            "\1\5\2\3\3\uffff\1\3\6\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1"+
            "\3\2\uffff\2\3\21\uffff\12\3\2\4",
            "\3\3\1\uffff\1\3\1\uffff\1\3\1\uffff\2\3\3\uffff\2\3\1\6\5"+
            "\uffff\1\3\24\uffff\2\3\2\uffff\2\3\1\1\6\uffff\1\1\6\uffff"+
            "\15\3\2\6",
            "\3\3\1\uffff\1\3\1\uffff\1\3\1\uffff\2\3\3\uffff\2\3\1\6\5"+
            "\uffff\1\3\24\uffff\2\3\2\uffff\2\3\7\uffff\1\1\6\uffff\15\3"+
            "\2\6"
    };

    static final short[] DFA131_eot = DFA.unpackEncodedString(DFA131_eotS);
    static final short[] DFA131_eof = DFA.unpackEncodedString(DFA131_eofS);
    static final char[] DFA131_min = DFA.unpackEncodedStringToUnsignedChars(DFA131_minS);
    static final char[] DFA131_max = DFA.unpackEncodedStringToUnsignedChars(DFA131_maxS);
    static final short[] DFA131_accept = DFA.unpackEncodedString(DFA131_acceptS);
    static final short[] DFA131_special = DFA.unpackEncodedString(DFA131_specialS);
    static final short[][] DFA131_transition;

    static {
        int numStates = DFA131_transitionS.length;
        DFA131_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA131_transition[i] = DFA.unpackEncodedString(DFA131_transitionS[i]);
        }
    }

    class DFA131 extends DFA {

        public DFA131(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 131;
            this.eot = DFA131_eot;
            this.eof = DFA131_eof;
            this.min = DFA131_min;
            this.max = DFA131_max;
            this.accept = DFA131_accept;
            this.special = DFA131_special;
            this.transition = DFA131_transition;
        }
        public String getDescription() {
            return "()* loopback of 748:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA134_eotS =
        "\13\uffff";
    static final String DFA134_eofS =
        "\13\uffff";
    static final String DFA134_minS =
        "\1\5\2\uffff\1\5\4\uffff\1\5\2\uffff";
    static final String DFA134_maxS =
        "\1\117\2\uffff\1\121\4\uffff\1\121\2\uffff";
    static final String DFA134_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\uffff\1\3\1\7";
    static final String DFA134_specialS =
        "\13\uffff}>";
    static final String[] DFA134_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\6\uffff\1\4\1\7\6\uffff\1\1\31\uffff"+
            "\1\6\21\uffff\12\1",
            "",
            "",
            "\3\11\1\uffff\1\11\1\uffff\1\11\1\uffff\2\11\3\uffff\2\11\1"+
            "\10\5\uffff\1\11\24\uffff\2\11\2\uffff\2\11\3\uffff\1\11\12"+
            "\uffff\1\12\14\11\2\10",
            "",
            "",
            "",
            "",
            "\3\11\1\uffff\1\11\1\uffff\1\11\1\uffff\2\11\3\uffff\2\11\1"+
            "\10\5\uffff\1\11\24\uffff\2\11\2\uffff\2\11\3\uffff\1\11\12"+
            "\uffff\1\12\14\11\2\10",
            "",
            ""
    };

    static final short[] DFA134_eot = DFA.unpackEncodedString(DFA134_eotS);
    static final short[] DFA134_eof = DFA.unpackEncodedString(DFA134_eofS);
    static final char[] DFA134_min = DFA.unpackEncodedStringToUnsignedChars(DFA134_minS);
    static final char[] DFA134_max = DFA.unpackEncodedStringToUnsignedChars(DFA134_maxS);
    static final short[] DFA134_accept = DFA.unpackEncodedString(DFA134_acceptS);
    static final short[] DFA134_special = DFA.unpackEncodedString(DFA134_specialS);
    static final short[][] DFA134_transition;

    static {
        int numStates = DFA134_transitionS.length;
        DFA134_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA134_transition[i] = DFA.unpackEncodedString(DFA134_transitionS[i]);
        }
    }

    class DFA134 extends DFA {

        public DFA134(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 134;
            this.eot = DFA134_eot;
            this.eof = DFA134_eof;
            this.min = DFA134_min;
            this.max = DFA134_max;
            this.accept = DFA134_accept;
            this.special = DFA134_special;
            this.transition = DFA134_transition;
        }
        public String getDescription() {
            return "753:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA140_eotS =
        "\5\uffff";
    static final String DFA140_eofS =
        "\5\uffff";
    static final String DFA140_minS =
        "\1\5\1\uffff\2\5\1\uffff";
    static final String DFA140_maxS =
        "\1\117\1\uffff\2\121\1\uffff";
    static final String DFA140_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA140_specialS =
        "\5\uffff}>";
    static final String[] DFA140_transitionS = {
            "\1\2\2\1\3\uffff\1\1\6\uffff\2\1\6\uffff\1\1\25\uffff\1\1\2"+
            "\uffff\2\1\21\uffff\12\1",
            "",
            "\3\1\3\uffff\1\1\2\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\24"+
            "\uffff\2\1\2\uffff\2\1\1\4\6\uffff\1\4\6\uffff\2\1\1\uffff\12"+
            "\1\2\3",
            "\3\1\3\uffff\1\1\2\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\24"+
            "\uffff\2\1\2\uffff\2\1\7\uffff\1\4\6\uffff\2\1\1\uffff\12\1"+
            "\2\3",
            ""
    };

    static final short[] DFA140_eot = DFA.unpackEncodedString(DFA140_eotS);
    static final short[] DFA140_eof = DFA.unpackEncodedString(DFA140_eofS);
    static final char[] DFA140_min = DFA.unpackEncodedStringToUnsignedChars(DFA140_minS);
    static final char[] DFA140_max = DFA.unpackEncodedStringToUnsignedChars(DFA140_maxS);
    static final short[] DFA140_accept = DFA.unpackEncodedString(DFA140_acceptS);
    static final short[] DFA140_special = DFA.unpackEncodedString(DFA140_specialS);
    static final short[][] DFA140_transition;

    static {
        int numStates = DFA140_transitionS.length;
        DFA140_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA140_transition[i] = DFA.unpackEncodedString(DFA140_transitionS[i]);
        }
    }

    class DFA140 extends DFA {

        public DFA140(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 140;
            this.eot = DFA140_eot;
            this.eof = DFA140_eof;
            this.min = DFA140_min;
            this.max = DFA140_max;
            this.accept = DFA140_accept;
            this.special = DFA140_special;
            this.transition = DFA140_transition;
        }
        public String getDescription() {
            return "781:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA161_eotS =
        "\5\uffff";
    static final String DFA161_eofS =
        "\5\uffff";
    static final String DFA161_minS =
        "\1\16\1\13\1\uffff\1\13\1\uffff";
    static final String DFA161_maxS =
        "\1\104\1\174\1\uffff\1\174\1\uffff";
    static final String DFA161_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA161_specialS =
        "\5\uffff}>";
    static final String[] DFA161_transitionS = {
            "\1\1\65\uffff\1\2",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\51\uffff\2\2",
            "",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\51\uffff\2\2",
            ""
    };

    static final short[] DFA161_eot = DFA.unpackEncodedString(DFA161_eotS);
    static final short[] DFA161_eof = DFA.unpackEncodedString(DFA161_eofS);
    static final char[] DFA161_min = DFA.unpackEncodedStringToUnsignedChars(DFA161_minS);
    static final char[] DFA161_max = DFA.unpackEncodedStringToUnsignedChars(DFA161_maxS);
    static final short[] DFA161_accept = DFA.unpackEncodedString(DFA161_acceptS);
    static final short[] DFA161_special = DFA.unpackEncodedString(DFA161_specialS);
    static final short[][] DFA161_transition;

    static {
        int numStates = DFA161_transitionS.length;
        DFA161_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA161_transition[i] = DFA.unpackEncodedString(DFA161_transitionS[i]);
        }
    }

    class DFA161 extends DFA {

        public DFA161(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 161;
            this.eot = DFA161_eot;
            this.eof = DFA161_eof;
            this.min = DFA161_min;
            this.max = DFA161_max;
            this.accept = DFA161_accept;
            this.special = DFA161_special;
            this.transition = DFA161_transition;
        }
        public String getDescription() {
            return "()* loopback of 876:16: ( COMMA ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x0BF040003A2C0D30L,0x0400000000000000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x0BF040003A3C0C30L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x0BF040003A2C0C30L,0x0400000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x0BF040003A2C0830L,0x0400000000000000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000100012L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000100200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000100040L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000100040L,0x0000000000030000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000100200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000100402L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000030000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x0000000000170220L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x0000000000070220L,0x0000000000000008L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x0000000000171020L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x0000000000071020L,0x0000000000000008L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x0BF040002A2C2020L,0x0400000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0400000000030000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0400000000030000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0400000000030000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media423 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_media427 = new BitSet(new long[]{0x0BF040002A2C2020L,0x0400000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList457 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList461 = new BitSet(new long[]{0x0000000000170020L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList463 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000008L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList466 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery485 = new BitSet(new long[]{0x0000000000170020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery487 = new BitSet(new long[]{0x0000000000070020L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery494 = new BitSet(new long[]{0x0000000000108002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery496 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery501 = new BitSet(new long[]{0x0000000000170020L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_mediaQuery503 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000008L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery506 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery514 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery518 = new BitSet(new long[]{0x0000000000170020L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_mediaQuery520 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000008L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery523 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression578 = new BitSet(new long[]{0x0000000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression580 = new BitSet(new long[]{0x0000000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression583 = new BitSet(new long[]{0x0000400000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_mediaExpression585 = new BitSet(new long[]{0x0000400000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression590 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_mediaExpression592 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_expression_in_mediaExpression595 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression600 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body634 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_body636 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0400000000000000L});
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
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document862 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document864 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document869 = new BitSet(new long[]{0x0000000000105000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document871 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document877 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document879 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document882 = new BitSet(new long[]{0x0000000000105000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document884 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document891 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document893 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_body_in_moz_document898 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes944 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes946 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes949 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes951 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes956 = new BitSet(new long[]{0x0000000004102020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes958 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes965 = new BitSet(new long[]{0x0000000004102020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes967 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock987 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock989 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock994 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock997 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1000 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1004 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1022 = new BitSet(new long[]{0x0000000000104002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1034 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1037 = new BitSet(new long[]{0x0000000004100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1039 = new BitSet(new long[]{0x0000000004000020L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1042 = new BitSet(new long[]{0x0000000000104002L,0x0000000000030000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1071 = new BitSet(new long[]{0x0000400000101020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1073 = new BitSet(new long[]{0x0000400000001020L});
    public static final BitSet FOLLOW_IDENT_in_page1078 = new BitSet(new long[]{0x0000400000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1080 = new BitSet(new long[]{0x0000400000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1087 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1089 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page1102 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_page1104 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0400000000000000L});
    public static final BitSet FOLLOW_declaration_in_page1159 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1161 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1163 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1169 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_page1171 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0400000000000000L});
    public static final BitSet FOLLOW_declaration_in_page1175 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1177 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1179 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1215 = new BitSet(new long[]{0x0000000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1217 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1220 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1222 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1233 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1235 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1238 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1242 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1273 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fontFace1275 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1286 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_fontFace1288 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1291 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1295 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1320 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_margin1322 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1325 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_margin1327 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1330 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_margin1332 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1563 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1615 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1626 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1637 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property1700 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_property1704 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_property1708 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_property1711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1738 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_less_mixin_declaration_in_rule1742 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1755 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_rule1757 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule1760 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_rule1774 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_declarations1862 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_declaration_in_declarations1905 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_declarations1907 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_less_mixin_call_in_declarations1945 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_declarations1985 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0400000000000000L});
    public static final BitSet FOLLOW_set_in_rulePredicate2050 = new BitSet(new long[]{0xFFFFFFFFFFFFDDF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_LBRACE_in_rulePredicate2067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_declarationPredicate2110 = new BitSet(new long[]{0xFFFFFFFFFFFFCFF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_SEMI_in_declarationPredicate2127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_lastDeclarationPredicate2170 = new BitSet(new long[]{0xFFFFFFFFFFFFEDF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_RBRACE_in_lastDeclarationPredicate2187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2208 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2211 = new BitSet(new long[]{0x0BF0400000140020L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2213 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2216 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2239 = new BitSet(new long[]{0x0BF7400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector2242 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2244 = new BitSet(new long[]{0x0BF7400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2277 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2284 = new BitSet(new long[]{0x0BF0400000140022L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2286 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2305 = new BitSet(new long[]{0x0BF0400000140022L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2307 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2410 = new BitSet(new long[]{0x0B00000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2416 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred2439 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred2448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2463 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2467 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_122_in_cssId2572 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2602 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2676 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2683 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2686 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2697 = new BitSet(new long[]{0xF000000000100000L,0x0000000000030007L});
    public static final BitSet FOLLOW_ws_in_slAttribute2699 = new BitSet(new long[]{0xF000000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_set_in_slAttribute2741 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2921 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2940 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030004L});
    public static final BitSet FOLLOW_ws_in_slAttribute2958 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3077 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo3141 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3198 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3201 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_ws_in_pseudo3203 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_expression_in_pseudo3208 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_STAR_in_pseudo3212 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3296 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3298 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3301 = new BitSet(new long[]{0x0BF0400000140020L,0x0400000000030010L});
    public static final BitSet FOLLOW_ws_in_pseudo3303 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000010L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3353 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_property_in_declaration3356 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration3358 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_declaration3360 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3363 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_prio_in_declaration3365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_function_in_propertyValue3393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3526 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_operator_in_expression3531 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_expression3533 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_expression3538 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_unaryOperator_in_term3563 = new BitSet(new long[]{0x0BF040003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_term3565 = new BitSet(new long[]{0x0BF040003E2C08E0L,0x040000000000FFC0L});
    public static final BitSet FOLLOW_set_in_term3589 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_STRING_in_term3789 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_IDENT_in_term3797 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_term3805 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_URI_in_term3813 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_hexColor_in_term3821 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_function_in_term3829 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_term3837 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_term3849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3865 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_function3867 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_function3872 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_function3874 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_expression_in_function3884 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_fnAttribute_in_function3902 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_function3905 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_function3907 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_fnAttribute_in_function3910 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_function3931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName3980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4000 = new BitSet(new long[]{0x1000000000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4002 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4005 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4007 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4010 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4025 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4028 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4030 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4062 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4083 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_less_variable_declaration4120 = new BitSet(new long[]{0x0000400000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_variable_declaration4122 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_less_variable_declaration4125 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_variable_declaration4127 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_less_expression_in_less_variable_declaration4130 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_variable_declaration4132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_variable0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_less_function4175 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_function4177 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_less_expression_in_less_function4180 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_function4183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_less_expression4200 = new BitSet(new long[]{0x0109800000000002L});
    public static final BitSet FOLLOW_less_expression_operator_in_less_expression4203 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_expression4205 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_less_expression4208 = new BitSet(new long[]{0x0109800000000002L});
    public static final BitSet FOLLOW_set_in_less_expression_operator4235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_declaration4318 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4320 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_declaration4323 = new BitSet(new long[]{0x0BF040003A2C0820L,0x1C00000000000010L});
    public static final BitSet FOLLOW_less_args_list_in_less_mixin_declaration4325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_declaration4328 = new BitSet(new long[]{0x0000000000100002L,0x0000000000070000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4330 = new BitSet(new long[]{0x0000000000000002L,0x0000000000040000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_less_mixin_declaration4334 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_call4361 = new BitSet(new long[]{0x0000000000100200L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call4363 = new BitSet(new long[]{0x0000000000000200L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_call4367 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_less_mixin_call_args_in_less_mixin_call4369 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_call4372 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_mixin_call4376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args4402 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_less_mixin_call_args4406 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call_args4408 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args4411 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list4443 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_less_args_list4447 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_args_list4449 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list4452 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_less_args_list4457 = new BitSet(new long[]{0x0000000000100000L,0x1800000000030000L});
    public static final BitSet FOLLOW_ws_in_less_args_list4459 = new BitSet(new long[]{0x0000000000000000L,0x1800000000000000L});
    public static final BitSet FOLLOW_set_in_less_args_list4462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list4484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_variable_in_less_arg4516 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_COLON_in_less_arg4520 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_arg4522 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_less_expression_in_less_arg4525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded4551 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded4553 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded4556 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded4560 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded4568 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded4571 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_NOT_in_less_condition4601 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_condition4603 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition4612 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_condition4614 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition4640 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_condition4642 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_less_variable_in_less_condition4673 = new BitSet(new long[]{0x1002000000100000L,0x00000000003B0010L});
    public static final BitSet FOLLOW_ws_in_less_condition4676 = new BitSet(new long[]{0x1002000000100000L,0x00000000003B0000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition4679 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_condition4681 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_less_expression_in_less_condition4684 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition4713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition4739 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition4741 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition4744 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition4746 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_variable_in_less_function_in_condition4749 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition4751 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition4754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name4776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rulePredicate_in_synpred1_Css31859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declarationPredicate_in_synpred2_Css31902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred3_Css32281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred4_Css32302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred5_Css32407 = new BitSet(new long[]{0x0000000000000002L});

}