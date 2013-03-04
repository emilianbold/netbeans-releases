// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-04 15:09:10

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "SASS_EXTEND_ONLY_SELECTOR", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "SASS_EXTEND", "SASS_OPTIONAL", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
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
    public static final int HEXCHAR=96;
    public static final int NONASCII=97;
    public static final int UNICODE=98;
    public static final int ESCAPE=99;
    public static final int NMSTART=100;
    public static final int NMCHAR=101;
    public static final int URL=102;
    public static final int A=103;
    public static final int B=104;
    public static final int C=105;
    public static final int D=106;
    public static final int E=107;
    public static final int F=108;
    public static final int G=109;
    public static final int H=110;
    public static final int I=111;
    public static final int J=112;
    public static final int K=113;
    public static final int L=114;
    public static final int M=115;
    public static final int N=116;
    public static final int O=117;
    public static final int P=118;
    public static final int Q=119;
    public static final int R=120;
    public static final int S=121;
    public static final int T=122;
    public static final int U=123;
    public static final int V=124;
    public static final int W=125;
    public static final int X=126;
    public static final int Y=127;
    public static final int Z=128;
    public static final int CDO=129;
    public static final int CDC=130;
    public static final int INVALID=131;
    public static final int LINE_COMMENT=132;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "property", "syncTo_SEMI", "less_mixin_guarded", 
        "namespacePrefix", "namespaces", "namespace", "esPred", "elementSubsequent", 
        "cp_mixin_name", "syncTo_RBRACE", "bodyItem", "webkitKeyframes", 
        "fnAttribute", "counterStyle", "cssClass", "media", "syncToFollow", 
        "synpred20_Css3", "fnAttributeName", "synpred5_Css3", "simpleSelectorSequence", 
        "less_args_list", "namespacePrefixName", "selector", "cp_mixin_call", 
        "cp_additionExp", "propertyValue", "cp_mixin_declaration", "operator", 
        "cssId", "synpred4_Css3", "cp_expression", "expressionPredicate", 
        "synpred15_Css3", "synpred13_Css3", "less_condition_operator", "charSetValue", 
        "synpred18_Css3", "mediaType", "mediaExpression", "synpred2_Css3", 
        "synpred17_Css3", "mediaQueryList", "pseudoPage", "cp_term", "generic_at_rule", 
        "moz_document_function", "webkitKeyframesBlock", "synpred10_Css3", 
        "selectorsGroup", "charSet", "sass_extend_only_selector", "resourceIdentifier", 
        "cp_variable", "page", "term", "unaryOperator", "slAttributeName", 
        "synpred14_Css3", "imports", "scss_nested_properties", "prio", "body", 
        "scss_declaration_interpolation_expression", "elementName", "moz_document", 
        "functionName", "synpred3_Css3", "styleSheet", "synpred11_Css3", 
        "importItem", "slAttribute", "fnAttributeValue", "fontFace", "margin", 
        "webkitKeyframeSelectors", "synpred12_Css3", "margin_sym", "declaration", 
        "mediaQuery", "less_arg", "less_condition", "sass_extend", "synpred8_Css3", 
        "scss_interpolation_expression_var", "synpred19_Css3", "typeSelector", 
        "cp_atomExp", "synpred1_Css3", "synpred16_Css3", "declarations", 
        "hexColor", "synpred6_Css3", "syncToDeclarationsRule", "less_fn_name", 
        "cp_variable_declaration", "cp_multiplyExp", "scss_mq_interpolation_expression", 
        "slAttributeValue", "cp_mixin_call_args", "pseudo", "combinator", 
        "atRuleId", "ws", "mediaFeature", "function", "vendorAtRule", "less_function_in_condition", 
        "scss_selector_interpolation_expression", "expression", "synpred7_Css3", 
        "mediaQueryOperator", "synpred9_Css3", "rule"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, true, false, 
            false, false, false, true, false, false, true, false, true, 
            false, true, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, true, false, false, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
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

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||LA6_0==GEN||LA6_0==COLON||LA6_0==AT_IDENT||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||(LA6_0>=MINUS && LA6_0<=PIPE)||LA6_0==LESS_AND||(LA6_0>=SASS_VAR && LA6_0<=SASS_INCLUDE)) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*
            try { dbg.enterSubRule(34);

            loop34:
            do {
                int alt34=8;
                try { dbg.enterDecision(34, decisionCanBacktrack[34]);

                try {
                    isCyclicDecision = true;
                    alt34 = dfa34.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(34);}

                switch (alt34) {
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:19: rule ( ws )?
            	    {
            	    dbg.location(379,19);
            	    pushFollow(FOLLOW_rule_in_media713);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(379,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:25: ws
            	            {
            	            dbg.location(379,25);
            	            pushFollow(FOLLOW_ws_in_media716);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:19: page ( ws )?
            	    {
            	    dbg.location(380,19);
            	    pushFollow(FOLLOW_page_in_media737);
            	    page();

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
            	            pushFollow(FOLLOW_ws_in_media740);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:19: fontFace ( ws )?
            	    {
            	    dbg.location(381,19);
            	    pushFollow(FOLLOW_fontFace_in_media761);
            	    fontFace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(381,29);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:29: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:29: ws
            	            {
            	            dbg.location(381,29);
            	            pushFollow(FOLLOW_ws_in_media764);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:19: vendorAtRule ( ws )?
            	    {
            	    dbg.location(382,19);
            	    pushFollow(FOLLOW_vendorAtRule_in_media785);
            	    vendorAtRule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(382,33);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:33: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:33: ws
            	            {
            	            dbg.location(382,33);
            	            pushFollow(FOLLOW_ws_in_media788);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:19: {...}? media ( ws )?
            	    {
            	    dbg.location(383,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(383,37);
            	    pushFollow(FOLLOW_media_in_media811);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(383,43);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:43: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:43: ws
            	            {
            	            dbg.location(383,43);
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

            	default :
            	    break loop34;
                }
            } while (true);
            } finally {dbg.exitSubRule(34);}

            dbg.location(386,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media857); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(387, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(389, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(390,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt37=2;
            try { dbg.enterSubRule(37);
            try { dbg.enterDecision(37, decisionCanBacktrack[37]);

            int LA37_0 = input.LA(1);

            if ( (LA37_0==IDENT||(LA37_0>=ONLY && LA37_0<=LPAREN)) ) {
                alt37=1;
            }
            } finally {dbg.exitDecision(37);}

            switch (alt37) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(390,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList873);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(390,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(36);

                    loop36:
                    do {
                        int alt36=2;
                        try { dbg.enterDecision(36, decisionCanBacktrack[36]);

                        int LA36_0 = input.LA(1);

                        if ( (LA36_0==COMMA) ) {
                            alt36=1;
                        }


                        } finally {dbg.exitDecision(36);}

                        switch (alt36) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(390,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList877); if (state.failed) return ;
                    	    dbg.location(390,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:25: ws
                    	            {
                    	            dbg.location(390,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList879);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(35);}

                    	    dbg.location(390,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList882);
                    	    mediaQuery();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(36);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(37);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(391, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(393, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt45=2;
            try { dbg.enterDecision(45, decisionCanBacktrack[45]);

            int LA45_0 = input.LA(1);

            if ( (LA45_0==IDENT||(LA45_0>=ONLY && LA45_0<=GEN)) ) {
                alt45=1;
            }
            else if ( (LA45_0==LPAREN) ) {
                alt45=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(394,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:4: ( mediaQueryOperator ( ws )? )?
                    int alt39=2;
                    try { dbg.enterSubRule(39);
                    try { dbg.enterDecision(39, decisionCanBacktrack[39]);

                    int LA39_0 = input.LA(1);

                    if ( ((LA39_0>=ONLY && LA39_0<=NOT)) ) {
                        alt39=1;
                    }
                    } finally {dbg.exitDecision(39);}

                    switch (alt39) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(394,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery901);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(394,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:24: ( ws )?
                            int alt38=2;
                            try { dbg.enterSubRule(38);
                            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                            int LA38_0 = input.LA(1);

                            if ( (LA38_0==WS||(LA38_0>=NL && LA38_0<=COMMENT)) ) {
                                alt38=1;
                            }
                            } finally {dbg.exitDecision(38);}

                            switch (alt38) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:24: ws
                                    {
                                    dbg.location(394,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery903);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(38);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(39);}

                    dbg.location(394,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery910);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(394,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:42: ws
                            {
                            dbg.location(394,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery912);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(40);}

                    dbg.location(394,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(42);

                    loop42:
                    do {
                        int alt42=2;
                        try { dbg.enterDecision(42, decisionCanBacktrack[42]);

                        int LA42_0 = input.LA(1);

                        if ( (LA42_0==AND) ) {
                            alt42=1;
                        }


                        } finally {dbg.exitDecision(42);}

                        switch (alt42) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(394,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery917); if (state.failed) return ;
                    	    dbg.location(394,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:52: ws
                    	            {
                    	            dbg.location(394,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery919);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(41);}

                    	    dbg.location(394,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery922);
                    	    mediaExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(42);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(395,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery930);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(395,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:20: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(44);

                    loop44:
                    do {
                        int alt44=2;
                        try { dbg.enterDecision(44, decisionCanBacktrack[44]);

                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==AND) ) {
                            alt44=1;
                        }


                        } finally {dbg.exitDecision(44);}

                        switch (alt44) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(395,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery934); if (state.failed) return ;
                    	    dbg.location(395,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:26: ( ws )?
                    	    int alt43=2;
                    	    try { dbg.enterSubRule(43);
                    	    try { dbg.enterDecision(43, decisionCanBacktrack[43]);

                    	    int LA43_0 = input.LA(1);

                    	    if ( (LA43_0==WS||(LA43_0>=NL && LA43_0<=COMMENT)) ) {
                    	        alt43=1;
                    	    }
                    	    } finally {dbg.exitDecision(43);}

                    	    switch (alt43) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:26: ws
                    	            {
                    	            dbg.location(395,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery936);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(43);}

                    	    dbg.location(395,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery939);
                    	    mediaExpression();

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
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(396, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(398, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(399,3);
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
        dbg.location(400, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(402, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(403,2);
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
        dbg.location(404, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(406, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(407,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression994); if (state.failed) return ;
            dbg.location(407,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:14: ws
                    {
                    dbg.location(407,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression996);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(46);}

            dbg.location(407,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression999);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(407,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:31: ws
                    {
                    dbg.location(407,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression1001);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(407,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:35: ( COLON ( ws )? expression )?
            int alt49=2;
            try { dbg.enterSubRule(49);
            try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            int LA49_0 = input.LA(1);

            if ( (LA49_0==COLON) ) {
                alt49=1;
            }
            } finally {dbg.exitDecision(49);}

            switch (alt49) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:37: COLON ( ws )? expression
                    {
                    dbg.location(407,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression1006); if (state.failed) return ;
                    dbg.location(407,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:43: ws
                            {
                            dbg.location(407,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression1008);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(48);}

                    dbg.location(407,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression1011);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(49);}

            dbg.location(407,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression1016); if (state.failed) return ;
            dbg.location(407,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:68: ws
                    {
                    dbg.location(407,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression1018);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(408, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(410, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:4: IDENT
            {
            dbg.location(411,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature1034); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(414, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: ( bodyItem ( ws )? )+
            {
            dbg.location(415,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: ( bodyItem ( ws )? )+
            int cnt52=0;
            try { dbg.enterSubRule(52);

            loop52:
            do {
                int alt52=2;
                try { dbg.enterDecision(52, decisionCanBacktrack[52]);

                int LA52_0 = input.LA(1);

                if ( (LA52_0==IDENT||LA52_0==MEDIA_SYM||LA52_0==GEN||LA52_0==COLON||LA52_0==AT_IDENT||LA52_0==MOZ_DOCUMENT_SYM||LA52_0==WEBKIT_KEYFRAMES_SYM||(LA52_0>=PAGE_SYM && LA52_0<=FONT_FACE_SYM)||(LA52_0>=MINUS && LA52_0<=PIPE)||LA52_0==LESS_AND||(LA52_0>=SASS_VAR && LA52_0<=SASS_INCLUDE)) ) {
                    alt52=1;
                }


                } finally {dbg.exitDecision(52);}

                switch (alt52) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:4: bodyItem ( ws )?
            	    {
            	    dbg.location(415,4);
            	    pushFollow(FOLLOW_bodyItem_in_body1050);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(415,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:13: ws
            	            {
            	            dbg.location(415,13);
            	            pushFollow(FOLLOW_ws_in_body1052);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(51);}


            	    }
            	    break;

            	default :
            	    if ( cnt52 >= 1 ) break loop52;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(52, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt52++;
            } while (true);
            } finally {dbg.exitSubRule(52);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(416, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(418, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call )
            int alt53=8;
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            try {
                isCyclicDecision = true;
                alt53 = dfa53.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:6: rule
                    {
                    dbg.location(420,6);
                    pushFollow(FOLLOW_rule_in_bodyItem1077);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:11: media
                    {
                    dbg.location(421,11);
                    pushFollow(FOLLOW_media_in_bodyItem1089);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:11: page
                    {
                    dbg.location(422,11);
                    pushFollow(FOLLOW_page_in_bodyItem1101);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:11: counterStyle
                    {
                    dbg.location(423,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem1113);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:11: fontFace
                    {
                    dbg.location(424,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem1125);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:11: vendorAtRule
                    {
                    dbg.location(425,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem1137);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:11: {...}? cp_variable_declaration
                    {
                    dbg.location(426,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(426,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem1151);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:11: {...}? cp_mixin_call
                    {
                    dbg.location(427,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(427,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem1165);
                    cp_mixin_call();

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
        dbg.location(428, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt54=3;
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt54=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt54=2;
                }
                break;
            case AT_IDENT:
                {
                alt54=3;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:3: moz_document
                    {
                    dbg.location(437,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule1188);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:18: webkitKeyframes
                    {
                    dbg.location(437,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule1192);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:36: generic_at_rule
                    {
                    dbg.location(437,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule1196);
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
        dbg.location(437, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(439, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(440,2);
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
        dbg.location(442, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(444, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(445,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule1232); if (state.failed) return ;
            dbg.location(445,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:16: WS
            	    {
            	    dbg.location(445,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule1234); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);
            } finally {dbg.exitSubRule(55);}

            dbg.location(445,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:20: ( atRuleId ( WS )* )?
            int alt57=2;
            try { dbg.enterSubRule(57);
            try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            int LA57_0 = input.LA(1);

            if ( ((LA57_0>=IDENT && LA57_0<=STRING)) ) {
                alt57=1;
            }
            } finally {dbg.exitDecision(57);}

            switch (alt57) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:22: atRuleId ( WS )*
                    {
                    dbg.location(445,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule1239);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(445,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:31: WS
                    	    {
                    	    dbg.location(445,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule1241); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop56;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(56);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(446,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule1256); if (state.failed) return ;
            dbg.location(447,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule1268);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(448,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule1278); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(449, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(450, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(452,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1294); if (state.failed) return ;
            dbg.location(452,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:19: ws
                    {
                    dbg.location(452,19);
                    pushFollow(FOLLOW_ws_in_moz_document1296);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}

            dbg.location(452,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:25: moz_document_function ( ws )?
            {
            dbg.location(452,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document1301);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(452,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:47: ws
                    {
                    dbg.location(452,47);
                    pushFollow(FOLLOW_ws_in_moz_document1303);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}


            }

            dbg.location(452,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(62);

            loop62:
            do {
                int alt62=2;
                try { dbg.enterDecision(62, decisionCanBacktrack[62]);

                int LA62_0 = input.LA(1);

                if ( (LA62_0==COMMA) ) {
                    alt62=1;
                }


                } finally {dbg.exitDecision(62);}

                switch (alt62) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(452,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document1309); if (state.failed) return ;
            	    dbg.location(452,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:60: ws
            	            {
            	            dbg.location(452,60);
            	            pushFollow(FOLLOW_ws_in_moz_document1311);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(60);}

            	    dbg.location(452,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document1314);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(452,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:86: ws
            	            {
            	            dbg.location(452,86);
            	            pushFollow(FOLLOW_ws_in_moz_document1316);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}


            	    }
            	    break;

            	default :
            	    break loop62;
                }
            } while (true);
            } finally {dbg.exitSubRule(62);}

            dbg.location(453,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document1323); if (state.failed) return ;
            dbg.location(453,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:9: ws
                    {
                    dbg.location(453,9);
                    pushFollow(FOLLOW_ws_in_moz_document1325);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(454,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:3: ( body )?
            int alt64=2;
            try { dbg.enterSubRule(64);
            try { dbg.enterDecision(64, decisionCanBacktrack[64]);

            int LA64_0 = input.LA(1);

            if ( (LA64_0==IDENT||LA64_0==MEDIA_SYM||LA64_0==GEN||LA64_0==COLON||LA64_0==AT_IDENT||LA64_0==MOZ_DOCUMENT_SYM||LA64_0==WEBKIT_KEYFRAMES_SYM||(LA64_0>=PAGE_SYM && LA64_0<=FONT_FACE_SYM)||(LA64_0>=MINUS && LA64_0<=PIPE)||LA64_0==LESS_AND||(LA64_0>=SASS_VAR && LA64_0<=SASS_INCLUDE)) ) {
                alt64=1;
            }
            } finally {dbg.exitDecision(64);}

            switch (alt64) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:3: body
                    {
                    dbg.location(454,3);
                    pushFollow(FOLLOW_body_in_moz_document1330);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(455,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document1335); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(456, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(458, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(459,2);
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
        dbg.location(461, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(464, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(466,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1376); if (state.failed) return ;
            dbg.location(466,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:23: ws
                    {
                    dbg.location(466,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1378);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(466,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1381);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(466,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:36: ws
                    {
                    dbg.location(466,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1383);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(66);}

            dbg.location(467,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1388); if (state.failed) return ;
            dbg.location(467,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:9: ws
                    {
                    dbg.location(467,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1390);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}

            dbg.location(468,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(69);

            loop69:
            do {
                int alt69=2;
                try { dbg.enterDecision(69, decisionCanBacktrack[69]);

                int LA69_0 = input.LA(1);

                if ( (LA69_0==IDENT||LA69_0==PERCENTAGE) ) {
                    alt69=1;
                }


                } finally {dbg.exitDecision(69);}

                switch (alt69) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(468,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1397);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(468,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:26: ws
            	            {
            	            dbg.location(468,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1399);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(68);}


            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);
            } finally {dbg.exitSubRule(69);}

            dbg.location(469,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1406); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(472, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(474,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1419);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(474,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:26: ws
                    {
                    dbg.location(474,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1421);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(476,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1426); if (state.failed) return ;
            dbg.location(476,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:10: ws
                    {
                    dbg.location(476,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1429);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(476,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1432);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(477,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1436);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(478,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1439); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(479, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(481, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(483,2);
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

            dbg.location(483,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(483,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:27: ws
            	            {
            	            dbg.location(483,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1466);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(483,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1469); if (state.failed) return ;
            	    dbg.location(483,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:37: ws
            	            {
            	            dbg.location(483,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1471);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(73);}

            	    dbg.location(483,41);
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
            	    break loop74;
                }
            } while (true);
            } finally {dbg.exitSubRule(74);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(484, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(486, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(487,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1503); if (state.failed) return ;
            dbg.location(487,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ws
                    {
                    dbg.location(487,16);
                    pushFollow(FOLLOW_ws_in_page1505);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(75);}

            dbg.location(487,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:20: ( IDENT ( ws )? )?
            int alt77=2;
            try { dbg.enterSubRule(77);
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            int LA77_0 = input.LA(1);

            if ( (LA77_0==IDENT) ) {
                alt77=1;
            }
            } finally {dbg.exitDecision(77);}

            switch (alt77) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:22: IDENT ( ws )?
                    {
                    dbg.location(487,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1510); if (state.failed) return ;
                    dbg.location(487,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:28: ws
                            {
                            dbg.location(487,28);
                            pushFollow(FOLLOW_ws_in_page1512);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(76);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(487,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:35: ( pseudoPage ( ws )? )?
            int alt79=2;
            try { dbg.enterSubRule(79);
            try { dbg.enterDecision(79, decisionCanBacktrack[79]);

            int LA79_0 = input.LA(1);

            if ( (LA79_0==COLON) ) {
                alt79=1;
            }
            } finally {dbg.exitDecision(79);}

            switch (alt79) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:36: pseudoPage ( ws )?
                    {
                    dbg.location(487,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1519);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(487,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:47: ws
                            {
                            dbg.location(487,47);
                            pushFollow(FOLLOW_ws_in_page1521);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(78);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(488,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1534); if (state.failed) return ;
            dbg.location(488,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:16: ws
                    {
                    dbg.location(488,16);
                    pushFollow(FOLLOW_ws_in_page1536);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(492,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:13: ( declaration | margin ( ws )? )?
            int alt82=3;
            try { dbg.enterSubRule(82);
            try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            int LA82_0 = input.LA(1);

            if ( (LA82_0==IDENT||LA82_0==MEDIA_SYM||LA82_0==GEN||LA82_0==AT_IDENT||(LA82_0>=MINUS && LA82_0<=DOT)||LA82_0==STAR||LA82_0==SASS_VAR) ) {
                alt82=1;
            }
            else if ( ((LA82_0>=TOPLEFTCORNER_SYM && LA82_0<=RIGHTBOTTOM_SYM)) ) {
                alt82=2;
            }
            } finally {dbg.exitDecision(82);}

            switch (alt82) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:14: declaration
                    {
                    dbg.location(492,14);
                    pushFollow(FOLLOW_declaration_in_page1591);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:26: margin ( ws )?
                    {
                    dbg.location(492,26);
                    pushFollow(FOLLOW_margin_in_page1593);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(492,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:33: ws
                            {
                            dbg.location(492,33);
                            pushFollow(FOLLOW_ws_in_page1595);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(81);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(492,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(492,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1601); if (state.failed) return ;
            	    dbg.location(492,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:45: ws
            	            {
            	            dbg.location(492,45);
            	            pushFollow(FOLLOW_ws_in_page1603);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(83);}

            	    dbg.location(492,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:49: ( declaration | margin ( ws )? )?
            	    int alt85=3;
            	    try { dbg.enterSubRule(85);
            	    try { dbg.enterDecision(85, decisionCanBacktrack[85]);

            	    int LA85_0 = input.LA(1);

            	    if ( (LA85_0==IDENT||LA85_0==MEDIA_SYM||LA85_0==GEN||LA85_0==AT_IDENT||(LA85_0>=MINUS && LA85_0<=DOT)||LA85_0==STAR||LA85_0==SASS_VAR) ) {
            	        alt85=1;
            	    }
            	    else if ( ((LA85_0>=TOPLEFTCORNER_SYM && LA85_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt85=2;
            	    }
            	    } finally {dbg.exitDecision(85);}

            	    switch (alt85) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:50: declaration
            	            {
            	            dbg.location(492,50);
            	            pushFollow(FOLLOW_declaration_in_page1607);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:62: margin ( ws )?
            	            {
            	            dbg.location(492,62);
            	            pushFollow(FOLLOW_margin_in_page1609);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(492,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:69: ws
            	                    {
            	                    dbg.location(492,69);
            	                    pushFollow(FOLLOW_ws_in_page1611);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(84);}


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

            dbg.location(493,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1626); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(494, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(496, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(497,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1647); if (state.failed) return ;
            dbg.location(497,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:25: ws
                    {
                    dbg.location(497,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1649);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(497,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1652); if (state.failed) return ;
            dbg.location(497,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:35: ws
                    {
                    dbg.location(497,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1654);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(88);}

            dbg.location(498,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1665); if (state.failed) return ;
            dbg.location(498,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:16: ws
                    {
                    dbg.location(498,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1667);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(498,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1670);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(499,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1674);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(500,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1684); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(501, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(503, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(504,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1705); if (state.failed) return ;
            dbg.location(504,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:21: ws
                    {
                    dbg.location(504,21);
                    pushFollow(FOLLOW_ws_in_fontFace1707);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(505,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1718); if (state.failed) return ;
            dbg.location(505,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:16: ws
                    {
                    dbg.location(505,16);
                    pushFollow(FOLLOW_ws_in_fontFace1720);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(505,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1723);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(506,3);
            pushFollow(FOLLOW_declarations_in_fontFace1727);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(507,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1737); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(508, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(510, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(511,4);
            pushFollow(FOLLOW_margin_sym_in_margin1752);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(511,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:15: ws
                    {
                    dbg.location(511,15);
                    pushFollow(FOLLOW_ws_in_margin1754);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(511,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1757); if (state.failed) return ;
            dbg.location(511,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:26: ws
                    {
                    dbg.location(511,26);
                    pushFollow(FOLLOW_ws_in_margin1759);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(511,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1762);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(511,53);
            pushFollow(FOLLOW_declarations_in_margin1764);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(511,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1766); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(512, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(514, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(515,2);
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
        dbg.location(532, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(534, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:7: COLON IDENT
            {
            dbg.location(535,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1995); if (state.failed) return ;
            dbg.location(535,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1997); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(538, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(539,5);
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
        dbg.location(541, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(543, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt97=4;
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt97=1;
                }
                break;
            case GREATER:
                {
                alt97=2;
                }
                break;
            case TILDE:
                {
                alt97=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:7: PLUS ( ws )?
                    {
                    dbg.location(544,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator2047); if (state.failed) return ;
                    dbg.location(544,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:12: ws
                            {
                            dbg.location(544,12);
                            pushFollow(FOLLOW_ws_in_combinator2049);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(94);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:7: GREATER ( ws )?
                    {
                    dbg.location(545,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator2058); if (state.failed) return ;
                    dbg.location(545,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:15: ws
                            {
                            dbg.location(545,15);
                            pushFollow(FOLLOW_ws_in_combinator2060);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(95);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:7: TILDE ( ws )?
                    {
                    dbg.location(546,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator2069); if (state.failed) return ;
                    dbg.location(546,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:13: ws
                            {
                            dbg.location(546,13);
                            pushFollow(FOLLOW_ws_in_combinator2071);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(96);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:5: 
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
        dbg.location(548, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(550, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(551,5);
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
        dbg.location(553, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(555, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(556,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:7: ( IDENT | GEN | {...}? cp_variable )
            int alt98=3;
            try { dbg.enterSubRule(98);
            try { dbg.enterDecision(98, decisionCanBacktrack[98]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt98=1;
                }
                break;
            case GEN:
                {
                alt98=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
            case SASS_VAR:
                {
                alt98=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:8: IDENT
                    {
                    dbg.location(556,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property2132); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:16: GEN
                    {
                    dbg.location(556,16);
                    match(input,GEN,FOLLOW_GEN_in_property2136); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:22: {...}? cp_variable
                    {
                    dbg.location(556,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(556,51);
                    pushFollow(FOLLOW_cp_variable_in_property2142);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}

            dbg.location(556,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:64: ( ws )?
            int alt99=2;
            try { dbg.enterSubRule(99);
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            int LA99_0 = input.LA(1);

            if ( (LA99_0==WS||(LA99_0>=NL && LA99_0<=COMMENT)) ) {
                alt99=1;
            }
            } finally {dbg.exitDecision(99);}

            switch (alt99) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:64: ws
                    {
                    dbg.location(556,64);
                    pushFollow(FOLLOW_ws_in_property2145);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(99);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(557, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(559, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(560,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(561,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(561,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(561,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule2189);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:13: ( selectorsGroup )
                    {
                    dbg.location(563,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:15: selectorsGroup
                    {
                    dbg.location(563,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule2222);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}

            dbg.location(566,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule2245); if (state.failed) return ;
            dbg.location(566,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:16: ws
                    {
                    dbg.location(566,16);
                    pushFollow(FOLLOW_ws_in_rule2247);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(101);}

            dbg.location(566,20);
            pushFollow(FOLLOW_syncToFollow_in_rule2250);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(567,13);
            pushFollow(FOLLOW_declarations_in_rule2264);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(568,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule2274); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(569, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(577, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(579,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*
            try { dbg.enterSubRule(108);

            loop108:
            do {
                int alt108=7;
                try { dbg.enterDecision(108, decisionCanBacktrack[108]);

                try {
                    isCyclicDecision = true;
                    alt108 = dfa108.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(108);}

                switch (alt108) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(583,120);
            	    pushFollow(FOLLOW_declaration_in_declarations2408);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(583,132);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2410); if (state.failed) return ;
            	    dbg.location(583,137);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:137: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:137: ws
            	            {
            	            dbg.location(583,137);
            	            pushFollow(FOLLOW_ws_in_declarations2412);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(102);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(585,122);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2456);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(585,145);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:145: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:145: ws
            	            {
            	            dbg.location(585,145);
            	            pushFollow(FOLLOW_ws_in_declarations2458);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(103);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(587,50);
            	    pushFollow(FOLLOW_rule_in_declarations2495);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(587,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:55: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:55: ws
            	            {
            	            dbg.location(587,55);
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
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:17: {...}? sass_extend ( ws )?
            	    {
            	    dbg.location(589,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(589,35);
            	    pushFollow(FOLLOW_sass_extend_in_declarations2536);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(589,47);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:47: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:47: ws
            	            {
            	            dbg.location(589,47);
            	            pushFollow(FOLLOW_ws_in_declarations2538);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(105);}


            	    }
            	    break;
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:17: {...}? media ( ws )?
            	    {
            	    dbg.location(591,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(591,46);
            	    pushFollow(FOLLOW_media_in_declarations2577);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(591,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:52: ws
            	            {
            	            dbg.location(591,52);
            	            pushFollow(FOLLOW_ws_in_declarations2579);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(106);}


            	    }
            	    break;
            	case 6 :
            	    dbg.enterAlt(6);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(593,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(593,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2618);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(593,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:60: ws
            	            {
            	            dbg.location(593,60);
            	            pushFollow(FOLLOW_ws_in_declarations2620);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(107);}


            	    }
            	    break;

            	default :
            	    break loop108;
                }
            } while (true);
            } finally {dbg.exitSubRule(108);}

            dbg.location(597,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt109=2;
            try { dbg.enterSubRule(109);
            try { dbg.enterDecision(109, decisionCanBacktrack[109]);

            int LA109_0 = input.LA(1);

            if ( (LA109_0==STAR) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==HASH_SYMBOL) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==IDENT) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==MINUS||(LA109_0>=HASH && LA109_0<=DOT)) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==GEN) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==MEDIA_SYM||LA109_0==AT_IDENT) && (synpred7_Css3())) {
                alt109=1;
            }
            else if ( (LA109_0==SASS_VAR) && (synpred7_Css3())) {
                alt109=1;
            }
            } finally {dbg.exitDecision(109);}

            switch (alt109) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(597,36);
                    pushFollow(FOLLOW_declaration_in_declarations2664);
                    declaration();

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
        dbg.location(598, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt113=2;
            try { dbg.enterDecision(113, decisionCanBacktrack[113]);

            try {
                isCyclicDecision = true;
                alt113 = dfa113.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(113);}

            switch (alt113) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(603,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2725);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(603,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:99: ws
                            {
                            dbg.location(603,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup2727);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(110);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(605,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup2742);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(605,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(112);

                    loop112:
                    do {
                        int alt112=2;
                        try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                        int LA112_0 = input.LA(1);

                        if ( (LA112_0==COMMA) ) {
                            alt112=1;
                        }


                        } finally {dbg.exitDecision(112);}

                        switch (alt112) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(605,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2745); if (state.failed) return ;
                    	    dbg.location(605,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:25: ws
                    	            {
                    	            dbg.location(605,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup2747);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(111);}

                    	    dbg.location(605,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup2750);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop112;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(112);}


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
        dbg.location(606, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(608, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(609,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2777);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(609,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(114);

            loop114:
            do {
                int alt114=2;
                try { dbg.enterDecision(114, decisionCanBacktrack[114]);

                int LA114_0 = input.LA(1);

                if ( (LA114_0==IDENT||LA114_0==GEN||LA114_0==COLON||(LA114_0>=PLUS && LA114_0<=TILDE)||(LA114_0>=HASH_SYMBOL && LA114_0<=PIPE)||LA114_0==LESS_AND) ) {
                    alt114=1;
                }


                } finally {dbg.exitDecision(114);}

                switch (alt114) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(609,31);
            	    pushFollow(FOLLOW_combinator_in_selector2780);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(609,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2782);
            	    simpleSelectorSequence();

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
            dbg.exitRule(getGrammarFileName(), "selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "simpleSelectorSequence"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(613, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt119=2;
            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

            int LA119_0 = input.LA(1);

            if ( (LA119_0==IDENT||LA119_0==GEN||(LA119_0>=STAR && LA119_0<=PIPE)||LA119_0==LESS_AND) ) {
                alt119=1;
            }
            else if ( (LA119_0==COLON||(LA119_0>=HASH_SYMBOL && LA119_0<=SASS_EXTEND_ONLY_SELECTOR)) ) {
                alt119=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(616,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(616,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2815);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(616,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(116);

                    loop116:
                    do {
                        int alt116=2;
                        try { dbg.enterDecision(116, decisionCanBacktrack[116]);

                        try {
                            isCyclicDecision = true;
                            alt116 = dfa116.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(116);}

                        switch (alt116) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(616,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2822);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(616,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: ws
                    	            {
                    	            dbg.location(616,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2824);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(115);}


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
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(618,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(618,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt118=0;
                    try { dbg.enterSubRule(118);

                    loop118:
                    do {
                        int alt118=2;
                        try { dbg.enterDecision(118, decisionCanBacktrack[118]);

                        switch ( input.LA(1) ) {
                        case SASS_EXTEND_ONLY_SELECTOR:
                            {
                            int LA118_2 = input.LA(2);

                            if ( ((synpred10_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                                alt118=1;
                            }


                            }
                            break;
                        case HASH:
                            {
                            int LA118_3 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt118=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA118_4 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt118=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA118_5 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt118=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA118_6 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt118=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA118_7 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt118=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(118);}

                        switch (alt118) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(618,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2843);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(618,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:33: ws
                    	            {
                    	            dbg.location(618,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2845);
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
                    	    if ( cnt118 >= 1 ) break loop118;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(118, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt118++;
                    } while (true);
                    } finally {dbg.exitSubRule(118);}


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
        dbg.location(619, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(626, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(627,5);
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
        dbg.location(628, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(630, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(632,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt120=2;
            try { dbg.enterSubRule(120);
            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

            int LA120_0 = input.LA(1);

            if ( (LA120_0==IDENT) ) {
                int LA120_1 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt120=1;
                }
            }
            else if ( (LA120_0==STAR) ) {
                int LA120_2 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt120=1;
                }
            }
            else if ( (LA120_0==PIPE) && (synpred11_Css3())) {
                alt120=1;
            }
            } finally {dbg.exitDecision(120);}

            switch (alt120) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(632,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2961);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(120);}

            dbg.location(632,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:51: elementName ( ws )?
            {
            dbg.location(632,51);
            pushFollow(FOLLOW_elementName_in_typeSelector2967);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(632,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:63: ws
                    {
                    dbg.location(632,63);
                    pushFollow(FOLLOW_ws_in_typeSelector2969);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(121);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(633, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(635, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(636,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:5: ( namespacePrefixName | STAR )?
            int alt122=3;
            try { dbg.enterSubRule(122);
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            int LA122_0 = input.LA(1);

            if ( (LA122_0==IDENT) ) {
                alt122=1;
            }
            else if ( (LA122_0==STAR) ) {
                alt122=2;
            }
            } finally {dbg.exitDecision(122);}

            switch (alt122) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:7: namespacePrefixName
                    {
                    dbg.location(636,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2987);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:29: STAR
                    {
                    dbg.location(636,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2991); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}

            dbg.location(636,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2995); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "namespacePrefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespacePrefix"


    // $ANTLR start "elementSubsequent"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:1: elementSubsequent : ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(640, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:5: ( ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(642,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            int alt123=5;
            try { dbg.enterSubRule(123);
            try { dbg.enterDecision(123, decisionCanBacktrack[123]);

            switch ( input.LA(1) ) {
            case SASS_EXTEND_ONLY_SELECTOR:
                {
                alt123=1;
                }
                break;
            case HASH_SYMBOL:
            case HASH:
                {
                alt123=2;
                }
                break;
            case DOT:
                {
                alt123=3;
                }
                break;
            case LBRACKET:
                {
                alt123=4;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt123=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 123, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:9: {...}? sass_extend_only_selector
                    {
                    dbg.location(643,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "elementSubsequent", "isScssSource()");
                    }
                    dbg.location(643,27);
                    pushFollow(FOLLOW_sass_extend_only_selector_in_elementSubsequent3034);
                    sass_extend_only_selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:8: cssId
                    {
                    dbg.location(644,8);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent3043);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:8: cssClass
                    {
                    dbg.location(645,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent3052);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:11: slAttribute
                    {
                    dbg.location(646,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent3064);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:11: pseudo
                    {
                    dbg.location(647,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent3076);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(649, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(652, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt124=2;
            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

            int LA124_0 = input.LA(1);

            if ( (LA124_0==HASH) ) {
                alt124=1;
            }
            else if ( (LA124_0==HASH_SYMBOL) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:7: HASH
                    {
                    dbg.location(653,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId3104); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(653,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:16: HASH_SYMBOL NAME
                    {
                    dbg.location(653,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId3110); if (state.failed) return ;
                    dbg.location(653,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId3112); if (state.failed) return ;

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
        dbg.location(654, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(660, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:7: DOT ( IDENT | GEN )
            {
            dbg.location(661,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass3140); if (state.failed) return ;
            dbg.location(661,11);
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
        dbg.location(662, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(669, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(670,5);
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
        dbg.location(671, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(673, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(674,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute3214); if (state.failed) return ;
            dbg.location(675,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:6: ( namespacePrefix )?
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            int LA125_0 = input.LA(1);

            if ( (LA125_0==IDENT) ) {
                int LA125_1 = input.LA(2);

                if ( (LA125_1==PIPE) ) {
                    alt125=1;
                }
            }
            else if ( ((LA125_0>=STAR && LA125_0<=PIPE)) ) {
                alt125=1;
            }
            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:6: namespacePrefix
                    {
                    dbg.location(675,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute3221);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(675,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:23: ws
                    {
                    dbg.location(675,23);
                    pushFollow(FOLLOW_ws_in_slAttribute3224);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(676,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute3235);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(676,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:25: ( ws )?
            int alt127=2;
            try { dbg.enterSubRule(127);
            try { dbg.enterDecision(127, decisionCanBacktrack[127]);

            int LA127_0 = input.LA(1);

            if ( (LA127_0==WS||(LA127_0>=NL && LA127_0<=COMMENT)) ) {
                alt127=1;
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:25: ws
                    {
                    dbg.location(676,25);
                    pushFollow(FOLLOW_ws_in_slAttribute3237);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}

            dbg.location(678,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt130=2;
            try { dbg.enterSubRule(130);
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            int LA130_0 = input.LA(1);

            if ( ((LA130_0>=OPEQ && LA130_0<=CONTAINS)) ) {
                alt130=1;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(679,17);
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

                    dbg.location(687,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:17: ws
                            {
                            dbg.location(687,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3459);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(128);}

                    dbg.location(688,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute3478);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(689,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:17: ws
                            {
                            dbg.location(689,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3496);
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

            dbg.location(692,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3525); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(693, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(700, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:4: IDENT
            {
            dbg.location(701,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3541); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(702, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(704, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:2: ( IDENT | STRING )
            {
            dbg.location(706,2);
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
        dbg.location(710, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(712, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(713,7);
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

            dbg.location(714,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt138=2;
            try { dbg.enterSubRule(138);
            try { dbg.enterDecision(138, decisionCanBacktrack[138]);

            int LA138_0 = input.LA(1);

            if ( (LA138_0==IDENT||LA138_0==GEN) ) {
                alt138=1;
            }
            else if ( (LA138_0==NOT) ) {
                alt138=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 138, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(138);}

            switch (alt138) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(715,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(716,21);
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

                    dbg.location(717,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    int alt134=2;
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(718,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:25: ws
                                    {
                                    dbg.location(718,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3736);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(131);}

                            dbg.location(718,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3739); if (state.failed) return ;
                            dbg.location(718,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:36: ws
                                    {
                                    dbg.location(718,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3741);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(132);}

                            dbg.location(718,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:40: ( expression | STAR )?
                            int alt133=3;
                            try { dbg.enterSubRule(133);
                            try { dbg.enterDecision(133, decisionCanBacktrack[133]);

                            int LA133_0 = input.LA(1);

                            if ( ((LA133_0>=IDENT && LA133_0<=URI)||LA133_0==MEDIA_SYM||LA133_0==GEN||LA133_0==AT_IDENT||LA133_0==PERCENTAGE||LA133_0==PLUS||LA133_0==MINUS||LA133_0==HASH||(LA133_0>=NUMBER && LA133_0<=DIMENSION)||LA133_0==SASS_VAR) ) {
                                alt133=1;
                            }
                            else if ( (LA133_0==STAR) ) {
                                alt133=2;
                            }
                            } finally {dbg.exitDecision(133);}

                            switch (alt133) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:42: expression
                                    {
                                    dbg.location(718,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3746);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:55: STAR
                                    {
                                    dbg.location(718,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3750); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(133);}

                            dbg.location(718,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3755); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(134);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(722,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(722,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3834); if (state.failed) return ;
                    dbg.location(722,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:23: ws
                            {
                            dbg.location(722,23);
                            pushFollow(FOLLOW_ws_in_pseudo3836);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(135);}

                    dbg.location(722,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3839); if (state.failed) return ;
                    dbg.location(722,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:34: ws
                            {
                            dbg.location(722,34);
                            pushFollow(FOLLOW_ws_in_pseudo3841);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(136);}

                    dbg.location(722,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:38: ( simpleSelectorSequence )?
                    int alt137=2;
                    try { dbg.enterSubRule(137);
                    try { dbg.enterDecision(137, decisionCanBacktrack[137]);

                    int LA137_0 = input.LA(1);

                    if ( (LA137_0==IDENT||LA137_0==GEN||LA137_0==COLON||(LA137_0>=HASH_SYMBOL && LA137_0<=PIPE)||LA137_0==LESS_AND) ) {
                        alt137=1;
                    }
                    } finally {dbg.exitDecision(137);}

                    switch (alt137) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:38: simpleSelectorSequence
                            {
                            dbg.location(722,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3844);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(137);}

                    dbg.location(722,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3847); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(138);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(724, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:1: declaration : ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(726, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:5: ( ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:5: ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(729,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:5: ( STAR )?
            int alt139=2;
            try { dbg.enterSubRule(139);
            try { dbg.enterDecision(139, decisionCanBacktrack[139]);

            int LA139_0 = input.LA(1);

            if ( (LA139_0==STAR) ) {
                alt139=1;
            }
            } finally {dbg.exitDecision(139);}

            switch (alt139) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:5: STAR
                    {
                    dbg.location(729,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3891); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(139);}

            dbg.location(730,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:5: ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property )
            int alt140=2;
            try { dbg.enterSubRule(140);
            try { dbg.enterDecision(140, decisionCanBacktrack[140]);

            int LA140_0 = input.LA(1);

            if ( (LA140_0==HASH_SYMBOL) && (synpred12_Css3())) {
                alt140=1;
            }
            else if ( (LA140_0==IDENT) ) {
                int LA140_2 = input.LA(2);

                if ( (synpred12_Css3()) ) {
                    alt140=1;
                }
                else if ( (true) ) {
                    alt140=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 140, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA140_0==MINUS||(LA140_0>=HASH && LA140_0<=DOT)) && (synpred12_Css3())) {
                alt140=1;
            }
            else if ( (LA140_0==MEDIA_SYM||LA140_0==GEN||LA140_0==AT_IDENT||LA140_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(731,74);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_declaration3937);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:9: property
                    {
                    dbg.location(733,9);
                    pushFollow(FOLLOW_property_in_declaration3958);
                    property();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(735,5);
            match(input,COLON,FOLLOW_COLON_in_declaration3971); if (state.failed) return ;
            dbg.location(735,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:11: ws
                    {
                    dbg.location(735,11);
                    pushFollow(FOLLOW_ws_in_declaration3973);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(735,15);
            pushFollow(FOLLOW_propertyValue_in_declaration3976);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(735,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:29: ( prio ( ws )? )?
            int alt143=2;
            try { dbg.enterSubRule(143);
            try { dbg.enterDecision(143, decisionCanBacktrack[143]);

            int LA143_0 = input.LA(1);

            if ( (LA143_0==IMPORTANT_SYM) ) {
                alt143=1;
            }
            } finally {dbg.exitDecision(143);}

            switch (alt143) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:30: prio ( ws )?
                    {
                    dbg.location(735,30);
                    pushFollow(FOLLOW_prio_in_declaration3979);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(735,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:35: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:35: ws
                            {
                            dbg.location(735,35);
                            pushFollow(FOLLOW_ws_in_declaration3981);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(142);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}


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
        dbg.location(736, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(744, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
            int alt144=2;
            try { dbg.enterDecision(144, decisionCanBacktrack[144]);

            try {
                isCyclicDecision = true;
                alt144 = dfa144.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(144);}

            switch (alt144) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(746,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(746,34);
                    pushFollow(FOLLOW_expression_in_propertyValue4021);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:9: ({...}? cp_expression )
                    {
                    dbg.location(756,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:11: {...}? cp_expression
                    {
                    dbg.location(756,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(756,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue4064);
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
        dbg.location(757, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(760, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(763,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt145=0;
            try { dbg.enterSubRule(145);

            loop145:
            do {
                int alt145=2;
                try { dbg.enterDecision(145, decisionCanBacktrack[145]);

                int LA145_0 = input.LA(1);

                if ( (LA145_0==NAMESPACE_SYM||(LA145_0>=IDENT && LA145_0<=MEDIA_SYM)||(LA145_0>=AND && LA145_0<=RPAREN)||(LA145_0>=WS && LA145_0<=RIGHTBOTTOM_SYM)||(LA145_0>=PLUS && LA145_0<=SASS_EXTEND_ONLY_SELECTOR)||(LA145_0>=PIPE && LA145_0<=LINE_COMMENT)) ) {
                    alt145=1;
                }


                } finally {dbg.exitDecision(145);}

                switch (alt145) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(763,7);
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
            	    if ( cnt145 >= 1 ) break loop145;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(145, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt145++;
            } while (true);
            } finally {dbg.exitSubRule(145);}

            dbg.location(763,65);
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
        dbg.location(764, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(768, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:6: 
            {
            }

        }
        finally {
        }
        dbg.location(774, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(776, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:6: 
            {
            }

        }
        finally {
        }
        dbg.location(781, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(783, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:13: SEMI
            {
            dbg.location(788,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI4249); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(789, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(792, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:6: 
            {
            }

        }
        finally {
        }
        dbg.location(797, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(799, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:7: IMPORTANT_SYM
            {
            dbg.location(800,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio4304); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(801, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(803, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(804,7);
            pushFollow(FOLLOW_term_in_expression4325);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(804,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(148);

            loop148:
            do {
                int alt148=2;
                try { dbg.enterDecision(148, decisionCanBacktrack[148]);

                try {
                    isCyclicDecision = true;
                    alt148 = dfa148.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(148);}

                switch (alt148) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(804,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:14: ( operator ( ws )? )?
            	    int alt147=2;
            	    try { dbg.enterSubRule(147);
            	    try { dbg.enterDecision(147, decisionCanBacktrack[147]);

            	    int LA147_0 = input.LA(1);

            	    if ( (LA147_0==COMMA||LA147_0==SOLIDUS) ) {
            	        alt147=1;
            	    }
            	    } finally {dbg.exitDecision(147);}

            	    switch (alt147) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:15: operator ( ws )?
            	            {
            	            dbg.location(804,15);
            	            pushFollow(FOLLOW_operator_in_expression4330);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(804,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:24: ws
            	                    {
            	                    dbg.location(804,24);
            	                    pushFollow(FOLLOW_ws_in_expression4332);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(146);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(147);}

            	    dbg.location(804,30);
            	    pushFollow(FOLLOW_term_in_expression4337);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop148;
                }
            } while (true);
            } finally {dbg.exitSubRule(148);}


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
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(807, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(808,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:7: ( unaryOperator ( ws )? )?
            int alt150=2;
            try { dbg.enterSubRule(150);
            try { dbg.enterDecision(150, decisionCanBacktrack[150]);

            int LA150_0 = input.LA(1);

            if ( (LA150_0==PLUS||LA150_0==MINUS) ) {
                alt150=1;
            }
            } finally {dbg.exitDecision(150);}

            switch (alt150) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:9: unaryOperator ( ws )?
                    {
                    dbg.location(808,9);
                    pushFollow(FOLLOW_unaryOperator_in_term4362);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(808,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:23: ( ws )?
                    int alt149=2;
                    try { dbg.enterSubRule(149);
                    try { dbg.enterDecision(149, decisionCanBacktrack[149]);

                    int LA149_0 = input.LA(1);

                    if ( (LA149_0==WS||(LA149_0>=NL && LA149_0<=COMMENT)) ) {
                        alt149=1;
                    }
                    } finally {dbg.exitDecision(149);}

                    switch (alt149) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:23: ws
                            {
                            dbg.location(808,23);
                            pushFollow(FOLLOW_ws_in_term4364);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(149);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}

            dbg.location(809,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt151=8;
            try { dbg.enterSubRule(151);
            try { dbg.enterDecision(151, decisionCanBacktrack[151]);

            try {
                isCyclicDecision = true;
                alt151 = dfa151.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(151);}

            switch (alt151) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(810,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:7: STRING
                    {
                    dbg.location(823,7);
                    match(input,STRING,FOLLOW_STRING_in_term4588); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:7: IDENT
                    {
                    dbg.location(824,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term4596); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:7: GEN
                    {
                    dbg.location(825,7);
                    match(input,GEN,FOLLOW_GEN_in_term4604); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:7: URI
                    {
                    dbg.location(826,7);
                    match(input,URI,FOLLOW_URI_in_term4612); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:7: hexColor
                    {
                    dbg.location(827,7);
                    pushFollow(FOLLOW_hexColor_in_term4620);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:7: function
                    {
                    dbg.location(828,7);
                    pushFollow(FOLLOW_function_in_term4628);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:829:7: {...}? cp_variable
                    {
                    dbg.location(829,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(829,36);
                    pushFollow(FOLLOW_cp_variable_in_term4638);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(831,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:5: ws
                    {
                    dbg.location(831,5);
                    pushFollow(FOLLOW_ws_in_term4650);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(152);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(834, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(835,5);
            pushFollow(FOLLOW_functionName_in_function4666);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(835,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:18: ws
                    {
                    dbg.location(835,18);
                    pushFollow(FOLLOW_ws_in_function4668);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(153);}

            dbg.location(836,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4673); if (state.failed) return ;
            dbg.location(836,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:10: ws
                    {
                    dbg.location(836,10);
                    pushFollow(FOLLOW_ws_in_function4675);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(154);}

            dbg.location(837,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt157=2;
            try { dbg.enterSubRule(157);
            try { dbg.enterDecision(157, decisionCanBacktrack[157]);

            try {
                isCyclicDecision = true;
                alt157 = dfa157.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(157);}

            switch (alt157) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:4: expression
                    {
                    dbg.location(838,4);
                    pushFollow(FOLLOW_expression_in_function4685);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(840,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(841,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4703);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(841,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(156);

                    loop156:
                    do {
                        int alt156=2;
                        try { dbg.enterDecision(156, decisionCanBacktrack[156]);

                        int LA156_0 = input.LA(1);

                        if ( (LA156_0==COMMA) ) {
                            alt156=1;
                        }


                        } finally {dbg.exitDecision(156);}

                        switch (alt156) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(841,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4706); if (state.failed) return ;
                    	    dbg.location(841,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:24: ws
                    	            {
                    	            dbg.location(841,24);
                    	            pushFollow(FOLLOW_ws_in_function4708);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(155);}

                    	    dbg.location(841,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4711);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop156;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(156);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(157);}

            dbg.location(844,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4732); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(845, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(851, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(855,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:4: ( IDENT COLON )?
            int alt158=2;
            try { dbg.enterSubRule(158);
            try { dbg.enterDecision(158, decisionCanBacktrack[158]);

            int LA158_0 = input.LA(1);

            if ( (LA158_0==IDENT) ) {
                int LA158_1 = input.LA(2);

                if ( (LA158_1==COLON) ) {
                    alt158=1;
                }
            }
            } finally {dbg.exitDecision(158);}

            switch (alt158) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:5: IDENT COLON
                    {
                    dbg.location(855,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4780); if (state.failed) return ;
                    dbg.location(855,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4782); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(158);}

            dbg.location(855,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4786); if (state.failed) return ;
            dbg.location(855,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:25: ( DOT IDENT )*
            try { dbg.enterSubRule(159);

            loop159:
            do {
                int alt159=2;
                try { dbg.enterDecision(159, decisionCanBacktrack[159]);

                int LA159_0 = input.LA(1);

                if ( (LA159_0==DOT) ) {
                    alt159=1;
                }


                } finally {dbg.exitDecision(159);}

                switch (alt159) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:26: DOT IDENT
            	    {
            	    dbg.location(855,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4789); if (state.failed) return ;
            	    dbg.location(855,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4791); if (state.failed) return ;

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
        dbg.location(857, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(859, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(860,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4814);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(860,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:20: ws
                    {
                    dbg.location(860,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4816);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(860,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4819); if (state.failed) return ;
            dbg.location(860,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:29: ( ws )?
            int alt161=2;
            try { dbg.enterSubRule(161);
            try { dbg.enterDecision(161, decisionCanBacktrack[161]);

            int LA161_0 = input.LA(1);

            if ( (LA161_0==WS||(LA161_0>=NL && LA161_0<=COMMENT)) ) {
                alt161=1;
            }
            } finally {dbg.exitDecision(161);}

            switch (alt161) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:29: ws
                    {
                    dbg.location(860,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4821);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(161);}

            dbg.location(860,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4824);
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
        dbg.location(861, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(863, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:4: IDENT ( DOT IDENT )*
            {
            dbg.location(864,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4839); if (state.failed) return ;
            dbg.location(864,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:10: ( DOT IDENT )*
            try { dbg.enterSubRule(162);

            loop162:
            do {
                int alt162=2;
                try { dbg.enterDecision(162, decisionCanBacktrack[162]);

                int LA162_0 = input.LA(1);

                if ( (LA162_0==DOT) ) {
                    alt162=1;
                }


                } finally {dbg.exitDecision(162);}

                switch (alt162) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:11: DOT IDENT
            	    {
            	    dbg.location(864,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4842); if (state.failed) return ;
            	    dbg.location(864,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4844); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop162;
                }
            } while (true);
            } finally {dbg.exitSubRule(162);}


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
            dbg.exitRule(getGrammarFileName(), "fnAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttributeName"


    // $ANTLR start "fnAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(867, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:4: expression
            {
            dbg.location(868,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4858);
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
        dbg.location(869, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(871, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:7: HASH
            {
            dbg.location(872,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4876); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(873, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(875, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:7: ( WS | NL | COMMENT )+
            {
            dbg.location(876,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:7: ( WS | NL | COMMENT )+
            int cnt163=0;
            try { dbg.enterSubRule(163);

            loop163:
            do {
                int alt163=2;
                try { dbg.enterDecision(163, decisionCanBacktrack[163]);

                int LA163_0 = input.LA(1);

                if ( (LA163_0==WS||(LA163_0>=NL && LA163_0<=COMMENT)) ) {
                    alt163=1;
                }


                } finally {dbg.exitDecision(163);}

                switch (alt163) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(876,7);
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
            	    if ( cnt163 >= 1 ) break loop163;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(163, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt163++;
            } while (true);
            } finally {dbg.exitSubRule(163);}


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
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"


    // $ANTLR start "cp_variable_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(882, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt170=2;
            try { dbg.enterDecision(170, decisionCanBacktrack[170]);

            int LA170_0 = input.LA(1);

            if ( (LA170_0==MEDIA_SYM||LA170_0==AT_IDENT) ) {
                int LA170_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt170=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt170=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 170, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA170_0==SASS_VAR) ) {
                int LA170_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt170=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt170=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 170, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 170, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(170);}

            switch (alt170) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
                    {
                    dbg.location(884,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(884,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4945);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(884,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:39: ( ws )?
                    int alt164=2;
                    try { dbg.enterSubRule(164);
                    try { dbg.enterDecision(164, decisionCanBacktrack[164]);

                    int LA164_0 = input.LA(1);

                    if ( (LA164_0==WS||(LA164_0>=NL && LA164_0<=COMMENT)) ) {
                        alt164=1;
                    }
                    } finally {dbg.exitDecision(164);}

                    switch (alt164) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:39: ws
                            {
                            dbg.location(884,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4947);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(164);}

                    dbg.location(884,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4950); if (state.failed) return ;
                    dbg.location(884,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:49: ws
                            {
                            dbg.location(884,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4952);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(165);}

                    dbg.location(884,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4955);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(884,67);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4957); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(886,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(886,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4984);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(886,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:39: ws
                            {
                            dbg.location(886,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4986);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(166);}

                    dbg.location(886,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4989); if (state.failed) return ;
                    dbg.location(886,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:49: ws
                            {
                            dbg.location(886,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4991);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(167);}

                    dbg.location(886,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4994);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(886,67);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:67: ( SASS_DEFAULT ( ws )? )?
                    int alt169=2;
                    try { dbg.enterSubRule(169);
                    try { dbg.enterDecision(169, decisionCanBacktrack[169]);

                    int LA169_0 = input.LA(1);

                    if ( (LA169_0==SASS_DEFAULT) ) {
                        alt169=1;
                    }
                    } finally {dbg.exitDecision(169);}

                    switch (alt169) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:68: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(886,68);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4997); if (state.failed) return ;
                            dbg.location(886,81);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:81: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:81: ws
                                    {
                                    dbg.location(886,81);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4999);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(168);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(169);}

                    dbg.location(886,87);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5004); if (state.failed) return ;

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
        dbg.location(887, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(890, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt171=2;
            try { dbg.enterDecision(171, decisionCanBacktrack[171]);

            int LA171_0 = input.LA(1);

            if ( (LA171_0==MEDIA_SYM||LA171_0==AT_IDENT) ) {
                alt171=1;
            }
            else if ( (LA171_0==SASS_VAR) ) {
                alt171=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 171, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(171);}

            switch (alt171) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(892,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(892,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(894,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(894,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:29: SASS_VAR
                    {
                    dbg.location(894,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable5069); if (state.failed) return ;

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
        dbg.location(896, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(899, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:10: cp_additionExp
            {
            dbg.location(900,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression5093);
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
        dbg.location(901, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(903, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(904,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5113);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(905,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(174);

            loop174:
            do {
                int alt174=3;
                try { dbg.enterDecision(174, decisionCanBacktrack[174]);

                int LA174_0 = input.LA(1);

                if ( (LA174_0==PLUS) ) {
                    alt174=1;
                }
                else if ( (LA174_0==MINUS) ) {
                    alt174=2;
                }


                } finally {dbg.exitDecision(174);}

                switch (alt174) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(905,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp5127); if (state.failed) return ;
            	    dbg.location(905,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:17: ws
            	            {
            	            dbg.location(905,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5129);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(172);}

            	    dbg.location(905,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5132);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(906,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp5145); if (state.failed) return ;
            	    dbg.location(906,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:18: ws
            	            {
            	            dbg.location(906,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5147);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(173);}

            	    dbg.location(906,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5150);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop174;
                }
            } while (true);
            } finally {dbg.exitSubRule(174);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(908, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(910, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(911,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5183);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(912,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(177);

            loop177:
            do {
                int alt177=3;
                try { dbg.enterDecision(177, decisionCanBacktrack[177]);

                int LA177_0 = input.LA(1);

                if ( (LA177_0==STAR) ) {
                    alt177=1;
                }
                else if ( (LA177_0==SOLIDUS) ) {
                    alt177=2;
                }


                } finally {dbg.exitDecision(177);}

                switch (alt177) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(912,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp5196); if (state.failed) return ;
            	    dbg.location(912,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:17: ws
            	            {
            	            dbg.location(912,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5198);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(175);}

            	    dbg.location(912,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5201);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(913,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp5215); if (state.failed) return ;
            	    dbg.location(913,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:20: ( ws )?
            	    int alt176=2;
            	    try { dbg.enterSubRule(176);
            	    try { dbg.enterDecision(176, decisionCanBacktrack[176]);

            	    int LA176_0 = input.LA(1);

            	    if ( (LA176_0==WS||(LA176_0>=NL && LA176_0<=COMMENT)) ) {
            	        alt176=1;
            	    }
            	    } finally {dbg.exitDecision(176);}

            	    switch (alt176) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:20: ws
            	            {
            	            dbg.location(913,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5217);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(176);}

            	    dbg.location(913,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5220);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop177;
                }
            } while (true);
            } finally {dbg.exitSubRule(177);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(915, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(917, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt181=2;
            try { dbg.enterDecision(181, decisionCanBacktrack[181]);

            int LA181_0 = input.LA(1);

            if ( ((LA181_0>=IDENT && LA181_0<=URI)||LA181_0==MEDIA_SYM||LA181_0==GEN||LA181_0==AT_IDENT||LA181_0==PERCENTAGE||LA181_0==PLUS||LA181_0==MINUS||LA181_0==HASH||(LA181_0>=NUMBER && LA181_0<=DIMENSION)||LA181_0==SASS_VAR) ) {
                alt181=1;
            }
            else if ( (LA181_0==LPAREN) ) {
                alt181=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 181, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(181);}

            switch (alt181) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:10: term ( ( term )=> term )*
                    {
                    dbg.location(918,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp5253);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(918,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(178);

                    loop178:
                    do {
                        int alt178=2;
                        try { dbg.enterDecision(178, decisionCanBacktrack[178]);

                        try {
                            isCyclicDecision = true;
                            alt178 = dfa178.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(178);}

                        switch (alt178) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:16: ( term )=> term
                    	    {
                    	    dbg.location(918,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp5260);
                    	    term();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop178;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(178);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(919,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp5274); if (state.failed) return ;
                    dbg.location(919,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:17: ws
                            {
                            dbg.location(919,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5276);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(179);}

                    dbg.location(919,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp5279);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(919,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp5281); if (state.failed) return ;
                    dbg.location(919,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:43: ws
                            {
                            dbg.location(919,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5283);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(180);}


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
        dbg.location(920, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(923, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(925,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt182=8;
            try { dbg.enterSubRule(182);
            try { dbg.enterDecision(182, decisionCanBacktrack[182]);

            try {
                isCyclicDecision = true;
                alt182 = dfa182.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(182);}

            switch (alt182) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(926,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:7: STRING
                    {
                    dbg.location(939,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term5521); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:7: IDENT
                    {
                    dbg.location(940,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term5529); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:7: GEN
                    {
                    dbg.location(941,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term5537); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:942:7: URI
                    {
                    dbg.location(942,7);
                    match(input,URI,FOLLOW_URI_in_cp_term5545); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:7: hexColor
                    {
                    dbg.location(943,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term5553);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:7: function
                    {
                    dbg.location(944,7);
                    pushFollow(FOLLOW_function_in_cp_term5561);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:7: cp_variable
                    {
                    dbg.location(945,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term5569);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(182);}

            dbg.location(947,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:5: ( ws )?
            int alt183=2;
            try { dbg.enterSubRule(183);
            try { dbg.enterDecision(183, decisionCanBacktrack[183]);

            int LA183_0 = input.LA(1);

            if ( (LA183_0==WS||(LA183_0>=NL && LA183_0<=COMMENT)) ) {
                alt183=1;
            }
            } finally {dbg.exitDecision(183);}

            switch (alt183) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:5: ws
                    {
                    dbg.location(947,5);
                    pushFollow(FOLLOW_ws_in_cp_term5581);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(183);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(948, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(957, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? )
            int alt193=2;
            try { dbg.enterDecision(193, decisionCanBacktrack[193]);

            int LA193_0 = input.LA(1);

            if ( (LA193_0==DOT) ) {
                alt193=1;
            }
            else if ( (LA193_0==SASS_MIXIN) ) {
                alt193=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 193, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(193);}

            switch (alt193) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(959,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(959,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration5612); if (state.failed) return ;
                    dbg.location(959,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5614);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(959,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:41: ( ws )?
                    int alt184=2;
                    try { dbg.enterSubRule(184);
                    try { dbg.enterDecision(184, decisionCanBacktrack[184]);

                    int LA184_0 = input.LA(1);

                    if ( (LA184_0==WS||(LA184_0>=NL && LA184_0<=COMMENT)) ) {
                        alt184=1;
                    }
                    } finally {dbg.exitDecision(184);}

                    switch (alt184) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:41: ws
                            {
                            dbg.location(959,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5616);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(184);}

                    dbg.location(959,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5619); if (state.failed) return ;
                    dbg.location(959,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:52: ( less_args_list )?
                    int alt185=2;
                    try { dbg.enterSubRule(185);
                    try { dbg.enterDecision(185, decisionCanBacktrack[185]);

                    int LA185_0 = input.LA(1);

                    if ( (LA185_0==MEDIA_SYM||LA185_0==AT_IDENT||LA185_0==SASS_VAR||(LA185_0>=LESS_DOTS && LA185_0<=LESS_REST)) ) {
                        alt185=1;
                    }
                    } finally {dbg.exitDecision(185);}

                    switch (alt185) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:52: less_args_list
                            {
                            dbg.location(959,52);
                            pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5621);
                            less_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(185);}

                    dbg.location(959,68);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5624); if (state.failed) return ;
                    dbg.location(959,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:75: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:75: ws
                            {
                            dbg.location(959,75);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5626);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(186);}

                    dbg.location(959,79);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:79: ( less_mixin_guarded ( ws )? )?
                    int alt188=2;
                    try { dbg.enterSubRule(188);
                    try { dbg.enterDecision(188, decisionCanBacktrack[188]);

                    int LA188_0 = input.LA(1);

                    if ( (LA188_0==LESS_WHEN) ) {
                        alt188=1;
                    }
                    } finally {dbg.exitDecision(188);}

                    switch (alt188) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:80: less_mixin_guarded ( ws )?
                            {
                            dbg.location(959,80);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5630);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(959,99);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:99: ( ws )?
                            int alt187=2;
                            try { dbg.enterSubRule(187);
                            try { dbg.enterDecision(187, decisionCanBacktrack[187]);

                            int LA187_0 = input.LA(1);

                            if ( (LA187_0==WS||(LA187_0>=NL && LA187_0<=COMMENT)) ) {
                                alt187=1;
                            }
                            } finally {dbg.exitDecision(187);}

                            switch (alt187) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:99: ws
                                    {
                                    dbg.location(959,99);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5632);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(187);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(188);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(961,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(961,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5649); if (state.failed) return ;
                    dbg.location(961,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5651);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(961,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5653);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(961,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:51: ws
                            {
                            dbg.location(961,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5655);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(189);}

                    dbg.location(961,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:55: ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    int alt192=2;
                    try { dbg.enterSubRule(192);
                    try { dbg.enterDecision(192, decisionCanBacktrack[192]);

                    int LA192_0 = input.LA(1);

                    if ( (LA192_0==LPAREN) ) {
                        alt192=1;
                    }
                    } finally {dbg.exitDecision(192);}

                    switch (alt192) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:56: LPAREN ( less_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(961,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5659); if (state.failed) return ;
                            dbg.location(961,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:63: ( less_args_list )?
                            int alt190=2;
                            try { dbg.enterSubRule(190);
                            try { dbg.enterDecision(190, decisionCanBacktrack[190]);

                            int LA190_0 = input.LA(1);

                            if ( (LA190_0==MEDIA_SYM||LA190_0==AT_IDENT||LA190_0==SASS_VAR||(LA190_0>=LESS_DOTS && LA190_0<=LESS_REST)) ) {
                                alt190=1;
                            }
                            } finally {dbg.exitDecision(190);}

                            switch (alt190) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:63: less_args_list
                                    {
                                    dbg.location(961,63);
                                    pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5661);
                                    less_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(190);}

                            dbg.location(961,79);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5664); if (state.failed) return ;
                            dbg.location(961,86);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:86: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:86: ws
                                    {
                                    dbg.location(961,86);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5666);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(191);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(192);}


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
        dbg.location(962, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(966, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:967:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(968,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt194=2;
            try { dbg.enterSubRule(194);
            try { dbg.enterDecision(194, decisionCanBacktrack[194]);

            int LA194_0 = input.LA(1);

            if ( (LA194_0==DOT) ) {
                alt194=1;
            }
            else if ( (LA194_0==SASS_INCLUDE) ) {
                alt194=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 194, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(194);}

            switch (alt194) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(969,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(969,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call5708); if (state.failed) return ;
                    dbg.location(969,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5710);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(971,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(971,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call5732); if (state.failed) return ;
                    dbg.location(971,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5734);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(971,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5736);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(194);}

            dbg.location(973,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?
            int alt197=2;
            try { dbg.enterSubRule(197);
            try { dbg.enterDecision(197, decisionCanBacktrack[197]);

            try {
                isCyclicDecision = true;
                alt197 = dfa197.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(197);}

            switch (alt197) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:6: ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(973,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:6: ( ws )?
                    int alt195=2;
                    try { dbg.enterSubRule(195);
                    try { dbg.enterDecision(195, decisionCanBacktrack[195]);

                    int LA195_0 = input.LA(1);

                    if ( (LA195_0==WS||(LA195_0>=NL && LA195_0<=COMMENT)) ) {
                        alt195=1;
                    }
                    } finally {dbg.exitDecision(195);}

                    switch (alt195) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:6: ws
                            {
                            dbg.location(973,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call5749);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(195);}

                    dbg.location(973,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call5752); if (state.failed) return ;
                    dbg.location(973,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:17: ( cp_mixin_call_args )?
                    int alt196=2;
                    try { dbg.enterSubRule(196);
                    try { dbg.enterDecision(196, decisionCanBacktrack[196]);

                    int LA196_0 = input.LA(1);

                    if ( ((LA196_0>=IDENT && LA196_0<=URI)||LA196_0==MEDIA_SYM||LA196_0==GEN||LA196_0==AT_IDENT||LA196_0==PERCENTAGE||LA196_0==PLUS||LA196_0==MINUS||LA196_0==HASH||(LA196_0>=NUMBER && LA196_0<=DIMENSION)||LA196_0==SASS_VAR) ) {
                        alt196=1;
                    }
                    } finally {dbg.exitDecision(196);}

                    switch (alt196) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:17: cp_mixin_call_args
                            {
                            dbg.location(973,17);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call5754);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(196);}

                    dbg.location(973,37);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call5757); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(197);}

            dbg.location(973,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:46: ( ws )?
            int alt198=2;
            try { dbg.enterSubRule(198);
            try { dbg.enterDecision(198, decisionCanBacktrack[198]);

            int LA198_0 = input.LA(1);

            if ( (LA198_0==WS||(LA198_0>=NL && LA198_0<=COMMENT)) ) {
                alt198=1;
            }
            } finally {dbg.exitDecision(198);}

            switch (alt198) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:46: ws
                    {
                    dbg.location(973,46);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5761);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(198);}

            dbg.location(973,50);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call5764); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(974, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:976:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(976, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:5: IDENT
            {
            dbg.location(978,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name5793); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(979, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:1: cp_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(981, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(985,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_args5829);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(985,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(200);

            loop200:
            do {
                int alt200=2;
                try { dbg.enterDecision(200, decisionCanBacktrack[200]);

                int LA200_0 = input.LA(1);

                if ( (LA200_0==SEMI||LA200_0==COMMA) ) {
                    alt200=1;
                }


                } finally {dbg.exitDecision(200);}

                switch (alt200) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(985,12);
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

            	    dbg.location(985,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:27: ( ws )?
            	    int alt199=2;
            	    try { dbg.enterSubRule(199);
            	    try { dbg.enterDecision(199, decisionCanBacktrack[199]);

            	    int LA199_0 = input.LA(1);

            	    if ( (LA199_0==WS||(LA199_0>=NL && LA199_0<=COMMENT)) ) {
            	        alt199=1;
            	    }
            	    } finally {dbg.exitDecision(199);}

            	    switch (alt199) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:27: ws
            	            {
            	            dbg.location(985,27);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args5841);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(199);}

            	    dbg.location(985,31);
            	    pushFollow(FOLLOW_term_in_cp_mixin_call_args5844);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop200;
                }
            } while (true);
            } finally {dbg.exitSubRule(200);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(986, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(989, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt205=2;
            try { dbg.enterDecision(205, decisionCanBacktrack[205]);

            int LA205_0 = input.LA(1);

            if ( (LA205_0==MEDIA_SYM||LA205_0==AT_IDENT||LA205_0==SASS_VAR) ) {
                alt205=1;
            }
            else if ( ((LA205_0>=LESS_DOTS && LA205_0<=LESS_REST)) ) {
                alt205=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 205, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(205);}

            switch (alt205) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(993,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(993,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5886);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(993,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
                    try { dbg.enterSubRule(202);

                    loop202:
                    do {
                        int alt202=2;
                        try { dbg.enterDecision(202, decisionCanBacktrack[202]);

                        try {
                            isCyclicDecision = true;
                            alt202 = dfa202.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(202);}

                        switch (alt202) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(993,18);
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

                    	    dbg.location(993,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:35: ws
                    	            {
                    	            dbg.location(993,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5900);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(201);}

                    	    dbg.location(993,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5903);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop202;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(202);}

                    dbg.location(993,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:50: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt204=2;
                    try { dbg.enterSubRule(204);
                    try { dbg.enterDecision(204, decisionCanBacktrack[204]);

                    int LA204_0 = input.LA(1);

                    if ( (LA204_0==SEMI||LA204_0==COMMA) ) {
                        alt204=1;
                    }
                    } finally {dbg.exitDecision(204);}

                    switch (alt204) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:52: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(993,52);
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

                            dbg.location(993,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:69: ws
                                    {
                                    dbg.location(993,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5919);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(203);}

                            dbg.location(993,73);
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
                    } finally {dbg.exitSubRule(204);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(995,5);
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
        dbg.location(996, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(999, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(1001,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg5976);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1001,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:17: ( COLON ( ws )? cp_expression )?
            int alt207=2;
            try { dbg.enterSubRule(207);
            try { dbg.enterDecision(207, decisionCanBacktrack[207]);

            int LA207_0 = input.LA(1);

            if ( (LA207_0==COLON) ) {
                alt207=1;
            }
            } finally {dbg.exitDecision(207);}

            switch (alt207) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(1001,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5980); if (state.failed) return ;
                    dbg.location(1001,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:25: ( ws )?
                    int alt206=2;
                    try { dbg.enterSubRule(206);
                    try { dbg.enterDecision(206, decisionCanBacktrack[206]);

                    int LA206_0 = input.LA(1);

                    if ( (LA206_0==WS||(LA206_0>=NL && LA206_0<=COMMENT)) ) {
                        alt206=1;
                    }
                    } finally {dbg.exitDecision(206);}

                    switch (alt206) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:25: ws
                            {
                            dbg.location(1001,25);
                            pushFollow(FOLLOW_ws_in_less_arg5982);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(206);}

                    dbg.location(1001,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg5985);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(207);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1002, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1006:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1006, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(1008,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded6011); if (state.failed) return ;
            dbg.location(1008,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:15: ws
                    {
                    dbg.location(1008,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded6013);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(208);}

            dbg.location(1008,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6016);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1008,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(210);

            loop210:
            do {
                int alt210=2;
                try { dbg.enterDecision(210, decisionCanBacktrack[210]);

                int LA210_0 = input.LA(1);

                if ( (LA210_0==COMMA||LA210_0==AND) ) {
                    alt210=1;
                }


                } finally {dbg.exitDecision(210);}

                switch (alt210) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(1008,36);
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

            	    dbg.location(1008,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:50: ( ws )?
            	    int alt209=2;
            	    try { dbg.enterSubRule(209);
            	    try { dbg.enterDecision(209, decisionCanBacktrack[209]);

            	    int LA209_0 = input.LA(1);

            	    if ( (LA209_0==WS||(LA209_0>=NL && LA209_0<=COMMENT)) ) {
            	        alt209=1;
            	    }
            	    } finally {dbg.exitDecision(209);}

            	    switch (alt209) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1008:50: ws
            	            {
            	            dbg.location(1008,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded6028);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(209);}

            	    dbg.location(1008,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6031);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop210;
                }
            } while (true);
            } finally {dbg.exitSubRule(210);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1009, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1013:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1013, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(1015,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:5: ( NOT ( ws )? )?
            int alt212=2;
            try { dbg.enterSubRule(212);
            try { dbg.enterDecision(212, decisionCanBacktrack[212]);

            int LA212_0 = input.LA(1);

            if ( (LA212_0==NOT) ) {
                alt212=1;
            }
            } finally {dbg.exitDecision(212);}

            switch (alt212) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:6: NOT ( ws )?
                    {
                    dbg.location(1015,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition6061); if (state.failed) return ;
                    dbg.location(1015,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:10: ws
                            {
                            dbg.location(1015,10);
                            pushFollow(FOLLOW_ws_in_less_condition6063);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(211);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(212);}

            dbg.location(1016,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition6072); if (state.failed) return ;
            dbg.location(1016,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:12: ws
                    {
                    dbg.location(1016,12);
                    pushFollow(FOLLOW_ws_in_less_condition6074);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(213);}

            dbg.location(1017,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1017:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt218=2;
            try { dbg.enterSubRule(218);
            try { dbg.enterDecision(218, decisionCanBacktrack[218]);

            int LA218_0 = input.LA(1);

            if ( (LA218_0==IDENT) ) {
                alt218=1;
            }
            else if ( (LA218_0==MEDIA_SYM||LA218_0==AT_IDENT||LA218_0==SASS_VAR) ) {
                alt218=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 218, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(218);}

            switch (alt218) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1018:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1018,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition6100);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1018,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1018:40: ( ws )?
                    int alt214=2;
                    try { dbg.enterSubRule(214);
                    try { dbg.enterDecision(214, decisionCanBacktrack[214]);

                    int LA214_0 = input.LA(1);

                    if ( (LA214_0==WS||(LA214_0>=NL && LA214_0<=COMMENT)) ) {
                        alt214=1;
                    }
                    } finally {dbg.exitDecision(214);}

                    switch (alt214) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1018:40: ws
                            {
                            dbg.location(1018,40);
                            pushFollow(FOLLOW_ws_in_less_condition6102);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(214);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1020,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1020,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition6133);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1020,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt217=2;
                    try { dbg.enterSubRule(217);
                    try { dbg.enterDecision(217, decisionCanBacktrack[217]);

                    int LA217_0 = input.LA(1);

                    if ( (LA217_0==WS||LA217_0==GREATER||LA217_0==OPEQ||(LA217_0>=NL && LA217_0<=COMMENT)||(LA217_0>=GREATER_OR_EQ && LA217_0<=LESS_OR_EQ)) ) {
                        alt217=1;
                    }
                    } finally {dbg.exitDecision(217);}

                    switch (alt217) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1020,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:28: ws
                                    {
                                    dbg.location(1020,28);
                                    pushFollow(FOLLOW_ws_in_less_condition6136);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(215);}

                            dbg.location(1020,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition6139);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1020,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:56: ws
                                    {
                                    dbg.location(1020,56);
                                    pushFollow(FOLLOW_ws_in_less_condition6141);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(216);}

                            dbg.location(1020,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition6144);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(217);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(218);}

            dbg.location(1022,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition6173); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1023, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1026:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1026, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1027:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1028,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition6199);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1028,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:18: ( ws )?
            int alt219=2;
            try { dbg.enterSubRule(219);
            try { dbg.enterDecision(219, decisionCanBacktrack[219]);

            int LA219_0 = input.LA(1);

            if ( (LA219_0==WS||(LA219_0>=NL && LA219_0<=COMMENT)) ) {
                alt219=1;
            }
            } finally {dbg.exitDecision(219);}

            switch (alt219) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:18: ws
                    {
                    dbg.location(1028,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6201);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(219);}

            dbg.location(1028,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition6204); if (state.failed) return ;
            dbg.location(1028,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:29: ( ws )?
            int alt220=2;
            try { dbg.enterSubRule(220);
            try { dbg.enterDecision(220, decisionCanBacktrack[220]);

            int LA220_0 = input.LA(1);

            if ( (LA220_0==WS||(LA220_0>=NL && LA220_0<=COMMENT)) ) {
                alt220=1;
            }
            } finally {dbg.exitDecision(220);}

            switch (alt220) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:29: ws
                    {
                    dbg.location(1028,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6206);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(220);}

            dbg.location(1028,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition6209);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1028,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:45: ws
                    {
                    dbg.location(1028,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6211);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(221);}

            dbg.location(1028,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition6214); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1029, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1032, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1033:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:5: IDENT
            {
            dbg.location(1034,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name6236); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1035, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1037, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1038,5);
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
        dbg.location(1040, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1058, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            {
            dbg.location(1060,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            int alt222=2;
            try { dbg.enterSubRule(222);
            try { dbg.enterDecision(222, decisionCanBacktrack[222]);

            try {
                isCyclicDecision = true;
                alt222 = dfa222.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(222);}

            switch (alt222) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1061,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6335);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
                    {
                    dbg.location(1063,13);
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
            } finally {dbg.exitSubRule(222);}

            dbg.location(1065,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            try { dbg.enterSubRule(225);

            loop225:
            do {
                int alt225=2;
                try { dbg.enterDecision(225, decisionCanBacktrack[225]);

                try {
                    isCyclicDecision = true;
                    alt225 = dfa225.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(225);}

                switch (alt225) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1066:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    {
            	    dbg.location(1066,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1066:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1066:13: ws
            	            {
            	            dbg.location(1066,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression6420);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(223);}

            	    dbg.location(1067,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1068,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6459);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
            	            {
            	            dbg.location(1070,17);
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


            	    }
            	    break;

            	default :
            	    break loop225;
                }
            } while (true);
            } finally {dbg.exitSubRule(225);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1074, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1076:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1076, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1077:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1078:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1078,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1078:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt226=2;
            try { dbg.enterSubRule(226);
            try { dbg.enterDecision(226, decisionCanBacktrack[226]);

            int LA226_0 = input.LA(1);

            if ( (LA226_0==HASH_SYMBOL) ) {
                int LA226_1 = input.LA(2);

                if ( (LA226_1==LBRACE) && (synpred17_Css3())) {
                    alt226=1;
                }
                else if ( (LA226_1==IDENT||LA226_1==COLON||LA226_1==WS||(LA226_1>=MINUS && LA226_1<=DOT)||(LA226_1>=NL && LA226_1<=COMMENT)) ) {
                    alt226=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 226, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA226_0==IDENT||LA226_0==MINUS||(LA226_0>=HASH && LA226_0<=DOT)) ) {
                alt226=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 226, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(226);}

            switch (alt226) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1079,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6593);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1081:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1081,13);
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
            } finally {dbg.exitSubRule(226);}

            dbg.location(1083,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1083:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(229);

            loop229:
            do {
                int alt229=2;
                try { dbg.enterDecision(229, decisionCanBacktrack[229]);

                int LA229_0 = input.LA(1);

                if ( (LA229_0==IDENT||LA229_0==WS||(LA229_0>=MINUS && LA229_0<=DOT)||(LA229_0>=NL && LA229_0<=COMMENT)) ) {
                    alt229=1;
                }


                } finally {dbg.exitDecision(229);}

                switch (alt229) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1084:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1084,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1084:13: ( ws )?
            	    int alt227=2;
            	    try { dbg.enterSubRule(227);
            	    try { dbg.enterDecision(227, decisionCanBacktrack[227]);

            	    int LA227_0 = input.LA(1);

            	    if ( (LA227_0==WS||(LA227_0>=NL && LA227_0<=COMMENT)) ) {
            	        alt227=1;
            	    }
            	    } finally {dbg.exitDecision(227);}

            	    switch (alt227) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1084:13: ws
            	            {
            	            dbg.location(1084,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression6674);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(227);}

            	    dbg.location(1085,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1085:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt228=2;
            	    try { dbg.enterSubRule(228);
            	    try { dbg.enterDecision(228, decisionCanBacktrack[228]);

            	    int LA228_0 = input.LA(1);

            	    if ( (LA228_0==HASH_SYMBOL) ) {
            	        int LA228_1 = input.LA(2);

            	        if ( (LA228_1==LBRACE) && (synpred18_Css3())) {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1086:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1086,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6713);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1088:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1088,17);
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


            	    }
            	    break;

            	default :
            	    break loop229;
                }
            } while (true);
            } finally {dbg.exitSubRule(229);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1092, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1094:1: scss_mq_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* ;
    public final void scss_mq_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_mq_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1094, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1095:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            {
            dbg.location(1096,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            int alt230=2;
            try { dbg.enterSubRule(230);
            try { dbg.enterDecision(230, decisionCanBacktrack[230]);

            try {
                isCyclicDecision = true;
                alt230 = dfa230.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(230);}

            switch (alt230) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1097:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1097,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6843);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1099:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
                    {
                    dbg.location(1099,13);
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
            } finally {dbg.exitSubRule(230);}

            dbg.location(1101,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            try { dbg.enterSubRule(233);

            loop233:
            do {
                int alt233=2;
                try { dbg.enterDecision(233, decisionCanBacktrack[233]);

                try {
                    isCyclicDecision = true;
                    alt233 = dfa233.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(233);}

                switch (alt233) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    {
            	    dbg.location(1102,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:13: ( ws )?
            	    int alt231=2;
            	    try { dbg.enterSubRule(231);
            	    try { dbg.enterDecision(231, decisionCanBacktrack[231]);

            	    int LA231_0 = input.LA(1);

            	    if ( (LA231_0==WS||(LA231_0>=NL && LA231_0<=COMMENT)) ) {
            	        alt231=1;
            	    }
            	    } finally {dbg.exitDecision(231);}

            	    switch (alt231) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:13: ws
            	            {
            	            dbg.location(1102,13);
            	            pushFollow(FOLLOW_ws_in_scss_mq_interpolation_expression6936);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(231);}

            	    dbg.location(1103,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1103:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1104:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1104,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6975);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1106:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
            	            {
            	            dbg.location(1106,17);
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


            	    }
            	    break;

            	default :
            	    break loop233;
                }
            } while (true);
            } finally {dbg.exitSubRule(233);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1110, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1112:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1112, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1113:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1114,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7096); if (state.failed) return ;
            dbg.location(1114,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var7098); if (state.failed) return ;
            dbg.location(1114,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:28: ( ws )?
            int alt234=2;
            try { dbg.enterSubRule(234);
            try { dbg.enterDecision(234, decisionCanBacktrack[234]);

            int LA234_0 = input.LA(1);

            if ( (LA234_0==WS||(LA234_0>=NL && LA234_0<=COMMENT)) ) {
                alt234=1;
            }
            } finally {dbg.exitDecision(234);}

            switch (alt234) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:28: ws
                    {
                    dbg.location(1114,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7100);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(234);}

            dbg.location(1114,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:32: ( cp_variable | less_function_in_condition )
            int alt235=2;
            try { dbg.enterSubRule(235);
            try { dbg.enterDecision(235, decisionCanBacktrack[235]);

            int LA235_0 = input.LA(1);

            if ( (LA235_0==MEDIA_SYM||LA235_0==AT_IDENT||LA235_0==SASS_VAR) ) {
                alt235=1;
            }
            else if ( (LA235_0==IDENT) ) {
                alt235=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 235, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(235);}

            switch (alt235) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:34: cp_variable
                    {
                    dbg.location(1114,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var7105);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:48: less_function_in_condition
                    {
                    dbg.location(1114,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7109);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(235);}

            dbg.location(1114,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:77: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1114:77: ws
                    {
                    dbg.location(1114,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7113);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(236);}

            dbg.location(1114,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var7116); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1115, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1135:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1135, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1137,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties7160);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1137,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties7162); if (state.failed) return ;
            dbg.location(1137,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:20: ( ws )?
            int alt237=2;
            try { dbg.enterSubRule(237);
            try { dbg.enterDecision(237, decisionCanBacktrack[237]);

            int LA237_0 = input.LA(1);

            if ( (LA237_0==WS||(LA237_0>=NL && LA237_0<=COMMENT)) ) {
                alt237=1;
            }
            } finally {dbg.exitDecision(237);}

            switch (alt237) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:20: ws
                    {
                    dbg.location(1137,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7164);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(237);}

            dbg.location(1137,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:24: ( propertyValue )?
            int alt238=2;
            try { dbg.enterSubRule(238);
            try { dbg.enterDecision(238, decisionCanBacktrack[238]);

            int LA238_0 = input.LA(1);

            if ( ((LA238_0>=IDENT && LA238_0<=URI)||LA238_0==MEDIA_SYM||(LA238_0>=GEN && LA238_0<=LPAREN)||LA238_0==AT_IDENT||LA238_0==PERCENTAGE||LA238_0==PLUS||LA238_0==MINUS||LA238_0==HASH||(LA238_0>=NUMBER && LA238_0<=DIMENSION)||LA238_0==SASS_VAR) ) {
                alt238=1;
            }
            } finally {dbg.exitDecision(238);}

            switch (alt238) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:24: propertyValue
                    {
                    dbg.location(1137,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties7167);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(238);}

            dbg.location(1137,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties7170); if (state.failed) return ;
            dbg.location(1137,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:46: ws
                    {
                    dbg.location(1137,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7172);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(239);}

            dbg.location(1137,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties7175);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1137,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties7177);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1137,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties7179); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1138, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1140:1: sass_extend : SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI ;
    public final void sass_extend() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1140, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1141:5: ( SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:5: SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI
            {
            dbg.location(1142,5);
            match(input,SASS_EXTEND,FOLLOW_SASS_EXTEND_in_sass_extend7200); if (state.failed) return ;
            dbg.location(1142,17);
            pushFollow(FOLLOW_ws_in_sass_extend7202);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1142,20);
            pushFollow(FOLLOW_simpleSelectorSequence_in_sass_extend7204);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1142,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:43: ( SASS_OPTIONAL ( ws )? )?
            int alt241=2;
            try { dbg.enterSubRule(241);
            try { dbg.enterDecision(241, decisionCanBacktrack[241]);

            int LA241_0 = input.LA(1);

            if ( (LA241_0==SASS_OPTIONAL) ) {
                alt241=1;
            }
            } finally {dbg.exitDecision(241);}

            switch (alt241) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:44: SASS_OPTIONAL ( ws )?
                    {
                    dbg.location(1142,44);
                    match(input,SASS_OPTIONAL,FOLLOW_SASS_OPTIONAL_in_sass_extend7207); if (state.failed) return ;
                    dbg.location(1142,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:58: ( ws )?
                    int alt240=2;
                    try { dbg.enterSubRule(240);
                    try { dbg.enterDecision(240, decisionCanBacktrack[240]);

                    int LA240_0 = input.LA(1);

                    if ( (LA240_0==WS||(LA240_0>=NL && LA240_0<=COMMENT)) ) {
                        alt240=1;
                    }
                    } finally {dbg.exitDecision(240);}

                    switch (alt240) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:58: ws
                            {
                            dbg.location(1142,58);
                            pushFollow(FOLLOW_ws_in_sass_extend7209);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(240);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(241);}

            dbg.location(1142,64);
            match(input,SEMI,FOLLOW_SEMI_in_sass_extend7214); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1143, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1145:1: sass_extend_only_selector : SASS_EXTEND_ONLY_SELECTOR ;
    public final void sass_extend_only_selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend_only_selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1145, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:5: ( SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1147:5: SASS_EXTEND_ONLY_SELECTOR
            {
            dbg.location(1147,5);
            match(input,SASS_EXTEND_ONLY_SELECTOR,FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector7239); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1148, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_extend_only_selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_extend_only_selector"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(368,15);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(242);

        loop242:
        do {
            int alt242=2;
            try { dbg.enterDecision(242, decisionCanBacktrack[242]);

            int LA242_0 = input.LA(1);

            if ( ((LA242_0>=NAMESPACE_SYM && LA242_0<=MEDIA_SYM)||(LA242_0>=RBRACE && LA242_0<=MINUS)||(LA242_0>=HASH && LA242_0<=LINE_COMMENT)) ) {
                alt242=1;
            }


            } finally {dbg.exitDecision(242);}

            switch (alt242) {
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
        	    break loop242;
            }
        } while (true);
        } finally {dbg.exitSubRule(242);}

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
        int alt245=2;
        try { dbg.enterDecision(245, decisionCanBacktrack[245]);

        try {
            isCyclicDecision = true;
            alt245 = dfa245.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(245);}

        switch (alt245) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(376,18);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt243=0;
                try { dbg.enterSubRule(243);

                loop243:
                do {
                    int alt243=2;
                    try { dbg.enterDecision(243, decisionCanBacktrack[243]);

                    int LA243_0 = input.LA(1);

                    if ( (LA243_0==NAMESPACE_SYM||(LA243_0>=IDENT && LA243_0<=MEDIA_SYM)||(LA243_0>=AND && LA243_0<=LPAREN)||(LA243_0>=RPAREN && LA243_0<=LINE_COMMENT)) ) {
                        alt243=1;
                    }


                    } finally {dbg.exitDecision(243);}

                    switch (alt243) {
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
                	    if ( cnt243 >= 1 ) break loop243;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(243, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt243++;
                } while (true);
                } finally {dbg.exitSubRule(243);}

                dbg.location(376,47);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3624); if (state.failed) return ;
                dbg.location(376,53);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt244=0;
                try { dbg.enterSubRule(244);

                loop244:
                do {
                    int alt244=2;
                    try { dbg.enterDecision(244, decisionCanBacktrack[244]);

                    int LA244_0 = input.LA(1);

                    if ( (LA244_0==NAMESPACE_SYM||(LA244_0>=IDENT && LA244_0<=MEDIA_SYM)||(LA244_0>=AND && LA244_0<=LINE_COMMENT)) ) {
                        alt244=1;
                    }


                    } finally {dbg.exitDecision(244);}

                    switch (alt244) {
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
                	    if ( cnt244 >= 1 ) break loop244;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(244, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt244++;
                } while (true);
                } finally {dbg.exitSubRule(244);}

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt248=2;
        try { dbg.enterDecision(248, decisionCanBacktrack[248]);

        try {
            isCyclicDecision = true;
            alt248 = dfa248.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(248);}

        switch (alt248) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(583,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt246=0;
                try { dbg.enterSubRule(246);

                loop246:
                do {
                    int alt246=2;
                    try { dbg.enterDecision(246, decisionCanBacktrack[246]);

                    int LA246_0 = input.LA(1);

                    if ( (LA246_0==NAMESPACE_SYM||(LA246_0>=IDENT && LA246_0<=MEDIA_SYM)||(LA246_0>=AND && LA246_0<=LPAREN)||(LA246_0>=RPAREN && LA246_0<=LINE_COMMENT)) ) {
                        alt246=1;
                    }


                    } finally {dbg.exitDecision(246);}

                    switch (alt246) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(583,4);
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

                dbg.location(583,33);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32386); if (state.failed) return ;
                dbg.location(583,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt247=0;
                try { dbg.enterSubRule(247);

                loop247:
                do {
                    int alt247=2;
                    try { dbg.enterDecision(247, decisionCanBacktrack[247]);

                    int LA247_0 = input.LA(1);

                    if ( (LA247_0==NAMESPACE_SYM||(LA247_0>=IDENT && LA247_0<=MEDIA_SYM)||(LA247_0>=AND && LA247_0<=LINE_COMMENT)) ) {
                        alt247=1;
                    }


                    } finally {dbg.exitDecision(247);}

                    switch (alt247) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(583,39);
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
                	    if ( cnt247 >= 1 ) break loop247;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(247, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt247++;
                } while (true);
                } finally {dbg.exitSubRule(247);}

                dbg.location(583,62);
                match(input,SEMI,FOLLOW_SEMI_in_synpred4_Css32398); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:69: scss_declaration_interpolation_expression COLON
                {
                dbg.location(583,69);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32402);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(583,111);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32404); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )
        int alt251=2;
        try { dbg.enterDecision(251, decisionCanBacktrack[251]);

        try {
            isCyclicDecision = true;
            alt251 = dfa251.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(251);}

        switch (alt251) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE
                {
                dbg.location(585,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt249=0;
                try { dbg.enterSubRule(249);

                loop249:
                do {
                    int alt249=2;
                    try { dbg.enterDecision(249, decisionCanBacktrack[249]);

                    int LA249_0 = input.LA(1);

                    if ( (LA249_0==NAMESPACE_SYM||(LA249_0>=IDENT && LA249_0<=MEDIA_SYM)||(LA249_0>=AND && LA249_0<=LPAREN)||(LA249_0>=RPAREN && LA249_0<=LINE_COMMENT)) ) {
                        alt249=1;
                    }


                    } finally {dbg.exitDecision(249);}

                    switch (alt249) {
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

                dbg.location(585,33);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32434); if (state.failed) return ;
                dbg.location(585,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt250=0;
                try { dbg.enterSubRule(250);

                loop250:
                do {
                    int alt250=2;
                    try { dbg.enterDecision(250, decisionCanBacktrack[250]);

                    int LA250_0 = input.LA(1);

                    if ( (LA250_0==NAMESPACE_SYM||(LA250_0>=IDENT && LA250_0<=MEDIA_SYM)||(LA250_0>=AND && LA250_0<=LINE_COMMENT)) ) {
                        alt250=1;
                    }


                    } finally {dbg.exitDecision(250);}

                    switch (alt250) {
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
                	    if ( cnt250 >= 1 ) break loop250;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(250, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt250++;
                } while (true);
                } finally {dbg.exitSubRule(250);}

                dbg.location(585,62);
                match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32446); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:71: scss_declaration_interpolation_expression COLON
                {
                dbg.location(585,71);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32450);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(585,113);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32452); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(587,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:18: (~ ( LBRACE | SEMI | RBRACE ) )+
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

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(587,18);
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

        dbg.location(587,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred6_Css32492); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(597,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:16: (~ ( RBRACE ) )+
        int cnt253=0;
        try { dbg.enterSubRule(253);

        loop253:
        do {
            int alt253=2;
            try { dbg.enterDecision(253, decisionCanBacktrack[253]);

            int LA253_0 = input.LA(1);

            if ( ((LA253_0>=NAMESPACE_SYM && LA253_0<=LBRACE)||(LA253_0>=AND && LA253_0<=LINE_COMMENT)) ) {
                alt253=1;
            }


            } finally {dbg.exitDecision(253);}

            switch (alt253) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:16: ~ ( RBRACE )
        	    {
        	    dbg.location(597,16);
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
        	    if ( cnt253 >= 1 ) break loop253;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(253, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt253++;
        } while (true);
        } finally {dbg.exitSubRule(253);}

        dbg.location(597,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred7_Css32661); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(603,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(254);

        loop254:
        do {
            int alt254=2;
            try { dbg.enterDecision(254, decisionCanBacktrack[254]);

            int LA254_0 = input.LA(1);

            if ( ((LA254_0>=NAMESPACE_SYM && LA254_0<=MEDIA_SYM)||(LA254_0>=RBRACE && LA254_0<=MINUS)||(LA254_0>=HASH && LA254_0<=LINE_COMMENT)) ) {
                alt254=1;
            }


            } finally {dbg.exitDecision(254);}

            switch (alt254) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(603,11);
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
        	    break loop254;
            }
        } while (true);
        } finally {dbg.exitSubRule(254);}

        dbg.location(603,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred8_Css32719); if (state.failed) return ;
        dbg.location(603,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred8_Css32721); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:19: esPred
        {
        dbg.location(616,19);
        pushFollow(FOLLOW_esPred_in_synpred9_Css32819);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:6: esPred
        {
        dbg.location(618,6);
        pushFollow(FOLLOW_esPred_in_synpred10_Css32840);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(632,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:8: ( IDENT | STAR )?
        int alt255=2;
        try { dbg.enterSubRule(255);
        try { dbg.enterDecision(255, decisionCanBacktrack[255]);

        int LA255_0 = input.LA(1);

        if ( (LA255_0==IDENT||LA255_0==STAR) ) {
            alt255=1;
        }
        } finally {dbg.exitDecision(255);}

        switch (alt255) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(632,8);
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
        } finally {dbg.exitSubRule(255);}

        dbg.location(632,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred11_Css32958); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(731,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )*
        try { dbg.enterSubRule(256);

        loop256:
        do {
            int alt256=2;
            try { dbg.enterDecision(256, decisionCanBacktrack[256]);

            int LA256_0 = input.LA(1);

            if ( (LA256_0==NAMESPACE_SYM||(LA256_0>=IDENT && LA256_0<=LBRACE)||(LA256_0>=AND && LA256_0<=LPAREN)||(LA256_0>=RPAREN && LA256_0<=MINUS)||(LA256_0>=HASH && LA256_0<=LINE_COMMENT)) ) {
                alt256=1;
            }


            } finally {dbg.exitDecision(256);}

            switch (alt256) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:11: ~ ( HASH_SYMBOL | COLON | SEMI | RBRACE )
        	    {
        	    dbg.location(731,11);
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
        	    break loop256;
            }
        } while (true);
        } finally {dbg.exitSubRule(256);}

        dbg.location(731,51);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred12_Css33930); if (state.failed) return ;
        dbg.location(731,63);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Css33932); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:12: expressionPredicate
        {
        dbg.location(746,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred13_Css34018);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:17: term
        {
        dbg.location(918,17);
        pushFollow(FOLLOW_term_in_synpred14_Css35257);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1061,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred15_Css36330); if (state.failed) return ;
        dbg.location(1061,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred15_Css36332); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

    // $ANTLR start synpred16_Css3
    public final void synpred16_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1068,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred16_Css36454); if (state.failed) return ;
        dbg.location(1068,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred16_Css36456); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Css3

    // $ANTLR start synpred17_Css3
    public final void synpred17_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1079,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred17_Css36588); if (state.failed) return ;
        dbg.location(1079,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred17_Css36590); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Css3

    // $ANTLR start synpred18_Css3
    public final void synpred18_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1086:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1086:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1086,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred18_Css36708); if (state.failed) return ;
        dbg.location(1086,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred18_Css36710); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Css3

    // $ANTLR start synpred19_Css3
    public final void synpred19_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1097:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1097:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1097,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred19_Css36838); if (state.failed) return ;
        dbg.location(1097,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred19_Css36840); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Css3

    // $ANTLR start synpred20_Css3
    public final void synpred20_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1104:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1104:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1104,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred20_Css36970); if (state.failed) return ;
        dbg.location(1104,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred20_Css36972); if (state.failed) return ;

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
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA74 dfa74 = new DFA74(this);
    protected DFA100 dfa100 = new DFA100(this);
    protected DFA108 dfa108 = new DFA108(this);
    protected DFA113 dfa113 = new DFA113(this);
    protected DFA116 dfa116 = new DFA116(this);
    protected DFA134 dfa134 = new DFA134(this);
    protected DFA144 dfa144 = new DFA144(this);
    protected DFA148 dfa148 = new DFA148(this);
    protected DFA151 dfa151 = new DFA151(this);
    protected DFA157 dfa157 = new DFA157(this);
    protected DFA178 dfa178 = new DFA178(this);
    protected DFA182 dfa182 = new DFA182(this);
    protected DFA197 dfa197 = new DFA197(this);
    protected DFA202 dfa202 = new DFA202(this);
    protected DFA222 dfa222 = new DFA222(this);
    protected DFA225 dfa225 = new DFA225(this);
    protected DFA224 dfa224 = new DFA224(this);
    protected DFA230 dfa230 = new DFA230(this);
    protected DFA233 dfa233 = new DFA233(this);
    protected DFA232 dfa232 = new DFA232(this);
    protected DFA245 dfa245 = new DFA245(this);
    protected DFA248 dfa248 = new DFA248(this);
    protected DFA251 dfa251 = new DFA251(this);
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
        "\1\3\1\uffff\1\4\1\2\5\uffff\1\0\1\uffff\1\1\2\uffff}>";
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
                    case 1 : 
                        int LA25_11 = input.LA(1);

                         
                        int index25_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA25_3 = input.LA(1);

                         
                        int index25_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
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
                    case 4 : 
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
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 25, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA34_eotS =
        "\31\uffff";
    static final String DFA34_eofS =
        "\31\uffff";
    static final String DFA34_minS =
        "\1\6\1\uffff\6\0\2\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA34_maxS =
        "\1\136\1\uffff\6\0\2\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA34_acceptS =
        "\1\uffff\1\10\6\uffff\1\1\1\2\1\uffff\1\3\7\uffff\1\4\1\5\1\6\2"+
        "\uffff\1\7";
    static final String DFA34_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\7\5\uffff\1\10\6"+
        "\uffff\1\11\1\uffff}>";
    static final String[] DFA34_transitionS = {
            "\1\4\5\uffff\1\27\1\uffff\1\1\3\uffff\1\6\1\uffff\1\13\1\uffff"+
            "\1\7\1\uffff\1\25\3\uffff\1\25\1\uffff\1\23\1\uffff\1\24\24"+
            "\uffff\1\20\1\3\1\12\1\5\3\13\1\2\1\13\1\uffff\1\13\25\uffff"+
            "\1\10\1\13\7\uffff\1\11",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    class DFA34 extends DFA {

        public DFA34(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 34;
            this.eot = DFA34_eot;
            this.eof = DFA34_eof;
            this.min = DFA34_min;
            this.max = DFA34_max;
            this.accept = DFA34_accept;
            this.special = DFA34_special;
            this.transition = DFA34_transition;
        }
        public String getDescription() {
            return "()* loopback of 374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA34_0 = input.LA(1);

                         
                        int index34_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_0==RBRACE) ) {s = 1;}

                        else if ( (LA34_0==STAR) ) {s = 2;}

                        else if ( (LA34_0==HASH_SYMBOL) ) {s = 3;}

                        else if ( (LA34_0==IDENT) ) {s = 4;}

                        else if ( (LA34_0==DOT) ) {s = 5;}

                        else if ( (LA34_0==GEN) ) {s = 6;}

                        else if ( (LA34_0==AT_IDENT) ) {s = 7;}

                        else if ( (LA34_0==SASS_VAR) && (synpred3_Css3())) {s = 8;}

                        else if ( (LA34_0==SASS_EXTEND) ) {s = 9;}

                        else if ( (LA34_0==HASH) ) {s = 10;}

                        else if ( (LA34_0==COLON||(LA34_0>=LBRACKET && LA34_0<=SASS_EXTEND_ONLY_SELECTOR)||LA34_0==PIPE||LA34_0==LESS_AND||LA34_0==SASS_MIXIN) ) {s = 11;}

                        else if ( (LA34_0==MINUS) ) {s = 16;}

                        else if ( (LA34_0==PAGE_SYM) ) {s = 19;}

                        else if ( (LA34_0==FONT_FACE_SYM) ) {s = 20;}

                        else if ( (LA34_0==MOZ_DOCUMENT_SYM||LA34_0==WEBKIT_KEYFRAMES_SYM) ) {s = 21;}

                        else if ( (LA34_0==MEDIA_SYM) ) {s = 23;}

                         
                        input.seek(index34_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA34_2 = input.LA(1);

                         
                        int index34_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA34_3 = input.LA(1);

                         
                        int index34_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA34_4 = input.LA(1);

                         
                        int index34_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA34_5 = input.LA(1);

                         
                        int index34_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA34_6 = input.LA(1);

                         
                        int index34_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA34_7 = input.LA(1);

                         
                        int index34_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index34_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA34_10 = input.LA(1);

                         
                        int index34_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_10);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA34_16 = input.LA(1);

                         
                        int index34_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index34_16);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA34_23 = input.LA(1);

                         
                        int index34_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 24;}

                         
                        input.seek(index34_23);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 34, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA53_eotS =
        "\155\uffff";
    static final String DFA53_eofS =
        "\155\uffff";
    static final String DFA53_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\2\uffff\1\5\1\6\1\uffff\2\6\1\5\3\6"+
        "\5\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\1\5\2\6\2\5\2\6\1\5\1\6\1\5\2\6"+
        "\2\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\2\5\4\6\2\5\1\6\1\5\1\6\1\5\2\6"+
        "\1\5\1\6\2\5\1\6\1\5\3\6\1\5\1\6\1\5\4\6\2\5\1\6\1\5\1\6\1\5\6\6"+
        "\1\5\1\6\1\5\7\6";
    static final String DFA53_maxS =
        "\1\127\1\123\1\uffff\1\123\4\uffff\1\123\2\uffff\2\123\1\uffff\1"+
        "\125\2\123\1\131\4\125\2\123\1\132\1\125\1\123\4\125\1\123\1\125"+
        "\1\131\1\132\1\123\4\125\1\123\1\125\1\123\1\125\1\123\1\131\4\123"+
        "\4\125\1\123\4\125\3\123\3\125\1\123\1\125\1\123\1\125\2\123\4\125"+
        "\1\123\1\125\3\123\1\125\1\123\1\125\3\123\3\125\1\123\1\125\1\123"+
        "\1\125\6\123\1\125\1\123\1\125\7\123";
    static final String DFA53_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\2\uffff\1"+
        "\2\137\uffff";
    static final String DFA53_specialS =
        "\155\uffff}>";
    static final String[] DFA53_transitionS = {
            "\1\2\5\uffff\1\3\5\uffff\1\2\1\uffff\1\2\1\uffff\1\10\1\uffff"+
            "\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3\2\1\1\5\2\1"+
            "\uffff\1\2\25\uffff\1\11\1\2\1\12",
            "\1\13\6\uffff\1\2\4\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\31\uffff\2\2",
            "",
            "\1\15\6\uffff\1\15\1\uffff\5\15\1\16\2\uffff\1\14\35\uffff"+
            "\4\15\31\uffff\2\14",
            "",
            "",
            "",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\17\72\uffff\2\11",
            "",
            "",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\21\1\2\2\uffff"+
            "\1\20\32\uffff\14\2\1\uffff\1\2\22\uffff\2\20",
            "\1\15\6\uffff\1\15\1\uffff\5\15\1\16\2\uffff\1\14\35\uffff"+
            "\4\15\31\uffff\2\14",
            "",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\22\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\23\1\15\1\25\1\15\17\uffff\12\11\2\22\1\uffff\1\11",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\17\72\uffff\2\11",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\21\1\2\2\uffff"+
            "\1\20\32\uffff\14\2\1\uffff\1\2\22\uffff\2\20",
            "\3\12\3\uffff\1\26\5\uffff\1\12\2\uffff\1\30\1\26\6\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\3"+
            "\uffff\1\27\2\uffff\2\2",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\22\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\23\1\15\1\25\1\15\17\uffff\12\11\2\22\1\uffff\1\11",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\31\5\uffff\1\11\27\uffff\2\15\1"+
            "\25\1\15\17\uffff\12\11\2\31\1\uffff\1\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\32\1\uffff\1\11\1\33\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\37\3\uffff\1\11\13\uffff\12\11\2\33\2\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\40\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\40\2\11",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\12\7\uffff\1\2\11\uffff\1\42\72\uffff\2\42\6\uffff\1\2",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\31\5\uffff\1\11\27\uffff\2\15\1"+
            "\25\1\15\17\uffff\12\11\2\31\1\uffff\1\11",
            "\1\43\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\33\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11\2\33\2\11",
            "\1\45\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\44\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\46\1\15\17\uffff\12\11\2\44\1\uffff\1\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\50\1\uffff\1\11\1\51\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\52\3\uffff\1\11\13\uffff\12\11\2\51\2\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\53\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\53\2\11",
            "\1\54\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\40\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\40\2\11",
            "\3\12\3\uffff\1\56\5\uffff\1\12\3\uffff\1\56\1\55\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\55\1\uffff\1\57\2\uffff\2\2",
            "\1\12\7\uffff\1\2\11\uffff\1\42\72\uffff\2\42\6\uffff\1\2",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\3\15\1\37\31\uffff\2\60",
            "\1\45\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\44\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\46\1\15\17\uffff\12\11\2\44\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\61\1\uffff\1\11\1\62\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\66\3\uffff\1\11\13\uffff\12\11\2\62\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\67\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\67\2\11",
            "\1\71\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\70\5\uffff\1\11\27\uffff\2\15\1"+
            "\72\1\15\17\uffff\12\11\2\70\1\uffff\1\11",
            "\1\73\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\51\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11\2\51\2\11",
            "\1\74\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\53\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\53\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\3\15\1\37\31\uffff\2\60",
            "\3\12\3\uffff\1\56\5\uffff\1\12\3\uffff\1\56\1\55\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\55\1\uffff\1\57\2\uffff\2\2",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\4\15\31\uffff\2\60",
            "\1\75\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\62\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11\2\62\2\11",
            "\1\77\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\76\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\100\1\15\17\uffff\12\11\2\76\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\101\1\uffff\1\11\1\102\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\63\1\15\1\65\1\103\3\uffff\1\11\13\uffff\12\11\2\102"+
            "\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\104\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\104\2\11",
            "\1\105\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\67\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\67\2\11",
            "\1\71\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\70\5\uffff\1\11\27\uffff\2\15\1"+
            "\72\1\15\17\uffff\12\11\2\70\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\106\1\uffff\1\11\1\107\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\113\3\uffff\1\11\13\uffff\12\11\2"+
            "\107\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\114\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\114\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\3\15\1\52\31\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\3\15\1\52\31\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\3\15\1\66\31\uffff\2\116",
            "\1\77\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\76\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\100\1\15\17\uffff\12\11\2\76\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\117\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\63\1\15\1\65\1\121\3\uffff\1\11\13\uffff\12\11\2\120"+
            "\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\122\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\122\2\11",
            "\1\123\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\102\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11\2\102\2\11",
            "\1\124\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\104\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\104\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\3\15\1\66\31\uffff\2\116",
            "\1\125\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\107\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12\11\2\107\2\11",
            "\1\127\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\126\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\47\1\15\1\130\1\15\17\uffff\12\11\2\126\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\131\1\uffff\1\11\1\132\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\133\3\uffff\1\11\13\uffff\12\11\2"+
            "\132\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\134\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\134\2\11",
            "\1\135\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\114\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\114\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\4\15\31\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\4\15\31\uffff\2\116",
            "\1\136\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11\2\120\2\11",
            "\1\137\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\122\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\3\uffff\1\11\13\uffff\12\11"+
            "\2\122\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\3\15\1\103\31\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\3\15\1\103\31\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\3\15\1\113\31\uffff\2\141",
            "\1\127\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\126\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\47\1\15\1\130\1\15\17\uffff\12\11\2\126\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\142\1\uffff\1\11\1\143\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\144\3\uffff\1\11\13\uffff\12\11\2"+
            "\143\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\145\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\145\2\11",
            "\1\146\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\132\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12\11\2\132\2\11",
            "\1\147\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\134\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\134\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\3\15\1\113\31\uffff\2\141",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\3\15\1\121\31\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\3\15\1\121\31\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\4\15\31\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\4\15\31\uffff\2\141",
            "\1\151\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\143\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12\11\2\143\2\11",
            "\1\152\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\31\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\145\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\3\uffff\1\11\13\uffff\12"+
            "\11\2\145\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\3\15\1\133\31\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\3\15\1\133\31\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\4\15\31\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\3\15\1\144\31\uffff\2\154",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\3\15\1\144\31\uffff\2\154",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\4\15\31\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\4\15\31\uffff\2\154"
    };

    static final short[] DFA53_eot = DFA.unpackEncodedString(DFA53_eotS);
    static final short[] DFA53_eof = DFA.unpackEncodedString(DFA53_eofS);
    static final char[] DFA53_min = DFA.unpackEncodedStringToUnsignedChars(DFA53_minS);
    static final char[] DFA53_max = DFA.unpackEncodedStringToUnsignedChars(DFA53_maxS);
    static final short[] DFA53_accept = DFA.unpackEncodedString(DFA53_acceptS);
    static final short[] DFA53_special = DFA.unpackEncodedString(DFA53_specialS);
    static final short[][] DFA53_transition;

    static {
        int numStates = DFA53_transitionS.length;
        DFA53_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA53_transition[i] = DFA.unpackEncodedString(DFA53_transitionS[i]);
        }
    }

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = DFA53_eot;
            this.eof = DFA53_eof;
            this.min = DFA53_min;
            this.max = DFA53_max;
            this.accept = DFA53_accept;
            this.special = DFA53_special;
            this.transition = DFA53_transition;
        }
        public String getDescription() {
            return "418:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA74_eotS =
        "\4\uffff";
    static final String DFA74_eofS =
        "\4\uffff";
    static final String DFA74_minS =
        "\2\13\2\uffff";
    static final String DFA74_maxS =
        "\2\123\2\uffff";
    static final String DFA74_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA74_specialS =
        "\4\uffff}>";
    static final String[] DFA74_transitionS = {
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "",
            ""
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
            return "()* loopback of 483:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA100_eotS =
        "\6\uffff";
    static final String DFA100_eofS =
        "\6\uffff";
    static final String DFA100_minS =
        "\2\6\2\uffff\2\6";
    static final String DFA100_maxS =
        "\1\126\1\123\2\uffff\2\123";
    static final String DFA100_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA100_specialS =
        "\6\uffff}>";
    static final String[] DFA100_transitionS = {
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
            return "560:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA108_eotS =
        "\30\uffff";
    static final String DFA108_eofS =
        "\30\uffff";
    static final String DFA108_minS =
        "\1\6\7\0\1\uffff\1\0\5\uffff\1\0\3\uffff\1\0\4\uffff";
    static final String DFA108_maxS =
        "\1\136\7\0\1\uffff\1\0\5\uffff\1\0\3\uffff\1\0\4\uffff";
    static final String DFA108_acceptS =
        "\10\uffff\1\7\1\uffff\5\3\1\uffff\2\3\1\4\1\uffff\1\6\1\1\1\2\1"+
        "\5";
    static final String DFA108_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\5\uffff\1\11\3\uffff"+
        "\1\12\4\uffff}>";
    static final String[] DFA108_transitionS = {
            "\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\13\1\uffff"+
            "\1\23\36\uffff\1\17\1\2\1\11\1\4\1\20\1\21\1\16\1\1\1\14\1\uffff"+
            "\1\15\25\uffff\1\7\1\12\1\24\6\uffff\1\22",
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
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA108_eot = DFA.unpackEncodedString(DFA108_eotS);
    static final short[] DFA108_eof = DFA.unpackEncodedString(DFA108_eofS);
    static final char[] DFA108_min = DFA.unpackEncodedStringToUnsignedChars(DFA108_minS);
    static final char[] DFA108_max = DFA.unpackEncodedStringToUnsignedChars(DFA108_maxS);
    static final short[] DFA108_accept = DFA.unpackEncodedString(DFA108_acceptS);
    static final short[] DFA108_special = DFA.unpackEncodedString(DFA108_specialS);
    static final short[][] DFA108_transition;

    static {
        int numStates = DFA108_transitionS.length;
        DFA108_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA108_transition[i] = DFA.unpackEncodedString(DFA108_transitionS[i]);
        }
    }

    class DFA108 extends DFA {

        public DFA108(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 108;
            this.eot = DFA108_eot;
            this.eof = DFA108_eof;
            this.min = DFA108_min;
            this.max = DFA108_max;
            this.accept = DFA108_accept;
            this.special = DFA108_special;
            this.transition = DFA108_transition;
        }
        public String getDescription() {
            return "()* loopback of 579:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA108_0 = input.LA(1);

                         
                        int index108_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA108_0==STAR) ) {s = 1;}

                        else if ( (LA108_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA108_0==IDENT) ) {s = 3;}

                        else if ( (LA108_0==DOT) ) {s = 4;}

                        else if ( (LA108_0==GEN) ) {s = 5;}

                        else if ( (LA108_0==MEDIA_SYM) ) {s = 6;}

                        else if ( (LA108_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA108_0==RBRACE) ) {s = 8;}

                        else if ( (LA108_0==HASH) ) {s = 9;}

                        else if ( (LA108_0==SASS_MIXIN) && (synpred6_Css3())) {s = 10;}

                        else if ( (LA108_0==COLON) && (synpred6_Css3())) {s = 11;}

                        else if ( (LA108_0==PIPE) && (synpred6_Css3())) {s = 12;}

                        else if ( (LA108_0==LESS_AND) && (synpred6_Css3())) {s = 13;}

                        else if ( (LA108_0==SASS_EXTEND_ONLY_SELECTOR) && (synpred6_Css3())) {s = 14;}

                        else if ( (LA108_0==MINUS) ) {s = 15;}

                        else if ( (LA108_0==LBRACKET) && (synpred6_Css3())) {s = 16;}

                        else if ( (LA108_0==DCOLON) && (synpred6_Css3())) {s = 17;}

                        else if ( (LA108_0==SASS_EXTEND) ) {s = 18;}

                        else if ( (LA108_0==AT_IDENT) ) {s = 19;}

                        else if ( (LA108_0==SASS_INCLUDE) ) {s = 20;}

                         
                        input.seek(index108_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA108_1 = input.LA(1);

                         
                        int index108_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA108_2 = input.LA(1);

                         
                        int index108_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA108_3 = input.LA(1);

                         
                        int index108_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred5_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA108_4 = input.LA(1);

                         
                        int index108_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( ((((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||synpred6_Css3())) ) {s = 17;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 20;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA108_5 = input.LA(1);

                         
                        int index108_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred5_Css3()) ) {s = 22;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA108_6 = input.LA(1);

                         
                        int index108_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 21;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 22;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 23;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index108_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA108_7 = input.LA(1);

                         
                        int index108_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 21;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 22;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index108_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA108_9 = input.LA(1);

                         
                        int index108_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA108_15 = input.LA(1);

                         
                        int index108_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 21;}

                        else if ( (synpred6_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index108_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA108_19 = input.LA(1);

                         
                        int index108_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 21;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 22;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index108_19);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 108, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA113_eotS =
        "\17\uffff";
    static final String DFA113_eofS =
        "\17\uffff";
    static final String DFA113_minS =
        "\2\6\2\0\1\uffff\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA113_maxS =
        "\1\77\1\123\2\0\1\uffff\2\123\5\uffff\1\0\1\uffff\1\0";
    static final String DFA113_acceptS =
        "\4\uffff\1\2\2\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA113_specialS =
        "\1\0\1\2\1\1\1\6\1\uffff\1\3\1\5\5\uffff\1\4\1\uffff\1\7}>";
    static final String[] DFA113_transitionS = {
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

    static final short[] DFA113_eot = DFA.unpackEncodedString(DFA113_eotS);
    static final short[] DFA113_eof = DFA.unpackEncodedString(DFA113_eofS);
    static final char[] DFA113_min = DFA.unpackEncodedStringToUnsignedChars(DFA113_minS);
    static final char[] DFA113_max = DFA.unpackEncodedStringToUnsignedChars(DFA113_maxS);
    static final short[] DFA113_accept = DFA.unpackEncodedString(DFA113_acceptS);
    static final short[] DFA113_special = DFA.unpackEncodedString(DFA113_specialS);
    static final short[][] DFA113_transition;

    static {
        int numStates = DFA113_transitionS.length;
        DFA113_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA113_transition[i] = DFA.unpackEncodedString(DFA113_transitionS[i]);
        }
    }

    class DFA113 extends DFA {

        public DFA113(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 113;
            this.eot = DFA113_eot;
            this.eof = DFA113_eof;
            this.min = DFA113_min;
            this.max = DFA113_max;
            this.accept = DFA113_accept;
            this.special = DFA113_special;
            this.transition = DFA113_transition;
        }
        public String getDescription() {
            return "600:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA113_0 = input.LA(1);

                         
                        int index113_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA113_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA113_0==IDENT) ) {s = 2;}

                        else if ( (LA113_0==HASH) ) {s = 3;}

                        else if ( (LA113_0==GEN||(LA113_0>=LBRACKET && LA113_0<=PIPE)||LA113_0==LESS_AND) ) {s = 4;}

                        else if ( (LA113_0==DOT) ) {s = 5;}

                        else if ( (LA113_0==COLON) ) {s = 6;}

                        else if ( (LA113_0==MINUS) && (synpred8_Css3())) {s = 7;}

                         
                        input.seek(index113_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA113_2 = input.LA(1);

                         
                        int index113_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index113_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA113_1 = input.LA(1);

                         
                        int index113_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA113_1==LBRACE) && (synpred8_Css3())) {s = 8;}

                        else if ( (LA113_1==NAME) ) {s = 4;}

                        else if ( (LA113_1==WS||(LA113_1>=NL && LA113_1<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA113_1==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA113_1==IDENT||LA113_1==COLON||LA113_1==MINUS||(LA113_1>=HASH && LA113_1<=DOT)) && (synpred8_Css3())) {s = 11;}

                         
                        input.seek(index113_1);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA113_5 = input.LA(1);

                         
                        int index113_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA113_5==IDENT) ) {s = 12;}

                        else if ( (LA113_5==WS||(LA113_5>=NL && LA113_5<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA113_5==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA113_5==GEN) ) {s = 4;}

                        else if ( (LA113_5==COLON||LA113_5==MINUS||(LA113_5>=HASH && LA113_5<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( (LA113_5==LBRACE) && (synpred8_Css3())) {s = 13;}

                         
                        input.seek(index113_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA113_12 = input.LA(1);

                         
                        int index113_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index113_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA113_6 = input.LA(1);

                         
                        int index113_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA113_6==WS||(LA113_6>=NL && LA113_6<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA113_6==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA113_6==IDENT) ) {s = 14;}

                        else if ( (LA113_6==LBRACE) && (synpred8_Css3())) {s = 13;}

                        else if ( (LA113_6==COLON||LA113_6==MINUS||(LA113_6>=HASH && LA113_6<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( ((LA113_6>=NOT && LA113_6<=GEN)) ) {s = 4;}

                         
                        input.seek(index113_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA113_3 = input.LA(1);

                         
                        int index113_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index113_3);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA113_14 = input.LA(1);

                         
                        int index113_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index113_14);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 113, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA116_eotS =
        "\24\uffff";
    static final String DFA116_eofS =
        "\24\uffff";
    static final String DFA116_minS =
        "\1\5\7\uffff\6\0\6\uffff";
    static final String DFA116_maxS =
        "\1\137\7\uffff\6\0\6\uffff";
    static final String DFA116_acceptS =
        "\1\uffff\1\2\21\uffff\1\1";
    static final String DFA116_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\1\5\6\uffff}>";
    static final String[] DFA116_transitionS = {
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

    static final short[] DFA116_eot = DFA.unpackEncodedString(DFA116_eotS);
    static final short[] DFA116_eof = DFA.unpackEncodedString(DFA116_eofS);
    static final char[] DFA116_min = DFA.unpackEncodedStringToUnsignedChars(DFA116_minS);
    static final char[] DFA116_max = DFA.unpackEncodedStringToUnsignedChars(DFA116_maxS);
    static final short[] DFA116_accept = DFA.unpackEncodedString(DFA116_acceptS);
    static final short[] DFA116_special = DFA.unpackEncodedString(DFA116_specialS);
    static final short[][] DFA116_transition;

    static {
        int numStates = DFA116_transitionS.length;
        DFA116_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA116_transition[i] = DFA.unpackEncodedString(DFA116_transitionS[i]);
        }
    }

    class DFA116 extends DFA {

        public DFA116(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 116;
            this.eot = DFA116_eot;
            this.eof = DFA116_eof;
            this.min = DFA116_min;
            this.max = DFA116_max;
            this.accept = DFA116_accept;
            this.special = DFA116_special;
            this.transition = DFA116_transition;
        }
        public String getDescription() {
            return "()* loopback of 616:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA116_8 = input.LA(1);

                         
                        int index116_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred9_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 1;}

                         
                        input.seek(index116_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA116_9 = input.LA(1);

                         
                        int index116_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index116_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA116_10 = input.LA(1);

                         
                        int index116_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index116_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA116_11 = input.LA(1);

                         
                        int index116_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index116_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA116_12 = input.LA(1);

                         
                        int index116_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index116_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA116_13 = input.LA(1);

                         
                        int index116_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index116_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 116, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA134_eotS =
        "\4\uffff";
    static final String DFA134_eofS =
        "\4\uffff";
    static final String DFA134_minS =
        "\2\5\2\uffff";
    static final String DFA134_maxS =
        "\2\137\2\uffff";
    static final String DFA134_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA134_specialS =
        "\4\uffff}>";
    static final String[] DFA134_transitionS = {
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
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
            return "717:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA144_eotS =
        "\17\uffff";
    static final String DFA144_eofS =
        "\17\uffff";
    static final String DFA144_minS =
        "\2\6\10\0\1\uffff\1\6\2\0\1\uffff";
    static final String DFA144_maxS =
        "\2\125\10\0\1\uffff\1\125\2\0\1\uffff";
    static final String DFA144_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA144_specialS =
        "\2\uffff\1\6\1\3\1\11\1\0\1\2\1\1\1\7\1\5\2\uffff\1\10\1\4\1\uffff}>";
    static final String[] DFA144_transitionS = {
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

    static final short[] DFA144_eot = DFA.unpackEncodedString(DFA144_eotS);
    static final short[] DFA144_eof = DFA.unpackEncodedString(DFA144_eofS);
    static final char[] DFA144_min = DFA.unpackEncodedStringToUnsignedChars(DFA144_minS);
    static final char[] DFA144_max = DFA.unpackEncodedStringToUnsignedChars(DFA144_maxS);
    static final short[] DFA144_accept = DFA.unpackEncodedString(DFA144_acceptS);
    static final short[] DFA144_special = DFA.unpackEncodedString(DFA144_specialS);
    static final short[][] DFA144_transition;

    static {
        int numStates = DFA144_transitionS.length;
        DFA144_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA144_transition[i] = DFA.unpackEncodedString(DFA144_transitionS[i]);
        }
    }

    class DFA144 extends DFA {

        public DFA144(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 144;
            this.eot = DFA144_eot;
            this.eof = DFA144_eof;
            this.min = DFA144_min;
            this.max = DFA144_max;
            this.accept = DFA144_accept;
            this.special = DFA144_special;
            this.transition = DFA144_transition;
        }
        public String getDescription() {
            return "744:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA144_5 = input.LA(1);

                         
                        int index144_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_5);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA144_7 = input.LA(1);

                         
                        int index144_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA144_6 = input.LA(1);

                         
                        int index144_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA144_3 = input.LA(1);

                         
                        int index144_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA144_13 = input.LA(1);

                         
                        int index144_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_13);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA144_9 = input.LA(1);

                         
                        int index144_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index144_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA144_2 = input.LA(1);

                         
                        int index144_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_2);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA144_8 = input.LA(1);

                         
                        int index144_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index144_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA144_12 = input.LA(1);

                         
                        int index144_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_12);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA144_4 = input.LA(1);

                         
                        int index144_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index144_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 144, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA148_eotS =
        "\12\uffff";
    static final String DFA148_eofS =
        "\12\uffff";
    static final String DFA148_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA148_maxS =
        "\1\125\1\uffff\1\125\1\uffff\2\125\1\6\1\125\2\123";
    static final String DFA148_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA148_specialS =
        "\12\uffff}>";
    static final String[] DFA148_transitionS = {
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

    static final short[] DFA148_eot = DFA.unpackEncodedString(DFA148_eotS);
    static final short[] DFA148_eof = DFA.unpackEncodedString(DFA148_eofS);
    static final char[] DFA148_min = DFA.unpackEncodedStringToUnsignedChars(DFA148_minS);
    static final char[] DFA148_max = DFA.unpackEncodedStringToUnsignedChars(DFA148_maxS);
    static final short[] DFA148_accept = DFA.unpackEncodedString(DFA148_acceptS);
    static final short[] DFA148_special = DFA.unpackEncodedString(DFA148_specialS);
    static final short[][] DFA148_transition;

    static {
        int numStates = DFA148_transitionS.length;
        DFA148_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA148_transition[i] = DFA.unpackEncodedString(DFA148_transitionS[i]);
        }
    }

    class DFA148 extends DFA {

        public DFA148(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 148;
            this.eot = DFA148_eot;
            this.eof = DFA148_eof;
            this.min = DFA148_min;
            this.max = DFA148_max;
            this.accept = DFA148_accept;
            this.special = DFA148_special;
            this.transition = DFA148_transition;
        }
        public String getDescription() {
            return "()* loopback of 804:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA151_eotS =
        "\13\uffff";
    static final String DFA151_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA151_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA151_maxS =
        "\1\125\2\uffff\1\125\5\uffff\1\125\1\uffff";
    static final String DFA151_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA151_specialS =
        "\13\uffff}>";
    static final String[] DFA151_transitionS = {
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

    static final short[] DFA151_eot = DFA.unpackEncodedString(DFA151_eotS);
    static final short[] DFA151_eof = DFA.unpackEncodedString(DFA151_eofS);
    static final char[] DFA151_min = DFA.unpackEncodedStringToUnsignedChars(DFA151_minS);
    static final char[] DFA151_max = DFA.unpackEncodedStringToUnsignedChars(DFA151_maxS);
    static final short[] DFA151_accept = DFA.unpackEncodedString(DFA151_acceptS);
    static final short[] DFA151_special = DFA.unpackEncodedString(DFA151_specialS);
    static final short[][] DFA151_transition;

    static {
        int numStates = DFA151_transitionS.length;
        DFA151_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA151_transition[i] = DFA.unpackEncodedString(DFA151_transitionS[i]);
        }
    }

    class DFA151 extends DFA {

        public DFA151(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 151;
            this.eot = DFA151_eot;
            this.eof = DFA151_eof;
            this.min = DFA151_min;
            this.max = DFA151_max;
            this.accept = DFA151_accept;
            this.special = DFA151_special;
            this.transition = DFA151_transition;
        }
        public String getDescription() {
            return "809:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA157_eotS =
        "\10\uffff";
    static final String DFA157_eofS =
        "\10\uffff";
    static final String DFA157_minS =
        "\1\6\1\uffff\3\6\1\uffff\2\23";
    static final String DFA157_maxS =
        "\1\125\1\uffff\2\125\1\6\1\uffff\2\123";
    static final String DFA157_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA157_specialS =
        "\10\uffff}>";
    static final String[] DFA157_transitionS = {
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

    static final short[] DFA157_eot = DFA.unpackEncodedString(DFA157_eotS);
    static final short[] DFA157_eof = DFA.unpackEncodedString(DFA157_eofS);
    static final char[] DFA157_min = DFA.unpackEncodedStringToUnsignedChars(DFA157_minS);
    static final char[] DFA157_max = DFA.unpackEncodedStringToUnsignedChars(DFA157_maxS);
    static final short[] DFA157_accept = DFA.unpackEncodedString(DFA157_acceptS);
    static final short[] DFA157_special = DFA.unpackEncodedString(DFA157_specialS);
    static final short[][] DFA157_transition;

    static {
        int numStates = DFA157_transitionS.length;
        DFA157_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA157_transition[i] = DFA.unpackEncodedString(DFA157_transitionS[i]);
        }
    }

    class DFA157 extends DFA {

        public DFA157(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 157;
            this.eot = DFA157_eot;
            this.eof = DFA157_eof;
            this.min = DFA157_min;
            this.max = DFA157_max;
            this.accept = DFA157_accept;
            this.special = DFA157_special;
            this.transition = DFA157_transition;
        }
        public String getDescription() {
            return "837:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA178_eotS =
        "\36\uffff";
    static final String DFA178_eofS =
        "\36\uffff";
    static final String DFA178_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA178_maxS =
        "\1\125\1\uffff\2\125\10\uffff\1\125\10\0\1\125\10\0";
    static final String DFA178_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA178_specialS =
        "\1\4\14\uffff\1\15\1\14\1\17\1\10\1\12\1\0\1\20\1\1\1\uffff\1\11"+
        "\1\13\1\16\1\6\1\3\1\7\1\2\1\5}>";
    static final String[] DFA178_transitionS = {
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

    static final short[] DFA178_eot = DFA.unpackEncodedString(DFA178_eotS);
    static final short[] DFA178_eof = DFA.unpackEncodedString(DFA178_eofS);
    static final char[] DFA178_min = DFA.unpackEncodedStringToUnsignedChars(DFA178_minS);
    static final char[] DFA178_max = DFA.unpackEncodedStringToUnsignedChars(DFA178_maxS);
    static final short[] DFA178_accept = DFA.unpackEncodedString(DFA178_acceptS);
    static final short[] DFA178_special = DFA.unpackEncodedString(DFA178_specialS);
    static final short[][] DFA178_transition;

    static {
        int numStates = DFA178_transitionS.length;
        DFA178_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA178_transition[i] = DFA.unpackEncodedString(DFA178_transitionS[i]);
        }
    }

    class DFA178 extends DFA {

        public DFA178(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 178;
            this.eot = DFA178_eot;
            this.eof = DFA178_eof;
            this.min = DFA178_min;
            this.max = DFA178_max;
            this.accept = DFA178_accept;
            this.special = DFA178_special;
            this.transition = DFA178_transition;
        }
        public String getDescription() {
            return "()* loopback of 918:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA178_18 = input.LA(1);

                         
                        int index178_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_18);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA178_20 = input.LA(1);

                         
                        int index178_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_20);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA178_28 = input.LA(1);

                         
                        int index178_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_28);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA178_26 = input.LA(1);

                         
                        int index178_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_26);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA178_0 = input.LA(1);

                         
                        int index178_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA178_0==SEMI||LA178_0==COMMA||(LA178_0>=LBRACE && LA178_0<=RBRACE)||LA178_0==RPAREN||LA178_0==SOLIDUS||LA178_0==STAR||LA178_0==IMPORTANT_SYM||LA178_0==SASS_DEFAULT) ) {s = 1;}

                        else if ( (LA178_0==PLUS) ) {s = 2;}

                        else if ( (LA178_0==MINUS) ) {s = 3;}

                        else if ( (LA178_0==PERCENTAGE||(LA178_0>=NUMBER && LA178_0<=DIMENSION)) && (synpred14_Css3())) {s = 4;}

                        else if ( (LA178_0==STRING) && (synpred14_Css3())) {s = 5;}

                        else if ( (LA178_0==IDENT) && (synpred14_Css3())) {s = 6;}

                        else if ( (LA178_0==GEN) && (synpred14_Css3())) {s = 7;}

                        else if ( (LA178_0==URI) && (synpred14_Css3())) {s = 8;}

                        else if ( (LA178_0==HASH) && (synpred14_Css3())) {s = 9;}

                        else if ( (LA178_0==MEDIA_SYM||LA178_0==AT_IDENT) && (synpred14_Css3())) {s = 10;}

                        else if ( (LA178_0==SASS_VAR) && (synpred14_Css3())) {s = 11;}

                         
                        input.seek(index178_0);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA178_29 = input.LA(1);

                         
                        int index178_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_29);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA178_25 = input.LA(1);

                         
                        int index178_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_25);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA178_27 = input.LA(1);

                         
                        int index178_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_27);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA178_16 = input.LA(1);

                         
                        int index178_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_16);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA178_22 = input.LA(1);

                         
                        int index178_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_22);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA178_17 = input.LA(1);

                         
                        int index178_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_17);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA178_23 = input.LA(1);

                         
                        int index178_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_23);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA178_14 = input.LA(1);

                         
                        int index178_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA178_13 = input.LA(1);

                         
                        int index178_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA178_24 = input.LA(1);

                         
                        int index178_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_24);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA178_15 = input.LA(1);

                         
                        int index178_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA178_19 = input.LA(1);

                         
                        int index178_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index178_19);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 178, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA182_eotS =
        "\13\uffff";
    static final String DFA182_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA182_minS =
        "\1\6\2\uffff\1\23\5\uffff\1\23\1\uffff";
    static final String DFA182_maxS =
        "\1\125\2\uffff\1\123\5\uffff\1\123\1\uffff";
    static final String DFA182_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA182_specialS =
        "\13\uffff}>";
    static final String[] DFA182_transitionS = {
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

    static final short[] DFA182_eot = DFA.unpackEncodedString(DFA182_eotS);
    static final short[] DFA182_eof = DFA.unpackEncodedString(DFA182_eofS);
    static final char[] DFA182_min = DFA.unpackEncodedStringToUnsignedChars(DFA182_minS);
    static final char[] DFA182_max = DFA.unpackEncodedStringToUnsignedChars(DFA182_maxS);
    static final short[] DFA182_accept = DFA.unpackEncodedString(DFA182_acceptS);
    static final short[] DFA182_special = DFA.unpackEncodedString(DFA182_specialS);
    static final short[][] DFA182_transition;

    static {
        int numStates = DFA182_transitionS.length;
        DFA182_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA182_transition[i] = DFA.unpackEncodedString(DFA182_transitionS[i]);
        }
    }

    class DFA182 extends DFA {

        public DFA182(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 182;
            this.eot = DFA182_eot;
            this.eof = DFA182_eof;
            this.min = DFA182_min;
            this.max = DFA182_max;
            this.accept = DFA182_accept;
            this.special = DFA182_special;
            this.transition = DFA182_transition;
        }
        public String getDescription() {
            return "925:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA197_eotS =
        "\4\uffff";
    static final String DFA197_eofS =
        "\4\uffff";
    static final String DFA197_minS =
        "\2\5\2\uffff";
    static final String DFA197_maxS =
        "\2\123\2\uffff";
    static final String DFA197_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA197_specialS =
        "\4\uffff}>";
    static final String[] DFA197_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA197_eot = DFA.unpackEncodedString(DFA197_eotS);
    static final short[] DFA197_eof = DFA.unpackEncodedString(DFA197_eofS);
    static final char[] DFA197_min = DFA.unpackEncodedStringToUnsignedChars(DFA197_minS);
    static final char[] DFA197_max = DFA.unpackEncodedStringToUnsignedChars(DFA197_maxS);
    static final short[] DFA197_accept = DFA.unpackEncodedString(DFA197_acceptS);
    static final short[] DFA197_special = DFA.unpackEncodedString(DFA197_specialS);
    static final short[][] DFA197_transition;

    static {
        int numStates = DFA197_transitionS.length;
        DFA197_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA197_transition[i] = DFA.unpackEncodedString(DFA197_transitionS[i]);
        }
    }

    class DFA197 extends DFA {

        public DFA197(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 197;
            this.eot = DFA197_eot;
            this.eof = DFA197_eof;
            this.min = DFA197_min;
            this.max = DFA197_max;
            this.accept = DFA197_accept;
            this.special = DFA197_special;
            this.transition = DFA197_transition;
        }
        public String getDescription() {
            return "973:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA202_eotS =
        "\5\uffff";
    static final String DFA202_eofS =
        "\5\uffff";
    static final String DFA202_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA202_maxS =
        "\1\25\1\131\1\uffff\1\131\1\uffff";
    static final String DFA202_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA202_specialS =
        "\5\uffff}>";
    static final String[] DFA202_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
    };

    static final short[] DFA202_eot = DFA.unpackEncodedString(DFA202_eotS);
    static final short[] DFA202_eof = DFA.unpackEncodedString(DFA202_eofS);
    static final char[] DFA202_min = DFA.unpackEncodedStringToUnsignedChars(DFA202_minS);
    static final char[] DFA202_max = DFA.unpackEncodedStringToUnsignedChars(DFA202_maxS);
    static final short[] DFA202_accept = DFA.unpackEncodedString(DFA202_acceptS);
    static final short[] DFA202_special = DFA.unpackEncodedString(DFA202_specialS);
    static final short[][] DFA202_transition;

    static {
        int numStates = DFA202_transitionS.length;
        DFA202_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA202_transition[i] = DFA.unpackEncodedString(DFA202_transitionS[i]);
        }
    }

    class DFA202 extends DFA {

        public DFA202(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 202;
            this.eot = DFA202_eot;
            this.eof = DFA202_eof;
            this.min = DFA202_min;
            this.max = DFA202_max;
            this.accept = DFA202_accept;
            this.special = DFA202_special;
            this.transition = DFA202_transition;
        }
        public String getDescription() {
            return "()* loopback of 993:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA222_eotS =
        "\16\uffff";
    static final String DFA222_eofS =
        "\16\uffff";
    static final String DFA222_minS =
        "\2\6\1\uffff\3\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA222_maxS =
        "\1\70\1\123\1\uffff\2\136\5\123\1\uffff\2\123\1\uffff";
    static final String DFA222_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA222_specialS =
        "\5\uffff\1\1\1\6\1\5\1\2\1\4\1\uffff\1\0\1\3\1\uffff}>";
    static final String[] DFA222_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\31\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2",
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

    static final short[] DFA222_eot = DFA.unpackEncodedString(DFA222_eotS);
    static final short[] DFA222_eof = DFA.unpackEncodedString(DFA222_eofS);
    static final char[] DFA222_min = DFA.unpackEncodedStringToUnsignedChars(DFA222_minS);
    static final char[] DFA222_max = DFA.unpackEncodedStringToUnsignedChars(DFA222_maxS);
    static final short[] DFA222_accept = DFA.unpackEncodedString(DFA222_acceptS);
    static final short[] DFA222_special = DFA.unpackEncodedString(DFA222_specialS);
    static final short[][] DFA222_transition;

    static {
        int numStates = DFA222_transitionS.length;
        DFA222_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA222_transition[i] = DFA.unpackEncodedString(DFA222_transitionS[i]);
        }
    }

    class DFA222 extends DFA {

        public DFA222(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 222;
            this.eot = DFA222_eot;
            this.eof = DFA222_eof;
            this.min = DFA222_min;
            this.max = DFA222_max;
            this.accept = DFA222_accept;
            this.special = DFA222_special;
            this.transition = DFA222_transition;
        }
        public String getDescription() {
            return "1060:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA222_11 = input.LA(1);

                         
                        int index222_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_11==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA222_11==WS||(LA222_11>=NL && LA222_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_11==COLON) ) {s = 2;}

                         
                        input.seek(index222_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA222_5 = input.LA(1);

                         
                        int index222_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_5==WS||(LA222_5>=NL && LA222_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA222_5==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA222_5==IDENT||LA222_5==LBRACE||(LA222_5>=AND && LA222_5<=COLON)||(LA222_5>=MINUS && LA222_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index222_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA222_8 = input.LA(1);

                         
                        int index222_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_8==WS||(LA222_8>=NL && LA222_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_8==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA222_8==COLON) ) {s = 2;}

                         
                        input.seek(index222_8);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA222_12 = input.LA(1);

                         
                        int index222_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_12==LPAREN) && (synpred15_Css3())) {s = 13;}

                        else if ( (LA222_12==WS||(LA222_12>=NL && LA222_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA222_12==IDENT||LA222_12==COMMA||LA222_12==LBRACE||LA222_12==GEN||LA222_12==COLON||(LA222_12>=PLUS && LA222_12<=PIPE)||LA222_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index222_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA222_9 = input.LA(1);

                         
                        int index222_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_9==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA222_9==WS||(LA222_9>=NL && LA222_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA222_9==IDENT||LA222_9==LBRACE||(LA222_9>=AND && LA222_9<=COLON)||(LA222_9>=MINUS && LA222_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index222_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA222_7 = input.LA(1);

                         
                        int index222_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_7==WS||(LA222_7>=NL && LA222_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA222_7==LPAREN) && (synpred15_Css3())) {s = 13;}

                        else if ( (LA222_7==IDENT||LA222_7==COMMA||LA222_7==LBRACE||LA222_7==GEN||LA222_7==COLON||(LA222_7>=PLUS && LA222_7<=PIPE)||LA222_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index222_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA222_6 = input.LA(1);

                         
                        int index222_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_6==WS||(LA222_6>=NL && LA222_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_6==RBRACE) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA222_6==COLON) ) {s = 2;}

                         
                        input.seek(index222_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 222, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA225_eotS =
        "\4\uffff";
    static final String DFA225_eofS =
        "\4\uffff";
    static final String DFA225_minS =
        "\2\6\2\uffff";
    static final String DFA225_maxS =
        "\2\123\2\uffff";
    static final String DFA225_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA225_specialS =
        "\4\uffff}>";
    static final String[] DFA225_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\31\uffff"+
            "\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\31\uffff"+
            "\2\1",
            "",
            ""
    };

    static final short[] DFA225_eot = DFA.unpackEncodedString(DFA225_eotS);
    static final short[] DFA225_eof = DFA.unpackEncodedString(DFA225_eofS);
    static final char[] DFA225_min = DFA.unpackEncodedStringToUnsignedChars(DFA225_minS);
    static final char[] DFA225_max = DFA.unpackEncodedStringToUnsignedChars(DFA225_maxS);
    static final short[] DFA225_accept = DFA.unpackEncodedString(DFA225_acceptS);
    static final short[] DFA225_special = DFA.unpackEncodedString(DFA225_specialS);
    static final short[][] DFA225_transition;

    static {
        int numStates = DFA225_transitionS.length;
        DFA225_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA225_transition[i] = DFA.unpackEncodedString(DFA225_transitionS[i]);
        }
    }

    class DFA225 extends DFA {

        public DFA225(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 225;
            this.eot = DFA225_eot;
            this.eof = DFA225_eof;
            this.min = DFA225_min;
            this.max = DFA225_max;
            this.accept = DFA225_accept;
            this.special = DFA225_special;
            this.transition = DFA225_transition;
        }
        public String getDescription() {
            return "()* loopback of 1065:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*";
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
        "\1\70\1\123\1\uffff\2\136\5\123\1\uffff\2\123\1\uffff";
    static final String DFA224_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA224_specialS =
        "\5\uffff\1\1\1\6\1\5\1\2\1\4\1\uffff\1\0\1\3\1\uffff}>";
    static final String[] DFA224_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\31\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2\6\uffff\1\2",
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
            return "1067:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA224_11 = input.LA(1);

                         
                        int index224_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_11==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA224_11==WS||(LA224_11>=NL && LA224_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_11==COLON) ) {s = 2;}

                         
                        input.seek(index224_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA224_5 = input.LA(1);

                         
                        int index224_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_5==WS||(LA224_5>=NL && LA224_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA224_5==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA224_5==IDENT||LA224_5==LBRACE||(LA224_5>=AND && LA224_5<=COLON)||(LA224_5>=MINUS && LA224_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index224_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA224_8 = input.LA(1);

                         
                        int index224_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_8==WS||(LA224_8>=NL && LA224_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_8==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA224_8==COLON) ) {s = 2;}

                         
                        input.seek(index224_8);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA224_12 = input.LA(1);

                         
                        int index224_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_12==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA224_12==WS||(LA224_12>=NL && LA224_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA224_12==IDENT||LA224_12==COMMA||LA224_12==LBRACE||LA224_12==GEN||LA224_12==COLON||(LA224_12>=PLUS && LA224_12<=PIPE)||LA224_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index224_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA224_9 = input.LA(1);

                         
                        int index224_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_9==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA224_9==WS||(LA224_9>=NL && LA224_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA224_9==IDENT||LA224_9==LBRACE||(LA224_9>=AND && LA224_9<=COLON)||(LA224_9>=MINUS && LA224_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index224_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA224_7 = input.LA(1);

                         
                        int index224_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_7==WS||(LA224_7>=NL && LA224_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA224_7==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA224_7==IDENT||LA224_7==COMMA||LA224_7==LBRACE||LA224_7==GEN||LA224_7==COLON||(LA224_7>=PLUS && LA224_7<=PIPE)||LA224_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index224_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA224_6 = input.LA(1);

                         
                        int index224_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA224_6==WS||(LA224_6>=NL && LA224_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA224_6==RBRACE) && (synpred16_Css3())) {s = 10;}

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
    static final String DFA230_eotS =
        "\17\uffff";
    static final String DFA230_eofS =
        "\17\uffff";
    static final String DFA230_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA230_maxS =
        "\1\70\1\123\1\uffff\2\136\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA230_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA230_specialS =
        "\5\uffff\1\5\1\6\1\7\1\0\1\2\1\uffff\1\1\1\3\1\uffff\1\4}>";
    static final String[] DFA230_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2",
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

    static final short[] DFA230_eot = DFA.unpackEncodedString(DFA230_eotS);
    static final short[] DFA230_eof = DFA.unpackEncodedString(DFA230_eofS);
    static final char[] DFA230_min = DFA.unpackEncodedStringToUnsignedChars(DFA230_minS);
    static final char[] DFA230_max = DFA.unpackEncodedStringToUnsignedChars(DFA230_maxS);
    static final short[] DFA230_accept = DFA.unpackEncodedString(DFA230_acceptS);
    static final short[] DFA230_special = DFA.unpackEncodedString(DFA230_specialS);
    static final short[][] DFA230_transition;

    static {
        int numStates = DFA230_transitionS.length;
        DFA230_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA230_transition[i] = DFA.unpackEncodedString(DFA230_transitionS[i]);
        }
    }

    class DFA230 extends DFA {

        public DFA230(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 230;
            this.eot = DFA230_eot;
            this.eof = DFA230_eof;
            this.min = DFA230_min;
            this.max = DFA230_max;
            this.accept = DFA230_accept;
            this.special = DFA230_special;
            this.transition = DFA230_transition;
        }
        public String getDescription() {
            return "1096:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA230_8 = input.LA(1);

                         
                        int index230_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_8==WS||(LA230_8>=NL && LA230_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA230_8==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA230_8==IDENT||LA230_8==LBRACE||(LA230_8>=AND && LA230_8<=COLON)||(LA230_8>=MINUS && LA230_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index230_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA230_11 = input.LA(1);

                         
                        int index230_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_11==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA230_11==WS||(LA230_11>=NL && LA230_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA230_11==COLON) ) {s = 2;}

                         
                        input.seek(index230_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA230_9 = input.LA(1);

                         
                        int index230_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_9==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA230_9==WS) ) {s = 9;}

                        else if ( ((LA230_9>=IDENT && LA230_9<=STRING)||LA230_9==LBRACE||LA230_9==COLON) ) {s = 2;}

                        else if ( ((LA230_9>=NL && LA230_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index230_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA230_12 = input.LA(1);

                         
                        int index230_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_12==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA230_12==WS||(LA230_12>=NL && LA230_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA230_12==IDENT||LA230_12==COMMA||LA230_12==LBRACE||LA230_12==GEN||LA230_12==COLON||(LA230_12>=PLUS && LA230_12<=PIPE)||LA230_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index230_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA230_14 = input.LA(1);

                         
                        int index230_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_14==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA230_14==WS||(LA230_14>=NL && LA230_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA230_14==IDENT||LA230_14==LBRACE||(LA230_14>=AND && LA230_14<=COLON)||(LA230_14>=MINUS && LA230_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index230_14);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA230_5 = input.LA(1);

                         
                        int index230_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_5==WS) ) {s = 9;}

                        else if ( (LA230_5==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( ((LA230_5>=IDENT && LA230_5<=STRING)||LA230_5==LBRACE||LA230_5==COLON) ) {s = 2;}

                        else if ( ((LA230_5>=NL && LA230_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index230_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA230_6 = input.LA(1);

                         
                        int index230_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_6==WS||(LA230_6>=NL && LA230_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA230_6==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA230_6==COLON) ) {s = 2;}

                         
                        input.seek(index230_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA230_7 = input.LA(1);

                         
                        int index230_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_7==WS||(LA230_7>=NL && LA230_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA230_7==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA230_7==IDENT||LA230_7==COMMA||LA230_7==LBRACE||LA230_7==GEN||LA230_7==COLON||(LA230_7>=PLUS && LA230_7<=PIPE)||LA230_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index230_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 230, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA233_eotS =
        "\4\uffff";
    static final String DFA233_eofS =
        "\4\uffff";
    static final String DFA233_minS =
        "\2\6\2\uffff";
    static final String DFA233_maxS =
        "\2\123\2\uffff";
    static final String DFA233_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA233_specialS =
        "\4\uffff}>";
    static final String[] DFA233_transitionS = {
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA233_eot = DFA.unpackEncodedString(DFA233_eotS);
    static final short[] DFA233_eof = DFA.unpackEncodedString(DFA233_eofS);
    static final char[] DFA233_min = DFA.unpackEncodedStringToUnsignedChars(DFA233_minS);
    static final char[] DFA233_max = DFA.unpackEncodedStringToUnsignedChars(DFA233_maxS);
    static final short[] DFA233_accept = DFA.unpackEncodedString(DFA233_acceptS);
    static final short[] DFA233_special = DFA.unpackEncodedString(DFA233_specialS);
    static final short[][] DFA233_transition;

    static {
        int numStates = DFA233_transitionS.length;
        DFA233_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA233_transition[i] = DFA.unpackEncodedString(DFA233_transitionS[i]);
        }
    }

    class DFA233 extends DFA {

        public DFA233(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 233;
            this.eot = DFA233_eot;
            this.eof = DFA233_eof;
            this.min = DFA233_min;
            this.max = DFA233_max;
            this.accept = DFA233_accept;
            this.special = DFA233_special;
            this.transition = DFA233_transition;
        }
        public String getDescription() {
            return "()* loopback of 1101:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA232_eotS =
        "\17\uffff";
    static final String DFA232_eofS =
        "\17\uffff";
    static final String DFA232_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA232_maxS =
        "\1\70\1\123\1\uffff\2\136\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA232_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA232_specialS =
        "\5\uffff\1\5\1\6\1\7\1\0\1\2\1\uffff\1\1\1\3\1\uffff\1\4}>";
    static final String[] DFA232_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2",
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
            return "1103:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA232_8 = input.LA(1);

                         
                        int index232_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_8==WS||(LA232_8>=NL && LA232_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA232_8==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA232_8==IDENT||LA232_8==LBRACE||(LA232_8>=AND && LA232_8<=COLON)||(LA232_8>=MINUS && LA232_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index232_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA232_11 = input.LA(1);

                         
                        int index232_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_11==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA232_11==WS||(LA232_11>=NL && LA232_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA232_11==COLON) ) {s = 2;}

                         
                        input.seek(index232_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA232_9 = input.LA(1);

                         
                        int index232_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_9==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA232_9==WS) ) {s = 9;}

                        else if ( ((LA232_9>=IDENT && LA232_9<=STRING)||LA232_9==LBRACE||LA232_9==COLON) ) {s = 2;}

                        else if ( ((LA232_9>=NL && LA232_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index232_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA232_12 = input.LA(1);

                         
                        int index232_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_12==LPAREN) && (synpred20_Css3())) {s = 13;}

                        else if ( (LA232_12==WS||(LA232_12>=NL && LA232_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA232_12==IDENT||LA232_12==COMMA||LA232_12==LBRACE||LA232_12==GEN||LA232_12==COLON||(LA232_12>=PLUS && LA232_12<=PIPE)||LA232_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index232_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA232_14 = input.LA(1);

                         
                        int index232_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_14==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA232_14==WS||(LA232_14>=NL && LA232_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA232_14==IDENT||LA232_14==LBRACE||(LA232_14>=AND && LA232_14<=COLON)||(LA232_14>=MINUS && LA232_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index232_14);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA232_5 = input.LA(1);

                         
                        int index232_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_5==WS) ) {s = 9;}

                        else if ( (LA232_5==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( ((LA232_5>=IDENT && LA232_5<=STRING)||LA232_5==LBRACE||LA232_5==COLON) ) {s = 2;}

                        else if ( ((LA232_5>=NL && LA232_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index232_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA232_6 = input.LA(1);

                         
                        int index232_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_6==WS||(LA232_6>=NL && LA232_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA232_6==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA232_6==COLON) ) {s = 2;}

                         
                        input.seek(index232_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA232_7 = input.LA(1);

                         
                        int index232_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA232_7==WS||(LA232_7>=NL && LA232_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA232_7==LPAREN) && (synpred20_Css3())) {s = 13;}

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
    static final String DFA245_eotS =
        "\11\uffff";
    static final String DFA245_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA245_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA245_maxS =
        "\3\u0084\2\uffff\4\u0084";
    static final String DFA245_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA245_specialS =
        "\11\uffff}>";
    static final String[] DFA245_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\114"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\166\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3"
    };

    static final short[] DFA245_eot = DFA.unpackEncodedString(DFA245_eotS);
    static final short[] DFA245_eof = DFA.unpackEncodedString(DFA245_eofS);
    static final char[] DFA245_min = DFA.unpackEncodedStringToUnsignedChars(DFA245_minS);
    static final char[] DFA245_max = DFA.unpackEncodedStringToUnsignedChars(DFA245_maxS);
    static final short[] DFA245_accept = DFA.unpackEncodedString(DFA245_acceptS);
    static final short[] DFA245_special = DFA.unpackEncodedString(DFA245_specialS);
    static final short[][] DFA245_transition;

    static {
        int numStates = DFA245_transitionS.length;
        DFA245_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA245_transition[i] = DFA.unpackEncodedString(DFA245_transitionS[i]);
        }
    }

    class DFA245 extends DFA {

        public DFA245(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 245;
            this.eot = DFA245_eot;
            this.eof = DFA245_eof;
            this.min = DFA245_min;
            this.max = DFA245_max;
            this.accept = DFA245_accept;
            this.special = DFA245_special;
            this.transition = DFA245_transition;
        }
        public String getDescription() {
            return "376:17: synpred3_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA248_eotS =
        "\11\uffff";
    static final String DFA248_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA248_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA248_maxS =
        "\3\u0084\2\uffff\4\u0084";
    static final String DFA248_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA248_specialS =
        "\11\uffff}>";
    static final String[] DFA248_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\114"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\166\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3"
    };

    static final short[] DFA248_eot = DFA.unpackEncodedString(DFA248_eotS);
    static final short[] DFA248_eof = DFA.unpackEncodedString(DFA248_eofS);
    static final char[] DFA248_min = DFA.unpackEncodedStringToUnsignedChars(DFA248_minS);
    static final char[] DFA248_max = DFA.unpackEncodedStringToUnsignedChars(DFA248_maxS);
    static final short[] DFA248_accept = DFA.unpackEncodedString(DFA248_acceptS);
    static final short[] DFA248_special = DFA.unpackEncodedString(DFA248_specialS);
    static final short[][] DFA248_transition;

    static {
        int numStates = DFA248_transitionS.length;
        DFA248_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA248_transition[i] = DFA.unpackEncodedString(DFA248_transitionS[i]);
        }
    }

    class DFA248 extends DFA {

        public DFA248(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 248;
            this.eot = DFA248_eot;
            this.eof = DFA248_eof;
            this.min = DFA248_min;
            this.max = DFA248_max;
            this.accept = DFA248_accept;
            this.special = DFA248_special;
            this.transition = DFA248_transition;
        }
        public String getDescription() {
            return "583:3: synpred4_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA251_eotS =
        "\11\uffff";
    static final String DFA251_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA251_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA251_maxS =
        "\3\u0084\2\uffff\4\u0084";
    static final String DFA251_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA251_specialS =
        "\11\uffff}>";
    static final String[] DFA251_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\114"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\166\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\61\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\61\3"
    };

    static final short[] DFA251_eot = DFA.unpackEncodedString(DFA251_eotS);
    static final short[] DFA251_eof = DFA.unpackEncodedString(DFA251_eofS);
    static final char[] DFA251_min = DFA.unpackEncodedStringToUnsignedChars(DFA251_minS);
    static final char[] DFA251_max = DFA.unpackEncodedStringToUnsignedChars(DFA251_maxS);
    static final short[] DFA251_accept = DFA.unpackEncodedString(DFA251_acceptS);
    static final short[] DFA251_special = DFA.unpackEncodedString(DFA251_specialS);
    static final short[][] DFA251_transition;

    static {
        int numStates = DFA251_transitionS.length;
        DFA251_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA251_transition[i] = DFA.unpackEncodedString(DFA251_transitionS[i]);
        }
    }

    class DFA251 extends DFA {

        public DFA251(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 251;
            this.eot = DFA251_eot;
            this.eof = DFA251_eof;
            this.min = DFA251_min;
            this.max = DFA251_max;
            this.accept = DFA251_accept;
            this.special = DFA251_special;
            this.transition = DFA251_transition;
        }
        public String getDescription() {
            return "585:3: synpred5_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0xBFE00001D1541650L,0x0000000000E00000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0xBFE00001D1D41450L,0x0000000000EC0000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0xBFE00001D1541450L,0x0000000000E00000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0xBFE00001D1541050L,0x0000000000E00000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0xBFE00001D1541040L,0x0000000000E00000L});
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
    public static final BitSet FOLLOW_LBRACE_in_media558 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media560 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_declaration_in_media646 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_media648 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media650 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_sass_extend_in_media673 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media675 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_rule_in_media713 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media716 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_page_in_media737 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media740 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_fontFace_in_media761 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media764 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media785 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media788 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_media_in_media811 = new BitSet(new long[]{0xBFE0000151D45040L,0x00000000406C0000L});
    public static final BitSet FOLLOW_ws_in_media813 = new BitSet(new long[]{0xBFE0000151545040L,0x0000000040600000L});
    public static final BitSet FOLLOW_RBRACE_in_media857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList873 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList877 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList879 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList882 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery901 = new BitSet(new long[]{0x0000000000870040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery903 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery910 = new BitSet(new long[]{0x0000000000808002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery912 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery917 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery919 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery922 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery930 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery934 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery936 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery939 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression994 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression996 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression999 = new BitSet(new long[]{0x0000000000B00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1001 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression1006 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1008 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_mediaExpression1011 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression1016 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body1050 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000000EC0000L});
    public static final BitSet FOLLOW_ws_in_body1052 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000000E00000L});
    public static final BitSet FOLLOW_rule_in_bodyItem1077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem1101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem1113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem1137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem1165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule1188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule1192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule1196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule1232 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1234 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule1239 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1241 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule1256 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule1268 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule1278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1294 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1296 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1301 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1303 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document1309 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1311 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1314 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1316 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document1323 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000000EC0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1325 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000000E00000L});
    public static final BitSet FOLLOW_body_in_moz_document1330 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document1335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1376 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1378 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1381 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1383 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1388 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1390 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1397 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1399 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1419 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1421 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1426 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1429 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1432 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1436 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1454 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1466 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1469 = new BitSet(new long[]{0x0000000020800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1471 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1474 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1503 = new BitSet(new long[]{0x0000000000902040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1505 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1510 = new BitSet(new long[]{0x0000000000902000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1512 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1519 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1521 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1534 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1536 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1591 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1593 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1595 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1601 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1603 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1607 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1609 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1611 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1647 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1649 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1652 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1654 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1665 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1667 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1670 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1674 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1705 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1707 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1718 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1720 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1723 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1727 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1752 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_margin1754 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1757 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_margin1759 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1762 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_margin1764 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1995 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator2047 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator2058 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator2069 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property2132 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_property2136 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_property2142 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_property2145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule2189 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule2222 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule2245 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_rule2247 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule2250 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_rule2264 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule2274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations2408 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2410 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2412 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2456 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2458 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_rule_in_declarations2495 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2497 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_sass_extend_in_declarations2536 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2538 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_media_in_declarations2577 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2579 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2618 = new BitSet(new long[]{0xBFE00001D1D41042L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2620 = new BitSet(new long[]{0xBFE00001D1541042L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2725 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2742 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2745 = new BitSet(new long[]{0xBFE0000000940040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2747 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2750 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2777 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_combinator_in_selector2780 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2782 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2815 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2822 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2824 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2843 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2845 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2961 = new BitSet(new long[]{0xB000000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2967 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2987 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2991 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_extend_only_selector_in_elementSubsequent3034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent3043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent3052 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent3064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent3076 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId3104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId3110 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId3112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass3140 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass3142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute3214 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute3221 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3224 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute3235 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C007FL});
    public static final BitSet FOLLOW_ws_in_slAttribute3237 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_slAttribute3279 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3459 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute3478 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0040L});
    public static final BitSet FOLLOW_ws_in_slAttribute3496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3615 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo3679 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3736 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3739 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_pseudo3741 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_pseudo3746 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo3750 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3834 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3836 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3839 = new BitSet(new long[]{0xBFE0000000B40040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo3841 = new BitSet(new long[]{0xBFE0000000340040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3844 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3891 = new BitSet(new long[]{0x11E0000000441040L,0x0000000000200000L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_declaration3937 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_property_in_declaration3958 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration3971 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_declaration3973 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3976 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_prio_in_declaration3979 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_declaration3981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue4021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue4064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4102 = new BitSet(new long[]{0xEFFDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_set_in_expressionPredicate4131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI4249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio4304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression4325 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_operator_in_expression4330 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_expression4332 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_expression4337 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_unaryOperator_in_term4362 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_term4364 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_set_in_term4388 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_term4588 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_term4596 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_term4604 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_term4612 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_term4620 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_term4628 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_term4638 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_term4650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4666 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function4668 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function4673 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function4675 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_function4685 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function4703 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function4706 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function4708 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttribute_in_function4711 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function4732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4780 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName4782 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4786 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4789 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4791 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4814 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0001L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4816 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4819 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4821 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4824 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4839 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4842 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4844 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4897 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4945 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4947 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4950 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4952 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4955 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4984 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4986 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4989 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4991 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4994 = new BitSet(new long[]{0x0000000000000020L,0x0000000000100000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4997 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4999 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable5037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable5069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression5093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5113 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp5127 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5129 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5132 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp5145 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5147 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5150 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5183 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp5196 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5198 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5201 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp5215 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5217 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5220 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5253 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5260 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp5274 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5276 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp5279 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp5281 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term5321 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_cp_term5521 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term5529 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_cp_term5537 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_cp_term5545 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term5553 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_cp_term5561 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term5569 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_term5581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration5612 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5614 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5616 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5619 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5621 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5624 = new BitSet(new long[]{0x0000000000800002L,0x00000000040C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5626 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5630 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5649 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5651 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5653 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5655 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5659 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5661 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5664 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call5708 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5710 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call5732 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5734 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5736 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5749 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call5752 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call5754 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call5757 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5761 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call5764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name5793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5829 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args5833 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args5841 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5844 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5886 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5890 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5900 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5903 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5909 = new BitSet(new long[]{0x0000000000800000L,0x00000000030C0000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5919 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_set_in_less_args_list5922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg5976 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5980 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_arg5982 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg5985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded6011 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6013 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6016 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded6020 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6028 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6031 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition6061 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6063 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition6072 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6074 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition6100 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6102 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition6133 = new BitSet(new long[]{0x0008000000A00000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_ws_in_less_condition6136 = new BitSet(new long[]{0x0008000000800000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition6139 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_condition6141 = new BitSet(new long[]{0x00A4000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition6144 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition6173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition6199 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6201 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition6204 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6206 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition6209 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6211 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition6214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name6236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6335 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6363 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression6420 = new BitSet(new long[]{0x01E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6459 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6495 = new BitSet(new long[]{0x01E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6593 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6621 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression6674 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6713 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6749 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6843 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression6871 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_mq_interpolation_expression6936 = new BitSet(new long[]{0x01E0000000128040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6975 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7011 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7096 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var7098 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7100 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var7105 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7109 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7113 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var7116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties7160 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties7162 = new BitSet(new long[]{0x00A4000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7164 = new BitSet(new long[]{0x00A4000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties7167 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties7170 = new BitSet(new long[]{0xBFE00001D1D45040L,0x0000000040EC0000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7172 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties7175 = new BitSet(new long[]{0xBFE00001D1545040L,0x0000000040E00000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties7177 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties7179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_in_sass_extend7200 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7202 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_sass_extend7204 = new BitSet(new long[]{0x0000000000000020L,0x0000000080000000L});
    public static final BitSet FOLLOW_SASS_OPTIONAL_in_sass_extend7207 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7209 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_extend7214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector7239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css3475 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred1_Css3487 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css3489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQueryList_in_synpred2_Css3526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3612 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3624 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_set_in_synpred3_Css3626 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_SEMI_in_synpred3_Css3636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3640 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32374 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32386 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_set_in_synpred4_Css32388 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_SEMI_in_synpred4_Css32398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32402 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32422 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32434 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_set_in_synpred5_Css32436 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32450 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred6_Css32482 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_LBRACE_in_synpred6_Css32492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred7_Css32655 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_RBRACE_in_synpred7_Css32661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_Css32707 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred8_Css32719 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred8_Css32721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred9_Css32819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred10_Css32840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred11_Css32949 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred11_Css32958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred12_Css33912 = new BitSet(new long[]{0xFFFFFFFFFFEFBFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000001FL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred12_Css33930 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Css33932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred13_Css34018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred14_Css35257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred15_Css36330 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred15_Css36332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred16_Css36454 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred16_Css36456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred17_Css36588 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred17_Css36590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred18_Css36708 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred18_Css36710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred19_Css36838 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred19_Css36840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred20_Css36970 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred20_Css36972 = new BitSet(new long[]{0x0000000000000002L});

}