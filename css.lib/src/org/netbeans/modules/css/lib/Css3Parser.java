// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-04 16:39:59

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "SASS_EXTEND_ONLY_SELECTOR", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "SASS_EXTEND", "SASS_OPTIONAL", "SASS_DEBUG", "SASS_WARN", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
    };
    public static final int EOF=-1;
    public static final int NAMESPACE_SYM=4;
    public static final int SEMI=5;
    public static final int IDENT=6;
    public static final int STRING=7;
    public static final int URI=8;
    public static final int CHARSET_SYM=9;
    public static final int IMPORT_SYM=10;
    public static final int COMMA=11;
    public static final int MEDIA_SYM=12;
    public static final int LBRACE=13;
    public static final int RBRACE=14;
    public static final int AND=15;
    public static final int ONLY=16;
    public static final int NOT=17;
    public static final int GEN=18;
    public static final int LPAREN=19;
    public static final int COLON=20;
    public static final int RPAREN=21;
    public static final int AT_IDENT=22;
    public static final int WS=23;
    public static final int MOZ_DOCUMENT_SYM=24;
    public static final int MOZ_URL_PREFIX=25;
    public static final int MOZ_DOMAIN=26;
    public static final int MOZ_REGEXP=27;
    public static final int WEBKIT_KEYFRAMES_SYM=28;
    public static final int PERCENTAGE=29;
    public static final int PAGE_SYM=30;
    public static final int COUNTER_STYLE_SYM=31;
    public static final int FONT_FACE_SYM=32;
    public static final int TOPLEFTCORNER_SYM=33;
    public static final int TOPLEFT_SYM=34;
    public static final int TOPCENTER_SYM=35;
    public static final int TOPRIGHT_SYM=36;
    public static final int TOPRIGHTCORNER_SYM=37;
    public static final int BOTTOMLEFTCORNER_SYM=38;
    public static final int BOTTOMLEFT_SYM=39;
    public static final int BOTTOMCENTER_SYM=40;
    public static final int BOTTOMRIGHT_SYM=41;
    public static final int BOTTOMRIGHTCORNER_SYM=42;
    public static final int LEFTTOP_SYM=43;
    public static final int LEFTMIDDLE_SYM=44;
    public static final int LEFTBOTTOM_SYM=45;
    public static final int RIGHTTOP_SYM=46;
    public static final int RIGHTMIDDLE_SYM=47;
    public static final int RIGHTBOTTOM_SYM=48;
    public static final int SOLIDUS=49;
    public static final int PLUS=50;
    public static final int GREATER=51;
    public static final int TILDE=52;
    public static final int MINUS=53;
    public static final int HASH_SYMBOL=54;
    public static final int HASH=55;
    public static final int DOT=56;
    public static final int LBRACKET=57;
    public static final int DCOLON=58;
    public static final int SASS_EXTEND_ONLY_SELECTOR=59;
    public static final int STAR=60;
    public static final int PIPE=61;
    public static final int NAME=62;
    public static final int LESS_AND=63;
    public static final int OPEQ=64;
    public static final int INCLUDES=65;
    public static final int DASHMATCH=66;
    public static final int BEGINS=67;
    public static final int ENDS=68;
    public static final int CONTAINS=69;
    public static final int RBRACKET=70;
    public static final int IMPORTANT_SYM=71;
    public static final int NUMBER=72;
    public static final int LENGTH=73;
    public static final int EMS=74;
    public static final int REM=75;
    public static final int EXS=76;
    public static final int ANGLE=77;
    public static final int TIME=78;
    public static final int FREQ=79;
    public static final int RESOLUTION=80;
    public static final int DIMENSION=81;
    public static final int NL=82;
    public static final int COMMENT=83;
    public static final int SASS_DEFAULT=84;
    public static final int SASS_VAR=85;
    public static final int SASS_MIXIN=86;
    public static final int SASS_INCLUDE=87;
    public static final int LESS_DOTS=88;
    public static final int LESS_REST=89;
    public static final int LESS_WHEN=90;
    public static final int GREATER_OR_EQ=91;
    public static final int LESS=92;
    public static final int LESS_OR_EQ=93;
    public static final int SASS_EXTEND=94;
    public static final int SASS_OPTIONAL=95;
    public static final int SASS_DEBUG=96;
    public static final int SASS_WARN=97;
    public static final int HEXCHAR=98;
    public static final int NONASCII=99;
    public static final int UNICODE=100;
    public static final int ESCAPE=101;
    public static final int NMSTART=102;
    public static final int NMCHAR=103;
    public static final int URL=104;
    public static final int A=105;
    public static final int B=106;
    public static final int C=107;
    public static final int D=108;
    public static final int E=109;
    public static final int F=110;
    public static final int G=111;
    public static final int H=112;
    public static final int I=113;
    public static final int J=114;
    public static final int K=115;
    public static final int L=116;
    public static final int M=117;
    public static final int N=118;
    public static final int O=119;
    public static final int P=120;
    public static final int Q=121;
    public static final int R=122;
    public static final int S=123;
    public static final int T=124;
    public static final int U=125;
    public static final int V=126;
    public static final int W=127;
    public static final int X=128;
    public static final int Y=129;
    public static final int Z=130;
    public static final int CDO=131;
    public static final int CDC=132;
    public static final int INVALID=133;
    public static final int LINE_COMMENT=134;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "selector", "namespace", "scss_declaration_interpolation_expression", 
        "pseudoPage", "synpred18_Css3", "less_fn_name", "declaration", "esPred", 
        "margin_sym", "vendorAtRule", "namespacePrefix", "slAttribute", 
        "typeSelector", "fnAttributeValue", "less_function_in_condition", 
        "expression", "mediaFeature", "mediaQueryOperator", "charSetValue", 
        "less_condition_operator", "synpred6_Css3", "sass_extend", "body", 
        "synpred13_Css3", "function", "cp_mixin_call_args", "namespaces", 
        "cp_variable", "simpleSelectorSequence", "cp_expression", "synpred7_Css3", 
        "synpred3_Css3", "cp_multiplyExp", "combinator", "cp_variable_declaration", 
        "fnAttributeName", "less_mixin_guarded", "cp_mixin_call", "resourceIdentifier", 
        "synpred11_Css3", "synpred8_Css3", "styleSheet", "unaryOperator", 
        "syncTo_SEMI", "sass_extend_only_selector", "property", "slAttributeValue", 
        "syncTo_RBRACE", "webkitKeyframeSelectors", "importItem", "namespacePrefixName", 
        "margin", "scss_interpolation_expression_var", "syncToFollow", "cp_additionExp", 
        "scss_nested_properties", "synpred17_Css3", "synpred16_Css3", "operator", 
        "syncToDeclarationsRule", "mediaQueryList", "media", "webkitKeyframesBlock", 
        "expressionPredicate", "functionName", "webkitKeyframes", "hexColor", 
        "synpred4_Css3", "cssId", "synpred2_Css3", "generic_at_rule", "moz_document", 
        "mediaQuery", "mediaExpression", "pseudo", "synpred1_Css3", "synpred20_Css3", 
        "imports", "page", "scss_selector_interpolation_expression", "fnAttribute", 
        "scss_mq_interpolation_expression", "synpred5_Css3", "mediaType", 
        "counterStyle", "prio", "charSet", "fontFace", "atRuleId", "rule", 
        "synpred12_Css3", "synpred14_Css3", "cp_mixin_declaration", "bodyItem", 
        "synpred10_Css3", "propertyValue", "sass_debug", "moz_document_function", 
        "cp_term", "cp_mixin_name", "elementName", "term", "less_args_list", 
        "synpred15_Css3", "cp_atomExp", "elementSubsequent", "synpred19_Css3", 
        "synpred9_Css3", "selectorsGroup", "ws", "less_condition", "declarations", 
        "slAttributeName", "less_arg", "cssClass"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, false, false, false, false, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, false, false, false, false, true, false, false, true, 
            false, true, false, true, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            true, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false
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



        protected boolean isLessSource() {
            return false;
        }
        
        protected boolean isScssSource() {
            return false;
        }
        
        private boolean isCssPreprocessorSource() {
            return isLessSource() || isScssSource();
        }
        
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:1: styleSheet : ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(317, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:5: ( ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:6: ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF
            {
            dbg.location(319,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:6: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:6: ws
                    {
                    dbg.location(319,6);
                    pushFollow(FOLLOW_ws_in_styleSheet125);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(320,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:6: ( charSet ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:8: charSet ( ws )?
                    {
                    dbg.location(320,8);
                    pushFollow(FOLLOW_charSet_in_styleSheet135);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(320,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:16: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:16: ws
                            {
                            dbg.location(320,16);
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

            dbg.location(321,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:9: ( imports )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:9: imports
                    {
                    dbg.location(321,9);
                    pushFollow(FOLLOW_imports_in_styleSheet151);
                    imports();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(4);}

            dbg.location(322,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:9: ( namespaces )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:9: namespaces
                    {
                    dbg.location(322,9);
                    pushFollow(FOLLOW_namespaces_in_styleSheet162);
                    namespaces();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(5);}

            dbg.location(323,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:9: ( body )?
            int alt6=2;
            try { dbg.enterSubRule(6);
            try { dbg.enterDecision(6, decisionCanBacktrack[6]);

            int LA6_0 = input.LA(1);

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||LA6_0==GEN||LA6_0==COLON||LA6_0==AT_IDENT||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||(LA6_0>=MINUS && LA6_0<=PIPE)||LA6_0==LESS_AND||(LA6_0>=SASS_VAR && LA6_0<=SASS_INCLUDE)||(LA6_0>=SASS_DEBUG && LA6_0<=SASS_WARN)) ) {
                alt6=1;
            }
            } finally {dbg.exitDecision(6);}

            switch (alt6) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:9: body
                    {
                    dbg.location(323,9);
                    pushFollow(FOLLOW_body_in_styleSheet174);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(6);}

            dbg.location(324,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet182); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "namespaces"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:1: namespaces : ( namespace ( ws )? )+ ;
    public final void namespaces() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaces");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(327, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:2: ( ( namespace ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:2: ( namespace ( ws )? )+
            {
            dbg.location(329,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:2: ( namespace ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:4: namespace ( ws )?
            	    {
            	    dbg.location(329,4);
            	    pushFollow(FOLLOW_namespace_in_namespaces199);
            	    namespace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(329,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:14: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:14: ws
            	            {
            	            dbg.location(329,14);
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
        dbg.location(330, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:1: namespace : NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? SEMI ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(332, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:3: ( NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:5: NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? SEMI
            {
            dbg.location(333,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace217); if (state.failed) return ;
            dbg.location(333,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:19: ws
                    {
                    dbg.location(333,19);
                    pushFollow(FOLLOW_ws_in_namespace219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(333,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:23: ( namespacePrefixName ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:24: namespacePrefixName ( ws )?
                    {
                    dbg.location(333,24);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespace223);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(333,44);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:44: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:44: ws
                            {
                            dbg.location(333,44);
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

            dbg.location(333,50);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace230);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(333,69);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:69: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:69: ws
                    {
                    dbg.location(333,69);
                    pushFollow(FOLLOW_ws_in_namespace232);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}

            dbg.location(333,73);
            match(input,SEMI,FOLLOW_SEMI_in_namespace235); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(334, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:1: namespacePrefixName : IDENT ;
    public final void namespacePrefixName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefixName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(336, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:5: IDENT
            {
            dbg.location(337,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespacePrefixName248); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(338, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(340, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(341,3);
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
        dbg.location(342, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:1: charSet : CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(344, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:5: ( CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:9: CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI
            {
            dbg.location(345,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet286); if (state.failed) return ;
            dbg.location(345,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:21: ws
                    {
                    dbg.location(345,21);
                    pushFollow(FOLLOW_ws_in_charSet288);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(13);}

            dbg.location(345,25);
            pushFollow(FOLLOW_charSetValue_in_charSet291);
            charSetValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(345,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:38: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:38: ws
                    {
                    dbg.location(345,38);
                    pushFollow(FOLLOW_ws_in_charSet293);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(14);}

            dbg.location(345,42);
            match(input,SEMI,FOLLOW_SEMI_in_charSet296); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "charSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSet"


    // $ANTLR start "charSetValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:1: charSetValue : STRING ;
    public final void charSetValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSetValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(348, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:2: ( STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:4: STRING
            {
            dbg.location(349,4);
            match(input,STRING,FOLLOW_STRING_in_charSetValue310); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "charSetValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSetValue"


    // $ANTLR start "imports"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:1: imports : ( importItem ( ws )? )+ ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(352, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:2: ( ( importItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:2: ( importItem ( ws )? )+
            {
            dbg.location(354,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:2: ( importItem ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:4: importItem ( ws )?
            	    {
            	    dbg.location(354,4);
            	    pushFollow(FOLLOW_importItem_in_imports324);
            	    importItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(354,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:15: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:15: ws
            	            {
            	            dbg.location(354,15);
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
        dbg.location(355, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: importItem : ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI | {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier ) mediaQueryList SEMI );
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI | {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier ) mediaQueryList SEMI )
            int alt22=2;
            try { dbg.enterDecision(22, decisionCanBacktrack[22]);

            try {
                isCyclicDecision = true;
                alt22 = dfa22.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(22);}

            switch (alt22) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:9: IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI
                    {
                    dbg.location(359,9);
                    match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem356); if (state.failed) return ;
                    dbg.location(359,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:20: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:20: ws
                            {
                            dbg.location(359,20);
                            pushFollow(FOLLOW_ws_in_importItem358);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(17);}

                    dbg.location(359,24);
                    pushFollow(FOLLOW_resourceIdentifier_in_importItem361);
                    resourceIdentifier();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(359,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:43: ws
                            {
                            dbg.location(359,43);
                            pushFollow(FOLLOW_ws_in_importItem363);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(18);}

                    dbg.location(359,47);
                    pushFollow(FOLLOW_mediaQueryList_in_importItem366);
                    mediaQueryList();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(359,62);
                    match(input,SEMI,FOLLOW_SEMI_in_importItem368); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:9: {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier ) mediaQueryList SEMI
                    {
                    dbg.location(362,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "importItem", "isScssSource()");
                    }
                    dbg.location(362,27);
                    match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem399); if (state.failed) return ;
                    dbg.location(362,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:38: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:38: ws
                            {
                            dbg.location(362,38);
                            pushFollow(FOLLOW_ws_in_importItem401);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(19);}

                    dbg.location(362,42);
                    pushFollow(FOLLOW_resourceIdentifier_in_importItem404);
                    resourceIdentifier();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(362,61);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:61: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:61: ws
                            {
                            dbg.location(362,61);
                            pushFollow(FOLLOW_ws_in_importItem406);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(20);}

                    dbg.location(362,65);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:65: ( COMMA ( ws )? resourceIdentifier )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:66: COMMA ( ws )? resourceIdentifier
                    {
                    dbg.location(362,66);
                    match(input,COMMA,FOLLOW_COMMA_in_importItem410); if (state.failed) return ;
                    dbg.location(362,72);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:72: ( ws )?
                    int alt21=2;
                    try { dbg.enterSubRule(21);
                    try { dbg.enterDecision(21, decisionCanBacktrack[21]);

                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==WS||(LA21_0>=NL && LA21_0<=COMMENT)) ) {
                        alt21=1;
                    }
                    } finally {dbg.exitDecision(21);}

                    switch (alt21) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:72: ws
                            {
                            dbg.location(362,72);
                            pushFollow(FOLLOW_ws_in_importItem412);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(21);}

                    dbg.location(362,76);
                    pushFollow(FOLLOW_resourceIdentifier_in_importItem415);
                    resourceIdentifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }

                    dbg.location(362,96);
                    pushFollow(FOLLOW_mediaQueryList_in_importItem418);
                    mediaQueryList();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(362,111);
                    match(input,SEMI,FOLLOW_SEMI_in_importItem420); if (state.failed) return ;

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
        dbg.location(363, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE
            {
            dbg.location(365,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media436); if (state.failed) return ;
            dbg.location(365,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:17: ( ws )?
            int alt23=2;
            try { dbg.enterSubRule(23);
            try { dbg.enterDecision(23, decisionCanBacktrack[23]);

            int LA23_0 = input.LA(1);

            if ( (LA23_0==WS||(LA23_0>=NL && LA23_0<=COMMENT)) ) {
                alt23=1;
            }
            } finally {dbg.exitDecision(23);}

            switch (alt23) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:17: ws
                    {
                    dbg.location(365,17);
                    pushFollow(FOLLOW_ws_in_media438);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(23);}

            dbg.location(367,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:9: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList )
            int alt25=2;
            try { dbg.enterSubRule(25);
            try { dbg.enterDecision(25, decisionCanBacktrack[25]);

            try {
                isCyclicDecision = true;
                alt25 = dfa25.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(25);}

            switch (alt25) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )?
                    {
                    dbg.location(368,64);
                    pushFollow(FOLLOW_scss_mq_interpolation_expression_in_media493);
                    scss_mq_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(368,97);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:97: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:97: ws
                            {
                            dbg.location(368,97);
                            pushFollow(FOLLOW_ws_in_media495);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(24);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:13: ( mediaQueryList )=> mediaQueryList
                    {
                    dbg.location(370,31);
                    pushFollow(FOLLOW_mediaQueryList_in_media529);
                    mediaQueryList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(25);}

            dbg.location(373,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media558); if (state.failed) return ;
            dbg.location(373,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:16: ( ws )?
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            int LA26_0 = input.LA(1);

            if ( (LA26_0==WS||(LA26_0>=NL && LA26_0<=COMMENT)) ) {
                alt26=1;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:16: ws
                    {
                    dbg.location(373,16);
                    pushFollow(FOLLOW_ws_in_media560);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(26);}

            dbg.location(374,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*
            try { dbg.enterSubRule(35);

            loop35:
            do {
                int alt35=9;
                try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                try {
                    isCyclicDecision = true;
                    alt35 = dfa35.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(35);}

                switch (alt35) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:17: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(376,134);
            	    pushFollow(FOLLOW_declaration_in_media646);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(376,146);
            	    match(input,SEMI,FOLLOW_SEMI_in_media648); if (state.failed) return ;
            	    dbg.location(376,151);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:151: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:151: ws
            	            {
            	            dbg.location(376,151);
            	            pushFollow(FOLLOW_ws_in_media650);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(27);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:19: {...}? sass_extend ( ws )?
            	    {
            	    dbg.location(377,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(377,37);
            	    pushFollow(FOLLOW_sass_extend_in_media673);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(377,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:49: ( ws )?
            	    int alt28=2;
            	    try { dbg.enterSubRule(28);
            	    try { dbg.enterDecision(28, decisionCanBacktrack[28]);

            	    int LA28_0 = input.LA(1);

            	    if ( (LA28_0==WS||(LA28_0>=NL && LA28_0<=COMMENT)) ) {
            	        alt28=1;
            	    }
            	    } finally {dbg.exitDecision(28);}

            	    switch (alt28) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:49: ws
            	            {
            	            dbg.location(377,49);
            	            pushFollow(FOLLOW_ws_in_media675);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(28);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:19: {...}? sass_debug ( ws )?
            	    {
            	    dbg.location(378,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(378,37);
            	    pushFollow(FOLLOW_sass_debug_in_media698);
            	    sass_debug();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(378,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:48: ws
            	            {
            	            dbg.location(378,48);
            	            pushFollow(FOLLOW_ws_in_media700);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(29);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:19: rule ( ws )?
            	    {
            	    dbg.location(380,19);
            	    pushFollow(FOLLOW_rule_in_media738);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(380,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:25: ws
            	            {
            	            dbg.location(380,25);
            	            pushFollow(FOLLOW_ws_in_media741);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(30);}


            	    }
            	    break;
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:19: page ( ws )?
            	    {
            	    dbg.location(381,19);
            	    pushFollow(FOLLOW_page_in_media762);
            	    page();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(381,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:25: ( ws )?
            	    int alt31=2;
            	    try { dbg.enterSubRule(31);
            	    try { dbg.enterDecision(31, decisionCanBacktrack[31]);

            	    int LA31_0 = input.LA(1);

            	    if ( (LA31_0==WS||(LA31_0>=NL && LA31_0<=COMMENT)) ) {
            	        alt31=1;
            	    }
            	    } finally {dbg.exitDecision(31);}

            	    switch (alt31) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:25: ws
            	            {
            	            dbg.location(381,25);
            	            pushFollow(FOLLOW_ws_in_media765);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(31);}


            	    }
            	    break;
            	case 6 :
            	    dbg.enterAlt(6);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:19: fontFace ( ws )?
            	    {
            	    dbg.location(382,19);
            	    pushFollow(FOLLOW_fontFace_in_media786);
            	    fontFace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(382,29);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:29: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:29: ws
            	            {
            	            dbg.location(382,29);
            	            pushFollow(FOLLOW_ws_in_media789);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(32);}


            	    }
            	    break;
            	case 7 :
            	    dbg.enterAlt(7);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:19: vendorAtRule ( ws )?
            	    {
            	    dbg.location(383,19);
            	    pushFollow(FOLLOW_vendorAtRule_in_media810);
            	    vendorAtRule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(383,33);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:33: ( ws )?
            	    int alt33=2;
            	    try { dbg.enterSubRule(33);
            	    try { dbg.enterDecision(33, decisionCanBacktrack[33]);

            	    int LA33_0 = input.LA(1);

            	    if ( (LA33_0==WS||(LA33_0>=NL && LA33_0<=COMMENT)) ) {
            	        alt33=1;
            	    }
            	    } finally {dbg.exitDecision(33);}

            	    switch (alt33) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:33: ws
            	            {
            	            dbg.location(383,33);
            	            pushFollow(FOLLOW_ws_in_media813);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(33);}


            	    }
            	    break;
            	case 8 :
            	    dbg.enterAlt(8);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:19: {...}? media ( ws )?
            	    {
            	    dbg.location(384,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(384,37);
            	    pushFollow(FOLLOW_media_in_media836);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(384,43);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:43: ( ws )?
            	    int alt34=2;
            	    try { dbg.enterSubRule(34);
            	    try { dbg.enterDecision(34, decisionCanBacktrack[34]);

            	    int LA34_0 = input.LA(1);

            	    if ( (LA34_0==WS||(LA34_0>=NL && LA34_0<=COMMENT)) ) {
            	        alt34=1;
            	    }
            	    } finally {dbg.exitDecision(34);}

            	    switch (alt34) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:43: ws
            	            {
            	            dbg.location(384,43);
            	            pushFollow(FOLLOW_ws_in_media838);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(34);}


            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);
            } finally {dbg.exitSubRule(35);}

            dbg.location(387,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media882); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(388, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(390, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(391,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt38=2;
            try { dbg.enterSubRule(38);
            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

            int LA38_0 = input.LA(1);

            if ( (LA38_0==IDENT||(LA38_0>=ONLY && LA38_0<=LPAREN)) ) {
                alt38=1;
            }
            } finally {dbg.exitDecision(38);}

            switch (alt38) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(391,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList898);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(391,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(37);

                    loop37:
                    do {
                        int alt37=2;
                        try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                        int LA37_0 = input.LA(1);

                        if ( (LA37_0==COMMA) ) {
                            alt37=1;
                        }


                        } finally {dbg.exitDecision(37);}

                        switch (alt37) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(391,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList902); if (state.failed) return ;
                    	    dbg.location(391,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:25: ws
                    	            {
                    	            dbg.location(391,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList904);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(36);}

                    	    dbg.location(391,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList907);
                    	    mediaQuery();

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
                    break;

            }
            } finally {dbg.exitSubRule(38);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(392, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(394, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt46=2;
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            int LA46_0 = input.LA(1);

            if ( (LA46_0==IDENT||(LA46_0>=ONLY && LA46_0<=GEN)) ) {
                alt46=1;
            }
            else if ( (LA46_0==LPAREN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(395,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:4: ( mediaQueryOperator ( ws )? )?
                    int alt40=2;
                    try { dbg.enterSubRule(40);
                    try { dbg.enterDecision(40, decisionCanBacktrack[40]);

                    int LA40_0 = input.LA(1);

                    if ( ((LA40_0>=ONLY && LA40_0<=NOT)) ) {
                        alt40=1;
                    }
                    } finally {dbg.exitDecision(40);}

                    switch (alt40) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(395,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery926);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(395,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:24: ws
                                    {
                                    dbg.location(395,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery928);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(39);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(40);}

                    dbg.location(395,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery935);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(395,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:42: ( ws )?
                    int alt41=2;
                    try { dbg.enterSubRule(41);
                    try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                    int LA41_0 = input.LA(1);

                    if ( (LA41_0==WS||(LA41_0>=NL && LA41_0<=COMMENT)) ) {
                        alt41=1;
                    }
                    } finally {dbg.exitDecision(41);}

                    switch (alt41) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:42: ws
                            {
                            dbg.location(395,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery937);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(41);}

                    dbg.location(395,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(43);

                    loop43:
                    do {
                        int alt43=2;
                        try { dbg.enterDecision(43, decisionCanBacktrack[43]);

                        int LA43_0 = input.LA(1);

                        if ( (LA43_0==AND) ) {
                            alt43=1;
                        }


                        } finally {dbg.exitDecision(43);}

                        switch (alt43) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(395,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery942); if (state.failed) return ;
                    	    dbg.location(395,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:52: ( ws )?
                    	    int alt42=2;
                    	    try { dbg.enterSubRule(42);
                    	    try { dbg.enterDecision(42, decisionCanBacktrack[42]);

                    	    int LA42_0 = input.LA(1);

                    	    if ( (LA42_0==WS||(LA42_0>=NL && LA42_0<=COMMENT)) ) {
                    	        alt42=1;
                    	    }
                    	    } finally {dbg.exitDecision(42);}

                    	    switch (alt42) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:52: ws
                    	            {
                    	            dbg.location(395,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery944);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(42);}

                    	    dbg.location(395,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery947);
                    	    mediaExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(396,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery955);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(396,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:20: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(45);

                    loop45:
                    do {
                        int alt45=2;
                        try { dbg.enterDecision(45, decisionCanBacktrack[45]);

                        int LA45_0 = input.LA(1);

                        if ( (LA45_0==AND) ) {
                            alt45=1;
                        }


                        } finally {dbg.exitDecision(45);}

                        switch (alt45) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(396,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery959); if (state.failed) return ;
                    	    dbg.location(396,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:26: ( ws )?
                    	    int alt44=2;
                    	    try { dbg.enterSubRule(44);
                    	    try { dbg.enterDecision(44, decisionCanBacktrack[44]);

                    	    int LA44_0 = input.LA(1);

                    	    if ( (LA44_0==WS||(LA44_0>=NL && LA44_0<=COMMENT)) ) {
                    	        alt44=1;
                    	    }
                    	    } finally {dbg.exitDecision(44);}

                    	    switch (alt44) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:26: ws
                    	            {
                    	            dbg.location(396,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery961);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(44);}

                    	    dbg.location(396,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery964);
                    	    mediaExpression();

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
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(397, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(399, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(400,3);
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
        dbg.location(401, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(403, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(404,2);
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
        dbg.location(405, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(407, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(408,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression1019); if (state.failed) return ;
            dbg.location(408,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:14: ws
                    {
                    dbg.location(408,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression1021);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(408,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression1024);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(408,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:31: ws
                    {
                    dbg.location(408,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression1026);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}

            dbg.location(408,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:35: ( COLON ( ws )? expression )?
            int alt50=2;
            try { dbg.enterSubRule(50);
            try { dbg.enterDecision(50, decisionCanBacktrack[50]);

            int LA50_0 = input.LA(1);

            if ( (LA50_0==COLON) ) {
                alt50=1;
            }
            } finally {dbg.exitDecision(50);}

            switch (alt50) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:37: COLON ( ws )? expression
                    {
                    dbg.location(408,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression1031); if (state.failed) return ;
                    dbg.location(408,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:43: ws
                            {
                            dbg.location(408,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression1033);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(49);}

                    dbg.location(408,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression1036);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}

            dbg.location(408,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression1041); if (state.failed) return ;
            dbg.location(408,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:68: ws
                    {
                    dbg.location(408,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression1043);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(409, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(411, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:4: IDENT
            {
            dbg.location(412,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature1059); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(415, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:2: ( bodyItem ( ws )? )+
            {
            dbg.location(416,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:2: ( bodyItem ( ws )? )+
            int cnt53=0;
            try { dbg.enterSubRule(53);

            loop53:
            do {
                int alt53=2;
                try { dbg.enterDecision(53, decisionCanBacktrack[53]);

                int LA53_0 = input.LA(1);

                if ( (LA53_0==IDENT||LA53_0==MEDIA_SYM||LA53_0==GEN||LA53_0==COLON||LA53_0==AT_IDENT||LA53_0==MOZ_DOCUMENT_SYM||LA53_0==WEBKIT_KEYFRAMES_SYM||(LA53_0>=PAGE_SYM && LA53_0<=FONT_FACE_SYM)||(LA53_0>=MINUS && LA53_0<=PIPE)||LA53_0==LESS_AND||(LA53_0>=SASS_VAR && LA53_0<=SASS_INCLUDE)||(LA53_0>=SASS_DEBUG && LA53_0<=SASS_WARN)) ) {
                    alt53=1;
                }


                } finally {dbg.exitDecision(53);}

                switch (alt53) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:4: bodyItem ( ws )?
            	    {
            	    dbg.location(416,4);
            	    pushFollow(FOLLOW_bodyItem_in_body1075);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(416,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:13: ws
            	            {
            	            dbg.location(416,13);
            	            pushFollow(FOLLOW_ws_in_body1077);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(52);}


            	    }
            	    break;

            	default :
            	    if ( cnt53 >= 1 ) break loop53;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(53, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt53++;
            } while (true);
            } finally {dbg.exitSubRule(53);}


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
            dbg.exitRule(getGrammarFileName(), "body");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "body"


    // $ANTLR start "bodyItem"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? sass_debug );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(419, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? sass_debug )
            int alt54=9;
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            try {
                isCyclicDecision = true;
                alt54 = dfa54.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:6: rule
                    {
                    dbg.location(421,6);
                    pushFollow(FOLLOW_rule_in_bodyItem1102);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:11: media
                    {
                    dbg.location(422,11);
                    pushFollow(FOLLOW_media_in_bodyItem1114);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:11: page
                    {
                    dbg.location(423,11);
                    pushFollow(FOLLOW_page_in_bodyItem1126);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:11: counterStyle
                    {
                    dbg.location(424,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem1138);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:11: fontFace
                    {
                    dbg.location(425,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem1150);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:11: vendorAtRule
                    {
                    dbg.location(426,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem1162);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:11: {...}? cp_variable_declaration
                    {
                    dbg.location(427,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(427,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem1176);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:11: {...}? cp_mixin_call
                    {
                    dbg.location(428,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(428,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem1190);
                    cp_mixin_call();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:11: {...}? sass_debug
                    {
                    dbg.location(429,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(429,29);
                    pushFollow(FOLLOW_sass_debug_in_bodyItem1204);
                    sass_debug();

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
        dbg.location(430, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(438, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt55=3;
            try { dbg.enterDecision(55, decisionCanBacktrack[55]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt55=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt55=2;
                }
                break;
            case AT_IDENT:
                {
                alt55=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:3: moz_document
                    {
                    dbg.location(439,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule1227);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:18: webkitKeyframes
                    {
                    dbg.location(439,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule1231);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:36: generic_at_rule
                    {
                    dbg.location(439,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule1235);
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
        dbg.location(439, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(442,2);
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
        dbg.location(444, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(446, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(447,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule1271); if (state.failed) return ;
            dbg.location(447,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:16: WS
            	    {
            	    dbg.location(447,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule1273); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);
            } finally {dbg.exitSubRule(56);}

            dbg.location(447,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:20: ( atRuleId ( WS )* )?
            int alt58=2;
            try { dbg.enterSubRule(58);
            try { dbg.enterDecision(58, decisionCanBacktrack[58]);

            int LA58_0 = input.LA(1);

            if ( ((LA58_0>=IDENT && LA58_0<=STRING)) ) {
                alt58=1;
            }
            } finally {dbg.exitDecision(58);}

            switch (alt58) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:22: atRuleId ( WS )*
                    {
                    dbg.location(447,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule1278);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(447,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:31: WS
                    	    {
                    	    dbg.location(447,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule1280); if (state.failed) return ;

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

            dbg.location(448,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule1295); if (state.failed) return ;
            dbg.location(449,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule1307);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(450,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule1317); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(451, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(452, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(454,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1333); if (state.failed) return ;
            dbg.location(454,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:19: ws
                    {
                    dbg.location(454,19);
                    pushFollow(FOLLOW_ws_in_moz_document1335);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(454,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:25: moz_document_function ( ws )?
            {
            dbg.location(454,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document1340);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(454,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:47: ws
                    {
                    dbg.location(454,47);
                    pushFollow(FOLLOW_ws_in_moz_document1342);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}


            }

            dbg.location(454,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(63);

            loop63:
            do {
                int alt63=2;
                try { dbg.enterDecision(63, decisionCanBacktrack[63]);

                int LA63_0 = input.LA(1);

                if ( (LA63_0==COMMA) ) {
                    alt63=1;
                }


                } finally {dbg.exitDecision(63);}

                switch (alt63) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(454,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document1348); if (state.failed) return ;
            	    dbg.location(454,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:60: ws
            	            {
            	            dbg.location(454,60);
            	            pushFollow(FOLLOW_ws_in_moz_document1350);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(454,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document1353);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(454,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:86: ws
            	            {
            	            dbg.location(454,86);
            	            pushFollow(FOLLOW_ws_in_moz_document1355);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

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

            dbg.location(455,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document1362); if (state.failed) return ;
            dbg.location(455,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:9: ws
                    {
                    dbg.location(455,9);
                    pushFollow(FOLLOW_ws_in_moz_document1364);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(456,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:3: ( body )?
            int alt65=2;
            try { dbg.enterSubRule(65);
            try { dbg.enterDecision(65, decisionCanBacktrack[65]);

            int LA65_0 = input.LA(1);

            if ( (LA65_0==IDENT||LA65_0==MEDIA_SYM||LA65_0==GEN||LA65_0==COLON||LA65_0==AT_IDENT||LA65_0==MOZ_DOCUMENT_SYM||LA65_0==WEBKIT_KEYFRAMES_SYM||(LA65_0>=PAGE_SYM && LA65_0<=FONT_FACE_SYM)||(LA65_0>=MINUS && LA65_0<=PIPE)||LA65_0==LESS_AND||(LA65_0>=SASS_VAR && LA65_0<=SASS_INCLUDE)||(LA65_0>=SASS_DEBUG && LA65_0<=SASS_WARN)) ) {
                alt65=1;
            }
            } finally {dbg.exitDecision(65);}

            switch (alt65) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:3: body
                    {
                    dbg.location(456,3);
                    pushFollow(FOLLOW_body_in_moz_document1369);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(457,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document1374); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(458, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(460, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(461,2);
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
        dbg.location(463, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(466, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(468,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1415); if (state.failed) return ;
            dbg.location(468,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:23: ( ws )?
            int alt66=2;
            try { dbg.enterSubRule(66);
            try { dbg.enterDecision(66, decisionCanBacktrack[66]);

            int LA66_0 = input.LA(1);

            if ( (LA66_0==WS||(LA66_0>=NL && LA66_0<=COMMENT)) ) {
                alt66=1;
            }
            } finally {dbg.exitDecision(66);}

            switch (alt66) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:23: ws
                    {
                    dbg.location(468,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1417);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(66);}

            dbg.location(468,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1420);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(468,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:36: ws
                    {
                    dbg.location(468,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1422);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}

            dbg.location(469,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1427); if (state.failed) return ;
            dbg.location(469,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:9: ws
                    {
                    dbg.location(469,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1429);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(470,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(70);

            loop70:
            do {
                int alt70=2;
                try { dbg.enterDecision(70, decisionCanBacktrack[70]);

                int LA70_0 = input.LA(1);

                if ( (LA70_0==IDENT||LA70_0==PERCENTAGE) ) {
                    alt70=1;
                }


                } finally {dbg.exitDecision(70);}

                switch (alt70) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(470,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1436);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(470,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:26: ws
            	            {
            	            dbg.location(470,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1438);
            	            ws();

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

            dbg.location(471,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1445); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(472, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(474, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(476,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1458);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(476,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:26: ws
                    {
                    dbg.location(476,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1460);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(478,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1465); if (state.failed) return ;
            dbg.location(478,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:10: ws
                    {
                    dbg.location(478,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1468);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(478,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1471);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(479,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1475);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(480,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1478); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(481, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(483, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(485,2);
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

            dbg.location(485,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            try { dbg.enterSubRule(75);

            loop75:
            do {
                int alt75=2;
                try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                try {
                    isCyclicDecision = true;
                    alt75 = dfa75.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(75);}

                switch (alt75) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(485,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:27: ws
            	            {
            	            dbg.location(485,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1505);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(73);}

            	    dbg.location(485,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1508); if (state.failed) return ;
            	    dbg.location(485,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:37: ws
            	            {
            	            dbg.location(485,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1510);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(74);}

            	    dbg.location(485,41);
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
        dbg.location(486, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(488, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(489,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1542); if (state.failed) return ;
            dbg.location(489,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:16: ws
                    {
                    dbg.location(489,16);
                    pushFollow(FOLLOW_ws_in_page1544);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(489,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:20: ( IDENT ( ws )? )?
            int alt78=2;
            try { dbg.enterSubRule(78);
            try { dbg.enterDecision(78, decisionCanBacktrack[78]);

            int LA78_0 = input.LA(1);

            if ( (LA78_0==IDENT) ) {
                alt78=1;
            }
            } finally {dbg.exitDecision(78);}

            switch (alt78) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:22: IDENT ( ws )?
                    {
                    dbg.location(489,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1549); if (state.failed) return ;
                    dbg.location(489,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:28: ws
                            {
                            dbg.location(489,28);
                            pushFollow(FOLLOW_ws_in_page1551);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(77);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(489,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:35: ( pseudoPage ( ws )? )?
            int alt80=2;
            try { dbg.enterSubRule(80);
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            int LA80_0 = input.LA(1);

            if ( (LA80_0==COLON) ) {
                alt80=1;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:36: pseudoPage ( ws )?
                    {
                    dbg.location(489,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1558);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(489,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:47: ws
                            {
                            dbg.location(489,47);
                            pushFollow(FOLLOW_ws_in_page1560);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(79);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(490,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1573); if (state.failed) return ;
            dbg.location(490,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:16: ws
                    {
                    dbg.location(490,16);
                    pushFollow(FOLLOW_ws_in_page1575);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(494,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:13: ( declaration | margin ( ws )? )?
            int alt83=3;
            try { dbg.enterSubRule(83);
            try { dbg.enterDecision(83, decisionCanBacktrack[83]);

            int LA83_0 = input.LA(1);

            if ( (LA83_0==IDENT||LA83_0==MEDIA_SYM||LA83_0==GEN||LA83_0==AT_IDENT||(LA83_0>=MINUS && LA83_0<=DOT)||LA83_0==STAR||LA83_0==SASS_VAR) ) {
                alt83=1;
            }
            else if ( ((LA83_0>=TOPLEFTCORNER_SYM && LA83_0<=RIGHTBOTTOM_SYM)) ) {
                alt83=2;
            }
            } finally {dbg.exitDecision(83);}

            switch (alt83) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:14: declaration
                    {
                    dbg.location(494,14);
                    pushFollow(FOLLOW_declaration_in_page1630);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:26: margin ( ws )?
                    {
                    dbg.location(494,26);
                    pushFollow(FOLLOW_margin_in_page1632);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(494,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:33: ws
                            {
                            dbg.location(494,33);
                            pushFollow(FOLLOW_ws_in_page1634);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(82);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(83);}

            dbg.location(494,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(87);

            loop87:
            do {
                int alt87=2;
                try { dbg.enterDecision(87, decisionCanBacktrack[87]);

                int LA87_0 = input.LA(1);

                if ( (LA87_0==SEMI) ) {
                    alt87=1;
                }


                } finally {dbg.exitDecision(87);}

                switch (alt87) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(494,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1640); if (state.failed) return ;
            	    dbg.location(494,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:45: ws
            	            {
            	            dbg.location(494,45);
            	            pushFollow(FOLLOW_ws_in_page1642);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(84);}

            	    dbg.location(494,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:49: ( declaration | margin ( ws )? )?
            	    int alt86=3;
            	    try { dbg.enterSubRule(86);
            	    try { dbg.enterDecision(86, decisionCanBacktrack[86]);

            	    int LA86_0 = input.LA(1);

            	    if ( (LA86_0==IDENT||LA86_0==MEDIA_SYM||LA86_0==GEN||LA86_0==AT_IDENT||(LA86_0>=MINUS && LA86_0<=DOT)||LA86_0==STAR||LA86_0==SASS_VAR) ) {
            	        alt86=1;
            	    }
            	    else if ( ((LA86_0>=TOPLEFTCORNER_SYM && LA86_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt86=2;
            	    }
            	    } finally {dbg.exitDecision(86);}

            	    switch (alt86) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:50: declaration
            	            {
            	            dbg.location(494,50);
            	            pushFollow(FOLLOW_declaration_in_page1646);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:62: margin ( ws )?
            	            {
            	            dbg.location(494,62);
            	            pushFollow(FOLLOW_margin_in_page1648);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(494,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:69: ws
            	                    {
            	                    dbg.location(494,69);
            	                    pushFollow(FOLLOW_ws_in_page1650);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(85);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(86);}


            	    }
            	    break;

            	default :
            	    break loop87;
                }
            } while (true);
            } finally {dbg.exitSubRule(87);}

            dbg.location(495,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1665); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(496, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(498, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(499,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1686); if (state.failed) return ;
            dbg.location(499,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:25: ws
                    {
                    dbg.location(499,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1688);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(88);}

            dbg.location(499,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1691); if (state.failed) return ;
            dbg.location(499,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:35: ( ws )?
            int alt89=2;
            try { dbg.enterSubRule(89);
            try { dbg.enterDecision(89, decisionCanBacktrack[89]);

            int LA89_0 = input.LA(1);

            if ( (LA89_0==WS||(LA89_0>=NL && LA89_0<=COMMENT)) ) {
                alt89=1;
            }
            } finally {dbg.exitDecision(89);}

            switch (alt89) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:35: ws
                    {
                    dbg.location(499,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1693);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(500,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1704); if (state.failed) return ;
            dbg.location(500,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:16: ws
                    {
                    dbg.location(500,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1706);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(500,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1709);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(501,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1713);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(502,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1723); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(503, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(505, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(506,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1744); if (state.failed) return ;
            dbg.location(506,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:21: ws
                    {
                    dbg.location(506,21);
                    pushFollow(FOLLOW_ws_in_fontFace1746);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(507,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1757); if (state.failed) return ;
            dbg.location(507,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:16: ws
                    {
                    dbg.location(507,16);
                    pushFollow(FOLLOW_ws_in_fontFace1759);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(507,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1762);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(508,3);
            pushFollow(FOLLOW_declarations_in_fontFace1766);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(509,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1776); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(512, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(513,4);
            pushFollow(FOLLOW_margin_sym_in_margin1791);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(513,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:15: ws
                    {
                    dbg.location(513,15);
                    pushFollow(FOLLOW_ws_in_margin1793);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(513,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1796); if (state.failed) return ;
            dbg.location(513,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:26: ws
                    {
                    dbg.location(513,26);
                    pushFollow(FOLLOW_ws_in_margin1798);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(513,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1801);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(513,53);
            pushFollow(FOLLOW_declarations_in_margin1803);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(513,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1805); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(514, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(516, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(517,2);
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
        dbg.location(534, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(536, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:7: COLON IDENT
            {
            dbg.location(537,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage2034); if (state.failed) return ;
            dbg.location(537,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage2036); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(538, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(540, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(541,5);
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
        dbg.location(543, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(545, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt98=4;
            try { dbg.enterDecision(98, decisionCanBacktrack[98]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt98=1;
                }
                break;
            case GREATER:
                {
                alt98=2;
                }
                break;
            case TILDE:
                {
                alt98=3;
                }
                break;
            case IDENT:
            case GEN:
            case COLON:
            case HASH_SYMBOL:
            case HASH:
            case DOT:
            case LBRACKET:
            case DCOLON:
            case SASS_EXTEND_ONLY_SELECTOR:
            case STAR:
            case PIPE:
            case LESS_AND:
                {
                alt98=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 98, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(98);}

            switch (alt98) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:7: PLUS ( ws )?
                    {
                    dbg.location(546,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator2086); if (state.failed) return ;
                    dbg.location(546,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:12: ( ws )?
                    int alt95=2;
                    try { dbg.enterSubRule(95);
                    try { dbg.enterDecision(95, decisionCanBacktrack[95]);

                    int LA95_0 = input.LA(1);

                    if ( (LA95_0==WS||(LA95_0>=NL && LA95_0<=COMMENT)) ) {
                        alt95=1;
                    }
                    } finally {dbg.exitDecision(95);}

                    switch (alt95) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:12: ws
                            {
                            dbg.location(546,12);
                            pushFollow(FOLLOW_ws_in_combinator2088);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(95);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: GREATER ( ws )?
                    {
                    dbg.location(547,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator2097); if (state.failed) return ;
                    dbg.location(547,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:15: ws
                            {
                            dbg.location(547,15);
                            pushFollow(FOLLOW_ws_in_combinator2099);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(96);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:7: TILDE ( ws )?
                    {
                    dbg.location(548,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator2108); if (state.failed) return ;
                    dbg.location(548,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:13: ws
                            {
                            dbg.location(548,13);
                            pushFollow(FOLLOW_ws_in_combinator2110);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(97);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:5: 
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
        dbg.location(550, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(552, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(553,5);
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
        dbg.location(555, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(557, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(558,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:7: ( IDENT | GEN | {...}? cp_variable )
            int alt99=3;
            try { dbg.enterSubRule(99);
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt99=1;
                }
                break;
            case GEN:
                {
                alt99=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
            case SASS_VAR:
                {
                alt99=3;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:8: IDENT
                    {
                    dbg.location(558,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property2171); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:16: GEN
                    {
                    dbg.location(558,16);
                    match(input,GEN,FOLLOW_GEN_in_property2175); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:22: {...}? cp_variable
                    {
                    dbg.location(558,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(558,51);
                    pushFollow(FOLLOW_cp_variable_in_property2181);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(99);}

            dbg.location(558,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:64: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:64: ws
                    {
                    dbg.location(558,64);
                    pushFollow(FOLLOW_ws_in_property2184);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(559, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(561, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(562,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
            int alt101=2;
            try { dbg.enterSubRule(101);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(563,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(563,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(563,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule2228);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:13: ( selectorsGroup )
                    {
                    dbg.location(565,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:15: selectorsGroup
                    {
                    dbg.location(565,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule2261);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(101);}

            dbg.location(568,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule2284); if (state.failed) return ;
            dbg.location(568,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:16: ws
                    {
                    dbg.location(568,16);
                    pushFollow(FOLLOW_ws_in_rule2286);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(102);}

            dbg.location(568,20);
            pushFollow(FOLLOW_syncToFollow_in_rule2289);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(569,13);
            pushFollow(FOLLOW_declarations_in_rule2303);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(570,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule2313); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(571, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(579, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(581,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*
            try { dbg.enterSubRule(110);

            loop110:
            do {
                int alt110=8;
                try { dbg.enterDecision(110, decisionCanBacktrack[110]);

                try {
                    isCyclicDecision = true;
                    alt110 = dfa110.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(110);}

                switch (alt110) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(585,120);
            	    pushFollow(FOLLOW_declaration_in_declarations2447);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(585,132);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2449); if (state.failed) return ;
            	    dbg.location(585,137);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:137: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:137: ws
            	            {
            	            dbg.location(585,137);
            	            pushFollow(FOLLOW_ws_in_declarations2451);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(103);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(587,122);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2495);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(587,145);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:145: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:145: ws
            	            {
            	            dbg.location(587,145);
            	            pushFollow(FOLLOW_ws_in_declarations2497);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(104);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(589,50);
            	    pushFollow(FOLLOW_rule_in_declarations2534);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(589,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:55: ( ws )?
            	    int alt105=2;
            	    try { dbg.enterSubRule(105);
            	    try { dbg.enterDecision(105, decisionCanBacktrack[105]);

            	    int LA105_0 = input.LA(1);

            	    if ( (LA105_0==WS||(LA105_0>=NL && LA105_0<=COMMENT)) ) {
            	        alt105=1;
            	    }
            	    } finally {dbg.exitDecision(105);}

            	    switch (alt105) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:55: ws
            	            {
            	            dbg.location(589,55);
            	            pushFollow(FOLLOW_ws_in_declarations2536);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(105);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:17: {...}? sass_extend ( ws )?
            	    {
            	    dbg.location(591,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(591,35);
            	    pushFollow(FOLLOW_sass_extend_in_declarations2575);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(591,47);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:47: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:47: ws
            	            {
            	            dbg.location(591,47);
            	            pushFollow(FOLLOW_ws_in_declarations2577);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(106);}


            	    }
            	    break;
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:17: {...}? sass_debug ( ws )?
            	    {
            	    dbg.location(593,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(593,35);
            	    pushFollow(FOLLOW_sass_debug_in_declarations2616);
            	    sass_debug();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(593,46);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:46: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:46: ws
            	            {
            	            dbg.location(593,46);
            	            pushFollow(FOLLOW_ws_in_declarations2618);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(107);}


            	    }
            	    break;
            	case 6 :
            	    dbg.enterAlt(6);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:17: {...}? media ( ws )?
            	    {
            	    dbg.location(595,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(595,46);
            	    pushFollow(FOLLOW_media_in_declarations2657);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(595,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:52: ( ws )?
            	    int alt108=2;
            	    try { dbg.enterSubRule(108);
            	    try { dbg.enterDecision(108, decisionCanBacktrack[108]);

            	    int LA108_0 = input.LA(1);

            	    if ( (LA108_0==WS||(LA108_0>=NL && LA108_0<=COMMENT)) ) {
            	        alt108=1;
            	    }
            	    } finally {dbg.exitDecision(108);}

            	    switch (alt108) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:52: ws
            	            {
            	            dbg.location(595,52);
            	            pushFollow(FOLLOW_ws_in_declarations2659);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(108);}


            	    }
            	    break;
            	case 7 :
            	    dbg.enterAlt(7);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(597,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(597,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2698);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(597,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:60: ( ws )?
            	    int alt109=2;
            	    try { dbg.enterSubRule(109);
            	    try { dbg.enterDecision(109, decisionCanBacktrack[109]);

            	    int LA109_0 = input.LA(1);

            	    if ( (LA109_0==WS||(LA109_0>=NL && LA109_0<=COMMENT)) ) {
            	        alt109=1;
            	    }
            	    } finally {dbg.exitDecision(109);}

            	    switch (alt109) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:60: ws
            	            {
            	            dbg.location(597,60);
            	            pushFollow(FOLLOW_ws_in_declarations2700);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(109);}


            	    }
            	    break;

            	default :
            	    break loop110;
                }
            } while (true);
            } finally {dbg.exitSubRule(110);}

            dbg.location(601,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt111=2;
            try { dbg.enterSubRule(111);
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( (LA111_0==STAR) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==HASH_SYMBOL) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==IDENT) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==MINUS||(LA111_0>=HASH && LA111_0<=DOT)) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==GEN) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==MEDIA_SYM||LA111_0==AT_IDENT) && (synpred7_Css3())) {
                alt111=1;
            }
            else if ( (LA111_0==SASS_VAR) && (synpred7_Css3())) {
                alt111=1;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(601,36);
                    pushFollow(FOLLOW_declaration_in_declarations2744);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(602, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(604, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt115=2;
            try { dbg.enterDecision(115, decisionCanBacktrack[115]);

            try {
                isCyclicDecision = true;
                alt115 = dfa115.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(115);}

            switch (alt115) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(607,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2805);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(607,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:99: ws
                            {
                            dbg.location(607,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup2807);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(609,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup2822);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(609,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(114);

                    loop114:
                    do {
                        int alt114=2;
                        try { dbg.enterDecision(114, decisionCanBacktrack[114]);

                        int LA114_0 = input.LA(1);

                        if ( (LA114_0==COMMA) ) {
                            alt114=1;
                        }


                        } finally {dbg.exitDecision(114);}

                        switch (alt114) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(609,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2825); if (state.failed) return ;
                    	    dbg.location(609,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:25: ws
                    	            {
                    	            dbg.location(609,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup2827);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(113);}

                    	    dbg.location(609,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup2830);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop114;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(114);}


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
        dbg.location(610, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(612, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(613,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2857);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(613,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(116);

            loop116:
            do {
                int alt116=2;
                try { dbg.enterDecision(116, decisionCanBacktrack[116]);

                int LA116_0 = input.LA(1);

                if ( (LA116_0==IDENT||LA116_0==GEN||LA116_0==COLON||(LA116_0>=PLUS && LA116_0<=TILDE)||(LA116_0>=HASH_SYMBOL && LA116_0<=PIPE)||LA116_0==LESS_AND) ) {
                    alt116=1;
                }


                } finally {dbg.exitDecision(116);}

                switch (alt116) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(613,31);
            	    pushFollow(FOLLOW_combinator_in_selector2860);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(613,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2862);
            	    simpleSelectorSequence();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(614, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(617, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt121=2;
            try { dbg.enterDecision(121, decisionCanBacktrack[121]);

            int LA121_0 = input.LA(1);

            if ( (LA121_0==IDENT||LA121_0==GEN||(LA121_0>=STAR && LA121_0<=PIPE)||LA121_0==LESS_AND) ) {
                alt121=1;
            }
            else if ( (LA121_0==COLON||(LA121_0>=HASH_SYMBOL && LA121_0<=SASS_EXTEND_ONLY_SELECTOR)) ) {
                alt121=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 121, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(121);}

            switch (alt121) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(620,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(620,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2895);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(620,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(118);

                    loop118:
                    do {
                        int alt118=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(620,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2902);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(620,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:46: ws
                    	            {
                    	            dbg.location(620,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2904);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(117);}


                    	    }
                    	    break;

                    	default :
                    	    break loop118;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(118);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(622,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(622,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt120=0;
                    try { dbg.enterSubRule(120);

                    loop120:
                    do {
                        int alt120=2;
                        try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                        switch ( input.LA(1) ) {
                        case SASS_EXTEND_ONLY_SELECTOR:
                            {
                            int LA120_2 = input.LA(2);

                            if ( ((synpred10_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                                alt120=1;
                            }


                            }
                            break;
                        case HASH:
                            {
                            int LA120_3 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt120=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA120_4 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt120=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA120_5 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt120=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA120_6 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt120=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA120_7 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt120=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(120);}

                        switch (alt120) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(622,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2923);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(622,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:33: ws
                    	            {
                    	            dbg.location(622,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2925);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(119);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt120 >= 1 ) break loop120;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(120, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt120++;
                    } while (true);
                    } finally {dbg.exitSubRule(120);}


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
        dbg.location(623, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(630, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(631,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH_SYMBOL && input.LA(1)<=SASS_EXTEND_ONLY_SELECTOR) ) {
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
        dbg.location(632, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(634, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(636,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt122=2;
            try { dbg.enterSubRule(122);
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            int LA122_0 = input.LA(1);

            if ( (LA122_0==IDENT) ) {
                int LA122_1 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt122=1;
                }
            }
            else if ( (LA122_0==STAR) ) {
                int LA122_2 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt122=1;
                }
            }
            else if ( (LA122_0==PIPE) && (synpred11_Css3())) {
                alt122=1;
            }
            } finally {dbg.exitDecision(122);}

            switch (alt122) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(636,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector3041);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}

            dbg.location(636,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:51: elementName ( ws )?
            {
            dbg.location(636,51);
            pushFollow(FOLLOW_elementName_in_typeSelector3047);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(636,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:63: ws
                    {
                    dbg.location(636,63);
                    pushFollow(FOLLOW_ws_in_typeSelector3049);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(637, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeSelector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeSelector"


    // $ANTLR start "namespacePrefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(639, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(640,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:5: ( namespacePrefixName | STAR )?
            int alt124=3;
            try { dbg.enterSubRule(124);
            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

            int LA124_0 = input.LA(1);

            if ( (LA124_0==IDENT) ) {
                alt124=1;
            }
            else if ( (LA124_0==STAR) ) {
                alt124=2;
            }
            } finally {dbg.exitDecision(124);}

            switch (alt124) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:7: namespacePrefixName
                    {
                    dbg.location(640,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix3067);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:29: STAR
                    {
                    dbg.location(640,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix3071); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(124);}

            dbg.location(640,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix3075); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(641, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:1: elementSubsequent : ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(644, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:5: ( ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(646,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            int alt125=5;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            switch ( input.LA(1) ) {
            case SASS_EXTEND_ONLY_SELECTOR:
                {
                alt125=1;
                }
                break;
            case HASH_SYMBOL:
            case HASH:
                {
                alt125=2;
                }
                break;
            case DOT:
                {
                alt125=3;
                }
                break;
            case LBRACKET:
                {
                alt125=4;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt125=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 125, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:9: {...}? sass_extend_only_selector
                    {
                    dbg.location(647,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "elementSubsequent", "isScssSource()");
                    }
                    dbg.location(647,27);
                    pushFollow(FOLLOW_sass_extend_only_selector_in_elementSubsequent3114);
                    sass_extend_only_selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:8: cssId
                    {
                    dbg.location(648,8);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent3123);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:8: cssClass
                    {
                    dbg.location(649,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent3132);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:11: slAttribute
                    {
                    dbg.location(650,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent3144);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:11: pseudo
                    {
                    dbg.location(651,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent3156);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(653, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(656, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt126=2;
            try { dbg.enterDecision(126, decisionCanBacktrack[126]);

            int LA126_0 = input.LA(1);

            if ( (LA126_0==HASH) ) {
                alt126=1;
            }
            else if ( (LA126_0==HASH_SYMBOL) ) {
                alt126=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 126, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:7: HASH
                    {
                    dbg.location(657,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId3184); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(657,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:16: HASH_SYMBOL NAME
                    {
                    dbg.location(657,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId3190); if (state.failed) return ;
                    dbg.location(657,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId3192); if (state.failed) return ;

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
        dbg.location(658, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(664, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:7: DOT ( IDENT | GEN )
            {
            dbg.location(665,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass3220); if (state.failed) return ;
            dbg.location(665,11);
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
        dbg.location(666, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(673, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(674,5);
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
        dbg.location(675, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(677, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(678,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute3294); if (state.failed) return ;
            dbg.location(679,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:6: ( namespacePrefix )?
            int alt127=2;
            try { dbg.enterSubRule(127);
            try { dbg.enterDecision(127, decisionCanBacktrack[127]);

            int LA127_0 = input.LA(1);

            if ( (LA127_0==IDENT) ) {
                int LA127_1 = input.LA(2);

                if ( (LA127_1==PIPE) ) {
                    alt127=1;
                }
            }
            else if ( ((LA127_0>=STAR && LA127_0<=PIPE)) ) {
                alt127=1;
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:6: namespacePrefix
                    {
                    dbg.location(679,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute3301);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}

            dbg.location(679,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:23: ws
                    {
                    dbg.location(679,23);
                    pushFollow(FOLLOW_ws_in_slAttribute3304);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(128);}

            dbg.location(680,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute3315);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(680,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:25: ws
                    {
                    dbg.location(680,25);
                    pushFollow(FOLLOW_ws_in_slAttribute3317);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(129);}

            dbg.location(682,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt132=2;
            try { dbg.enterSubRule(132);
            try { dbg.enterDecision(132, decisionCanBacktrack[132]);

            int LA132_0 = input.LA(1);

            if ( ((LA132_0>=OPEQ && LA132_0<=CONTAINS)) ) {
                alt132=1;
            }
            } finally {dbg.exitDecision(132);}

            switch (alt132) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(683,17);
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

                    dbg.location(691,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:17: ( ws )?
                    int alt130=2;
                    try { dbg.enterSubRule(130);
                    try { dbg.enterDecision(130, decisionCanBacktrack[130]);

                    int LA130_0 = input.LA(1);

                    if ( (LA130_0==WS||(LA130_0>=NL && LA130_0<=COMMENT)) ) {
                        alt130=1;
                    }
                    } finally {dbg.exitDecision(130);}

                    switch (alt130) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:17: ws
                            {
                            dbg.location(691,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3539);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(130);}

                    dbg.location(692,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute3558);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(693,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:17: ws
                            {
                            dbg.location(693,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3576);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(131);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(132);}

            dbg.location(696,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3605); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(697, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(704, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:4: IDENT
            {
            dbg.location(705,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3621); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(706, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(708, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:2: ( IDENT | STRING )
            {
            dbg.location(710,2);
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
        dbg.location(714, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(716, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(717,7);
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

            dbg.location(718,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt140=2;
            try { dbg.enterSubRule(140);
            try { dbg.enterDecision(140, decisionCanBacktrack[140]);

            int LA140_0 = input.LA(1);

            if ( (LA140_0==IDENT||LA140_0==GEN) ) {
                alt140=1;
            }
            else if ( (LA140_0==NOT) ) {
                alt140=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 140, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(140);}

            switch (alt140) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(719,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(720,21);
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

                    dbg.location(721,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(722,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:25: ws
                                    {
                                    dbg.location(722,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3816);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(133);}

                            dbg.location(722,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3819); if (state.failed) return ;
                            dbg.location(722,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:36: ws
                                    {
                                    dbg.location(722,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3821);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(134);}

                            dbg.location(722,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:40: ( expression | STAR )?
                            int alt135=3;
                            try { dbg.enterSubRule(135);
                            try { dbg.enterDecision(135, decisionCanBacktrack[135]);

                            int LA135_0 = input.LA(1);

                            if ( ((LA135_0>=IDENT && LA135_0<=URI)||LA135_0==MEDIA_SYM||LA135_0==GEN||LA135_0==AT_IDENT||LA135_0==PERCENTAGE||LA135_0==PLUS||LA135_0==MINUS||LA135_0==HASH||(LA135_0>=NUMBER && LA135_0<=DIMENSION)||LA135_0==SASS_VAR) ) {
                                alt135=1;
                            }
                            else if ( (LA135_0==STAR) ) {
                                alt135=2;
                            }
                            } finally {dbg.exitDecision(135);}

                            switch (alt135) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:42: expression
                                    {
                                    dbg.location(722,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3826);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:55: STAR
                                    {
                                    dbg.location(722,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3830); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(135);}

                            dbg.location(722,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3835); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(136);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(726,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(726,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3914); if (state.failed) return ;
                    dbg.location(726,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:23: ws
                            {
                            dbg.location(726,23);
                            pushFollow(FOLLOW_ws_in_pseudo3916);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(137);}

                    dbg.location(726,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3919); if (state.failed) return ;
                    dbg.location(726,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:34: ws
                            {
                            dbg.location(726,34);
                            pushFollow(FOLLOW_ws_in_pseudo3921);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(138);}

                    dbg.location(726,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:38: ( simpleSelectorSequence )?
                    int alt139=2;
                    try { dbg.enterSubRule(139);
                    try { dbg.enterDecision(139, decisionCanBacktrack[139]);

                    int LA139_0 = input.LA(1);

                    if ( (LA139_0==IDENT||LA139_0==GEN||LA139_0==COLON||(LA139_0>=HASH_SYMBOL && LA139_0<=PIPE)||LA139_0==LESS_AND) ) {
                        alt139=1;
                    }
                    } finally {dbg.exitDecision(139);}

                    switch (alt139) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:38: simpleSelectorSequence
                            {
                            dbg.location(726,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3924);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(139);}

                    dbg.location(726,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3927); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(728, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:1: declaration : ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(730, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:5: ( ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:5: ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(733,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:5: ( STAR )?
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            int LA141_0 = input.LA(1);

            if ( (LA141_0==STAR) ) {
                alt141=1;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:5: STAR
                    {
                    dbg.location(733,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3971); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(734,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:5: ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property )
            int alt142=2;
            try { dbg.enterSubRule(142);
            try { dbg.enterDecision(142, decisionCanBacktrack[142]);

            int LA142_0 = input.LA(1);

            if ( (LA142_0==HASH_SYMBOL) && (synpred12_Css3())) {
                alt142=1;
            }
            else if ( (LA142_0==IDENT) ) {
                int LA142_2 = input.LA(2);

                if ( (synpred12_Css3()) ) {
                    alt142=1;
                }
                else if ( (true) ) {
                    alt142=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 142, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA142_0==MINUS||(LA142_0>=HASH && LA142_0<=DOT)) && (synpred12_Css3())) {
                alt142=1;
            }
            else if ( (LA142_0==MEDIA_SYM||LA142_0==GEN||LA142_0==AT_IDENT||LA142_0==SASS_VAR) ) {
                alt142=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 142, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(142);}

            switch (alt142) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(735,74);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_declaration4017);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:9: property
                    {
                    dbg.location(737,9);
                    pushFollow(FOLLOW_property_in_declaration4038);
                    property();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(142);}

            dbg.location(739,5);
            match(input,COLON,FOLLOW_COLON_in_declaration4051); if (state.failed) return ;
            dbg.location(739,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:11: ( ws )?
            int alt143=2;
            try { dbg.enterSubRule(143);
            try { dbg.enterDecision(143, decisionCanBacktrack[143]);

            int LA143_0 = input.LA(1);

            if ( (LA143_0==WS||(LA143_0>=NL && LA143_0<=COMMENT)) ) {
                alt143=1;
            }
            } finally {dbg.exitDecision(143);}

            switch (alt143) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:11: ws
                    {
                    dbg.location(739,11);
                    pushFollow(FOLLOW_ws_in_declaration4053);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}

            dbg.location(739,15);
            pushFollow(FOLLOW_propertyValue_in_declaration4056);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(739,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:29: ( prio ( ws )? )?
            int alt145=2;
            try { dbg.enterSubRule(145);
            try { dbg.enterDecision(145, decisionCanBacktrack[145]);

            int LA145_0 = input.LA(1);

            if ( (LA145_0==IMPORTANT_SYM) ) {
                alt145=1;
            }
            } finally {dbg.exitDecision(145);}

            switch (alt145) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:30: prio ( ws )?
                    {
                    dbg.location(739,30);
                    pushFollow(FOLLOW_prio_in_declaration4059);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(739,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:35: ( ws )?
                    int alt144=2;
                    try { dbg.enterSubRule(144);
                    try { dbg.enterDecision(144, decisionCanBacktrack[144]);

                    int LA144_0 = input.LA(1);

                    if ( (LA144_0==WS||(LA144_0>=NL && LA144_0<=COMMENT)) ) {
                        alt144=1;
                    }
                    } finally {dbg.exitDecision(144);}

                    switch (alt144) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:35: ws
                            {
                            dbg.location(739,35);
                            pushFollow(FOLLOW_ws_in_declaration4061);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(144);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(145);}


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
        dbg.location(740, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(748, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
            int alt146=2;
            try { dbg.enterDecision(146, decisionCanBacktrack[146]);

            try {
                isCyclicDecision = true;
                alt146 = dfa146.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(146);}

            switch (alt146) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(750,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(750,34);
                    pushFollow(FOLLOW_expression_in_propertyValue4101);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:9: ({...}? cp_expression )
                    {
                    dbg.location(760,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:11: {...}? cp_expression
                    {
                    dbg.location(760,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(760,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue4144);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(761, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "propertyValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "expressionPredicate"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(764, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(767,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt147=0;
            try { dbg.enterSubRule(147);

            loop147:
            do {
                int alt147=2;
                try { dbg.enterDecision(147, decisionCanBacktrack[147]);

                int LA147_0 = input.LA(1);

                if ( (LA147_0==NAMESPACE_SYM||(LA147_0>=IDENT && LA147_0<=MEDIA_SYM)||(LA147_0>=AND && LA147_0<=RPAREN)||(LA147_0>=WS && LA147_0<=RIGHTBOTTOM_SYM)||(LA147_0>=PLUS && LA147_0<=SASS_EXTEND_ONLY_SELECTOR)||(LA147_0>=PIPE && LA147_0<=LINE_COMMENT)) ) {
                    alt147=1;
                }


                } finally {dbg.exitDecision(147);}

                switch (alt147) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(767,7);
            	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=RPAREN)||(input.LA(1)>=WS && input.LA(1)<=RIGHTBOTTOM_SYM)||(input.LA(1)>=PLUS && input.LA(1)<=SASS_EXTEND_ONLY_SELECTOR)||(input.LA(1)>=PIPE && input.LA(1)<=LINE_COMMENT) ) {
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
            	    if ( cnt147 >= 1 ) break loop147;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(147, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt147++;
            } while (true);
            } finally {dbg.exitSubRule(147);}

            dbg.location(767,65);
            if ( input.LA(1)==SEMI||input.LA(1)==RBRACE ) {
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
        dbg.location(768, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expressionPredicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expressionPredicate"


    // $ANTLR start "syncToDeclarationsRule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(772, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:6: 
            {
            }

        }
        finally {
        }
        dbg.location(778, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(780, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:6: 
            {
            }

        }
        finally {
        }
        dbg.location(785, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_RBRACE"


    // $ANTLR start "syncTo_SEMI"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(787, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:13: SEMI
            {
            dbg.location(792,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI4329); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(793, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_SEMI");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_SEMI"


    // $ANTLR start "syncToFollow"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(796, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:6: 
            {
            }

        }
        finally {
        }
        dbg.location(801, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(803, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:7: IMPORTANT_SYM
            {
            dbg.location(804,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio4384); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(805, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(807, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(808,7);
            pushFollow(FOLLOW_term_in_expression4405);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(808,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(150);

            loop150:
            do {
                int alt150=2;
                try { dbg.enterDecision(150, decisionCanBacktrack[150]);

                try {
                    isCyclicDecision = true;
                    alt150 = dfa150.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(150);}

                switch (alt150) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(808,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:14: ( operator ( ws )? )?
            	    int alt149=2;
            	    try { dbg.enterSubRule(149);
            	    try { dbg.enterDecision(149, decisionCanBacktrack[149]);

            	    int LA149_0 = input.LA(1);

            	    if ( (LA149_0==COMMA||LA149_0==SOLIDUS) ) {
            	        alt149=1;
            	    }
            	    } finally {dbg.exitDecision(149);}

            	    switch (alt149) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:15: operator ( ws )?
            	            {
            	            dbg.location(808,15);
            	            pushFollow(FOLLOW_operator_in_expression4410);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(808,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:24: ws
            	                    {
            	                    dbg.location(808,24);
            	                    pushFollow(FOLLOW_ws_in_expression4412);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(148);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(149);}

            	    dbg.location(808,30);
            	    pushFollow(FOLLOW_term_in_expression4417);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop150;
                }
            } while (true);
            } finally {dbg.exitSubRule(150);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(809, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(811, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(812,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:7: ( unaryOperator ( ws )? )?
            int alt152=2;
            try { dbg.enterSubRule(152);
            try { dbg.enterDecision(152, decisionCanBacktrack[152]);

            int LA152_0 = input.LA(1);

            if ( (LA152_0==PLUS||LA152_0==MINUS) ) {
                alt152=1;
            }
            } finally {dbg.exitDecision(152);}

            switch (alt152) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:9: unaryOperator ( ws )?
                    {
                    dbg.location(812,9);
                    pushFollow(FOLLOW_unaryOperator_in_term4442);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(812,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:23: ( ws )?
                    int alt151=2;
                    try { dbg.enterSubRule(151);
                    try { dbg.enterDecision(151, decisionCanBacktrack[151]);

                    int LA151_0 = input.LA(1);

                    if ( (LA151_0==WS||(LA151_0>=NL && LA151_0<=COMMENT)) ) {
                        alt151=1;
                    }
                    } finally {dbg.exitDecision(151);}

                    switch (alt151) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:23: ws
                            {
                            dbg.location(812,23);
                            pushFollow(FOLLOW_ws_in_term4444);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(151);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(152);}

            dbg.location(813,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt153=8;
            try { dbg.enterSubRule(153);
            try { dbg.enterDecision(153, decisionCanBacktrack[153]);

            try {
                isCyclicDecision = true;
                alt153 = dfa153.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(153);}

            switch (alt153) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(814,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:7: STRING
                    {
                    dbg.location(827,7);
                    match(input,STRING,FOLLOW_STRING_in_term4668); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:7: IDENT
                    {
                    dbg.location(828,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term4676); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:829:7: GEN
                    {
                    dbg.location(829,7);
                    match(input,GEN,FOLLOW_GEN_in_term4684); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:7: URI
                    {
                    dbg.location(830,7);
                    match(input,URI,FOLLOW_URI_in_term4692); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:7: hexColor
                    {
                    dbg.location(831,7);
                    pushFollow(FOLLOW_hexColor_in_term4700);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:7: function
                    {
                    dbg.location(832,7);
                    pushFollow(FOLLOW_function_in_term4708);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:7: {...}? cp_variable
                    {
                    dbg.location(833,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(833,36);
                    pushFollow(FOLLOW_cp_variable_in_term4718);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(153);}

            dbg.location(835,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:5: ( ws )?
            int alt154=2;
            try { dbg.enterSubRule(154);
            try { dbg.enterDecision(154, decisionCanBacktrack[154]);

            int LA154_0 = input.LA(1);

            if ( (LA154_0==WS||(LA154_0>=NL && LA154_0<=COMMENT)) ) {
                alt154=1;
            }
            } finally {dbg.exitDecision(154);}

            switch (alt154) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:5: ws
                    {
                    dbg.location(835,5);
                    pushFollow(FOLLOW_ws_in_term4730);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(836, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(838, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(839,5);
            pushFollow(FOLLOW_functionName_in_function4746);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(839,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:839:18: ws
                    {
                    dbg.location(839,18);
                    pushFollow(FOLLOW_ws_in_function4748);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(155);}

            dbg.location(840,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4753); if (state.failed) return ;
            dbg.location(840,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:10: ( ws )?
            int alt156=2;
            try { dbg.enterSubRule(156);
            try { dbg.enterDecision(156, decisionCanBacktrack[156]);

            int LA156_0 = input.LA(1);

            if ( (LA156_0==WS||(LA156_0>=NL && LA156_0<=COMMENT)) ) {
                alt156=1;
            }
            } finally {dbg.exitDecision(156);}

            switch (alt156) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:10: ws
                    {
                    dbg.location(840,10);
                    pushFollow(FOLLOW_ws_in_function4755);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(156);}

            dbg.location(841,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt159=2;
            try { dbg.enterSubRule(159);
            try { dbg.enterDecision(159, decisionCanBacktrack[159]);

            try {
                isCyclicDecision = true;
                alt159 = dfa159.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(159);}

            switch (alt159) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:4: expression
                    {
                    dbg.location(842,4);
                    pushFollow(FOLLOW_expression_in_function4765);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(844,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(845,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4783);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(845,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(158);

                    loop158:
                    do {
                        int alt158=2;
                        try { dbg.enterDecision(158, decisionCanBacktrack[158]);

                        int LA158_0 = input.LA(1);

                        if ( (LA158_0==COMMA) ) {
                            alt158=1;
                        }


                        } finally {dbg.exitDecision(158);}

                        switch (alt158) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(845,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4786); if (state.failed) return ;
                    	    dbg.location(845,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:24: ( ws )?
                    	    int alt157=2;
                    	    try { dbg.enterSubRule(157);
                    	    try { dbg.enterDecision(157, decisionCanBacktrack[157]);

                    	    int LA157_0 = input.LA(1);

                    	    if ( (LA157_0==WS||(LA157_0>=NL && LA157_0<=COMMENT)) ) {
                    	        alt157=1;
                    	    }
                    	    } finally {dbg.exitDecision(157);}

                    	    switch (alt157) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:24: ws
                    	            {
                    	            dbg.location(845,24);
                    	            pushFollow(FOLLOW_ws_in_function4788);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(157);}

                    	    dbg.location(845,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4791);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop158;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(158);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(159);}

            dbg.location(848,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4812); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(849, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(855, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(859,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:4: ( IDENT COLON )?
            int alt160=2;
            try { dbg.enterSubRule(160);
            try { dbg.enterDecision(160, decisionCanBacktrack[160]);

            int LA160_0 = input.LA(1);

            if ( (LA160_0==IDENT) ) {
                int LA160_1 = input.LA(2);

                if ( (LA160_1==COLON) ) {
                    alt160=1;
                }
            }
            } finally {dbg.exitDecision(160);}

            switch (alt160) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:5: IDENT COLON
                    {
                    dbg.location(859,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4860); if (state.failed) return ;
                    dbg.location(859,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4862); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(859,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4866); if (state.failed) return ;
            dbg.location(859,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:25: ( DOT IDENT )*
            try { dbg.enterSubRule(161);

            loop161:
            do {
                int alt161=2;
                try { dbg.enterDecision(161, decisionCanBacktrack[161]);

                int LA161_0 = input.LA(1);

                if ( (LA161_0==DOT) ) {
                    alt161=1;
                }


                } finally {dbg.exitDecision(161);}

                switch (alt161) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:26: DOT IDENT
            	    {
            	    dbg.location(859,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4869); if (state.failed) return ;
            	    dbg.location(859,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4871); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop161;
                }
            } while (true);
            } finally {dbg.exitSubRule(161);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(861, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(863, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(864,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4894);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(864,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:20: ws
                    {
                    dbg.location(864,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4896);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(162);}

            dbg.location(864,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4899); if (state.failed) return ;
            dbg.location(864,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:29: ( ws )?
            int alt163=2;
            try { dbg.enterSubRule(163);
            try { dbg.enterDecision(163, decisionCanBacktrack[163]);

            int LA163_0 = input.LA(1);

            if ( (LA163_0==WS||(LA163_0>=NL && LA163_0<=COMMENT)) ) {
                alt163=1;
            }
            } finally {dbg.exitDecision(163);}

            switch (alt163) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:29: ws
                    {
                    dbg.location(864,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4901);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(163);}

            dbg.location(864,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4904);
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
        dbg.location(865, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(867, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:4: IDENT ( DOT IDENT )*
            {
            dbg.location(868,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4919); if (state.failed) return ;
            dbg.location(868,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:10: ( DOT IDENT )*
            try { dbg.enterSubRule(164);

            loop164:
            do {
                int alt164=2;
                try { dbg.enterDecision(164, decisionCanBacktrack[164]);

                int LA164_0 = input.LA(1);

                if ( (LA164_0==DOT) ) {
                    alt164=1;
                }


                } finally {dbg.exitDecision(164);}

                switch (alt164) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:11: DOT IDENT
            	    {
            	    dbg.location(868,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4922); if (state.failed) return ;
            	    dbg.location(868,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4924); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop164;
                }
            } while (true);
            } finally {dbg.exitSubRule(164);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(869, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(871, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:4: expression
            {
            dbg.location(872,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4938);
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
        dbg.location(873, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(875, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:7: HASH
            {
            dbg.location(876,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4956); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(877, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(879, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:7: ( WS | NL | COMMENT )+
            {
            dbg.location(880,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:7: ( WS | NL | COMMENT )+
            int cnt165=0;
            try { dbg.enterSubRule(165);

            loop165:
            do {
                int alt165=2;
                try { dbg.enterDecision(165, decisionCanBacktrack[165]);

                int LA165_0 = input.LA(1);

                if ( (LA165_0==WS||(LA165_0>=NL && LA165_0<=COMMENT)) ) {
                    alt165=1;
                }


                } finally {dbg.exitDecision(165);}

                switch (alt165) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(880,7);
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
            	    if ( cnt165 >= 1 ) break loop165;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(165, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt165++;
            } while (true);
            } finally {dbg.exitSubRule(165);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(881, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"


    // $ANTLR start "cp_variable_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(886, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt172=2;
            try { dbg.enterDecision(172, decisionCanBacktrack[172]);

            int LA172_0 = input.LA(1);

            if ( (LA172_0==MEDIA_SYM||LA172_0==AT_IDENT) ) {
                int LA172_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt172=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt172=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 172, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA172_0==SASS_VAR) ) {
                int LA172_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt172=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt172=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 172, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 172, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(172);}

            switch (alt172) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
                    {
                    dbg.location(888,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(888,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5025);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(888,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:39: ( ws )?
                    int alt166=2;
                    try { dbg.enterSubRule(166);
                    try { dbg.enterDecision(166, decisionCanBacktrack[166]);

                    int LA166_0 = input.LA(1);

                    if ( (LA166_0==WS||(LA166_0>=NL && LA166_0<=COMMENT)) ) {
                        alt166=1;
                    }
                    } finally {dbg.exitDecision(166);}

                    switch (alt166) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:39: ws
                            {
                            dbg.location(888,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5027);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(166);}

                    dbg.location(888,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5030); if (state.failed) return ;
                    dbg.location(888,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:49: ws
                            {
                            dbg.location(888,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5032);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(167);}

                    dbg.location(888,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration5035);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(888,67);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5037); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(890,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(890,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5064);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(890,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:39: ws
                            {
                            dbg.location(890,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5066);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(168);}

                    dbg.location(890,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5069); if (state.failed) return ;
                    dbg.location(890,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:49: ( ws )?
                    int alt169=2;
                    try { dbg.enterSubRule(169);
                    try { dbg.enterDecision(169, decisionCanBacktrack[169]);

                    int LA169_0 = input.LA(1);

                    if ( (LA169_0==WS||(LA169_0>=NL && LA169_0<=COMMENT)) ) {
                        alt169=1;
                    }
                    } finally {dbg.exitDecision(169);}

                    switch (alt169) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:49: ws
                            {
                            dbg.location(890,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5071);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(169);}

                    dbg.location(890,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration5074);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(890,67);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:67: ( SASS_DEFAULT ( ws )? )?
                    int alt171=2;
                    try { dbg.enterSubRule(171);
                    try { dbg.enterDecision(171, decisionCanBacktrack[171]);

                    int LA171_0 = input.LA(1);

                    if ( (LA171_0==SASS_DEFAULT) ) {
                        alt171=1;
                    }
                    } finally {dbg.exitDecision(171);}

                    switch (alt171) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:68: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(890,68);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5077); if (state.failed) return ;
                            dbg.location(890,81);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:81: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:81: ws
                                    {
                                    dbg.location(890,81);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration5079);
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

                    dbg.location(890,87);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5084); if (state.failed) return ;

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
        dbg.location(891, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_variable_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable_declaration"


    // $ANTLR start "cp_variable"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(894, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt173=2;
            try { dbg.enterDecision(173, decisionCanBacktrack[173]);

            int LA173_0 = input.LA(1);

            if ( (LA173_0==MEDIA_SYM||LA173_0==AT_IDENT) ) {
                alt173=1;
            }
            else if ( (LA173_0==SASS_VAR) ) {
                alt173=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 173, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(173);}

            switch (alt173) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(896,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(896,27);
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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(898,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(898,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:29: SASS_VAR
                    {
                    dbg.location(898,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable5149); if (state.failed) return ;

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
        dbg.location(900, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable"


    // $ANTLR start "cp_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(903, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:10: cp_additionExp
            {
            dbg.location(904,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression5173);
            cp_additionExp();

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
        dbg.location(905, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_expression"


    // $ANTLR start "cp_additionExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(907, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(908,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5193);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(909,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(176);

            loop176:
            do {
                int alt176=3;
                try { dbg.enterDecision(176, decisionCanBacktrack[176]);

                int LA176_0 = input.LA(1);

                if ( (LA176_0==PLUS) ) {
                    alt176=1;
                }
                else if ( (LA176_0==MINUS) ) {
                    alt176=2;
                }


                } finally {dbg.exitDecision(176);}

                switch (alt176) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(909,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp5207); if (state.failed) return ;
            	    dbg.location(909,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:17: ws
            	            {
            	            dbg.location(909,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5209);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(174);}

            	    dbg.location(909,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5212);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(910,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp5225); if (state.failed) return ;
            	    dbg.location(910,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:18: ws
            	            {
            	            dbg.location(910,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5227);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(175);}

            	    dbg.location(910,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5230);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop176;
                }
            } while (true);
            } finally {dbg.exitSubRule(176);}


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
            dbg.exitRule(getGrammarFileName(), "cp_additionExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_additionExp"


    // $ANTLR start "cp_multiplyExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(914, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(915,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5263);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(916,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(179);

            loop179:
            do {
                int alt179=3;
                try { dbg.enterDecision(179, decisionCanBacktrack[179]);

                int LA179_0 = input.LA(1);

                if ( (LA179_0==STAR) ) {
                    alt179=1;
                }
                else if ( (LA179_0==SOLIDUS) ) {
                    alt179=2;
                }


                } finally {dbg.exitDecision(179);}

                switch (alt179) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(916,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp5276); if (state.failed) return ;
            	    dbg.location(916,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:17: ( ws )?
            	    int alt177=2;
            	    try { dbg.enterSubRule(177);
            	    try { dbg.enterDecision(177, decisionCanBacktrack[177]);

            	    int LA177_0 = input.LA(1);

            	    if ( (LA177_0==WS||(LA177_0>=NL && LA177_0<=COMMENT)) ) {
            	        alt177=1;
            	    }
            	    } finally {dbg.exitDecision(177);}

            	    switch (alt177) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:17: ws
            	            {
            	            dbg.location(916,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5278);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(177);}

            	    dbg.location(916,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5281);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(917,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp5295); if (state.failed) return ;
            	    dbg.location(917,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:20: ws
            	            {
            	            dbg.location(917,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5297);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(178);}

            	    dbg.location(917,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5300);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop179;
                }
            } while (true);
            } finally {dbg.exitSubRule(179);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(919, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_multiplyExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_multiplyExp"


    // $ANTLR start "cp_atomExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(921, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt183=2;
            try { dbg.enterDecision(183, decisionCanBacktrack[183]);

            int LA183_0 = input.LA(1);

            if ( ((LA183_0>=IDENT && LA183_0<=URI)||LA183_0==MEDIA_SYM||LA183_0==GEN||LA183_0==AT_IDENT||LA183_0==PERCENTAGE||LA183_0==PLUS||LA183_0==MINUS||LA183_0==HASH||(LA183_0>=NUMBER && LA183_0<=DIMENSION)||LA183_0==SASS_VAR) ) {
                alt183=1;
            }
            else if ( (LA183_0==LPAREN) ) {
                alt183=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 183, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(183);}

            switch (alt183) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:10: term ( ( term )=> term )*
                    {
                    dbg.location(922,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp5333);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(922,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(180);

                    loop180:
                    do {
                        int alt180=2;
                        try { dbg.enterDecision(180, decisionCanBacktrack[180]);

                        try {
                            isCyclicDecision = true;
                            alt180 = dfa180.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(180);}

                        switch (alt180) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:16: ( term )=> term
                    	    {
                    	    dbg.location(922,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp5340);
                    	    term();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop180;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(180);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(923,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp5354); if (state.failed) return ;
                    dbg.location(923,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:17: ( ws )?
                    int alt181=2;
                    try { dbg.enterSubRule(181);
                    try { dbg.enterDecision(181, decisionCanBacktrack[181]);

                    int LA181_0 = input.LA(1);

                    if ( (LA181_0==WS||(LA181_0>=NL && LA181_0<=COMMENT)) ) {
                        alt181=1;
                    }
                    } finally {dbg.exitDecision(181);}

                    switch (alt181) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:17: ws
                            {
                            dbg.location(923,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5356);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(181);}

                    dbg.location(923,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp5359);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(923,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp5361); if (state.failed) return ;
                    dbg.location(923,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:43: ( ws )?
                    int alt182=2;
                    try { dbg.enterSubRule(182);
                    try { dbg.enterDecision(182, decisionCanBacktrack[182]);

                    int LA182_0 = input.LA(1);

                    if ( (LA182_0==WS||(LA182_0>=NL && LA182_0<=COMMENT)) ) {
                        alt182=1;
                    }
                    } finally {dbg.exitDecision(182);}

                    switch (alt182) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:43: ws
                            {
                            dbg.location(923,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5363);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(182);}


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
        dbg.location(924, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_atomExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_atomExp"


    // $ANTLR start "cp_term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(927, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:928:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(929,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt184=8;
            try { dbg.enterSubRule(184);
            try { dbg.enterDecision(184, decisionCanBacktrack[184]);

            try {
                isCyclicDecision = true;
                alt184 = dfa184.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(184);}

            switch (alt184) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(930,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:7: STRING
                    {
                    dbg.location(943,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term5601); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:7: IDENT
                    {
                    dbg.location(944,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term5609); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:7: GEN
                    {
                    dbg.location(945,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term5617); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:7: URI
                    {
                    dbg.location(946,7);
                    match(input,URI,FOLLOW_URI_in_cp_term5625); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:7: hexColor
                    {
                    dbg.location(947,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term5633);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:7: function
                    {
                    dbg.location(948,7);
                    pushFollow(FOLLOW_function_in_cp_term5641);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:949:7: cp_variable
                    {
                    dbg.location(949,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term5649);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(184);}

            dbg.location(951,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:951:5: ( ws )?
            int alt185=2;
            try { dbg.enterSubRule(185);
            try { dbg.enterDecision(185, decisionCanBacktrack[185]);

            int LA185_0 = input.LA(1);

            if ( (LA185_0==WS||(LA185_0>=NL && LA185_0<=COMMENT)) ) {
                alt185=1;
            }
            } finally {dbg.exitDecision(185);}

            switch (alt185) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:951:5: ws
                    {
                    dbg.location(951,5);
                    pushFollow(FOLLOW_ws_in_cp_term5661);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(185);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(952, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_term"


    // $ANTLR start "cp_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(961, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? )
            int alt195=2;
            try { dbg.enterDecision(195, decisionCanBacktrack[195]);

            int LA195_0 = input.LA(1);

            if ( (LA195_0==DOT) ) {
                alt195=1;
            }
            else if ( (LA195_0==SASS_MIXIN) ) {
                alt195=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 195, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(195);}

            switch (alt195) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(963,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(963,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration5692); if (state.failed) return ;
                    dbg.location(963,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5694);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(963,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:41: ( ws )?
                    int alt186=2;
                    try { dbg.enterSubRule(186);
                    try { dbg.enterDecision(186, decisionCanBacktrack[186]);

                    int LA186_0 = input.LA(1);

                    if ( (LA186_0==WS||(LA186_0>=NL && LA186_0<=COMMENT)) ) {
                        alt186=1;
                    }
                    } finally {dbg.exitDecision(186);}

                    switch (alt186) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:41: ws
                            {
                            dbg.location(963,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5696);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(186);}

                    dbg.location(963,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5699); if (state.failed) return ;
                    dbg.location(963,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:52: ( less_args_list )?
                    int alt187=2;
                    try { dbg.enterSubRule(187);
                    try { dbg.enterDecision(187, decisionCanBacktrack[187]);

                    int LA187_0 = input.LA(1);

                    if ( (LA187_0==MEDIA_SYM||LA187_0==AT_IDENT||LA187_0==SASS_VAR||(LA187_0>=LESS_DOTS && LA187_0<=LESS_REST)) ) {
                        alt187=1;
                    }
                    } finally {dbg.exitDecision(187);}

                    switch (alt187) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:52: less_args_list
                            {
                            dbg.location(963,52);
                            pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5701);
                            less_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(187);}

                    dbg.location(963,68);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5704); if (state.failed) return ;
                    dbg.location(963,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:75: ( ws )?
                    int alt188=2;
                    try { dbg.enterSubRule(188);
                    try { dbg.enterDecision(188, decisionCanBacktrack[188]);

                    int LA188_0 = input.LA(1);

                    if ( (LA188_0==WS||(LA188_0>=NL && LA188_0<=COMMENT)) ) {
                        alt188=1;
                    }
                    } finally {dbg.exitDecision(188);}

                    switch (alt188) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:75: ws
                            {
                            dbg.location(963,75);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5706);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(188);}

                    dbg.location(963,79);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:79: ( less_mixin_guarded ( ws )? )?
                    int alt190=2;
                    try { dbg.enterSubRule(190);
                    try { dbg.enterDecision(190, decisionCanBacktrack[190]);

                    int LA190_0 = input.LA(1);

                    if ( (LA190_0==LESS_WHEN) ) {
                        alt190=1;
                    }
                    } finally {dbg.exitDecision(190);}

                    switch (alt190) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:80: less_mixin_guarded ( ws )?
                            {
                            dbg.location(963,80);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5710);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(963,99);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:99: ( ws )?
                            int alt189=2;
                            try { dbg.enterSubRule(189);
                            try { dbg.enterDecision(189, decisionCanBacktrack[189]);

                            int LA189_0 = input.LA(1);

                            if ( (LA189_0==WS||(LA189_0>=NL && LA189_0<=COMMENT)) ) {
                                alt189=1;
                            }
                            } finally {dbg.exitDecision(189);}

                            switch (alt189) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:99: ws
                                    {
                                    dbg.location(963,99);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5712);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(189);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(190);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(965,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(965,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5729); if (state.failed) return ;
                    dbg.location(965,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5731);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(965,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5733);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(965,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:51: ( ws )?
                    int alt191=2;
                    try { dbg.enterSubRule(191);
                    try { dbg.enterDecision(191, decisionCanBacktrack[191]);

                    int LA191_0 = input.LA(1);

                    if ( (LA191_0==WS||(LA191_0>=NL && LA191_0<=COMMENT)) ) {
                        alt191=1;
                    }
                    } finally {dbg.exitDecision(191);}

                    switch (alt191) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:51: ws
                            {
                            dbg.location(965,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5735);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(191);}

                    dbg.location(965,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:55: ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    int alt194=2;
                    try { dbg.enterSubRule(194);
                    try { dbg.enterDecision(194, decisionCanBacktrack[194]);

                    int LA194_0 = input.LA(1);

                    if ( (LA194_0==LPAREN) ) {
                        alt194=1;
                    }
                    } finally {dbg.exitDecision(194);}

                    switch (alt194) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:56: LPAREN ( less_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(965,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5739); if (state.failed) return ;
                            dbg.location(965,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:63: ( less_args_list )?
                            int alt192=2;
                            try { dbg.enterSubRule(192);
                            try { dbg.enterDecision(192, decisionCanBacktrack[192]);

                            int LA192_0 = input.LA(1);

                            if ( (LA192_0==MEDIA_SYM||LA192_0==AT_IDENT||LA192_0==SASS_VAR||(LA192_0>=LESS_DOTS && LA192_0<=LESS_REST)) ) {
                                alt192=1;
                            }
                            } finally {dbg.exitDecision(192);}

                            switch (alt192) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:63: less_args_list
                                    {
                                    dbg.location(965,63);
                                    pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5741);
                                    less_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(192);}

                            dbg.location(965,79);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5744); if (state.failed) return ;
                            dbg.location(965,86);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:86: ( ws )?
                            int alt193=2;
                            try { dbg.enterSubRule(193);
                            try { dbg.enterDecision(193, decisionCanBacktrack[193]);

                            int LA193_0 = input.LA(1);

                            if ( (LA193_0==WS||(LA193_0>=NL && LA193_0<=COMMENT)) ) {
                                alt193=1;
                            }
                            } finally {dbg.exitDecision(193);}

                            switch (alt193) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:86: ws
                                    {
                                    dbg.location(965,86);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5746);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(193);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(194);}


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
        dbg.location(966, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_declaration"


    // $ANTLR start "cp_mixin_call"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(970, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(972,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt196=2;
            try { dbg.enterSubRule(196);
            try { dbg.enterDecision(196, decisionCanBacktrack[196]);

            int LA196_0 = input.LA(1);

            if ( (LA196_0==DOT) ) {
                alt196=1;
            }
            else if ( (LA196_0==SASS_INCLUDE) ) {
                alt196=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 196, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(196);}

            switch (alt196) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(973,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(973,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call5788); if (state.failed) return ;
                    dbg.location(973,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5790);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(975,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(975,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call5812); if (state.failed) return ;
                    dbg.location(975,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5814);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(975,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5816);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(196);}

            dbg.location(977,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?
            int alt199=2;
            try { dbg.enterSubRule(199);
            try { dbg.enterDecision(199, decisionCanBacktrack[199]);

            try {
                isCyclicDecision = true;
                alt199 = dfa199.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(199);}

            switch (alt199) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:6: ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(977,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:6: ( ws )?
                    int alt197=2;
                    try { dbg.enterSubRule(197);
                    try { dbg.enterDecision(197, decisionCanBacktrack[197]);

                    int LA197_0 = input.LA(1);

                    if ( (LA197_0==WS||(LA197_0>=NL && LA197_0<=COMMENT)) ) {
                        alt197=1;
                    }
                    } finally {dbg.exitDecision(197);}

                    switch (alt197) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:6: ws
                            {
                            dbg.location(977,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call5829);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(197);}

                    dbg.location(977,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call5832); if (state.failed) return ;
                    dbg.location(977,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:17: ( cp_mixin_call_args )?
                    int alt198=2;
                    try { dbg.enterSubRule(198);
                    try { dbg.enterDecision(198, decisionCanBacktrack[198]);

                    int LA198_0 = input.LA(1);

                    if ( ((LA198_0>=IDENT && LA198_0<=URI)||LA198_0==MEDIA_SYM||LA198_0==GEN||LA198_0==AT_IDENT||LA198_0==PERCENTAGE||LA198_0==PLUS||LA198_0==MINUS||LA198_0==HASH||(LA198_0>=NUMBER && LA198_0<=DIMENSION)||LA198_0==SASS_VAR) ) {
                        alt198=1;
                    }
                    } finally {dbg.exitDecision(198);}

                    switch (alt198) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:17: cp_mixin_call_args
                            {
                            dbg.location(977,17);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call5834);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(198);}

                    dbg.location(977,37);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call5837); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(199);}

            dbg.location(977,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:46: ( ws )?
            int alt200=2;
            try { dbg.enterSubRule(200);
            try { dbg.enterDecision(200, decisionCanBacktrack[200]);

            int LA200_0 = input.LA(1);

            if ( (LA200_0==WS||(LA200_0>=NL && LA200_0<=COMMENT)) ) {
                alt200=1;
            }
            } finally {dbg.exitDecision(200);}

            switch (alt200) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:46: ws
                    {
                    dbg.location(977,46);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5841);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(200);}

            dbg.location(977,50);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call5844); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(978, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call"


    // $ANTLR start "cp_mixin_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:980:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(980, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:5: IDENT
            {
            dbg.location(982,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name5873); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(983, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_name"


    // $ANTLR start "cp_mixin_call_args"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:1: cp_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(985, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(989,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_args5909);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(989,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(202);

            loop202:
            do {
                int alt202=2;
                try { dbg.enterDecision(202, decisionCanBacktrack[202]);

                int LA202_0 = input.LA(1);

                if ( (LA202_0==SEMI||LA202_0==COMMA) ) {
                    alt202=1;
                }


                } finally {dbg.exitDecision(202);}

                switch (alt202) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(989,12);
            	    if ( input.LA(1)==SEMI||input.LA(1)==COMMA ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(989,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:27: ( ws )?
            	    int alt201=2;
            	    try { dbg.enterSubRule(201);
            	    try { dbg.enterDecision(201, decisionCanBacktrack[201]);

            	    int LA201_0 = input.LA(1);

            	    if ( (LA201_0==WS||(LA201_0>=NL && LA201_0<=COMMENT)) ) {
            	        alt201=1;
            	    }
            	    } finally {dbg.exitDecision(201);}

            	    switch (alt201) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:27: ws
            	            {
            	            dbg.location(989,27);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args5921);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(201);}

            	    dbg.location(989,31);
            	    pushFollow(FOLLOW_term_in_cp_mixin_call_args5924);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop202;
                }
            } while (true);
            } finally {dbg.exitSubRule(202);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(990, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_args"


    // $ANTLR start "less_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(993, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:994:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt207=2;
            try { dbg.enterDecision(207, decisionCanBacktrack[207]);

            int LA207_0 = input.LA(1);

            if ( (LA207_0==MEDIA_SYM||LA207_0==AT_IDENT||LA207_0==SASS_VAR) ) {
                alt207=1;
            }
            else if ( ((LA207_0>=LESS_DOTS && LA207_0<=LESS_REST)) ) {
                alt207=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 207, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(207);}

            switch (alt207) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(997,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(997,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5966);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(997,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
                    try { dbg.enterSubRule(204);

                    loop204:
                    do {
                        int alt204=2;
                        try { dbg.enterDecision(204, decisionCanBacktrack[204]);

                        try {
                            isCyclicDecision = true;
                            alt204 = dfa204.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(204);}

                        switch (alt204) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(997,18);
                    	    if ( input.LA(1)==SEMI||input.LA(1)==COMMA ) {
                    	        input.consume();
                    	        state.errorRecovery=false;state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        dbg.recognitionException(mse);
                    	        throw mse;
                    	    }

                    	    dbg.location(997,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:35: ( ws )?
                    	    int alt203=2;
                    	    try { dbg.enterSubRule(203);
                    	    try { dbg.enterDecision(203, decisionCanBacktrack[203]);

                    	    int LA203_0 = input.LA(1);

                    	    if ( (LA203_0==WS||(LA203_0>=NL && LA203_0<=COMMENT)) ) {
                    	        alt203=1;
                    	    }
                    	    } finally {dbg.exitDecision(203);}

                    	    switch (alt203) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:35: ws
                    	            {
                    	            dbg.location(997,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5980);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(203);}

                    	    dbg.location(997,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5983);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop204;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(204);}

                    dbg.location(997,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:50: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt206=2;
                    try { dbg.enterSubRule(206);
                    try { dbg.enterDecision(206, decisionCanBacktrack[206]);

                    int LA206_0 = input.LA(1);

                    if ( (LA206_0==SEMI||LA206_0==COMMA) ) {
                        alt206=1;
                    }
                    } finally {dbg.exitDecision(206);}

                    switch (alt206) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:52: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(997,52);
                            if ( input.LA(1)==SEMI||input.LA(1)==COMMA ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                dbg.recognitionException(mse);
                                throw mse;
                            }

                            dbg.location(997,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:69: ( ws )?
                            int alt205=2;
                            try { dbg.enterSubRule(205);
                            try { dbg.enterDecision(205, decisionCanBacktrack[205]);

                            int LA205_0 = input.LA(1);

                            if ( (LA205_0==WS||(LA205_0>=NL && LA205_0<=COMMENT)) ) {
                                alt205=1;
                            }
                            } finally {dbg.exitDecision(205);}

                            switch (alt205) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:69: ws
                                    {
                                    dbg.location(997,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5999);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(205);}

                            dbg.location(997,73);
                            if ( (input.LA(1)>=LESS_DOTS && input.LA(1)<=LESS_REST) ) {
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
                    } finally {dbg.exitSubRule(206);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(999,5);
                    if ( (input.LA(1)>=LESS_DOTS && input.LA(1)<=LESS_REST) ) {
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
        dbg.location(1000, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1003:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1003, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(1005,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg6056);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1005,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:17: ( COLON ( ws )? cp_expression )?
            int alt209=2;
            try { dbg.enterSubRule(209);
            try { dbg.enterDecision(209, decisionCanBacktrack[209]);

            int LA209_0 = input.LA(1);

            if ( (LA209_0==COLON) ) {
                alt209=1;
            }
            } finally {dbg.exitDecision(209);}

            switch (alt209) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(1005,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg6060); if (state.failed) return ;
                    dbg.location(1005,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:25: ( ws )?
                    int alt208=2;
                    try { dbg.enterSubRule(208);
                    try { dbg.enterDecision(208, decisionCanBacktrack[208]);

                    int LA208_0 = input.LA(1);

                    if ( (LA208_0==WS||(LA208_0>=NL && LA208_0<=COMMENT)) ) {
                        alt208=1;
                    }
                    } finally {dbg.exitDecision(208);}

                    switch (alt208) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:25: ws
                            {
                            dbg.location(1005,25);
                            pushFollow(FOLLOW_ws_in_less_arg6062);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(208);}

                    dbg.location(1005,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg6065);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(209);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1006, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1010, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(1012,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded6091); if (state.failed) return ;
            dbg.location(1012,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:15: ( ws )?
            int alt210=2;
            try { dbg.enterSubRule(210);
            try { dbg.enterDecision(210, decisionCanBacktrack[210]);

            int LA210_0 = input.LA(1);

            if ( (LA210_0==WS||(LA210_0>=NL && LA210_0<=COMMENT)) ) {
                alt210=1;
            }
            } finally {dbg.exitDecision(210);}

            switch (alt210) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:15: ws
                    {
                    dbg.location(1012,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded6093);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(210);}

            dbg.location(1012,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6096);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1012,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(212);

            loop212:
            do {
                int alt212=2;
                try { dbg.enterDecision(212, decisionCanBacktrack[212]);

                int LA212_0 = input.LA(1);

                if ( (LA212_0==COMMA||LA212_0==AND) ) {
                    alt212=1;
                }


                } finally {dbg.exitDecision(212);}

                switch (alt212) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(1012,36);
            	    if ( input.LA(1)==COMMA||input.LA(1)==AND ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(1012,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:50: ( ws )?
            	    int alt211=2;
            	    try { dbg.enterSubRule(211);
            	    try { dbg.enterDecision(211, decisionCanBacktrack[211]);

            	    int LA211_0 = input.LA(1);

            	    if ( (LA211_0==WS||(LA211_0>=NL && LA211_0<=COMMENT)) ) {
            	        alt211=1;
            	    }
            	    } finally {dbg.exitDecision(211);}

            	    switch (alt211) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:50: ws
            	            {
            	            dbg.location(1012,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded6108);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(211);}

            	    dbg.location(1012,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6111);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop212;
                }
            } while (true);
            } finally {dbg.exitSubRule(212);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1013, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1017:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1017, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1018:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(1019,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:5: ( NOT ( ws )? )?
            int alt214=2;
            try { dbg.enterSubRule(214);
            try { dbg.enterDecision(214, decisionCanBacktrack[214]);

            int LA214_0 = input.LA(1);

            if ( (LA214_0==NOT) ) {
                alt214=1;
            }
            } finally {dbg.exitDecision(214);}

            switch (alt214) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:6: NOT ( ws )?
                    {
                    dbg.location(1019,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition6141); if (state.failed) return ;
                    dbg.location(1019,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:10: ( ws )?
                    int alt213=2;
                    try { dbg.enterSubRule(213);
                    try { dbg.enterDecision(213, decisionCanBacktrack[213]);

                    int LA213_0 = input.LA(1);

                    if ( (LA213_0==WS||(LA213_0>=NL && LA213_0<=COMMENT)) ) {
                        alt213=1;
                    }
                    } finally {dbg.exitDecision(213);}

                    switch (alt213) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:10: ws
                            {
                            dbg.location(1019,10);
                            pushFollow(FOLLOW_ws_in_less_condition6143);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(213);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(214);}

            dbg.location(1020,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition6152); if (state.failed) return ;
            dbg.location(1020,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:12: ( ws )?
            int alt215=2;
            try { dbg.enterSubRule(215);
            try { dbg.enterDecision(215, decisionCanBacktrack[215]);

            int LA215_0 = input.LA(1);

            if ( (LA215_0==WS||(LA215_0>=NL && LA215_0<=COMMENT)) ) {
                alt215=1;
            }
            } finally {dbg.exitDecision(215);}

            switch (alt215) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:12: ws
                    {
                    dbg.location(1020,12);
                    pushFollow(FOLLOW_ws_in_less_condition6154);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(215);}

            dbg.location(1021,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1021:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt220=2;
            try { dbg.enterSubRule(220);
            try { dbg.enterDecision(220, decisionCanBacktrack[220]);

            int LA220_0 = input.LA(1);

            if ( (LA220_0==IDENT) ) {
                alt220=1;
            }
            else if ( (LA220_0==MEDIA_SYM||LA220_0==AT_IDENT||LA220_0==SASS_VAR) ) {
                alt220=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 220, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(220);}

            switch (alt220) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1022,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition6180);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1022,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:40: ( ws )?
                    int alt216=2;
                    try { dbg.enterSubRule(216);
                    try { dbg.enterDecision(216, decisionCanBacktrack[216]);

                    int LA216_0 = input.LA(1);

                    if ( (LA216_0==WS||(LA216_0>=NL && LA216_0<=COMMENT)) ) {
                        alt216=1;
                    }
                    } finally {dbg.exitDecision(216);}

                    switch (alt216) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:40: ws
                            {
                            dbg.location(1022,40);
                            pushFollow(FOLLOW_ws_in_less_condition6182);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(216);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1024,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1024,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition6213);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1024,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt219=2;
                    try { dbg.enterSubRule(219);
                    try { dbg.enterDecision(219, decisionCanBacktrack[219]);

                    int LA219_0 = input.LA(1);

                    if ( (LA219_0==WS||LA219_0==GREATER||LA219_0==OPEQ||(LA219_0>=NL && LA219_0<=COMMENT)||(LA219_0>=GREATER_OR_EQ && LA219_0<=LESS_OR_EQ)) ) {
                        alt219=1;
                    }
                    } finally {dbg.exitDecision(219);}

                    switch (alt219) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1024,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:28: ( ws )?
                            int alt217=2;
                            try { dbg.enterSubRule(217);
                            try { dbg.enterDecision(217, decisionCanBacktrack[217]);

                            int LA217_0 = input.LA(1);

                            if ( (LA217_0==WS||(LA217_0>=NL && LA217_0<=COMMENT)) ) {
                                alt217=1;
                            }
                            } finally {dbg.exitDecision(217);}

                            switch (alt217) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:28: ws
                                    {
                                    dbg.location(1024,28);
                                    pushFollow(FOLLOW_ws_in_less_condition6216);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(217);}

                            dbg.location(1024,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition6219);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1024,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:56: ( ws )?
                            int alt218=2;
                            try { dbg.enterSubRule(218);
                            try { dbg.enterDecision(218, decisionCanBacktrack[218]);

                            int LA218_0 = input.LA(1);

                            if ( (LA218_0==WS||(LA218_0>=NL && LA218_0<=COMMENT)) ) {
                                alt218=1;
                            }
                            } finally {dbg.exitDecision(218);}

                            switch (alt218) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:56: ws
                                    {
                                    dbg.location(1024,56);
                                    pushFollow(FOLLOW_ws_in_less_condition6221);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(218);}

                            dbg.location(1024,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition6224);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(219);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(220);}

            dbg.location(1026,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition6253); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1027, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1030:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1030, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1031:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1032,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition6279);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1032,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:18: ( ws )?
            int alt221=2;
            try { dbg.enterSubRule(221);
            try { dbg.enterDecision(221, decisionCanBacktrack[221]);

            int LA221_0 = input.LA(1);

            if ( (LA221_0==WS||(LA221_0>=NL && LA221_0<=COMMENT)) ) {
                alt221=1;
            }
            } finally {dbg.exitDecision(221);}

            switch (alt221) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:18: ws
                    {
                    dbg.location(1032,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6281);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(221);}

            dbg.location(1032,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition6284); if (state.failed) return ;
            dbg.location(1032,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:29: ( ws )?
            int alt222=2;
            try { dbg.enterSubRule(222);
            try { dbg.enterDecision(222, decisionCanBacktrack[222]);

            int LA222_0 = input.LA(1);

            if ( (LA222_0==WS||(LA222_0>=NL && LA222_0<=COMMENT)) ) {
                alt222=1;
            }
            } finally {dbg.exitDecision(222);}

            switch (alt222) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:29: ws
                    {
                    dbg.location(1032,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6286);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(222);}

            dbg.location(1032,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition6289);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1032,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:45: ( ws )?
            int alt223=2;
            try { dbg.enterSubRule(223);
            try { dbg.enterDecision(223, decisionCanBacktrack[223]);

            int LA223_0 = input.LA(1);

            if ( (LA223_0==WS||(LA223_0>=NL && LA223_0<=COMMENT)) ) {
                alt223=1;
            }
            } finally {dbg.exitDecision(223);}

            switch (alt223) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:45: ws
                    {
                    dbg.location(1032,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6291);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(223);}

            dbg.location(1032,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition6294); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1033, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1036, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:5: IDENT
            {
            dbg.location(1038,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name6316); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1039, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1041, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1042,5);
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
        dbg.location(1044, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_condition_operator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition_operator"


    // $ANTLR start "scss_selector_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1062, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            {
            dbg.location(1064,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            int alt224=2;
            try { dbg.enterSubRule(224);
            try { dbg.enterDecision(224, decisionCanBacktrack[224]);

            try {
                isCyclicDecision = true;
                alt224 = dfa224.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(224);}

            switch (alt224) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1065,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6415);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
                    {
                    dbg.location(1067,13);
                    if ( input.LA(1)==IDENT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            } finally {dbg.exitSubRule(224);}

            dbg.location(1069,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            try { dbg.enterSubRule(227);

            loop227:
            do {
                int alt227=2;
                try { dbg.enterDecision(227, decisionCanBacktrack[227]);

                try {
                    isCyclicDecision = true;
                    alt227 = dfa227.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(227);}

                switch (alt227) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    {
            	    dbg.location(1070,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:13: ( ws )?
            	    int alt225=2;
            	    try { dbg.enterSubRule(225);
            	    try { dbg.enterDecision(225, decisionCanBacktrack[225]);

            	    int LA225_0 = input.LA(1);

            	    if ( (LA225_0==WS||(LA225_0>=NL && LA225_0<=COMMENT)) ) {
            	        alt225=1;
            	    }
            	    } finally {dbg.exitDecision(225);}

            	    switch (alt225) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:13: ws
            	            {
            	            dbg.location(1070,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression6500);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(225);}

            	    dbg.location(1071,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1071:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    int alt226=2;
            	    try { dbg.enterSubRule(226);
            	    try { dbg.enterDecision(226, decisionCanBacktrack[226]);

            	    try {
            	        isCyclicDecision = true;
            	        alt226 = dfa226.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(226);}

            	    switch (alt226) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1072,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6539);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1074:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
            	            {
            	            dbg.location(1074,17);
            	            if ( input.LA(1)==IDENT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            	    } finally {dbg.exitSubRule(226);}


            	    }
            	    break;

            	default :
            	    break loop227;
                }
            } while (true);
            } finally {dbg.exitSubRule(227);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1078, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_selector_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_selector_interpolation_expression"


    // $ANTLR start "scss_declaration_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1080, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1081:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1082:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1082,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1082:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt228=2;
            try { dbg.enterSubRule(228);
            try { dbg.enterDecision(228, decisionCanBacktrack[228]);

            int LA228_0 = input.LA(1);

            if ( (LA228_0==HASH_SYMBOL) ) {
                int LA228_1 = input.LA(2);

                if ( (LA228_1==LBRACE) && (synpred17_Css3())) {
                    alt228=1;
                }
                else if ( (LA228_1==IDENT||LA228_1==COLON||LA228_1==WS||(LA228_1>=MINUS && LA228_1<=DOT)||(LA228_1>=NL && LA228_1<=COMMENT)) ) {
                    alt228=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 228, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA228_0==IDENT||LA228_0==MINUS||(LA228_0>=HASH && LA228_0<=DOT)) ) {
                alt228=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 228, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(228);}

            switch (alt228) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1083:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1083,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6673);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1085:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1085,13);
                    if ( input.LA(1)==IDENT||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            } finally {dbg.exitSubRule(228);}

            dbg.location(1087,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1087:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(231);

            loop231:
            do {
                int alt231=2;
                try { dbg.enterDecision(231, decisionCanBacktrack[231]);

                int LA231_0 = input.LA(1);

                if ( (LA231_0==IDENT||LA231_0==WS||(LA231_0>=MINUS && LA231_0<=DOT)||(LA231_0>=NL && LA231_0<=COMMENT)) ) {
                    alt231=1;
                }


                } finally {dbg.exitDecision(231);}

                switch (alt231) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1088:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1088,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1088:13: ( ws )?
            	    int alt229=2;
            	    try { dbg.enterSubRule(229);
            	    try { dbg.enterDecision(229, decisionCanBacktrack[229]);

            	    int LA229_0 = input.LA(1);

            	    if ( (LA229_0==WS||(LA229_0>=NL && LA229_0<=COMMENT)) ) {
            	        alt229=1;
            	    }
            	    } finally {dbg.exitDecision(229);}

            	    switch (alt229) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1088:13: ws
            	            {
            	            dbg.location(1088,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression6754);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(229);}

            	    dbg.location(1089,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1089:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt230=2;
            	    try { dbg.enterSubRule(230);
            	    try { dbg.enterDecision(230, decisionCanBacktrack[230]);

            	    int LA230_0 = input.LA(1);

            	    if ( (LA230_0==HASH_SYMBOL) ) {
            	        int LA230_1 = input.LA(2);

            	        if ( (LA230_1==LBRACE) && (synpred18_Css3())) {
            	            alt230=1;
            	        }
            	        else if ( (LA230_1==IDENT||LA230_1==COLON||LA230_1==WS||(LA230_1>=MINUS && LA230_1<=DOT)||(LA230_1>=NL && LA230_1<=COMMENT)) ) {
            	            alt230=2;
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return ;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 230, 1, input);

            	            dbg.recognitionException(nvae);
            	            throw nvae;
            	        }
            	    }
            	    else if ( (LA230_0==IDENT||LA230_0==MINUS||(LA230_0>=HASH && LA230_0<=DOT)) ) {
            	        alt230=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 230, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(230);}

            	    switch (alt230) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1090,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6793);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1092,17);
            	            if ( input.LA(1)==IDENT||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            	    } finally {dbg.exitSubRule(230);}


            	    }
            	    break;

            	default :
            	    break loop231;
                }
            } while (true);
            } finally {dbg.exitSubRule(231);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1096, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_declaration_interpolation_expression"


    // $ANTLR start "scss_mq_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1098:1: scss_mq_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* ;
    public final void scss_mq_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_mq_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1098, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1099:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            {
            dbg.location(1100,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            int alt232=2;
            try { dbg.enterSubRule(232);
            try { dbg.enterDecision(232, decisionCanBacktrack[232]);

            try {
                isCyclicDecision = true;
                alt232 = dfa232.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(232);}

            switch (alt232) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1101,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6923);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1103:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
                    {
                    dbg.location(1103,13);
                    if ( input.LA(1)==IDENT||input.LA(1)==AND||input.LA(1)==NOT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            } finally {dbg.exitSubRule(232);}

            dbg.location(1105,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1105:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            try { dbg.enterSubRule(235);

            loop235:
            do {
                int alt235=2;
                try { dbg.enterDecision(235, decisionCanBacktrack[235]);

                try {
                    isCyclicDecision = true;
                    alt235 = dfa235.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(235);}

                switch (alt235) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1106:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    {
            	    dbg.location(1106,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1106:13: ( ws )?
            	    int alt233=2;
            	    try { dbg.enterSubRule(233);
            	    try { dbg.enterDecision(233, decisionCanBacktrack[233]);

            	    int LA233_0 = input.LA(1);

            	    if ( (LA233_0==WS||(LA233_0>=NL && LA233_0<=COMMENT)) ) {
            	        alt233=1;
            	    }
            	    } finally {dbg.exitDecision(233);}

            	    switch (alt233) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1106:13: ws
            	            {
            	            dbg.location(1106,13);
            	            pushFollow(FOLLOW_ws_in_scss_mq_interpolation_expression7016);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(233);}

            	    dbg.location(1107,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    int alt234=2;
            	    try { dbg.enterSubRule(234);
            	    try { dbg.enterDecision(234, decisionCanBacktrack[234]);

            	    try {
            	        isCyclicDecision = true;
            	        alt234 = dfa234.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(234);}

            	    switch (alt234) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1108,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7055);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
            	            {
            	            dbg.location(1110,17);
            	            if ( input.LA(1)==IDENT||input.LA(1)==AND||input.LA(1)==NOT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            	    } finally {dbg.exitSubRule(234);}


            	    }
            	    break;

            	default :
            	    break loop235;
                }
            } while (true);
            } finally {dbg.exitSubRule(235);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1114, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_mq_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_mq_interpolation_expression"


    // $ANTLR start "scss_interpolation_expression_var"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1116:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1116, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1117:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1118,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7176); if (state.failed) return ;
            dbg.location(1118,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var7178); if (state.failed) return ;
            dbg.location(1118,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:28: ( ws )?
            int alt236=2;
            try { dbg.enterSubRule(236);
            try { dbg.enterDecision(236, decisionCanBacktrack[236]);

            int LA236_0 = input.LA(1);

            if ( (LA236_0==WS||(LA236_0>=NL && LA236_0<=COMMENT)) ) {
                alt236=1;
            }
            } finally {dbg.exitDecision(236);}

            switch (alt236) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:28: ws
                    {
                    dbg.location(1118,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7180);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(236);}

            dbg.location(1118,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:32: ( cp_variable | less_function_in_condition )
            int alt237=2;
            try { dbg.enterSubRule(237);
            try { dbg.enterDecision(237, decisionCanBacktrack[237]);

            int LA237_0 = input.LA(1);

            if ( (LA237_0==MEDIA_SYM||LA237_0==AT_IDENT||LA237_0==SASS_VAR) ) {
                alt237=1;
            }
            else if ( (LA237_0==IDENT) ) {
                alt237=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 237, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(237);}

            switch (alt237) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:34: cp_variable
                    {
                    dbg.location(1118,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var7185);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:48: less_function_in_condition
                    {
                    dbg.location(1118,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7189);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(237);}

            dbg.location(1118,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:77: ( ws )?
            int alt238=2;
            try { dbg.enterSubRule(238);
            try { dbg.enterDecision(238, decisionCanBacktrack[238]);

            int LA238_0 = input.LA(1);

            if ( (LA238_0==WS||(LA238_0>=NL && LA238_0<=COMMENT)) ) {
                alt238=1;
            }
            } finally {dbg.exitDecision(238);}

            switch (alt238) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:77: ws
                    {
                    dbg.location(1118,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7193);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(238);}

            dbg.location(1118,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var7196); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1119, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_interpolation_expression_var");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_interpolation_expression_var"


    // $ANTLR start "scss_nested_properties"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1139:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1139, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1140:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1141,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties7240);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1141,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties7242); if (state.failed) return ;
            dbg.location(1141,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:20: ( ws )?
            int alt239=2;
            try { dbg.enterSubRule(239);
            try { dbg.enterDecision(239, decisionCanBacktrack[239]);

            int LA239_0 = input.LA(1);

            if ( (LA239_0==WS||(LA239_0>=NL && LA239_0<=COMMENT)) ) {
                alt239=1;
            }
            } finally {dbg.exitDecision(239);}

            switch (alt239) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:20: ws
                    {
                    dbg.location(1141,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7244);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(239);}

            dbg.location(1141,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:24: ( propertyValue )?
            int alt240=2;
            try { dbg.enterSubRule(240);
            try { dbg.enterDecision(240, decisionCanBacktrack[240]);

            int LA240_0 = input.LA(1);

            if ( ((LA240_0>=IDENT && LA240_0<=URI)||LA240_0==MEDIA_SYM||(LA240_0>=GEN && LA240_0<=LPAREN)||LA240_0==AT_IDENT||LA240_0==PERCENTAGE||LA240_0==PLUS||LA240_0==MINUS||LA240_0==HASH||(LA240_0>=NUMBER && LA240_0<=DIMENSION)||LA240_0==SASS_VAR) ) {
                alt240=1;
            }
            } finally {dbg.exitDecision(240);}

            switch (alt240) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:24: propertyValue
                    {
                    dbg.location(1141,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties7247);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(240);}

            dbg.location(1141,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties7250); if (state.failed) return ;
            dbg.location(1141,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:46: ( ws )?
            int alt241=2;
            try { dbg.enterSubRule(241);
            try { dbg.enterDecision(241, decisionCanBacktrack[241]);

            int LA241_0 = input.LA(1);

            if ( (LA241_0==WS||(LA241_0>=NL && LA241_0<=COMMENT)) ) {
                alt241=1;
            }
            } finally {dbg.exitDecision(241);}

            switch (alt241) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:46: ws
                    {
                    dbg.location(1141,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7252);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(241);}

            dbg.location(1141,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties7255);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1141,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties7257);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1141,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties7259); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1142, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_nested_properties");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_nested_properties"


    // $ANTLR start "sass_extend"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1144:1: sass_extend : SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI ;
    public final void sass_extend() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1144, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1145:5: ( SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:5: SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI
            {
            dbg.location(1146,5);
            match(input,SASS_EXTEND,FOLLOW_SASS_EXTEND_in_sass_extend7280); if (state.failed) return ;
            dbg.location(1146,17);
            pushFollow(FOLLOW_ws_in_sass_extend7282);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1146,20);
            pushFollow(FOLLOW_simpleSelectorSequence_in_sass_extend7284);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1146,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:43: ( SASS_OPTIONAL ( ws )? )?
            int alt243=2;
            try { dbg.enterSubRule(243);
            try { dbg.enterDecision(243, decisionCanBacktrack[243]);

            int LA243_0 = input.LA(1);

            if ( (LA243_0==SASS_OPTIONAL) ) {
                alt243=1;
            }
            } finally {dbg.exitDecision(243);}

            switch (alt243) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:44: SASS_OPTIONAL ( ws )?
                    {
                    dbg.location(1146,44);
                    match(input,SASS_OPTIONAL,FOLLOW_SASS_OPTIONAL_in_sass_extend7287); if (state.failed) return ;
                    dbg.location(1146,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:58: ( ws )?
                    int alt242=2;
                    try { dbg.enterSubRule(242);
                    try { dbg.enterDecision(242, decisionCanBacktrack[242]);

                    int LA242_0 = input.LA(1);

                    if ( (LA242_0==WS||(LA242_0>=NL && LA242_0<=COMMENT)) ) {
                        alt242=1;
                    }
                    } finally {dbg.exitDecision(242);}

                    switch (alt242) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:58: ws
                            {
                            dbg.location(1146,58);
                            pushFollow(FOLLOW_ws_in_sass_extend7289);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(242);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(243);}

            dbg.location(1146,64);
            match(input,SEMI,FOLLOW_SEMI_in_sass_extend7294); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1147, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_extend");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_extend"


    // $ANTLR start "sass_extend_only_selector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1149:1: sass_extend_only_selector : SASS_EXTEND_ONLY_SELECTOR ;
    public final void sass_extend_only_selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend_only_selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1149, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1150:5: ( SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1151:5: SASS_EXTEND_ONLY_SELECTOR
            {
            dbg.location(1151,5);
            match(input,SASS_EXTEND_ONLY_SELECTOR,FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector7319); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1152, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_extend_only_selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_extend_only_selector"


    // $ANTLR start "sass_debug"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1154:1: sass_debug : ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI ;
    public final void sass_debug() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_debug");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1154, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1155:5: ( ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:5: ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI
            {
            dbg.location(1156,5);
            if ( (input.LA(1)>=SASS_DEBUG && input.LA(1)<=SASS_WARN) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(1156,32);
            pushFollow(FOLLOW_ws_in_sass_debug7350);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1156,35);
            pushFollow(FOLLOW_cp_expression_in_sass_debug7352);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1156,49);
            match(input,SEMI,FOLLOW_SEMI_in_sass_debug7354); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1157, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_debug");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_debug"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(368,15);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(244);

        loop244:
        do {
            int alt244=2;
            try { dbg.enterDecision(244, decisionCanBacktrack[244]);

            int LA244_0 = input.LA(1);

            if ( ((LA244_0>=NAMESPACE_SYM && LA244_0<=MEDIA_SYM)||(LA244_0>=RBRACE && LA244_0<=MINUS)||(LA244_0>=HASH && LA244_0<=LINE_COMMENT)) ) {
                alt244=1;
            }


            } finally {dbg.exitDecision(244);}

            switch (alt244) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(368,15);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=RBRACE && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop244;
            }
        } while (true);
        } finally {dbg.exitSubRule(244);}

        dbg.location(368,42);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred1_Css3487); if (state.failed) return ;
        dbg.location(368,54);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Css3489); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:13: ( mediaQueryList )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:14: mediaQueryList
        {
        dbg.location(370,14);
        pushFollow(FOLLOW_mediaQueryList_in_synpred2_Css3526);
        mediaQueryList();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:17: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt247=2;
        try { dbg.enterDecision(247, decisionCanBacktrack[247]);

        try {
            isCyclicDecision = true;
            alt247 = dfa247.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(247);}

        switch (alt247) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(376,18);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt245=0;
                try { dbg.enterSubRule(245);

                loop245:
                do {
                    int alt245=2;
                    try { dbg.enterDecision(245, decisionCanBacktrack[245]);

                    int LA245_0 = input.LA(1);

                    if ( (LA245_0==NAMESPACE_SYM||(LA245_0>=IDENT && LA245_0<=MEDIA_SYM)||(LA245_0>=AND && LA245_0<=LPAREN)||(LA245_0>=RPAREN && LA245_0<=LINE_COMMENT)) ) {
                        alt245=1;
                    }


                    } finally {dbg.exitDecision(245);}

                    switch (alt245) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(376,18);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt245 >= 1 ) break loop245;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(245, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt245++;
                } while (true);
                } finally {dbg.exitSubRule(245);}

                dbg.location(376,47);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3624); if (state.failed) return ;
                dbg.location(376,53);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt246=0;
                try { dbg.enterSubRule(246);

                loop246:
                do {
                    int alt246=2;
                    try { dbg.enterDecision(246, decisionCanBacktrack[246]);

                    int LA246_0 = input.LA(1);

                    if ( (LA246_0==NAMESPACE_SYM||(LA246_0>=IDENT && LA246_0<=MEDIA_SYM)||(LA246_0>=AND && LA246_0<=LINE_COMMENT)) ) {
                        alt246=1;
                    }


                    } finally {dbg.exitDecision(246);}

                    switch (alt246) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(376,53);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt246 >= 1 ) break loop246;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(246, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt246++;
                } while (true);
                } finally {dbg.exitSubRule(246);}

                dbg.location(376,76);
                match(input,SEMI,FOLLOW_SEMI_in_synpred3_Css3636); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:83: scss_declaration_interpolation_expression COLON
                {
                dbg.location(376,83);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3640);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(376,125);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3642); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt250=2;
        try { dbg.enterDecision(250, decisionCanBacktrack[250]);

        try {
            isCyclicDecision = true;
            alt250 = dfa250.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(250);}

        switch (alt250) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(585,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt248=0;
                try { dbg.enterSubRule(248);

                loop248:
                do {
                    int alt248=2;
                    try { dbg.enterDecision(248, decisionCanBacktrack[248]);

                    int LA248_0 = input.LA(1);

                    if ( (LA248_0==NAMESPACE_SYM||(LA248_0>=IDENT && LA248_0<=MEDIA_SYM)||(LA248_0>=AND && LA248_0<=LPAREN)||(LA248_0>=RPAREN && LA248_0<=LINE_COMMENT)) ) {
                        alt248=1;
                    }


                    } finally {dbg.exitDecision(248);}

                    switch (alt248) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(585,4);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt248 >= 1 ) break loop248;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(248, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt248++;
                } while (true);
                } finally {dbg.exitSubRule(248);}

                dbg.location(585,33);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32425); if (state.failed) return ;
                dbg.location(585,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt249=0;
                try { dbg.enterSubRule(249);

                loop249:
                do {
                    int alt249=2;
                    try { dbg.enterDecision(249, decisionCanBacktrack[249]);

                    int LA249_0 = input.LA(1);

                    if ( (LA249_0==NAMESPACE_SYM||(LA249_0>=IDENT && LA249_0<=MEDIA_SYM)||(LA249_0>=AND && LA249_0<=LINE_COMMENT)) ) {
                        alt249=1;
                    }


                    } finally {dbg.exitDecision(249);}

                    switch (alt249) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(585,39);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt249 >= 1 ) break loop249;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(249, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt249++;
                } while (true);
                } finally {dbg.exitSubRule(249);}

                dbg.location(585,62);
                match(input,SEMI,FOLLOW_SEMI_in_synpred4_Css32437); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:69: scss_declaration_interpolation_expression COLON
                {
                dbg.location(585,69);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32441);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(585,111);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32443); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )
        int alt253=2;
        try { dbg.enterDecision(253, decisionCanBacktrack[253]);

        try {
            isCyclicDecision = true;
            alt253 = dfa253.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(253);}

        switch (alt253) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE
                {
                dbg.location(587,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt251=0;
                try { dbg.enterSubRule(251);

                loop251:
                do {
                    int alt251=2;
                    try { dbg.enterDecision(251, decisionCanBacktrack[251]);

                    int LA251_0 = input.LA(1);

                    if ( (LA251_0==NAMESPACE_SYM||(LA251_0>=IDENT && LA251_0<=MEDIA_SYM)||(LA251_0>=AND && LA251_0<=LPAREN)||(LA251_0>=RPAREN && LA251_0<=LINE_COMMENT)) ) {
                        alt251=1;
                    }


                    } finally {dbg.exitDecision(251);}

                    switch (alt251) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(587,4);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt251 >= 1 ) break loop251;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(251, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt251++;
                } while (true);
                } finally {dbg.exitSubRule(251);}

                dbg.location(587,33);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32473); if (state.failed) return ;
                dbg.location(587,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt252=0;
                try { dbg.enterSubRule(252);

                loop252:
                do {
                    int alt252=2;
                    try { dbg.enterDecision(252, decisionCanBacktrack[252]);

                    int LA252_0 = input.LA(1);

                    if ( (LA252_0==NAMESPACE_SYM||(LA252_0>=IDENT && LA252_0<=MEDIA_SYM)||(LA252_0>=AND && LA252_0<=LINE_COMMENT)) ) {
                        alt252=1;
                    }


                    } finally {dbg.exitDecision(252);}

                    switch (alt252) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(587,39);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LINE_COMMENT) ) {
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
                	    if ( cnt252 >= 1 ) break loop252;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(252, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt252++;
                } while (true);
                } finally {dbg.exitSubRule(252);}

                dbg.location(587,62);
                match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32485); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:71: scss_declaration_interpolation_expression COLON
                {
                dbg.location(587,71);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32489);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(587,113);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32491); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(589,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:18: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt254=0;
        try { dbg.enterSubRule(254);

        loop254:
        do {
            int alt254=2;
            try { dbg.enterDecision(254, decisionCanBacktrack[254]);

            int LA254_0 = input.LA(1);

            if ( (LA254_0==NAMESPACE_SYM||(LA254_0>=IDENT && LA254_0<=MEDIA_SYM)||(LA254_0>=AND && LA254_0<=LINE_COMMENT)) ) {
                alt254=1;
            }


            } finally {dbg.exitDecision(254);}

            switch (alt254) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(589,18);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=LINE_COMMENT) ) {
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
        	    if ( cnt254 >= 1 ) break loop254;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(254, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt254++;
        } while (true);
        } finally {dbg.exitSubRule(254);}

        dbg.location(589,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred6_Css32531); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(601,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:16: (~ ( RBRACE ) )+
        int cnt255=0;
        try { dbg.enterSubRule(255);

        loop255:
        do {
            int alt255=2;
            try { dbg.enterDecision(255, decisionCanBacktrack[255]);

            int LA255_0 = input.LA(1);

            if ( ((LA255_0>=NAMESPACE_SYM && LA255_0<=LBRACE)||(LA255_0>=AND && LA255_0<=LINE_COMMENT)) ) {
                alt255=1;
            }


            } finally {dbg.exitDecision(255);}

            switch (alt255) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:16: ~ ( RBRACE )
        	    {
        	    dbg.location(601,16);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=LBRACE)||(input.LA(1)>=AND && input.LA(1)<=LINE_COMMENT) ) {
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
        	    if ( cnt255 >= 1 ) break loop255;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(255, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt255++;
        } while (true);
        } finally {dbg.exitSubRule(255);}

        dbg.location(601,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred7_Css32741); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(607,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(256);

        loop256:
        do {
            int alt256=2;
            try { dbg.enterDecision(256, decisionCanBacktrack[256]);

            int LA256_0 = input.LA(1);

            if ( ((LA256_0>=NAMESPACE_SYM && LA256_0<=MEDIA_SYM)||(LA256_0>=RBRACE && LA256_0<=MINUS)||(LA256_0>=HASH && LA256_0<=LINE_COMMENT)) ) {
                alt256=1;
            }


            } finally {dbg.exitDecision(256);}

            switch (alt256) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(607,11);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=RBRACE && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop256;
            }
        } while (true);
        } finally {dbg.exitSubRule(256);}

        dbg.location(607,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred8_Css32799); if (state.failed) return ;
        dbg.location(607,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred8_Css32801); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:19: esPred
        {
        dbg.location(620,19);
        pushFollow(FOLLOW_esPred_in_synpred9_Css32899);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:6: esPred
        {
        dbg.location(622,6);
        pushFollow(FOLLOW_esPred_in_synpred10_Css32920);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(636,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:8: ( IDENT | STAR )?
        int alt257=2;
        try { dbg.enterSubRule(257);
        try { dbg.enterDecision(257, decisionCanBacktrack[257]);

        int LA257_0 = input.LA(1);

        if ( (LA257_0==IDENT||LA257_0==STAR) ) {
            alt257=1;
        }
        } finally {dbg.exitDecision(257);}

        switch (alt257) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(636,8);
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
        } finally {dbg.exitSubRule(257);}

        dbg.location(636,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred11_Css33038); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(735,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )*
        try { dbg.enterSubRule(258);

        loop258:
        do {
            int alt258=2;
            try { dbg.enterDecision(258, decisionCanBacktrack[258]);

            int LA258_0 = input.LA(1);

            if ( (LA258_0==NAMESPACE_SYM||(LA258_0>=IDENT && LA258_0<=LBRACE)||(LA258_0>=AND && LA258_0<=LPAREN)||(LA258_0>=RPAREN && LA258_0<=MINUS)||(LA258_0>=HASH && LA258_0<=LINE_COMMENT)) ) {
                alt258=1;
            }


            } finally {dbg.exitDecision(258);}

            switch (alt258) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:11: ~ ( HASH_SYMBOL | COLON | SEMI | RBRACE )
        	    {
        	    dbg.location(735,11);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=LBRACE)||(input.LA(1)>=AND && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop258;
            }
        } while (true);
        } finally {dbg.exitSubRule(258);}

        dbg.location(735,51);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred12_Css34010); if (state.failed) return ;
        dbg.location(735,63);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Css34012); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:12: expressionPredicate
        {
        dbg.location(750,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred13_Css34098);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:17: term
        {
        dbg.location(922,17);
        pushFollow(FOLLOW_term_in_synpred14_Css35337);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1065,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred15_Css36410); if (state.failed) return ;
        dbg.location(1065,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred15_Css36412); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

    // $ANTLR start synpred16_Css3
    public final void synpred16_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1072,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred16_Css36534); if (state.failed) return ;
        dbg.location(1072,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred16_Css36536); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Css3

    // $ANTLR start synpred17_Css3
    public final void synpred17_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1083:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1083:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1083,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred17_Css36668); if (state.failed) return ;
        dbg.location(1083,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred17_Css36670); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Css3

    // $ANTLR start synpred18_Css3
    public final void synpred18_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1090,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred18_Css36788); if (state.failed) return ;
        dbg.location(1090,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred18_Css36790); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Css3

    // $ANTLR start synpred19_Css3
    public final void synpred19_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1101,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred19_Css36918); if (state.failed) return ;
        dbg.location(1101,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred19_Css36920); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Css3

    // $ANTLR start synpred20_Css3
    public final void synpred20_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1108,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred20_Css37050); if (state.failed) return ;
        dbg.location(1108,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred20_Css37052); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Css3

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
    public final boolean synpred10_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred10_Css3_fragment(); // can never throw exception
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
    public final boolean synpred11_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred11_Css3_fragment(); // can never throw exception
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
    public final boolean synpred20_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred20_Css3_fragment(); // can never throw exception
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
    public final boolean synpred16_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred16_Css3_fragment(); // can never throw exception
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
    public final boolean synpred18_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred18_Css3_fragment(); // can never throw exception
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
    public final boolean synpred17_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred17_Css3_fragment(); // can never throw exception
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
    public final boolean synpred14_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred14_Css3_fragment(); // can never throw exception
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
    public final boolean synpred7_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred7_Css3_fragment(); // can never throw exception
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
    public final boolean synpred15_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred15_Css3_fragment(); // can never throw exception
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
    public final boolean synpred8_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred8_Css3_fragment(); // can never throw exception
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
    public final boolean synpred6_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred6_Css3_fragment(); // can never throw exception
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
    public final boolean synpred13_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred13_Css3_fragment(); // can never throw exception
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
    public final boolean synpred9_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred9_Css3_fragment(); // can never throw exception
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
    public final boolean synpred12_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred12_Css3_fragment(); // can never throw exception
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
    public final boolean synpred19_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred19_Css3_fragment(); // can never throw exception
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


    protected DFA22 dfa22 = new DFA22(this);
    protected DFA25 dfa25 = new DFA25(this);
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA75 dfa75 = new DFA75(this);
    protected DFA101 dfa101 = new DFA101(this);
    protected DFA110 dfa110 = new DFA110(this);
    protected DFA115 dfa115 = new DFA115(this);
    protected DFA118 dfa118 = new DFA118(this);
    protected DFA136 dfa136 = new DFA136(this);
    protected DFA146 dfa146 = new DFA146(this);
    protected DFA150 dfa150 = new DFA150(this);
    protected DFA153 dfa153 = new DFA153(this);
    protected DFA159 dfa159 = new DFA159(this);
    protected DFA180 dfa180 = new DFA180(this);
    protected DFA184 dfa184 = new DFA184(this);
    protected DFA199 dfa199 = new DFA199(this);
    protected DFA204 dfa204 = new DFA204(this);
    protected DFA224 dfa224 = new DFA224(this);
    protected DFA227 dfa227 = new DFA227(this);
    protected DFA226 dfa226 = new DFA226(this);
    protected DFA232 dfa232 = new DFA232(this);
    protected DFA235 dfa235 = new DFA235(this);
    protected DFA234 dfa234 = new DFA234(this);
    protected DFA247 dfa247 = new DFA247(this);
    protected DFA250 dfa250 = new DFA250(this);
    protected DFA253 dfa253 = new DFA253(this);
    static final String DFA22_eotS =
        "\7\uffff";
    static final String DFA22_eofS =
        "\7\uffff";
    static final String DFA22_minS =
        "\1\12\2\7\2\5\2\uffff";
    static final String DFA22_maxS =
        "\1\12\4\123\2\uffff";
    static final String DFA22_acceptS =
        "\5\uffff\1\1\1\2";
    static final String DFA22_specialS =
        "\7\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\1",
            "\2\3\16\uffff\1\2\72\uffff\2\2",
            "\2\3\16\uffff\1\2\72\uffff\2\2",
            "\2\5\4\uffff\1\6\4\uffff\4\5\3\uffff\1\4\72\uffff\2\4",
            "\2\5\4\uffff\1\6\4\uffff\4\5\3\uffff\1\4\72\uffff\2\4",
            "",
            ""
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "357:1: importItem : ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI | {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier ) mediaQueryList SEMI );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA25_eotS =
        "\16\uffff";
    static final String DFA25_eofS =
        "\16\uffff";
    static final String DFA25_minS =
        "\1\6\1\uffff\1\6\1\0\5\uffff\1\6\1\uffff\1\0\2\uffff";
    static final String DFA25_maxS =
        "\1\70\1\uffff\1\123\1\0\5\uffff\1\123\1\uffff\1\0\2\uffff";
    static final String DFA25_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\1\3\2\1\uffff\1\1\1\uffff\2\1";
    static final String DFA25_specialS =
        "\1\0\1\uffff\1\1\1\3\5\uffff\1\4\1\uffff\1\2\2\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\3\6\uffff\1\10\1\uffff\1\5\1\4\1\2\1\6\1\7\1\5\40\uffff\1"+
            "\5\1\1\2\5",
            "",
            "\1\13\6\uffff\1\14\1\uffff\1\15\1\uffff\1\15\1\6\1\uffff\1"+
            "\15\2\uffff\1\11\35\uffff\1\15\1\12\2\15\31\uffff\2\11",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\13\6\uffff\1\14\1\uffff\1\15\1\uffff\1\15\1\6\1\uffff\1"+
            "\15\2\uffff\1\11\35\uffff\1\15\1\12\2\15\31\uffff\2\11",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }
        public String getDescription() {
            return "367:9: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA25_0 = input.LA(1);

                         
                        int index25_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA25_0==HASH_SYMBOL) && (synpred1_Css3())) {s = 1;}

                        else if ( (LA25_0==NOT) ) {s = 2;}

                        else if ( (LA25_0==IDENT) ) {s = 3;}

                        else if ( (LA25_0==ONLY) && (synpred2_Css3())) {s = 4;}

                        else if ( (LA25_0==AND||LA25_0==COLON||LA25_0==MINUS||(LA25_0>=HASH && LA25_0<=DOT)) && (synpred1_Css3())) {s = 5;}

                        else if ( (LA25_0==GEN) && (synpred2_Css3())) {s = 6;}

                        else if ( (LA25_0==LPAREN) && (synpred2_Css3())) {s = 7;}

                        else if ( (LA25_0==LBRACE) && (synpred2_Css3())) {s = 8;}

                         
                        input.seek(index25_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA25_2 = input.LA(1);

                         
                        int index25_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA25_2==WS||(LA25_2>=NL && LA25_2<=COMMENT)) ) {s = 9;}

                        else if ( (LA25_2==HASH_SYMBOL) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA25_2==IDENT) ) {s = 11;}

                        else if ( (LA25_2==LBRACE) && (synpred1_Css3())) {s = 12;}

                        else if ( (LA25_2==AND||LA25_2==NOT||LA25_2==COLON||LA25_2==MINUS||(LA25_2>=HASH && LA25_2<=DOT)) && (synpred1_Css3())) {s = 13;}

                        else if ( (LA25_2==GEN) && (synpred2_Css3())) {s = 6;}

                         
                        input.seek(index25_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA25_11 = input.LA(1);

                         
                        int index25_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_11);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA25_3 = input.LA(1);

                         
                        int index25_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA25_9 = input.LA(1);

                         
                        int index25_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA25_9==HASH_SYMBOL) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA25_9==IDENT) ) {s = 11;}

                        else if ( (LA25_9==WS||(LA25_9>=NL && LA25_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA25_9==LBRACE) && (synpred1_Css3())) {s = 12;}

                        else if ( (LA25_9==AND||LA25_9==NOT||LA25_9==COLON||LA25_9==MINUS||(LA25_9>=HASH && LA25_9<=DOT)) && (synpred1_Css3())) {s = 13;}

                        else if ( (LA25_9==GEN) && (synpred2_Css3())) {s = 6;}

                         
                        input.seek(index25_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 25, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA35_eotS =
        "\32\uffff";
    static final String DFA35_eofS =
        "\32\uffff";
    static final String DFA35_minS =
        "\1\6\1\uffff\6\0\3\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA35_maxS =
        "\1\141\1\uffff\6\0\3\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA35_acceptS =
        "\1\uffff\1\11\6\uffff\1\1\1\2\1\3\1\uffff\1\4\7\uffff\1\5\1\6\1"+
        "\7\2\uffff\1\10";
    static final String DFA35_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\3\uffff\1\7\5\uffff\1\10\6"+
        "\uffff\1\11\1\uffff}>";
    static final String[] DFA35_transitionS = {
            "\1\4\5\uffff\1\30\1\uffff\1\1\3\uffff\1\6\1\uffff\1\14\1\uffff"+
            "\1\7\1\uffff\1\26\3\uffff\1\26\1\uffff\1\24\1\uffff\1\25\24"+
            "\uffff\1\21\1\3\1\13\1\5\3\14\1\2\1\14\1\uffff\1\14\25\uffff"+
            "\1\10\1\14\7\uffff\1\11\1\uffff\2\12",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            ""
    };

    static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
    static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
    static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
    static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
    static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
    static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
    static final short[][] DFA35_transition;

    static {
        int numStates = DFA35_transitionS.length;
        DFA35_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
        }
    }

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = DFA35_eot;
            this.eof = DFA35_eof;
            this.min = DFA35_min;
            this.max = DFA35_max;
            this.accept = DFA35_accept;
            this.special = DFA35_special;
            this.transition = DFA35_transition;
        }
        public String getDescription() {
            return "()* loopback of 374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA35_0 = input.LA(1);

                         
                        int index35_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_0==RBRACE) ) {s = 1;}

                        else if ( (LA35_0==STAR) ) {s = 2;}

                        else if ( (LA35_0==HASH_SYMBOL) ) {s = 3;}

                        else if ( (LA35_0==IDENT) ) {s = 4;}

                        else if ( (LA35_0==DOT) ) {s = 5;}

                        else if ( (LA35_0==GEN) ) {s = 6;}

                        else if ( (LA35_0==AT_IDENT) ) {s = 7;}

                        else if ( (LA35_0==SASS_VAR) && (synpred3_Css3())) {s = 8;}

                        else if ( (LA35_0==SASS_EXTEND) ) {s = 9;}

                        else if ( ((LA35_0>=SASS_DEBUG && LA35_0<=SASS_WARN)) ) {s = 10;}

                        else if ( (LA35_0==HASH) ) {s = 11;}

                        else if ( (LA35_0==COLON||(LA35_0>=LBRACKET && LA35_0<=SASS_EXTEND_ONLY_SELECTOR)||LA35_0==PIPE||LA35_0==LESS_AND||LA35_0==SASS_MIXIN) ) {s = 12;}

                        else if ( (LA35_0==MINUS) ) {s = 17;}

                        else if ( (LA35_0==PAGE_SYM) ) {s = 20;}

                        else if ( (LA35_0==FONT_FACE_SYM) ) {s = 21;}

                        else if ( (LA35_0==MOZ_DOCUMENT_SYM||LA35_0==WEBKIT_KEYFRAMES_SYM) ) {s = 22;}

                        else if ( (LA35_0==MEDIA_SYM) ) {s = 24;}

                         
                        input.seek(index35_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA35_2 = input.LA(1);

                         
                        int index35_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA35_3 = input.LA(1);

                         
                        int index35_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA35_4 = input.LA(1);

                         
                        int index35_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA35_5 = input.LA(1);

                         
                        int index35_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA35_6 = input.LA(1);

                         
                        int index35_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA35_7 = input.LA(1);

                         
                        int index35_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index35_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA35_11 = input.LA(1);

                         
                        int index35_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_11);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA35_17 = input.LA(1);

                         
                        int index35_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index35_17);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA35_24 = input.LA(1);

                         
                        int index35_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 25;}

                         
                        input.seek(index35_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 35, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA54_eotS =
        "\156\uffff";
    static final String DFA54_eofS =
        "\156\uffff";
    static final String DFA54_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\3\uffff\1\5\1\6\1\uffff\2\6\1\5\3\6"+
        "\5\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\1\5\2\6\2\5\2\6\1\5\1\6\1\5\2\6"+
        "\2\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\2\5\4\6\2\5\1\6\1\5\1\6\1\5\2\6"+
        "\1\5\1\6\2\5\1\6\1\5\3\6\1\5\1\6\1\5\4\6\2\5\1\6\1\5\1\6\1\5\6\6"+
        "\1\5\1\6\1\5\7\6";
    static final String DFA54_maxS =
        "\1\141\1\123\1\uffff\1\123\4\uffff\1\123\3\uffff\2\123\1\uffff\1"+
        "\125\2\123\1\131\4\125\2\123\1\132\1\125\1\123\4\125\1\123\1\125"+
        "\1\131\1\132\1\123\4\125\1\123\1\125\1\123\1\125\1\123\1\131\4\123"+
        "\4\125\1\123\4\125\3\123\3\125\1\123\1\125\1\123\1\125\2\123\4\125"+
        "\1\123\1\125\3\123\1\125\1\123\1\125\3\123\3\125\1\123\1\125\1\123"+
        "\1\125\6\123\1\125\1\123\1\125\7\123";
    static final String DFA54_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\1\11\2\uffff"+
        "\1\2\137\uffff";
    static final String DFA54_specialS =
        "\156\uffff}>";
    static final String[] DFA54_transitionS = {
            "\1\2\5\uffff\1\3\5\uffff\1\2\1\uffff\1\2\1\uffff\1\10\1\uffff"+
            "\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3\2\1\1\5\2\1"+
            "\uffff\1\2\25\uffff\1\11\1\2\1\12\10\uffff\2\13",
            "\1\14\6\uffff\1\2\4\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\31\uffff\2\2",
            "",
            "\1\16\6\uffff\1\16\1\uffff\5\16\1\17\2\uffff\1\15\35\uffff"+
            "\4\16\31\uffff\2\15",
            "",
            "",
            "",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\20\72\uffff\2\11",
            "",
            "",
            "",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\22\1\2\2\uffff"+
            "\1\21\32\uffff\14\2\1\uffff\1\2\22\uffff\2\21",
            "\1\16\6\uffff\1\16\1\uffff\5\16\1\17\2\uffff\1\15\35\uffff"+
            "\4\16\31\uffff\2\15",
            "",
            "\1\25\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\23\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\24\1\16\1\26\1\16\17\uffff\12\11\2\23\1\uffff\1\11",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\20\72\uffff\2\11",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\22\1\2\2\uffff"+
            "\1\21\32\uffff\14\2\1\uffff\1\2\22\uffff\2\21",
            "\3\12\3\uffff\1\27\5\uffff\1\12\2\uffff\1\31\1\27\6\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\3"+
            "\uffff\1\30\2\uffff\2\2",
            "\1\25\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\23\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\24\1\16\1\26\1\16\17\uffff\12\11\2\23\1\uffff\1\11",
            "\1\25\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\1\11"+
            "\1\uffff\1\16\1\uffff\1\11\1\32\5\uffff\1\11\27\uffff\2\16\1"+
            "\26\1\16\17\uffff\12\11\2\32\1\uffff\1\11",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\33\1\uffff\1\11\1\34\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\35\1\16\1\37\1\40\3\uffff\1\11\13\uffff\12\11\2\34\2\11",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\41\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\41\2\11",
            "\1\42\5\uffff\1\42\10\uffff\1\2\1\31\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\42\5\uffff\1\42\10\uffff\1\2\1\31\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\12\7\uffff\1\2\11\uffff\1\43\72\uffff\2\43\6\uffff\1\2",
            "\1\25\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\1\11"+
            "\1\uffff\1\16\1\uffff\1\11\1\32\5\uffff\1\11\27\uffff\2\16\1"+
            "\26\1\16\17\uffff\12\11\2\32\1\uffff\1\11",
            "\1\44\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16\2"+
            "\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\34\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11\2\34\2\11",
            "\1\46\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\45\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\50\1\16\1\47\1\16\17\uffff\12\11\2\45\1\uffff\1\11",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\51\1\uffff\1\11\1\52\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\35\1\16\1\37\1\53\3\uffff\1\11\13\uffff\12\11\2\52\2\11",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\54\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\54\2\11",
            "\1\55\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16\2"+
            "\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\41\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\41\2\11",
            "\3\12\3\uffff\1\57\5\uffff\1\12\3\uffff\1\57\1\56\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\56\1\uffff\1\60\2\uffff\2\2",
            "\1\12\7\uffff\1\2\11\uffff\1\43\72\uffff\2\43\6\uffff\1\2",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\61\35\uffff\3\16\1\40\31\uffff\2\61",
            "\1\46\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\45\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\50\1\16\1\47\1\16\17\uffff\12\11\2\45\1\uffff\1\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\62\1\uffff\1\11\1\63\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\64\1\16\1\66\1\67\3\uffff\1\11\13\uffff\12\11\2\63\2\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\70\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\70\2\11",
            "\1\72\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\1\11"+
            "\1\uffff\1\16\1\uffff\1\11\1\71\5\uffff\1\11\27\uffff\2\16\1"+
            "\73\1\16\17\uffff\12\11\2\71\1\uffff\1\11",
            "\1\74\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16\2"+
            "\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\52\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11\2\52\2\11",
            "\1\75\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16\2"+
            "\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\36\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\54\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\35\1\16\1\37\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\54\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\61\35\uffff\3\16\1\40\31\uffff\2\61",
            "\3\12\3\uffff\1\57\5\uffff\1\12\3\uffff\1\57\1\56\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\56\1\uffff\1\60\2\uffff\2\2",
            "\1\42\5\uffff\1\42\10\uffff\1\2\1\31\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\42\5\uffff\1\42\10\uffff\1\2\1\31\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\61\35\uffff\4\16\31\uffff\2\61",
            "\1\76\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16\2"+
            "\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\63\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11\2\63\2\11",
            "\1\100\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\77\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\50\1\16\1\101\1\16\17\uffff\12\11\2\77\1\uffff\1\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\102\1\uffff\1\11\1\103\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\64\1\16\1\66\1\104\3\uffff\1\11\13\uffff\12\11\2\103"+
            "\2\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\105\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\105\2\11",
            "\1\106\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\70\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\70\2\11",
            "\1\72\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\1\11"+
            "\1\uffff\1\16\1\uffff\1\11\1\71\5\uffff\1\11\27\uffff\2\16\1"+
            "\73\1\16\17\uffff\12\11\2\71\1\uffff\1\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\107\1\uffff\1\11\1\110\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\111\1\16\1\113\1\114\3\uffff\1\11\13\uffff\12\11\2"+
            "\110\2\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\115\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\115\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\116\35\uffff\3\16\1\53\31\uffff\2\116",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\116\35\uffff\3\16\1\53\31\uffff\2\116",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\117\35\uffff\3\16\1\67\31\uffff\2\117",
            "\1\100\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\77\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\50\1\16\1\101\1\16\17\uffff\12\11\2\77\1\uffff\1\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\120\1\uffff\1\11\1\121\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\64\1\16\1\66\1\122\3\uffff\1\11\13\uffff\12\11\2\121"+
            "\2\11",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\123\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\123\2\11",
            "\1\124\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\103\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11\2\103\2\11",
            "\1\125\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\105\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\105\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\117\35\uffff\3\16\1\67\31\uffff\2\117",
            "\1\126\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\110\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12\11\2\110\2\11",
            "\1\130\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\127\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\50\1\16\1\131\1\16\17\uffff\12\11\2\127\1\uffff\1\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\132\1\uffff\1\11\1\133\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\111\1\16\1\113\1\134\3\uffff\1\11\13\uffff\12\11\2"+
            "\133\2\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\135\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\135\2\11",
            "\1\136\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\115\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\115\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\116\35\uffff\4\16\31\uffff\2\116",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\117\35\uffff\4\16\31\uffff\2\117",
            "\1\137\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\121\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11\2\121\2\11",
            "\1\140\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\65\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\123\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\64\1\16\1\66\1\16\3\uffff\1\11\13\uffff\12\11"+
            "\2\123\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\141\35\uffff\3\16\1\104\31\uffff\2\141",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\141\35\uffff\3\16\1\104\31\uffff\2\141",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\142\35\uffff\3\16\1\114\31\uffff\2\142",
            "\1\130\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16\2\11"+
            "\1\16\1\uffff\1\11\1\127\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\50\1\16\1\131\1\16\17\uffff\12\11\2\127\1\uffff\1\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\143\1\uffff\1\11\1\144\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\111\1\16\1\113\1\145\3\uffff\1\11\13\uffff\12\11\2"+
            "\144\2\11",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\146\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\146\2\11",
            "\1\147\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\133\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12\11\2\133\2\11",
            "\1\150\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\135\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\135\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\142\35\uffff\3\16\1\114\31\uffff\2\142",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\151\35\uffff\3\16\1\122\31\uffff\2\151",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\151\35\uffff\3\16\1\122\31\uffff\2\151",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\141\35\uffff\4\16\31\uffff\2\141",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\142\35\uffff\4\16\31\uffff\2\142",
            "\1\152\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\2\11\1\16\1\uffff\1\11\1\144\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12\11\2\144\2\11",
            "\1\153\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\2\uffff\1\16"+
            "\2\uffff\1\16\35\uffff\4\16\31\uffff\2\16",
            "\1\11\1\112\2\11\3\uffff\1\11\1\16\1\uffff\1\16\1\uffff\1\16"+
            "\1\11\1\uffff\1\16\1\uffff\1\11\1\146\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\111\1\16\1\113\1\16\3\uffff\1\11\13\uffff\12"+
            "\11\2\146\2\11",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\154\35\uffff\3\16\1\134\31\uffff\2\154",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\154\35\uffff\3\16\1\134\31\uffff\2\154",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\151\35\uffff\4\16\31\uffff\2\151",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\155\35\uffff\3\16\1\145\31\uffff\2\155",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\155\35\uffff\3\16\1\145\31\uffff\2\155",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\154\35\uffff\4\16\31\uffff\2\154",
            "\1\16\6\uffff\1\16\1\uffff\1\16\1\uffff\1\16\1\uffff\1\11\1"+
            "\16\2\uffff\1\155\35\uffff\4\16\31\uffff\2\155"
    };

    static final short[] DFA54_eot = DFA.unpackEncodedString(DFA54_eotS);
    static final short[] DFA54_eof = DFA.unpackEncodedString(DFA54_eofS);
    static final char[] DFA54_min = DFA.unpackEncodedStringToUnsignedChars(DFA54_minS);
    static final char[] DFA54_max = DFA.unpackEncodedStringToUnsignedChars(DFA54_maxS);
    static final short[] DFA54_accept = DFA.unpackEncodedString(DFA54_acceptS);
    static final short[] DFA54_special = DFA.unpackEncodedString(DFA54_specialS);
    static final short[][] DFA54_transition;

    static {
        int numStates = DFA54_transitionS.length;
        DFA54_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA54_transition[i] = DFA.unpackEncodedString(DFA54_transitionS[i]);
        }
    }

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = DFA54_eot;
            this.eof = DFA54_eof;
            this.min = DFA54_min;
            this.max = DFA54_max;
            this.accept = DFA54_accept;
            this.special = DFA54_special;
            this.transition = DFA54_transition;
        }
        public String getDescription() {
            return "419:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? sass_debug );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA75_eotS =
        "\4\uffff";
    static final String DFA75_eofS =
        "\4\uffff";
    static final String DFA75_minS =
        "\2\13\2\uffff";
    static final String DFA75_maxS =
        "\2\123\2\uffff";
    static final String DFA75_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA75_specialS =
        "\4\uffff}>";
    static final String[] DFA75_transitionS = {
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA75_eot = DFA.unpackEncodedString(DFA75_eotS);
    static final short[] DFA75_eof = DFA.unpackEncodedString(DFA75_eofS);
    static final char[] DFA75_min = DFA.unpackEncodedStringToUnsignedChars(DFA75_minS);
    static final char[] DFA75_max = DFA.unpackEncodedStringToUnsignedChars(DFA75_maxS);
    static final short[] DFA75_accept = DFA.unpackEncodedString(DFA75_acceptS);
    static final short[] DFA75_special = DFA.unpackEncodedString(DFA75_specialS);
    static final short[][] DFA75_transition;

    static {
        int numStates = DFA75_transitionS.length;
        DFA75_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA75_transition[i] = DFA.unpackEncodedString(DFA75_transitionS[i]);
        }
    }

    class DFA75 extends DFA {

        public DFA75(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 75;
            this.eot = DFA75_eot;
            this.eof = DFA75_eof;
            this.min = DFA75_min;
            this.max = DFA75_max;
            this.accept = DFA75_accept;
            this.special = DFA75_special;
            this.transition = DFA75_transition;
        }
        public String getDescription() {
            return "()* loopback of 485:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA101_eotS =
        "\6\uffff";
    static final String DFA101_eofS =
        "\6\uffff";
    static final String DFA101_minS =
        "\2\6\2\uffff\2\6";
    static final String DFA101_maxS =
        "\1\126\1\123\2\uffff\2\123";
    static final String DFA101_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA101_specialS =
        "\6\uffff}>";
    static final String[] DFA101_transitionS = {
            "\1\3\13\uffff\1\3\1\uffff\1\3\40\uffff\3\3\1\1\5\3\1\uffff\1"+
            "\3\26\uffff\1\2",
            "\1\4\6\uffff\1\3\4\uffff\1\3\1\uffff\1\3\2\uffff\1\3\35\uffff"+
            "\4\3\31\uffff\2\3",
            "",
            "",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5"
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
            return "562:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA110_eotS =
        "\31\uffff";
    static final String DFA110_eofS =
        "\31\uffff";
    static final String DFA110_minS =
        "\1\6\7\0\1\uffff\1\0\5\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA110_maxS =
        "\1\141\7\0\1\uffff\1\0\5\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA110_acceptS =
        "\10\uffff\1\10\1\uffff\5\3\1\uffff\2\3\1\4\1\5\1\uffff\1\7\1\1\1"+
        "\2\1\6";
    static final String DFA110_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\5\uffff\1\11\4\uffff"+
        "\1\12\4\uffff}>";
    static final String[] DFA110_transitionS = {
            "\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\13\1\uffff"+
            "\1\24\36\uffff\1\17\1\2\1\11\1\4\1\20\1\21\1\16\1\1\1\14\1\uffff"+
            "\1\15\25\uffff\1\7\1\12\1\25\6\uffff\1\22\1\uffff\2\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA110_eot = DFA.unpackEncodedString(DFA110_eotS);
    static final short[] DFA110_eof = DFA.unpackEncodedString(DFA110_eofS);
    static final char[] DFA110_min = DFA.unpackEncodedStringToUnsignedChars(DFA110_minS);
    static final char[] DFA110_max = DFA.unpackEncodedStringToUnsignedChars(DFA110_maxS);
    static final short[] DFA110_accept = DFA.unpackEncodedString(DFA110_acceptS);
    static final short[] DFA110_special = DFA.unpackEncodedString(DFA110_specialS);
    static final short[][] DFA110_transition;

    static {
        int numStates = DFA110_transitionS.length;
        DFA110_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA110_transition[i] = DFA.unpackEncodedString(DFA110_transitionS[i]);
        }
    }

    class DFA110 extends DFA {

        public DFA110(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 110;
            this.eot = DFA110_eot;
            this.eof = DFA110_eof;
            this.min = DFA110_min;
            this.max = DFA110_max;
            this.accept = DFA110_accept;
            this.special = DFA110_special;
            this.transition = DFA110_transition;
        }
        public String getDescription() {
            return "()* loopback of 581:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA110_0 = input.LA(1);

                         
                        int index110_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA110_0==STAR) ) {s = 1;}

                        else if ( (LA110_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA110_0==IDENT) ) {s = 3;}

                        else if ( (LA110_0==DOT) ) {s = 4;}

                        else if ( (LA110_0==GEN) ) {s = 5;}

                        else if ( (LA110_0==MEDIA_SYM) ) {s = 6;}

                        else if ( (LA110_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA110_0==RBRACE) ) {s = 8;}

                        else if ( (LA110_0==HASH) ) {s = 9;}

                        else if ( (LA110_0==SASS_MIXIN) && (synpred6_Css3())) {s = 10;}

                        else if ( (LA110_0==COLON) && (synpred6_Css3())) {s = 11;}

                        else if ( (LA110_0==PIPE) && (synpred6_Css3())) {s = 12;}

                        else if ( (LA110_0==LESS_AND) && (synpred6_Css3())) {s = 13;}

                        else if ( (LA110_0==SASS_EXTEND_ONLY_SELECTOR) && (synpred6_Css3())) {s = 14;}

                        else if ( (LA110_0==MINUS) ) {s = 15;}

                        else if ( (LA110_0==LBRACKET) && (synpred6_Css3())) {s = 16;}

                        else if ( (LA110_0==DCOLON) && (synpred6_Css3())) {s = 17;}

                        else if ( (LA110_0==SASS_EXTEND) ) {s = 18;}

                        else if ( ((LA110_0>=SASS_DEBUG && LA110_0<=SASS_WARN)) ) {s = 19;}

                        else if ( (LA110_0==AT_IDENT) ) {s = 20;}

                        else if ( (LA110_0==SASS_INCLUDE) ) {s = 21;}

                         
                        input.seek(index110_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA110_1 = input.LA(1);

                         
                        int index110_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA110_2 = input.LA(1);

                         
                        int index110_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA110_3 = input.LA(1);

                         
                        int index110_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred5_Css3()) ) {s = 23;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA110_4 = input.LA(1);

                         
                        int index110_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( ((((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||synpred6_Css3())) ) {s = 17;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 21;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA110_5 = input.LA(1);

                         
                        int index110_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred5_Css3()) ) {s = 23;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA110_6 = input.LA(1);

                         
                        int index110_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 22;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 23;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 24;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index110_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA110_7 = input.LA(1);

                         
                        int index110_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 22;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 23;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index110_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA110_9 = input.LA(1);

                         
                        int index110_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA110_15 = input.LA(1);

                         
                        int index110_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index110_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA110_20 = input.LA(1);

                         
                        int index110_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 22;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 23;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index110_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 110, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA115_eotS =
        "\17\uffff";
    static final String DFA115_eofS =
        "\17\uffff";
    static final String DFA115_minS =
        "\2\6\2\0\1\uffff\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA115_maxS =
        "\1\77\1\123\2\0\1\uffff\2\123\5\uffff\1\0\1\uffff\1\0";
    static final String DFA115_acceptS =
        "\4\uffff\1\2\2\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA115_specialS =
        "\1\6\1\1\1\2\1\5\1\uffff\1\7\1\0\5\uffff\1\4\1\uffff\1\3}>";
    static final String[] DFA115_transitionS = {
            "\1\2\13\uffff\1\4\1\uffff\1\6\40\uffff\1\7\1\1\1\3\1\5\5\4\1"+
            "\uffff\1\4",
            "\1\13\6\uffff\1\10\6\uffff\1\13\2\uffff\1\11\35\uffff\1\13"+
            "\1\12\2\13\5\uffff\1\4\23\uffff\2\11",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\14\6\uffff\1\15\4\uffff\1\4\1\uffff\1\13\2\uffff\1\11\35"+
            "\uffff\1\13\1\12\2\13\31\uffff\2\11",
            "\1\16\6\uffff\1\15\3\uffff\2\4\1\uffff\1\13\2\uffff\1\11\35"+
            "\uffff\1\13\1\12\2\13\31\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff"
    };

    static final short[] DFA115_eot = DFA.unpackEncodedString(DFA115_eotS);
    static final short[] DFA115_eof = DFA.unpackEncodedString(DFA115_eofS);
    static final char[] DFA115_min = DFA.unpackEncodedStringToUnsignedChars(DFA115_minS);
    static final char[] DFA115_max = DFA.unpackEncodedStringToUnsignedChars(DFA115_maxS);
    static final short[] DFA115_accept = DFA.unpackEncodedString(DFA115_acceptS);
    static final short[] DFA115_special = DFA.unpackEncodedString(DFA115_specialS);
    static final short[][] DFA115_transition;

    static {
        int numStates = DFA115_transitionS.length;
        DFA115_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA115_transition[i] = DFA.unpackEncodedString(DFA115_transitionS[i]);
        }
    }

    class DFA115 extends DFA {

        public DFA115(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 115;
            this.eot = DFA115_eot;
            this.eof = DFA115_eof;
            this.min = DFA115_min;
            this.max = DFA115_max;
            this.accept = DFA115_accept;
            this.special = DFA115_special;
            this.transition = DFA115_transition;
        }
        public String getDescription() {
            return "604:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA115_6 = input.LA(1);

                         
                        int index115_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA115_6==WS||(LA115_6>=NL && LA115_6<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA115_6==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA115_6==IDENT) ) {s = 14;}

                        else if ( (LA115_6==LBRACE) && (synpred8_Css3())) {s = 13;}

                        else if ( (LA115_6==COLON||LA115_6==MINUS||(LA115_6>=HASH && LA115_6<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( ((LA115_6>=NOT && LA115_6<=GEN)) ) {s = 4;}

                         
                        input.seek(index115_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA115_1 = input.LA(1);

                         
                        int index115_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA115_1==LBRACE) && (synpred8_Css3())) {s = 8;}

                        else if ( (LA115_1==NAME) ) {s = 4;}

                        else if ( (LA115_1==WS||(LA115_1>=NL && LA115_1<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA115_1==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA115_1==IDENT||LA115_1==COLON||LA115_1==MINUS||(LA115_1>=HASH && LA115_1<=DOT)) && (synpred8_Css3())) {s = 11;}

                         
                        input.seek(index115_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA115_2 = input.LA(1);

                         
                        int index115_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index115_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA115_14 = input.LA(1);

                         
                        int index115_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index115_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA115_12 = input.LA(1);

                         
                        int index115_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index115_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA115_3 = input.LA(1);

                         
                        int index115_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index115_3);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA115_0 = input.LA(1);

                         
                        int index115_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA115_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA115_0==IDENT) ) {s = 2;}

                        else if ( (LA115_0==HASH) ) {s = 3;}

                        else if ( (LA115_0==GEN||(LA115_0>=LBRACKET && LA115_0<=PIPE)||LA115_0==LESS_AND) ) {s = 4;}

                        else if ( (LA115_0==DOT) ) {s = 5;}

                        else if ( (LA115_0==COLON) ) {s = 6;}

                        else if ( (LA115_0==MINUS) && (synpred8_Css3())) {s = 7;}

                         
                        input.seek(index115_0);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA115_5 = input.LA(1);

                         
                        int index115_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA115_5==IDENT) ) {s = 12;}

                        else if ( (LA115_5==WS||(LA115_5>=NL && LA115_5<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA115_5==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA115_5==GEN) ) {s = 4;}

                        else if ( (LA115_5==COLON||LA115_5==MINUS||(LA115_5>=HASH && LA115_5<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( (LA115_5==LBRACE) && (synpred8_Css3())) {s = 13;}

                         
                        input.seek(index115_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 115, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA118_eotS =
        "\24\uffff";
    static final String DFA118_eofS =
        "\24\uffff";
    static final String DFA118_minS =
        "\1\5\7\uffff\6\0\6\uffff";
    static final String DFA118_maxS =
        "\1\137\7\uffff\6\0\6\uffff";
    static final String DFA118_acceptS =
        "\1\uffff\1\2\21\uffff\1\1";
    static final String DFA118_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\1\5\6\uffff}>";
    static final String[] DFA118_transitionS = {
            "\2\1\4\uffff\1\1\1\uffff\1\1\4\uffff\1\1\1\uffff\1\15\1\1\34"+
            "\uffff\3\1\1\uffff\1\12\1\11\1\13\1\14\1\15\1\10\2\1\1\uffff"+
            "\1\1\37\uffff\1\1",
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
            "\1\uffff",
            "",
            "",
            "",
            "",
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
            return "()* loopback of 620:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA118_8 = input.LA(1);

                         
                        int index118_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred9_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 1;}

                         
                        input.seek(index118_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA118_9 = input.LA(1);

                         
                        int index118_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index118_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA118_10 = input.LA(1);

                         
                        int index118_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index118_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA118_11 = input.LA(1);

                         
                        int index118_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index118_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA118_12 = input.LA(1);

                         
                        int index118_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index118_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA118_13 = input.LA(1);

                         
                        int index118_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index118_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 118, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA136_eotS =
        "\4\uffff";
    static final String DFA136_eofS =
        "\4\uffff";
    static final String DFA136_minS =
        "\2\5\2\uffff";
    static final String DFA136_maxS =
        "\2\137\2\uffff";
    static final String DFA136_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA136_specialS =
        "\4\uffff}>";
    static final String[] DFA136_transitionS = {
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "",
            ""
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
            return "721:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA146_eotS =
        "\17\uffff";
    static final String DFA146_eofS =
        "\17\uffff";
    static final String DFA146_minS =
        "\2\6\10\0\1\uffff\1\6\2\0\1\uffff";
    static final String DFA146_maxS =
        "\2\125\10\0\1\uffff\1\125\2\0\1\uffff";
    static final String DFA146_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA146_specialS =
        "\2\uffff\1\0\1\3\1\5\1\11\1\10\1\4\1\6\1\2\2\uffff\1\7\1\1\1\uffff}>";
    static final String[] DFA146_transitionS = {
            "\1\4\1\3\1\6\3\uffff\1\10\5\uffff\1\5\1\12\2\uffff\1\10\6\uffff"+
            "\1\2\24\uffff\1\1\2\uffff\1\1\1\uffff\1\7\20\uffff\12\2\3\uffff"+
            "\1\11",
            "\1\4\1\3\1\6\3\uffff\1\14\5\uffff\1\5\3\uffff\1\14\1\13\5\uffff"+
            "\1\2\31\uffff\1\7\20\uffff\12\2\2\13\1\uffff\1\15",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\4\1\3\1\6\3\uffff\1\14\5\uffff\1\5\3\uffff\1\14\1\13\5\uffff"+
            "\1\2\31\uffff\1\7\20\uffff\12\2\2\13\1\uffff\1\15",
            "\1\uffff",
            "\1\uffff",
            ""
    };

    static final short[] DFA146_eot = DFA.unpackEncodedString(DFA146_eotS);
    static final short[] DFA146_eof = DFA.unpackEncodedString(DFA146_eofS);
    static final char[] DFA146_min = DFA.unpackEncodedStringToUnsignedChars(DFA146_minS);
    static final char[] DFA146_max = DFA.unpackEncodedStringToUnsignedChars(DFA146_maxS);
    static final short[] DFA146_accept = DFA.unpackEncodedString(DFA146_acceptS);
    static final short[] DFA146_special = DFA.unpackEncodedString(DFA146_specialS);
    static final short[][] DFA146_transition;

    static {
        int numStates = DFA146_transitionS.length;
        DFA146_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA146_transition[i] = DFA.unpackEncodedString(DFA146_transitionS[i]);
        }
    }

    class DFA146 extends DFA {

        public DFA146(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 146;
            this.eot = DFA146_eot;
            this.eof = DFA146_eof;
            this.min = DFA146_min;
            this.max = DFA146_max;
            this.accept = DFA146_accept;
            this.special = DFA146_special;
            this.transition = DFA146_transition;
        }
        public String getDescription() {
            return "748:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA146_2 = input.LA(1);

                         
                        int index146_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA146_13 = input.LA(1);

                         
                        int index146_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_13);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA146_9 = input.LA(1);

                         
                        int index146_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index146_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA146_3 = input.LA(1);

                         
                        int index146_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA146_7 = input.LA(1);

                         
                        int index146_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA146_4 = input.LA(1);

                         
                        int index146_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_4);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA146_8 = input.LA(1);

                         
                        int index146_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index146_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA146_12 = input.LA(1);

                         
                        int index146_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA146_6 = input.LA(1);

                         
                        int index146_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_6);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA146_5 = input.LA(1);

                         
                        int index146_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index146_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 146, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA150_eotS =
        "\12\uffff";
    static final String DFA150_eofS =
        "\12\uffff";
    static final String DFA150_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA150_maxS =
        "\1\125\1\uffff\1\125\1\uffff\2\125\1\6\1\125\2\123";
    static final String DFA150_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA150_specialS =
        "\12\uffff}>";
    static final String[] DFA150_transitionS = {
            "\1\1\3\3\2\uffff\1\2\1\3\2\1\3\uffff\1\3\2\uffff\1\1\1\3\6\uffff"+
            "\1\3\23\uffff\2\3\2\uffff\1\3\1\uffff\1\3\17\uffff\1\1\12\3"+
            "\3\uffff\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\5\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
            "\3\24\uffff\1\3\2\uffff\1\3\1\uffff\1\3\20\uffff\12\3\2\4\1"+
            "\uffff\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\5\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
            "\3\24\uffff\1\3\2\uffff\1\3\1\uffff\1\3\20\uffff\12\3\2\4\1"+
            "\uffff\1\3",
            "\4\3\2\uffff\4\3\3\uffff\5\3\1\7\5\uffff\1\3\23\uffff\2\3\2"+
            "\uffff\1\3\1\uffff\1\3\1\6\7\uffff\1\1\6\uffff\13\3\2\7\1\uffff"+
            "\1\3",
            "\1\10",
            "\4\3\2\uffff\4\3\3\uffff\2\3\1\uffff\2\3\1\7\5\uffff\1\3\23"+
            "\uffff\2\3\2\uffff\1\3\1\uffff\1\3\10\uffff\1\1\6\uffff\13\3"+
            "\2\7\1\uffff\1\3",
            "\1\3\3\uffff\1\11\40\uffff\1\6\7\uffff\1\1\21\uffff\2\11",
            "\1\3\3\uffff\1\11\50\uffff\1\1\21\uffff\2\11"
    };

    static final short[] DFA150_eot = DFA.unpackEncodedString(DFA150_eotS);
    static final short[] DFA150_eof = DFA.unpackEncodedString(DFA150_eofS);
    static final char[] DFA150_min = DFA.unpackEncodedStringToUnsignedChars(DFA150_minS);
    static final char[] DFA150_max = DFA.unpackEncodedStringToUnsignedChars(DFA150_maxS);
    static final short[] DFA150_accept = DFA.unpackEncodedString(DFA150_acceptS);
    static final short[] DFA150_special = DFA.unpackEncodedString(DFA150_specialS);
    static final short[][] DFA150_transition;

    static {
        int numStates = DFA150_transitionS.length;
        DFA150_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA150_transition[i] = DFA.unpackEncodedString(DFA150_transitionS[i]);
        }
    }

    class DFA150 extends DFA {

        public DFA150(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 150;
            this.eot = DFA150_eot;
            this.eof = DFA150_eof;
            this.min = DFA150_min;
            this.max = DFA150_max;
            this.accept = DFA150_accept;
            this.special = DFA150_special;
            this.transition = DFA150_transition;
        }
        public String getDescription() {
            return "()* loopback of 808:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA153_eotS =
        "\13\uffff";
    static final String DFA153_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA153_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA153_maxS =
        "\1\125\2\uffff\1\125\5\uffff\1\125\1\uffff";
    static final String DFA153_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA153_specialS =
        "\13\uffff}>";
    static final String[] DFA153_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\20\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\2\10\2\12\1\11\5\uffff\1\12"+
            "\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\1\10\3\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12",
            "",
            "",
            "",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\1\10\1\uffff\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\4\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12",
            ""
    };

    static final short[] DFA153_eot = DFA.unpackEncodedString(DFA153_eotS);
    static final short[] DFA153_eof = DFA.unpackEncodedString(DFA153_eofS);
    static final char[] DFA153_min = DFA.unpackEncodedStringToUnsignedChars(DFA153_minS);
    static final char[] DFA153_max = DFA.unpackEncodedStringToUnsignedChars(DFA153_maxS);
    static final short[] DFA153_accept = DFA.unpackEncodedString(DFA153_acceptS);
    static final short[] DFA153_special = DFA.unpackEncodedString(DFA153_specialS);
    static final short[][] DFA153_transition;

    static {
        int numStates = DFA153_transitionS.length;
        DFA153_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA153_transition[i] = DFA.unpackEncodedString(DFA153_transitionS[i]);
        }
    }

    class DFA153 extends DFA {

        public DFA153(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 153;
            this.eot = DFA153_eot;
            this.eof = DFA153_eof;
            this.min = DFA153_min;
            this.max = DFA153_max;
            this.accept = DFA153_accept;
            this.special = DFA153_special;
            this.transition = DFA153_transition;
        }
        public String getDescription() {
            return "813:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA159_eotS =
        "\10\uffff";
    static final String DFA159_eofS =
        "\10\uffff";
    static final String DFA159_minS =
        "\1\6\1\uffff\3\6\1\uffff\2\23";
    static final String DFA159_maxS =
        "\1\125\1\uffff\2\125\1\6\1\uffff\2\123";
    static final String DFA159_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA159_specialS =
        "\10\uffff}>";
    static final String[] DFA159_transitionS = {
            "\1\2\2\1\3\uffff\1\1\5\uffff\1\1\3\uffff\1\1\6\uffff\1\1\24"+
            "\uffff\1\1\2\uffff\1\1\1\uffff\1\1\20\uffff\12\1\3\uffff\1\1",
            "",
            "\3\1\2\uffff\2\1\5\uffff\5\1\1\3\5\uffff\1\1\23\uffff\2\1\2"+
            "\uffff\1\1\1\uffff\1\1\1\4\7\uffff\1\5\7\uffff\12\1\2\3\1\uffff"+
            "\1\1",
            "\3\1\2\uffff\2\1\5\uffff\2\1\1\uffff\2\1\1\3\5\uffff\1\1\23"+
            "\uffff\2\1\2\uffff\1\1\1\uffff\1\1\10\uffff\1\5\7\uffff\12\1"+
            "\2\3\1\uffff\1\1",
            "\1\6",
            "",
            "\1\1\3\uffff\1\7\40\uffff\1\4\7\uffff\1\5\21\uffff\2\7",
            "\1\1\3\uffff\1\7\50\uffff\1\5\21\uffff\2\7"
    };

    static final short[] DFA159_eot = DFA.unpackEncodedString(DFA159_eotS);
    static final short[] DFA159_eof = DFA.unpackEncodedString(DFA159_eofS);
    static final char[] DFA159_min = DFA.unpackEncodedStringToUnsignedChars(DFA159_minS);
    static final char[] DFA159_max = DFA.unpackEncodedStringToUnsignedChars(DFA159_maxS);
    static final short[] DFA159_accept = DFA.unpackEncodedString(DFA159_acceptS);
    static final short[] DFA159_special = DFA.unpackEncodedString(DFA159_specialS);
    static final short[][] DFA159_transition;

    static {
        int numStates = DFA159_transitionS.length;
        DFA159_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA159_transition[i] = DFA.unpackEncodedString(DFA159_transitionS[i]);
        }
    }

    class DFA159 extends DFA {

        public DFA159(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 159;
            this.eot = DFA159_eot;
            this.eof = DFA159_eof;
            this.min = DFA159_min;
            this.max = DFA159_max;
            this.accept = DFA159_accept;
            this.special = DFA159_special;
            this.transition = DFA159_transition;
        }
        public String getDescription() {
            return "841:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA180_eotS =
        "\36\uffff";
    static final String DFA180_eofS =
        "\36\uffff";
    static final String DFA180_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA180_maxS =
        "\1\125\1\uffff\2\125\10\uffff\1\125\10\0\1\125\10\0";
    static final String DFA180_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA180_specialS =
        "\1\15\14\uffff\1\17\1\2\1\13\1\6\1\3\1\12\1\7\1\16\1\uffff\1\0\1"+
        "\4\1\1\1\11\1\10\1\5\1\14\1\20}>";
    static final String[] DFA180_transitionS = {
            "\1\1\1\6\1\5\1\10\2\uffff\1\1\1\12\2\1\3\uffff\1\7\2\uffff\1"+
            "\1\1\12\6\uffff\1\4\23\uffff\1\1\1\2\2\uffff\1\3\1\uffff\1\11"+
            "\4\uffff\1\1\12\uffff\1\1\12\4\2\uffff\1\1\1\13",
            "",
            "\1\17\1\16\1\21\3\uffff\1\23\5\uffff\1\20\1\1\2\uffff\1\23"+
            "\1\14\5\uffff\1\15\24\uffff\1\1\2\uffff\1\1\1\uffff\1\22\20"+
            "\uffff\12\15\2\14\1\uffff\1\24",
            "\1\30\1\27\1\32\3\uffff\1\34\5\uffff\1\31\1\1\2\uffff\1\34"+
            "\1\25\5\uffff\1\26\24\uffff\1\1\2\uffff\1\1\1\uffff\1\33\20"+
            "\uffff\12\26\2\25\1\uffff\1\35",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17\1\16\1\21\3\uffff\1\23\5\uffff\1\20\1\1\2\uffff\1\23"+
            "\1\14\5\uffff\1\15\24\uffff\1\1\2\uffff\1\1\1\uffff\1\22\20"+
            "\uffff\12\15\2\14\1\uffff\1\24",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\30\1\27\1\32\3\uffff\1\34\5\uffff\1\31\1\1\2\uffff\1\34"+
            "\1\25\5\uffff\1\26\24\uffff\1\1\2\uffff\1\1\1\uffff\1\33\20"+
            "\uffff\12\26\2\25\1\uffff\1\35",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA180_eot = DFA.unpackEncodedString(DFA180_eotS);
    static final short[] DFA180_eof = DFA.unpackEncodedString(DFA180_eofS);
    static final char[] DFA180_min = DFA.unpackEncodedStringToUnsignedChars(DFA180_minS);
    static final char[] DFA180_max = DFA.unpackEncodedStringToUnsignedChars(DFA180_maxS);
    static final short[] DFA180_accept = DFA.unpackEncodedString(DFA180_acceptS);
    static final short[] DFA180_special = DFA.unpackEncodedString(DFA180_specialS);
    static final short[][] DFA180_transition;

    static {
        int numStates = DFA180_transitionS.length;
        DFA180_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA180_transition[i] = DFA.unpackEncodedString(DFA180_transitionS[i]);
        }
    }

    class DFA180 extends DFA {

        public DFA180(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 180;
            this.eot = DFA180_eot;
            this.eof = DFA180_eof;
            this.min = DFA180_min;
            this.max = DFA180_max;
            this.accept = DFA180_accept;
            this.special = DFA180_special;
            this.transition = DFA180_transition;
        }
        public String getDescription() {
            return "()* loopback of 922:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA180_22 = input.LA(1);

                         
                        int index180_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_22);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA180_24 = input.LA(1);

                         
                        int index180_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_24);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA180_14 = input.LA(1);

                         
                        int index180_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_14);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA180_17 = input.LA(1);

                         
                        int index180_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_17);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA180_23 = input.LA(1);

                         
                        int index180_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_23);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA180_27 = input.LA(1);

                         
                        int index180_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_27);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA180_16 = input.LA(1);

                         
                        int index180_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_16);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA180_19 = input.LA(1);

                         
                        int index180_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_19);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA180_26 = input.LA(1);

                         
                        int index180_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_26);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA180_25 = input.LA(1);

                         
                        int index180_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_25);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA180_18 = input.LA(1);

                         
                        int index180_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_18);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA180_15 = input.LA(1);

                         
                        int index180_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_15);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA180_28 = input.LA(1);

                         
                        int index180_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_28);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA180_0 = input.LA(1);

                         
                        int index180_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA180_0==SEMI||LA180_0==COMMA||(LA180_0>=LBRACE && LA180_0<=RBRACE)||LA180_0==RPAREN||LA180_0==SOLIDUS||LA180_0==STAR||LA180_0==IMPORTANT_SYM||LA180_0==SASS_DEFAULT) ) {s = 1;}

                        else if ( (LA180_0==PLUS) ) {s = 2;}

                        else if ( (LA180_0==MINUS) ) {s = 3;}

                        else if ( (LA180_0==PERCENTAGE||(LA180_0>=NUMBER && LA180_0<=DIMENSION)) && (synpred14_Css3())) {s = 4;}

                        else if ( (LA180_0==STRING) && (synpred14_Css3())) {s = 5;}

                        else if ( (LA180_0==IDENT) && (synpred14_Css3())) {s = 6;}

                        else if ( (LA180_0==GEN) && (synpred14_Css3())) {s = 7;}

                        else if ( (LA180_0==URI) && (synpred14_Css3())) {s = 8;}

                        else if ( (LA180_0==HASH) && (synpred14_Css3())) {s = 9;}

                        else if ( (LA180_0==MEDIA_SYM||LA180_0==AT_IDENT) && (synpred14_Css3())) {s = 10;}

                        else if ( (LA180_0==SASS_VAR) && (synpred14_Css3())) {s = 11;}

                         
                        input.seek(index180_0);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA180_20 = input.LA(1);

                         
                        int index180_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_20);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA180_13 = input.LA(1);

                         
                        int index180_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_13);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA180_29 = input.LA(1);

                         
                        int index180_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index180_29);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 180, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA184_eotS =
        "\13\uffff";
    static final String DFA184_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA184_minS =
        "\1\6\2\uffff\1\23\5\uffff\1\23\1\uffff";
    static final String DFA184_maxS =
        "\1\125\2\uffff\1\123\5\uffff\1\123\1\uffff";
    static final String DFA184_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA184_specialS =
        "\13\uffff}>";
    static final String[] DFA184_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\20\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\2\10\2\uffff\1\11\40\uffff\1\10\31\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\10\3\uffff\1\11\72\uffff\2\11",
            ""
    };

    static final short[] DFA184_eot = DFA.unpackEncodedString(DFA184_eotS);
    static final short[] DFA184_eof = DFA.unpackEncodedString(DFA184_eofS);
    static final char[] DFA184_min = DFA.unpackEncodedStringToUnsignedChars(DFA184_minS);
    static final char[] DFA184_max = DFA.unpackEncodedStringToUnsignedChars(DFA184_maxS);
    static final short[] DFA184_accept = DFA.unpackEncodedString(DFA184_acceptS);
    static final short[] DFA184_special = DFA.unpackEncodedString(DFA184_specialS);
    static final short[][] DFA184_transition;

    static {
        int numStates = DFA184_transitionS.length;
        DFA184_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA184_transition[i] = DFA.unpackEncodedString(DFA184_transitionS[i]);
        }
    }

    class DFA184 extends DFA {

        public DFA184(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 184;
            this.eot = DFA184_eot;
            this.eof = DFA184_eof;
            this.min = DFA184_min;
            this.max = DFA184_max;
            this.accept = DFA184_accept;
            this.special = DFA184_special;
            this.transition = DFA184_transition;
        }
        public String getDescription() {
            return "929:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA199_eotS =
        "\4\uffff";
    static final String DFA199_eofS =
        "\4\uffff";
    static final String DFA199_minS =
        "\2\5\2\uffff";
    static final String DFA199_maxS =
        "\2\123\2\uffff";
    static final String DFA199_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA199_specialS =
        "\4\uffff}>";
    static final String[] DFA199_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA199_eot = DFA.unpackEncodedString(DFA199_eotS);
    static final short[] DFA199_eof = DFA.unpackEncodedString(DFA199_eofS);
    static final char[] DFA199_min = DFA.unpackEncodedStringToUnsignedChars(DFA199_minS);
    static final char[] DFA199_max = DFA.unpackEncodedStringToUnsignedChars(DFA199_maxS);
    static final short[] DFA199_accept = DFA.unpackEncodedString(DFA199_acceptS);
    static final short[] DFA199_special = DFA.unpackEncodedString(DFA199_specialS);
    static final short[][] DFA199_transition;

    static {
        int numStates = DFA199_transitionS.length;
        DFA199_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA199_transition[i] = DFA.unpackEncodedString(DFA199_transitionS[i]);
        }
    }

    class DFA199 extends DFA {

        public DFA199(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 199;
            this.eot = DFA199_eot;
            this.eof = DFA199_eof;
            this.min = DFA199_min;
            this.max = DFA199_max;
            this.accept = DFA199_accept;
            this.special = DFA199_special;
            this.transition = DFA199_transition;
        }
        public String getDescription() {
            return "977:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA204_eotS =
        "\5\uffff";
    static final String DFA204_eofS =
        "\5\uffff";
    static final String DFA204_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA204_maxS =
        "\1\25\1\131\1\uffff\1\131\1\uffff";
    static final String DFA204_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA204_specialS =
        "\5\uffff}>";
    static final String[] DFA204_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
    };

    static final short[] DFA204_eot = DFA.unpackEncodedString(DFA204_eotS);
    static final short[] DFA204_eof = DFA.unpackEncodedString(DFA204_eofS);
    static final char[] DFA204_min = DFA.unpackEncodedStringToUnsignedChars(DFA204_minS);
    static final char[] DFA204_max = DFA.unpackEncodedStringToUnsignedChars(DFA204_maxS);
    static final short[] DFA204_accept = DFA.unpackEncodedString(DFA204_acceptS);
    static final short[] DFA204_special = DFA.unpackEncodedString(DFA204_specialS);
    static final short[][] DFA204_transition;

    static {
        int numStates = DFA204_transitionS.length;
        DFA204_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA204_transition[i] = DFA.unpackEncodedString(DFA204_transitionS[i]);
        }
    }

    class DFA204 extends DFA {

        public DFA204(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 204;
            this.eot = DFA204_eot;
            this.eof = DFA204_eof;
            this.min = DFA204_min;
            this.max = DFA204_max;
            this.accept = DFA204_accept;
            this.special = DFA204_special;
            this.transition = DFA204_transition;
        }
        public String getDescription() {
            return "()* loopback of 997:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA224_eotS =
        "\16\uffff";
    static final String DFA224_eofS =
        "\16\uffff";
    static final String DFA224_minS =
        "\2\6\1\uffff\3\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA224_maxS =
        "\1\70\1\123\1\uffff\2\141\5\123\1\uffff\2\123\1\uffff";
    static final String DFA224_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA224_specialS =
        "\5\uffff\1\1\1\6\1\2\1\4\1\0\1\uffff\1\5\1\3\1\uffff}>";
    static final String[] DFA224_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\31\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2\1\uffff\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            ""
    };

    static final short[] DFA224_eot = DFA.unpackEncodedString(DFA224_eotS);
    static final short[] DFA224_eof = DFA.unpackEncodedString(DFA224_eofS);
    static final char[] DFA224_min = DFA.unpackEncodedStringToUnsignedChars(DFA224_minS);
    static final char[] DFA224_max = DFA.unpackEncodedStringToUnsignedChars(DFA224_maxS);
    static final short[] DFA224_accept = DFA.unpackEncodedString(DFA224_acceptS);
    static final short[] DFA224_special = DFA.unpackEncodedString(DFA224_specialS);
    static final short[][] DFA224_transition;

    static {
        int numStates = DFA224_transitionS.length;
        DFA224_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA224_transition[i] = DFA.unpackEncodedString(DFA224_transitionS[i]);
        }
    }

    class DFA224 extends DFA {

        public DFA224(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 224;
            this.eot = DFA224_eot;
            this.eof = DFA224_eof;
            this.min = DFA224_min;
            this.max = DFA224_max;
            this.accept = DFA224_accept;
            this.special = DFA224_special;
            this.transition = DFA224_transition;
        }
        public String getDescription() {
            return "1064:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA224_9 = input.LA(1);

                         
                        int index224_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_9==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA224_9==WS||(LA224_9>=NL && LA224_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA224_9==IDENT||LA224_9==LBRACE||(LA224_9>=AND && LA224_9<=COLON)||(LA224_9>=MINUS && LA224_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index224_9);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA224_5 = input.LA(1);

                         
                        int index224_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_5==WS||(LA224_5>=NL && LA224_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA224_5==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA224_5==IDENT||LA224_5==LBRACE||(LA224_5>=AND && LA224_5<=COLON)||(LA224_5>=MINUS && LA224_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index224_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA224_7 = input.LA(1);

                         
                        int index224_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_7==WS||(LA224_7>=NL && LA224_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA224_7==LPAREN) && (synpred15_Css3())) {s = 13;}

                        else if ( (LA224_7==IDENT||LA224_7==COMMA||LA224_7==LBRACE||LA224_7==GEN||LA224_7==COLON||(LA224_7>=PLUS && LA224_7<=PIPE)||LA224_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index224_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA224_12 = input.LA(1);

                         
                        int index224_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_12==LPAREN) && (synpred15_Css3())) {s = 13;}

                        else if ( (LA224_12==WS||(LA224_12>=NL && LA224_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA224_12==IDENT||LA224_12==COMMA||LA224_12==LBRACE||LA224_12==GEN||LA224_12==COLON||(LA224_12>=PLUS && LA224_12<=PIPE)||LA224_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index224_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA224_8 = input.LA(1);

                         
                        int index224_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_8==WS||(LA224_8>=NL && LA224_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_8==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA224_8==COLON) ) {s = 2;}

                         
                        input.seek(index224_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA224_11 = input.LA(1);

                         
                        int index224_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_11==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA224_11==WS||(LA224_11>=NL && LA224_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_11==COLON) ) {s = 2;}

                         
                        input.seek(index224_11);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA224_6 = input.LA(1);

                         
                        int index224_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_6==WS||(LA224_6>=NL && LA224_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_6==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA224_6==COLON) ) {s = 2;}

                         
                        input.seek(index224_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 224, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA227_eotS =
        "\4\uffff";
    static final String DFA227_eofS =
        "\4\uffff";
    static final String DFA227_minS =
        "\2\6\2\uffff";
    static final String DFA227_maxS =
        "\2\123\2\uffff";
    static final String DFA227_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA227_specialS =
        "\4\uffff}>";
    static final String[] DFA227_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\31\uffff"+
            "\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\31\uffff"+
            "\2\1",
            "",
            ""
    };

    static final short[] DFA227_eot = DFA.unpackEncodedString(DFA227_eotS);
    static final short[] DFA227_eof = DFA.unpackEncodedString(DFA227_eofS);
    static final char[] DFA227_min = DFA.unpackEncodedStringToUnsignedChars(DFA227_minS);
    static final char[] DFA227_max = DFA.unpackEncodedStringToUnsignedChars(DFA227_maxS);
    static final short[] DFA227_accept = DFA.unpackEncodedString(DFA227_acceptS);
    static final short[] DFA227_special = DFA.unpackEncodedString(DFA227_specialS);
    static final short[][] DFA227_transition;

    static {
        int numStates = DFA227_transitionS.length;
        DFA227_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA227_transition[i] = DFA.unpackEncodedString(DFA227_transitionS[i]);
        }
    }

    class DFA227 extends DFA {

        public DFA227(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 227;
            this.eot = DFA227_eot;
            this.eof = DFA227_eof;
            this.min = DFA227_min;
            this.max = DFA227_max;
            this.accept = DFA227_accept;
            this.special = DFA227_special;
            this.transition = DFA227_transition;
        }
        public String getDescription() {
            return "()* loopback of 1069:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA226_eotS =
        "\16\uffff";
    static final String DFA226_eofS =
        "\16\uffff";
    static final String DFA226_minS =
        "\2\6\1\uffff\3\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA226_maxS =
        "\1\70\1\123\1\uffff\2\141\5\123\1\uffff\2\123\1\uffff";
    static final String DFA226_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA226_specialS =
        "\5\uffff\1\1\1\6\1\2\1\4\1\0\1\uffff\1\5\1\3\1\uffff}>";
    static final String[] DFA226_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\31\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2\1\uffff\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            ""
    };

    static final short[] DFA226_eot = DFA.unpackEncodedString(DFA226_eotS);
    static final short[] DFA226_eof = DFA.unpackEncodedString(DFA226_eofS);
    static final char[] DFA226_min = DFA.unpackEncodedStringToUnsignedChars(DFA226_minS);
    static final char[] DFA226_max = DFA.unpackEncodedStringToUnsignedChars(DFA226_maxS);
    static final short[] DFA226_accept = DFA.unpackEncodedString(DFA226_acceptS);
    static final short[] DFA226_special = DFA.unpackEncodedString(DFA226_specialS);
    static final short[][] DFA226_transition;

    static {
        int numStates = DFA226_transitionS.length;
        DFA226_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA226_transition[i] = DFA.unpackEncodedString(DFA226_transitionS[i]);
        }
    }

    class DFA226 extends DFA {

        public DFA226(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 226;
            this.eot = DFA226_eot;
            this.eof = DFA226_eof;
            this.min = DFA226_min;
            this.max = DFA226_max;
            this.accept = DFA226_accept;
            this.special = DFA226_special;
            this.transition = DFA226_transition;
        }
        public String getDescription() {
            return "1071:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA226_9 = input.LA(1);

                         
                        int index226_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_9==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA226_9==WS||(LA226_9>=NL && LA226_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA226_9==IDENT||LA226_9==LBRACE||(LA226_9>=AND && LA226_9<=COLON)||(LA226_9>=MINUS && LA226_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index226_9);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA226_5 = input.LA(1);

                         
                        int index226_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_5==WS||(LA226_5>=NL && LA226_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA226_5==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA226_5==IDENT||LA226_5==LBRACE||(LA226_5>=AND && LA226_5<=COLON)||(LA226_5>=MINUS && LA226_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index226_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA226_7 = input.LA(1);

                         
                        int index226_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_7==WS||(LA226_7>=NL && LA226_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA226_7==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA226_7==IDENT||LA226_7==COMMA||LA226_7==LBRACE||LA226_7==GEN||LA226_7==COLON||(LA226_7>=PLUS && LA226_7<=PIPE)||LA226_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index226_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA226_12 = input.LA(1);

                         
                        int index226_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_12==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA226_12==WS||(LA226_12>=NL && LA226_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA226_12==IDENT||LA226_12==COMMA||LA226_12==LBRACE||LA226_12==GEN||LA226_12==COLON||(LA226_12>=PLUS && LA226_12<=PIPE)||LA226_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index226_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA226_8 = input.LA(1);

                         
                        int index226_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_8==WS||(LA226_8>=NL && LA226_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA226_8==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA226_8==COLON) ) {s = 2;}

                         
                        input.seek(index226_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA226_11 = input.LA(1);

                         
                        int index226_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_11==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA226_11==WS||(LA226_11>=NL && LA226_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA226_11==COLON) ) {s = 2;}

                         
                        input.seek(index226_11);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA226_6 = input.LA(1);

                         
                        int index226_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA226_6==WS||(LA226_6>=NL && LA226_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA226_6==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA226_6==COLON) ) {s = 2;}

                         
                        input.seek(index226_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 226, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA232_eotS =
        "\17\uffff";
    static final String DFA232_eofS =
        "\17\uffff";
    static final String DFA232_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA232_maxS =
        "\1\70\1\123\1\uffff\2\141\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA232_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA232_specialS =
        "\5\uffff\1\5\1\4\1\7\1\2\1\1\1\uffff\1\0\1\6\1\uffff\1\3}>";
    static final String[] DFA232_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\2\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\2\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\31\uffff"+
            "\2\16",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\31\uffff"+
            "\2\16"
    };

    static final short[] DFA232_eot = DFA.unpackEncodedString(DFA232_eotS);
    static final short[] DFA232_eof = DFA.unpackEncodedString(DFA232_eofS);
    static final char[] DFA232_min = DFA.unpackEncodedStringToUnsignedChars(DFA232_minS);
    static final char[] DFA232_max = DFA.unpackEncodedStringToUnsignedChars(DFA232_maxS);
    static final short[] DFA232_accept = DFA.unpackEncodedString(DFA232_acceptS);
    static final short[] DFA232_special = DFA.unpackEncodedString(DFA232_specialS);
    static final short[][] DFA232_transition;

    static {
        int numStates = DFA232_transitionS.length;
        DFA232_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA232_transition[i] = DFA.unpackEncodedString(DFA232_transitionS[i]);
        }
    }

    class DFA232 extends DFA {

        public DFA232(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 232;
            this.eot = DFA232_eot;
            this.eof = DFA232_eof;
            this.min = DFA232_min;
            this.max = DFA232_max;
            this.accept = DFA232_accept;
            this.special = DFA232_special;
            this.transition = DFA232_transition;
        }
        public String getDescription() {
            return "1100:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA232_11 = input.LA(1);

                         
                        int index232_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_11==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA232_11==WS||(LA232_11>=NL && LA232_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA232_11==COLON) ) {s = 2;}

                         
                        input.seek(index232_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA232_9 = input.LA(1);

                         
                        int index232_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_9==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA232_9==WS) ) {s = 9;}

                        else if ( ((LA232_9>=IDENT && LA232_9<=STRING)||LA232_9==LBRACE||LA232_9==COLON) ) {s = 2;}

                        else if ( ((LA232_9>=NL && LA232_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index232_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA232_8 = input.LA(1);

                         
                        int index232_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_8==WS||(LA232_8>=NL && LA232_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA232_8==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA232_8==IDENT||LA232_8==LBRACE||(LA232_8>=AND && LA232_8<=COLON)||(LA232_8>=MINUS && LA232_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index232_8);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA232_14 = input.LA(1);

                         
                        int index232_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_14==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA232_14==WS||(LA232_14>=NL && LA232_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA232_14==IDENT||LA232_14==LBRACE||(LA232_14>=AND && LA232_14<=COLON)||(LA232_14>=MINUS && LA232_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index232_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA232_6 = input.LA(1);

                         
                        int index232_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_6==WS||(LA232_6>=NL && LA232_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA232_6==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA232_6==COLON) ) {s = 2;}

                         
                        input.seek(index232_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA232_5 = input.LA(1);

                         
                        int index232_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_5==WS) ) {s = 9;}

                        else if ( (LA232_5==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( ((LA232_5>=IDENT && LA232_5<=STRING)||LA232_5==LBRACE||LA232_5==COLON) ) {s = 2;}

                        else if ( ((LA232_5>=NL && LA232_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index232_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA232_12 = input.LA(1);

                         
                        int index232_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_12==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA232_12==WS||(LA232_12>=NL && LA232_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA232_12==IDENT||LA232_12==COMMA||LA232_12==LBRACE||LA232_12==GEN||LA232_12==COLON||(LA232_12>=PLUS && LA232_12<=PIPE)||LA232_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index232_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA232_7 = input.LA(1);

                         
                        int index232_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_7==WS||(LA232_7>=NL && LA232_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA232_7==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA232_7==IDENT||LA232_7==COMMA||LA232_7==LBRACE||LA232_7==GEN||LA232_7==COLON||(LA232_7>=PLUS && LA232_7<=PIPE)||LA232_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index232_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 232, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA235_eotS =
        "\4\uffff";
    static final String DFA235_eofS =
        "\4\uffff";
    static final String DFA235_minS =
        "\2\6\2\uffff";
    static final String DFA235_maxS =
        "\2\123\2\uffff";
    static final String DFA235_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA235_specialS =
        "\4\uffff}>";
    static final String[] DFA235_transitionS = {
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA235_eot = DFA.unpackEncodedString(DFA235_eotS);
    static final short[] DFA235_eof = DFA.unpackEncodedString(DFA235_eofS);
    static final char[] DFA235_min = DFA.unpackEncodedStringToUnsignedChars(DFA235_minS);
    static final char[] DFA235_max = DFA.unpackEncodedStringToUnsignedChars(DFA235_maxS);
    static final short[] DFA235_accept = DFA.unpackEncodedString(DFA235_acceptS);
    static final short[] DFA235_special = DFA.unpackEncodedString(DFA235_specialS);
    static final short[][] DFA235_transition;

    static {
        int numStates = DFA235_transitionS.length;
        DFA235_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA235_transition[i] = DFA.unpackEncodedString(DFA235_transitionS[i]);
        }
    }

    class DFA235 extends DFA {

        public DFA235(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 235;
            this.eot = DFA235_eot;
            this.eof = DFA235_eof;
            this.min = DFA235_min;
            this.max = DFA235_max;
            this.accept = DFA235_accept;
            this.special = DFA235_special;
            this.transition = DFA235_transition;
        }
        public String getDescription() {
            return "()* loopback of 1105:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA234_eotS =
        "\17\uffff";
    static final String DFA234_eofS =
        "\17\uffff";
    static final String DFA234_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA234_maxS =
        "\1\70\1\123\1\uffff\2\141\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA234_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA234_specialS =
        "\5\uffff\1\5\1\4\1\7\1\2\1\1\1\uffff\1\0\1\6\1\uffff\1\3}>";
    static final String[] DFA234_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\2\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\2\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\31\uffff"+
            "\2\16",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\31\uffff"+
            "\2\16"
    };

    static final short[] DFA234_eot = DFA.unpackEncodedString(DFA234_eotS);
    static final short[] DFA234_eof = DFA.unpackEncodedString(DFA234_eofS);
    static final char[] DFA234_min = DFA.unpackEncodedStringToUnsignedChars(DFA234_minS);
    static final char[] DFA234_max = DFA.unpackEncodedStringToUnsignedChars(DFA234_maxS);
    static final short[] DFA234_accept = DFA.unpackEncodedString(DFA234_acceptS);
    static final short[] DFA234_special = DFA.unpackEncodedString(DFA234_specialS);
    static final short[][] DFA234_transition;

    static {
        int numStates = DFA234_transitionS.length;
        DFA234_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA234_transition[i] = DFA.unpackEncodedString(DFA234_transitionS[i]);
        }
    }

    class DFA234 extends DFA {

        public DFA234(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 234;
            this.eot = DFA234_eot;
            this.eof = DFA234_eof;
            this.min = DFA234_min;
            this.max = DFA234_max;
            this.accept = DFA234_accept;
            this.special = DFA234_special;
            this.transition = DFA234_transition;
        }
        public String getDescription() {
            return "1107:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA234_11 = input.LA(1);

                         
                        int index234_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_11==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA234_11==WS||(LA234_11>=NL && LA234_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA234_11==COLON) ) {s = 2;}

                         
                        input.seek(index234_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA234_9 = input.LA(1);

                         
                        int index234_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_9==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA234_9==WS) ) {s = 9;}

                        else if ( ((LA234_9>=IDENT && LA234_9<=STRING)||LA234_9==LBRACE||LA234_9==COLON) ) {s = 2;}

                        else if ( ((LA234_9>=NL && LA234_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index234_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA234_8 = input.LA(1);

                         
                        int index234_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_8==WS||(LA234_8>=NL && LA234_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA234_8==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA234_8==IDENT||LA234_8==LBRACE||(LA234_8>=AND && LA234_8<=COLON)||(LA234_8>=MINUS && LA234_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index234_8);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA234_14 = input.LA(1);

                         
                        int index234_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_14==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA234_14==WS||(LA234_14>=NL && LA234_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA234_14==IDENT||LA234_14==LBRACE||(LA234_14>=AND && LA234_14<=COLON)||(LA234_14>=MINUS && LA234_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index234_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA234_6 = input.LA(1);

                         
                        int index234_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_6==WS||(LA234_6>=NL && LA234_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA234_6==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA234_6==COLON) ) {s = 2;}

                         
                        input.seek(index234_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA234_5 = input.LA(1);

                         
                        int index234_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_5==WS) ) {s = 9;}

                        else if ( (LA234_5==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( ((LA234_5>=IDENT && LA234_5<=STRING)||LA234_5==LBRACE||LA234_5==COLON) ) {s = 2;}

                        else if ( ((LA234_5>=NL && LA234_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index234_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA234_12 = input.LA(1);

                         
                        int index234_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_12==LPAREN) && (synpred20_Css3())) {s = 13;}

                        else if ( (LA234_12==WS||(LA234_12>=NL && LA234_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA234_12==IDENT||LA234_12==COMMA||LA234_12==LBRACE||LA234_12==GEN||LA234_12==COLON||(LA234_12>=PLUS && LA234_12<=PIPE)||LA234_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index234_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA234_7 = input.LA(1);

                         
                        int index234_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_7==WS||(LA234_7>=NL && LA234_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA234_7==LPAREN) && (synpred20_Css3())) {s = 13;}

                        else if ( (LA234_7==IDENT||LA234_7==COMMA||LA234_7==LBRACE||LA234_7==GEN||LA234_7==COLON||(LA234_7>=PLUS && LA234_7<=PIPE)||LA234_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index234_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 234, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA247_eotS =
        "\11\uffff";
    static final String DFA247_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA247_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA247_maxS =
        "\3\u0086\2\uffff\4\u0086";
    static final String DFA247_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA247_specialS =
        "\11\uffff}>";
    static final String[] DFA247_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\116"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\170\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3"
    };

    static final short[] DFA247_eot = DFA.unpackEncodedString(DFA247_eotS);
    static final short[] DFA247_eof = DFA.unpackEncodedString(DFA247_eofS);
    static final char[] DFA247_min = DFA.unpackEncodedStringToUnsignedChars(DFA247_minS);
    static final char[] DFA247_max = DFA.unpackEncodedStringToUnsignedChars(DFA247_maxS);
    static final short[] DFA247_accept = DFA.unpackEncodedString(DFA247_acceptS);
    static final short[] DFA247_special = DFA.unpackEncodedString(DFA247_specialS);
    static final short[][] DFA247_transition;

    static {
        int numStates = DFA247_transitionS.length;
        DFA247_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA247_transition[i] = DFA.unpackEncodedString(DFA247_transitionS[i]);
        }
    }

    class DFA247 extends DFA {

        public DFA247(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 247;
            this.eot = DFA247_eot;
            this.eof = DFA247_eof;
            this.min = DFA247_min;
            this.max = DFA247_max;
            this.accept = DFA247_accept;
            this.special = DFA247_special;
            this.transition = DFA247_transition;
        }
        public String getDescription() {
            return "376:17: synpred3_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA250_eotS =
        "\11\uffff";
    static final String DFA250_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA250_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA250_maxS =
        "\3\u0086\2\uffff\4\u0086";
    static final String DFA250_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA250_specialS =
        "\11\uffff}>";
    static final String[] DFA250_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\116"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\170\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3"
    };

    static final short[] DFA250_eot = DFA.unpackEncodedString(DFA250_eotS);
    static final short[] DFA250_eof = DFA.unpackEncodedString(DFA250_eofS);
    static final char[] DFA250_min = DFA.unpackEncodedStringToUnsignedChars(DFA250_minS);
    static final char[] DFA250_max = DFA.unpackEncodedStringToUnsignedChars(DFA250_maxS);
    static final short[] DFA250_accept = DFA.unpackEncodedString(DFA250_acceptS);
    static final short[] DFA250_special = DFA.unpackEncodedString(DFA250_specialS);
    static final short[][] DFA250_transition;

    static {
        int numStates = DFA250_transitionS.length;
        DFA250_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA250_transition[i] = DFA.unpackEncodedString(DFA250_transitionS[i]);
        }
    }

    class DFA250 extends DFA {

        public DFA250(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 250;
            this.eot = DFA250_eot;
            this.eof = DFA250_eof;
            this.min = DFA250_min;
            this.max = DFA250_max;
            this.accept = DFA250_accept;
            this.special = DFA250_special;
            this.transition = DFA250_transition;
        }
        public String getDescription() {
            return "585:3: synpred4_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA253_eotS =
        "\11\uffff";
    static final String DFA253_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA253_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA253_maxS =
        "\3\u0086\2\uffff\4\u0086";
    static final String DFA253_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA253_specialS =
        "\11\uffff}>";
    static final String[] DFA253_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\116"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\170\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\63\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\63\3"
    };

    static final short[] DFA253_eot = DFA.unpackEncodedString(DFA253_eotS);
    static final short[] DFA253_eof = DFA.unpackEncodedString(DFA253_eofS);
    static final char[] DFA253_min = DFA.unpackEncodedStringToUnsignedChars(DFA253_minS);
    static final char[] DFA253_max = DFA.unpackEncodedStringToUnsignedChars(DFA253_maxS);
    static final short[] DFA253_accept = DFA.unpackEncodedString(DFA253_acceptS);
    static final short[] DFA253_special = DFA.unpackEncodedString(DFA253_specialS);
    static final short[][] DFA253_transition;

    static {
        int numStates = DFA253_transitionS.length;
        DFA253_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA253_transition[i] = DFA.unpackEncodedString(DFA253_transitionS[i]);
        }
    }

    class DFA253 extends DFA {

        public DFA253(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 253;
            this.eot = DFA253_eot;
            this.eof = DFA253_eof;
            this.min = DFA253_min;
            this.max = DFA253_max;
            this.accept = DFA253_accept;
            this.special = DFA253_special;
            this.transition = DFA253_transition;
        }
        public String getDescription() {
            return "587:3: synpred5_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0xBFE00001D1541650L,0x0000000300E00000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0xBFE00001D1D41450L,0x0000000300EC0000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0xBFE00001D1541450L,0x0000000300E00000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0xBFE00001D1541050L,0x0000000300E00000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0xBFE00001D1541040L,0x0000000300E00000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000800012L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000800080L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000800080L,0x00000000000C0000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000800402L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem356 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem358 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem361 = new BitSet(new long[]{0x00000000008F0060L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem363 = new BitSet(new long[]{0x00000000000F0060L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem366 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem399 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem401 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem404 = new BitSet(new long[]{0x0000000000800800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem406 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_importItem410 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem412 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem415 = new BitSet(new long[]{0x00000000000F0060L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem418 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media436 = new BitSet(new long[]{0x01E00000009FA040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_media438 = new BitSet(new long[]{0x01E00000001FA040L});
    public static final BitSet FOLLOW_scss_mq_interpolation_expression_in_media493 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_media495 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_mediaQueryList_in_media529 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media558 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media560 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declaration_in_media646 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_media648 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media650 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_sass_extend_in_media673 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media675 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_sass_debug_in_media698 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media700 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_rule_in_media738 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media741 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_page_in_media762 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media765 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_fontFace_in_media786 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media789 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media810 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media813 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_media_in_media836 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_media838 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_RBRACE_in_media882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList898 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList902 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList904 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList907 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery926 = new BitSet(new long[]{0x0000000000870040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery928 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery935 = new BitSet(new long[]{0x0000000000808002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery937 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery942 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery944 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery947 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery955 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery959 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery961 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery964 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression1019 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1021 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression1024 = new BitSet(new long[]{0x0000000000B00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1026 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression1031 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1033 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_mediaExpression1036 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression1041 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body1075 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000300EC0000L});
    public static final BitSet FOLLOW_ws_in_body1077 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000300E00000L});
    public static final BitSet FOLLOW_rule_in_bodyItem1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem1114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem1126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem1138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem1150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem1162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem1176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_debug_in_bodyItem1204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule1227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule1231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule1235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule1271 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1273 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule1278 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1280 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule1295 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule1307 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule1317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1333 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1335 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1340 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1342 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document1348 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1350 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1353 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1355 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document1362 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000300EC0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1364 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000300E00000L});
    public static final BitSet FOLLOW_body_in_moz_document1369 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document1374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1415 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1417 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1420 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1422 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1427 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1429 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1436 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1438 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1458 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1460 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1465 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1468 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1471 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1475 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1493 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1505 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1508 = new BitSet(new long[]{0x0000000020800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1510 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1513 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1542 = new BitSet(new long[]{0x0000000000902040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1544 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1549 = new BitSet(new long[]{0x0000000000902000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1551 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1558 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1560 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1573 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1575 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1630 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1632 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1634 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1640 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1642 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1646 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1648 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1650 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1686 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1688 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1691 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1693 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1704 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1706 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1709 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1713 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1744 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1746 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1757 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1759 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1762 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1766 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1791 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_margin1793 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1796 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_margin1798 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1801 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_margin1803 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage2034 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage2036 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator2086 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator2097 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator2108 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property2171 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_property2175 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_property2181 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_property2184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule2228 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule2261 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule2284 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_rule2286 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule2289 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_rule2303 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule2313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations2447 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2449 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2451 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2495 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2497 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_rule_in_declarations2534 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2536 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_sass_extend_in_declarations2575 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2577 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_sass_debug_in_declarations2616 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2618 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_media_in_declarations2657 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2659 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2698 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2700 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2805 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2822 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2825 = new BitSet(new long[]{0xBFE0000000940040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2827 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2830 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2857 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_combinator_in_selector2860 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2862 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2895 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2902 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2904 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2923 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2925 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector3041 = new BitSet(new long[]{0xB000000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector3047 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_typeSelector3049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix3067 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix3071 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix3075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_extend_only_selector_in_elementSubsequent3114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent3123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent3132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent3144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent3156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId3184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId3190 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId3192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass3220 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass3222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute3294 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute3301 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3304 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute3315 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C007FL});
    public static final BitSet FOLLOW_ws_in_slAttribute3317 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_slAttribute3359 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3539 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute3558 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0040L});
    public static final BitSet FOLLOW_ws_in_slAttribute3576 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3695 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo3759 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3816 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3819 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_pseudo3821 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_pseudo3826 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo3830 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3914 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3916 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3919 = new BitSet(new long[]{0xBFE0000000B40040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3921 = new BitSet(new long[]{0xBFE0000000340040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3924 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3971 = new BitSet(new long[]{0x11E0000000441040L,0x0000000000200000L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_declaration4017 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_property_in_declaration4038 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration4051 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_declaration4053 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_declaration4056 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_prio_in_declaration4059 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_declaration4061 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue4101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue4144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4182 = new BitSet(new long[]{0xEFFDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_expressionPredicate4211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI4329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio4384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression4405 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_operator_in_expression4410 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_expression4412 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_expression4417 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_unaryOperator_in_term4442 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_term4444 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_set_in_term4468 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_term4668 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_term4676 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_term4684 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_term4692 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_term4700 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_term4708 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_term4718 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_term4730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4746 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function4748 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function4753 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function4755 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_function4765 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function4783 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function4786 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function4788 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttribute_in_function4791 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function4812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4860 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName4862 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4866 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4869 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4871 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4894 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0001L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4896 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4899 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4901 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4904 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4919 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4922 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4924 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4977 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5025 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5027 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5030 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5032 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration5035 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5064 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5066 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5069 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5071 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration5074 = new BitSet(new long[]{0x0000000000000020L,0x0000000000100000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5077 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5079 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable5117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable5149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression5173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5193 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp5207 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5209 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5212 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp5225 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5227 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5230 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5263 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp5276 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5278 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5281 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp5295 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5297 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5300 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5333 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5340 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp5354 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5356 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp5359 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp5361 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term5401 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_cp_term5601 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term5609 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_cp_term5617 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_cp_term5625 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term5633 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_cp_term5641 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term5649 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_term5661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration5692 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5694 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5696 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5699 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5701 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5704 = new BitSet(new long[]{0x0000000000800002L,0x00000000040C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5706 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5710 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5729 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5731 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5733 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5735 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5739 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5741 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5744 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call5788 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5790 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call5812 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5814 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5816 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5829 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call5832 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call5834 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call5837 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5841 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call5844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name5873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5909 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args5913 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args5921 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5924 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5966 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5970 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5980 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5983 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5989 = new BitSet(new long[]{0x0000000000800000L,0x00000000030C0000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5999 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_set_in_less_args_list6002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list6024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg6056 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_less_arg6060 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_arg6062 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg6065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded6091 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6093 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6096 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded6100 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6108 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6111 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition6141 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6143 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition6152 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6154 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition6180 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6182 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition6213 = new BitSet(new long[]{0x0008000000A00000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_ws_in_less_condition6216 = new BitSet(new long[]{0x0008000000800000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition6219 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_condition6221 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition6224 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition6253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition6279 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6281 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition6284 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6286 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition6289 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6291 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition6294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name6316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6415 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6443 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression6500 = new BitSet(new long[]{0x01E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6539 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6575 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6673 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6701 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression6754 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6793 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6829 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6923 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression6951 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_mq_interpolation_expression7016 = new BitSet(new long[]{0x01E0000000128040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7055 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7091 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7176 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var7178 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7180 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var7185 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7189 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7193 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var7196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties7240 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties7242 = new BitSet(new long[]{0x00A4000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7244 = new BitSet(new long[]{0x00A4000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties7247 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties7250 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000340EC0000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7252 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties7255 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000340E00000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties7257 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties7259 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_in_sass_extend7280 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7282 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_sass_extend7284 = new BitSet(new long[]{0x0000000000000020L,0x0000000080000000L});
    public static final BitSet FOLLOW_SASS_OPTIONAL_in_sass_extend7287 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7289 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_extend7294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector7319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_sass_debug7340 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_debug7350 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_debug7352 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_debug7354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css3475 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred1_Css3487 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css3489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQueryList_in_synpred2_Css3526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3612 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3624 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_synpred3_Css3626 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_SEMI_in_synpred3_Css3636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3640 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32413 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32425 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_synpred4_Css32427 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_SEMI_in_synpred4_Css32437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32441 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32461 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32473 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_synpred5_Css32475 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32489 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred6_Css32521 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_LBRACE_in_synpred6_Css32531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred7_Css32735 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_RBRACE_in_synpred7_Css32741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_Css32787 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred8_Css32799 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred8_Css32801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred9_Css32899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred10_Css32920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred11_Css33029 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred11_Css33038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred12_Css33992 = new BitSet(new long[]{0xFFFFFFFFFFEFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000007FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred12_Css34010 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Css34012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred13_Css34098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred14_Css35337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred15_Css36410 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred15_Css36412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred16_Css36534 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred16_Css36536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred17_Css36668 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred17_Css36670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred18_Css36788 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred18_Css36790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred19_Css36918 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred19_Css36920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred20_Css37050 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred20_Css37052 = new BitSet(new long[]{0x0000000000000002L});

}