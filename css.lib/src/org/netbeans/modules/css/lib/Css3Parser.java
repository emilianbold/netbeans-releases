// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-12 18:08:32

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "SASS_EXTEND_ONLY_SELECTOR", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "SASS_EXTEND", "SASS_OPTIONAL", "SASS_DEBUG", "SASS_WARN", "SASS_IF", "SASS_ELSE", "CP_EQ", "SASS_FOR", "SASS_EACH", "SASS_WHILE", "SASS_FUNCTION", "SASS_RETURN", "SASS_CONTENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
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
    public static final int SASS_IF=98;
    public static final int SASS_ELSE=99;
    public static final int CP_EQ=100;
    public static final int SASS_FOR=101;
    public static final int SASS_EACH=102;
    public static final int SASS_WHILE=103;
    public static final int SASS_FUNCTION=104;
    public static final int SASS_RETURN=105;
    public static final int SASS_CONTENT=106;
    public static final int HEXCHAR=107;
    public static final int NONASCII=108;
    public static final int UNICODE=109;
    public static final int ESCAPE=110;
    public static final int NMSTART=111;
    public static final int NMCHAR=112;
    public static final int URL=113;
    public static final int A=114;
    public static final int B=115;
    public static final int C=116;
    public static final int D=117;
    public static final int E=118;
    public static final int F=119;
    public static final int G=120;
    public static final int H=121;
    public static final int I=122;
    public static final int J=123;
    public static final int K=124;
    public static final int L=125;
    public static final int M=126;
    public static final int N=127;
    public static final int O=128;
    public static final int P=129;
    public static final int Q=130;
    public static final int R=131;
    public static final int S=132;
    public static final int T=133;
    public static final int U=134;
    public static final int V=135;
    public static final int W=136;
    public static final int X=137;
    public static final int Y=138;
    public static final int Z=139;
    public static final int CDO=140;
    public static final int CDC=141;
    public static final int INVALID=142;
    public static final int LINE_COMMENT=143;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "elementName", "page", "namespace", "resourceIdentifier", 
        "cp_mixin_name", "pseudo", "sass_control_block", "declarations", 
        "body", "atRuleId", "scss_selector_interpolation_expression", "esPred", 
        "syncToDeclarationsRule", "importItem", "slAttributeName", "synpred8_Css3", 
        "sass_while", "synpred21_Css3", "scss_declaration_interpolation_expression", 
        "unaryOperator", "vendorAtRule", "slAttributeValue", "sass_control_expression", 
        "cp_term", "term", "cp_arg", "sass_else", "operator", "slAttribute", 
        "sass_extend_only_selector", "less_fn_name", "synpred20_Css3", "scss_nested_properties", 
        "synpred25_Css3", "synpred6_Css3", "mediaQueryOperator", "cp_mixin_call", 
        "cp_mixin_declaration", "namespaces", "cp_additionExp", "mediaExpression", 
        "fnAttribute", "cp_variable_value", "charSet", "synpred1_Css3", 
        "synpred3_Css3", "combinator", "sass_for", "sass_if", "namespacePrefixName", 
        "less_condition_operator", "synpred18_Css3", "sass_extend", "cp_expression", 
        "fnAttributeName", "function", "webkitKeyframesBlock", "less_condition", 
        "selectorsGroup", "styleSheet", "prio", "cssClass", "charSetValue", 
        "bodyItem", "elementSubsequent", "synpred17_Css3", "cp_variable", 
        "synpred15_Css3", "sass_each_list", "cp_multiplyExp", "scss_interpolation_expression_var", 
        "webkitKeyframeSelectors", "scss_declaration_property_value_interpolation_expression", 
        "propertyValue", "cp_args_list", "simpleSelectorSequence", "hexColor", 
        "imports", "cp_atomExp", "syncTo_RBRACE", "fontFace", "namespacePrefix", 
        "synpred23_Css3", "synpred4_Css3", "margin", "scss_mq_interpolation_expression", 
        "synpred24_Css3", "syncToFollow", "sass_function_return", "moz_document_function", 
        "synpred13_Css3", "ws", "generic_at_rule", "cssId", "expression", 
        "synpred11_Css3", "typeSelector", "cp_variable_declaration", "sass_each", 
        "cp_mixin_call_arg", "synpred19_Css3", "synpred7_Css3", "synpred9_Css3", 
        "fnAttributeValue", "sass_debug", "mediaType", "pseudoPage", "declaration", 
        "expressionPredicate", "synpred10_Css3", "synpred5_Css3", "synpred16_Css3", 
        "sass_control", "mediaFeature", "functionName", "rule", "syncTo_SEMI", 
        "less_mixin_guarded", "synpred22_Css3", "synpred2_Css3", "counterStyle", 
        "less_function_in_condition", "sass_function_declaration", "selector", 
        "sass_function_name", "mediaQuery", "synpred14_Css3", "cp_mixin_call_args", 
        "margin_sym", "webkitKeyframes", "property", "sass_content", "synpred12_Css3", 
        "moz_document", "mediaQueryList", "media"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
            false, false, false, false, true, false, false, true, false, 
            true, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
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

            try {
                isCyclicDecision = true;
                alt4 = dfa4.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
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

            if ( (LA6_0==IDENT||LA6_0==IMPORT_SYM||LA6_0==MEDIA_SYM||LA6_0==GEN||LA6_0==COLON||LA6_0==AT_IDENT||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||(LA6_0>=MINUS && LA6_0<=PIPE)||LA6_0==LESS_AND||(LA6_0>=SASS_VAR && LA6_0<=SASS_INCLUDE)||(LA6_0>=SASS_DEBUG && LA6_0<=SASS_IF)||(LA6_0>=SASS_FOR && LA6_0<=SASS_FUNCTION)) ) {
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

                try {
                    isCyclicDecision = true;
                    alt16 = dfa16.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: importItem : ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI | {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier )* mediaQueryList SEMI );
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI | {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier )* mediaQueryList SEMI )
            int alt23=2;
            try { dbg.enterDecision(23, decisionCanBacktrack[23]);

            int LA23_0 = input.LA(1);

            if ( (LA23_0==IMPORT_SYM) ) {
                int LA23_1 = input.LA(2);

                if ( (!(evalPredicate(evalPredicate(isScssSource(),"isScssSource()"),""))) ) {
                    alt23=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:9: {...}? IMPORT_SYM ( ws )? resourceIdentifier ( ws )? ( COMMA ( ws )? resourceIdentifier )* mediaQueryList SEMI
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:65: ( COMMA ( ws )? resourceIdentifier )*
                    try { dbg.enterSubRule(22);

                    loop22:
                    do {
                        int alt22=2;
                        try { dbg.enterDecision(22, decisionCanBacktrack[22]);

                        int LA22_0 = input.LA(1);

                        if ( (LA22_0==COMMA) ) {
                            alt22=1;
                        }


                        } finally {dbg.exitDecision(22);}

                        switch (alt22) {
                    	case 1 :
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
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(22);}

                    dbg.location(362,97);
                    pushFollow(FOLLOW_mediaQueryList_in_importItem419);
                    mediaQueryList();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(362,112);
                    match(input,SEMI,FOLLOW_SEMI_in_importItem421); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE
            {
            dbg.location(365,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media437); if (state.failed) return ;
            dbg.location(365,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:17: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:17: ws
                    {
                    dbg.location(365,17);
                    pushFollow(FOLLOW_ws_in_media439);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(24);}

            dbg.location(367,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:9: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList )
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            try {
                isCyclicDecision = true;
                alt26 = dfa26.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )?
                    {
                    dbg.location(368,64);
                    pushFollow(FOLLOW_scss_mq_interpolation_expression_in_media494);
                    scss_mq_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(368,97);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:97: ( ws )?
                    int alt25=2;
                    try { dbg.enterSubRule(25);
                    try { dbg.enterDecision(25, decisionCanBacktrack[25]);

                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==WS||(LA25_0>=NL && LA25_0<=COMMENT)) ) {
                        alt25=1;
                    }
                    } finally {dbg.exitDecision(25);}

                    switch (alt25) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:97: ws
                            {
                            dbg.location(368,97);
                            pushFollow(FOLLOW_ws_in_media496);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(25);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:13: ( mediaQueryList )=> mediaQueryList
                    {
                    dbg.location(370,31);
                    pushFollow(FOLLOW_mediaQueryList_in_media530);
                    mediaQueryList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(26);}

            dbg.location(373,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media559); if (state.failed) return ;
            dbg.location(373,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:16: ws
                    {
                    dbg.location(373,16);
                    pushFollow(FOLLOW_ws_in_media561);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(27);}

            dbg.location(374,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*
            try { dbg.enterSubRule(38);

            loop38:
            do {
                int alt38=11;
                try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                try {
                    isCyclicDecision = true;
                    alt38 = dfa38.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(38);}

                switch (alt38) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:17: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(376,134);
            	    pushFollow(FOLLOW_declaration_in_media647);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(376,146);
            	    match(input,SEMI,FOLLOW_SEMI_in_media649); if (state.failed) return ;
            	    dbg.location(376,151);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:151: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:151: ws
            	            {
            	            dbg.location(376,151);
            	            pushFollow(FOLLOW_ws_in_media651);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(28);}


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
            	    pushFollow(FOLLOW_sass_extend_in_media674);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(377,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:49: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:49: ws
            	            {
            	            dbg.location(377,49);
            	            pushFollow(FOLLOW_ws_in_media676);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(29);}


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
            	    pushFollow(FOLLOW_sass_debug_in_media699);
            	    sass_debug();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(378,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:48: ws
            	            {
            	            dbg.location(378,48);
            	            pushFollow(FOLLOW_ws_in_media701);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(30);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:19: {...}? sass_control ( ws )?
            	    {
            	    dbg.location(379,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(379,37);
            	    pushFollow(FOLLOW_sass_control_in_media724);
            	    sass_control();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(379,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:50: ws
            	            {
            	            dbg.location(379,50);
            	            pushFollow(FOLLOW_ws_in_media726);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(31);}


            	    }
            	    break;
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:19: {...}? sass_content ( ws )?
            	    {
            	    dbg.location(380,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(380,37);
            	    pushFollow(FOLLOW_sass_content_in_media749);
            	    sass_content();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(380,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:50: ws
            	            {
            	            dbg.location(380,50);
            	            pushFollow(FOLLOW_ws_in_media751);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(32);}


            	    }
            	    break;
            	case 6 :
            	    dbg.enterAlt(6);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:19: rule ( ws )?
            	    {
            	    dbg.location(382,19);
            	    pushFollow(FOLLOW_rule_in_media789);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(382,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:25: ws
            	            {
            	            dbg.location(382,25);
            	            pushFollow(FOLLOW_ws_in_media792);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(33);}


            	    }
            	    break;
            	case 7 :
            	    dbg.enterAlt(7);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:19: page ( ws )?
            	    {
            	    dbg.location(383,19);
            	    pushFollow(FOLLOW_page_in_media813);
            	    page();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(383,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:25: ws
            	            {
            	            dbg.location(383,25);
            	            pushFollow(FOLLOW_ws_in_media816);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(34);}


            	    }
            	    break;
            	case 8 :
            	    dbg.enterAlt(8);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:19: fontFace ( ws )?
            	    {
            	    dbg.location(384,19);
            	    pushFollow(FOLLOW_fontFace_in_media837);
            	    fontFace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(384,29);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:29: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:29: ws
            	            {
            	            dbg.location(384,29);
            	            pushFollow(FOLLOW_ws_in_media840);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(35);}


            	    }
            	    break;
            	case 9 :
            	    dbg.enterAlt(9);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:19: vendorAtRule ( ws )?
            	    {
            	    dbg.location(385,19);
            	    pushFollow(FOLLOW_vendorAtRule_in_media861);
            	    vendorAtRule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(385,33);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:33: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:33: ws
            	            {
            	            dbg.location(385,33);
            	            pushFollow(FOLLOW_ws_in_media864);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(36);}


            	    }
            	    break;
            	case 10 :
            	    dbg.enterAlt(10);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:19: {...}? media ( ws )?
            	    {
            	    dbg.location(386,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(386,37);
            	    pushFollow(FOLLOW_media_in_media887);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(386,43);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:43: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:43: ws
            	            {
            	            dbg.location(386,43);
            	            pushFollow(FOLLOW_ws_in_media889);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(37);}


            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);
            } finally {dbg.exitSubRule(38);}

            dbg.location(389,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media933); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(390, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(393,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt41=2;
            try { dbg.enterSubRule(41);
            try { dbg.enterDecision(41, decisionCanBacktrack[41]);

            int LA41_0 = input.LA(1);

            if ( (LA41_0==IDENT||(LA41_0>=ONLY && LA41_0<=LPAREN)) ) {
                alt41=1;
            }
            } finally {dbg.exitDecision(41);}

            switch (alt41) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(393,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList949);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(393,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(40);

                    loop40:
                    do {
                        int alt40=2;
                        try { dbg.enterDecision(40, decisionCanBacktrack[40]);

                        int LA40_0 = input.LA(1);

                        if ( (LA40_0==COMMA) ) {
                            alt40=1;
                        }


                        } finally {dbg.exitDecision(40);}

                        switch (alt40) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(393,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList953); if (state.failed) return ;
                    	    dbg.location(393,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:25: ws
                    	            {
                    	            dbg.location(393,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList955);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(39);}

                    	    dbg.location(393,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList958);
                    	    mediaQuery();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(40);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(41);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(394, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt49=2;
            try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            int LA49_0 = input.LA(1);

            if ( (LA49_0==IDENT||(LA49_0>=ONLY && LA49_0<=GEN)) ) {
                alt49=1;
            }
            else if ( (LA49_0==LPAREN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(397,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:4: ( mediaQueryOperator ( ws )? )?
                    int alt43=2;
                    try { dbg.enterSubRule(43);
                    try { dbg.enterDecision(43, decisionCanBacktrack[43]);

                    int LA43_0 = input.LA(1);

                    if ( ((LA43_0>=ONLY && LA43_0<=NOT)) ) {
                        alt43=1;
                    }
                    } finally {dbg.exitDecision(43);}

                    switch (alt43) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(397,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery977);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(397,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:24: ws
                                    {
                                    dbg.location(397,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery979);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(42);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(43);}

                    dbg.location(397,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery986);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(397,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:42: ws
                            {
                            dbg.location(397,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery988);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(44);}

                    dbg.location(397,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(46);

                    loop46:
                    do {
                        int alt46=2;
                        try { dbg.enterDecision(46, decisionCanBacktrack[46]);

                        int LA46_0 = input.LA(1);

                        if ( (LA46_0==AND) ) {
                            alt46=1;
                        }


                        } finally {dbg.exitDecision(46);}

                        switch (alt46) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(397,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery993); if (state.failed) return ;
                    	    dbg.location(397,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:52: ( ws )?
                    	    int alt45=2;
                    	    try { dbg.enterSubRule(45);
                    	    try { dbg.enterDecision(45, decisionCanBacktrack[45]);

                    	    int LA45_0 = input.LA(1);

                    	    if ( (LA45_0==WS||(LA45_0>=NL && LA45_0<=COMMENT)) ) {
                    	        alt45=1;
                    	    }
                    	    } finally {dbg.exitDecision(45);}

                    	    switch (alt45) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:52: ws
                    	            {
                    	            dbg.location(397,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery995);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(45);}

                    	    dbg.location(397,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery998);
                    	    mediaExpression();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(398,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery1006);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(398,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:20: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(48);

                    loop48:
                    do {
                        int alt48=2;
                        try { dbg.enterDecision(48, decisionCanBacktrack[48]);

                        int LA48_0 = input.LA(1);

                        if ( (LA48_0==AND) ) {
                            alt48=1;
                        }


                        } finally {dbg.exitDecision(48);}

                        switch (alt48) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(398,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery1010); if (state.failed) return ;
                    	    dbg.location(398,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:26: ws
                    	            {
                    	            dbg.location(398,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery1012);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(47);}

                    	    dbg.location(398,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery1015);
                    	    mediaExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

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
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(399, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(401, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(402,3);
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
        dbg.location(403, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(406,2);
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
        dbg.location(407, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(410,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression1070); if (state.failed) return ;
            dbg.location(410,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:14: ws
                    {
                    dbg.location(410,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression1072);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}

            dbg.location(410,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression1075);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(410,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:31: ws
                    {
                    dbg.location(410,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression1077);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}

            dbg.location(410,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:35: ( COLON ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:37: COLON ( ws )? expression
                    {
                    dbg.location(410,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression1082); if (state.failed) return ;
                    dbg.location(410,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:43: ws
                            {
                            dbg.location(410,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression1084);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(52);}

                    dbg.location(410,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression1087);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(410,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression1092); if (state.failed) return ;
            dbg.location(410,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:68: ws
                    {
                    dbg.location(410,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression1094);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(411, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: mediaFeature : ( IDENT | GEN | {...}? cp_variable );
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:2: ( IDENT | GEN | {...}? cp_variable )
            int alt55=3;
            try { dbg.enterDecision(55, decisionCanBacktrack[55]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt55=1;
                }
                break;
            case GEN:
                {
                alt55=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
            case SASS_VAR:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:4: IDENT
                    {
                    dbg.location(414,4);
                    match(input,IDENT,FOLLOW_IDENT_in_mediaFeature1110); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:12: GEN
                    {
                    dbg.location(414,12);
                    match(input,GEN,FOLLOW_GEN_in_mediaFeature1114); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:18: {...}? cp_variable
                    {
                    dbg.location(414,18);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "mediaFeature", "isCssPreprocessorSource()");
                    }
                    dbg.location(414,47);
                    pushFollow(FOLLOW_cp_variable_in_mediaFeature1120);
                    cp_variable();

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
        dbg.location(415, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:2: ( bodyItem ( ws )? )+
            {
            dbg.location(418,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:2: ( bodyItem ( ws )? )+
            int cnt57=0;
            try { dbg.enterSubRule(57);

            loop57:
            do {
                int alt57=2;
                try { dbg.enterDecision(57, decisionCanBacktrack[57]);

                int LA57_0 = input.LA(1);

                if ( (LA57_0==IDENT||LA57_0==IMPORT_SYM||LA57_0==MEDIA_SYM||LA57_0==GEN||LA57_0==COLON||LA57_0==AT_IDENT||LA57_0==MOZ_DOCUMENT_SYM||LA57_0==WEBKIT_KEYFRAMES_SYM||(LA57_0>=PAGE_SYM && LA57_0<=FONT_FACE_SYM)||(LA57_0>=MINUS && LA57_0<=PIPE)||LA57_0==LESS_AND||(LA57_0>=SASS_VAR && LA57_0<=SASS_INCLUDE)||(LA57_0>=SASS_DEBUG && LA57_0<=SASS_IF)||(LA57_0>=SASS_FOR && LA57_0<=SASS_FUNCTION)) ) {
                    alt57=1;
                }


                } finally {dbg.exitDecision(57);}

                switch (alt57) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:4: bodyItem ( ws )?
            	    {
            	    dbg.location(418,4);
            	    pushFollow(FOLLOW_bodyItem_in_body1136);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(418,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:13: ws
            	            {
            	            dbg.location(418,13);
            	            pushFollow(FOLLOW_ws_in_body1138);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(56);}


            	    }
            	    break;

            	default :
            	    if ( cnt57 >= 1 ) break loop57;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(57, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt57++;
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
        dbg.location(419, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration )
            int alt58=12;
            try { dbg.enterDecision(58, decisionCanBacktrack[58]);

            try {
                isCyclicDecision = true;
                alt58 = dfa58.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(58);}

            switch (alt58) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:6: rule
                    {
                    dbg.location(423,6);
                    pushFollow(FOLLOW_rule_in_bodyItem1163);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:11: media
                    {
                    dbg.location(424,11);
                    pushFollow(FOLLOW_media_in_bodyItem1175);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:11: page
                    {
                    dbg.location(425,11);
                    pushFollow(FOLLOW_page_in_bodyItem1187);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:11: counterStyle
                    {
                    dbg.location(426,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem1199);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:11: fontFace
                    {
                    dbg.location(427,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem1211);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:11: vendorAtRule
                    {
                    dbg.location(428,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem1223);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:11: {...}? cp_variable_declaration
                    {
                    dbg.location(429,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(429,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem1237);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:11: {...}? cp_mixin_call
                    {
                    dbg.location(430,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(430,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem1251);
                    cp_mixin_call();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:11: {...}? importItem
                    {
                    dbg.location(431,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(431,40);
                    pushFollow(FOLLOW_importItem_in_bodyItem1265);
                    importItem();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 10 :
                    dbg.enterAlt(10);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:11: {...}? sass_debug
                    {
                    dbg.location(432,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(432,29);
                    pushFollow(FOLLOW_sass_debug_in_bodyItem1280);
                    sass_debug();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 11 :
                    dbg.enterAlt(11);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:11: {...}? sass_control
                    {
                    dbg.location(433,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(433,29);
                    pushFollow(FOLLOW_sass_control_in_bodyItem1294);
                    sass_control();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 12 :
                    dbg.enterAlt(12);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:11: {...}? sass_function_declaration
                    {
                    dbg.location(434,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(434,29);
                    pushFollow(FOLLOW_sass_function_declaration_in_bodyItem1308);
                    sass_function_declaration();

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
        dbg.location(435, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(443, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt59=3;
            try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt59=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt59=2;
                }
                break;
            case AT_IDENT:
                {
                alt59=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(59);}

            switch (alt59) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:3: moz_document
                    {
                    dbg.location(444,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule1331);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:18: webkitKeyframes
                    {
                    dbg.location(444,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule1335);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:36: generic_at_rule
                    {
                    dbg.location(444,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule1339);
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
        dbg.location(444, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(446, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(447,2);
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
        dbg.location(449, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(451, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(452,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule1375); if (state.failed) return ;
            dbg.location(452,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:16: WS
            	    {
            	    dbg.location(452,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule1377); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);
            } finally {dbg.exitSubRule(60);}

            dbg.location(452,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:20: ( atRuleId ( WS )* )?
            int alt62=2;
            try { dbg.enterSubRule(62);
            try { dbg.enterDecision(62, decisionCanBacktrack[62]);

            int LA62_0 = input.LA(1);

            if ( ((LA62_0>=IDENT && LA62_0<=STRING)) ) {
                alt62=1;
            }
            } finally {dbg.exitDecision(62);}

            switch (alt62) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:22: atRuleId ( WS )*
                    {
                    dbg.location(452,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule1382);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(452,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:31: WS
                    	    {
                    	    dbg.location(452,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule1384); if (state.failed) return ;

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

            dbg.location(453,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule1399); if (state.failed) return ;
            dbg.location(454,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule1411);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(455,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule1421); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "generic_at_rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "generic_at_rule"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(457, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(459,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1437); if (state.failed) return ;
            dbg.location(459,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:19: ws
                    {
                    dbg.location(459,19);
                    pushFollow(FOLLOW_ws_in_moz_document1439);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(459,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:25: moz_document_function ( ws )?
            {
            dbg.location(459,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document1444);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(459,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:47: ws
                    {
                    dbg.location(459,47);
                    pushFollow(FOLLOW_ws_in_moz_document1446);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}


            }

            dbg.location(459,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(67);

            loop67:
            do {
                int alt67=2;
                try { dbg.enterDecision(67, decisionCanBacktrack[67]);

                int LA67_0 = input.LA(1);

                if ( (LA67_0==COMMA) ) {
                    alt67=1;
                }


                } finally {dbg.exitDecision(67);}

                switch (alt67) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(459,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document1452); if (state.failed) return ;
            	    dbg.location(459,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:60: ws
            	            {
            	            dbg.location(459,60);
            	            pushFollow(FOLLOW_ws_in_moz_document1454);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(65);}

            	    dbg.location(459,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document1457);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(459,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:86: ws
            	            {
            	            dbg.location(459,86);
            	            pushFollow(FOLLOW_ws_in_moz_document1459);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(66);}


            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}

            dbg.location(460,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document1466); if (state.failed) return ;
            dbg.location(460,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:9: ws
                    {
                    dbg.location(460,9);
                    pushFollow(FOLLOW_ws_in_moz_document1468);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(461,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:3: ( body )?
            int alt69=2;
            try { dbg.enterSubRule(69);
            try { dbg.enterDecision(69, decisionCanBacktrack[69]);

            int LA69_0 = input.LA(1);

            if ( (LA69_0==IDENT||LA69_0==IMPORT_SYM||LA69_0==MEDIA_SYM||LA69_0==GEN||LA69_0==COLON||LA69_0==AT_IDENT||LA69_0==MOZ_DOCUMENT_SYM||LA69_0==WEBKIT_KEYFRAMES_SYM||(LA69_0>=PAGE_SYM && LA69_0<=FONT_FACE_SYM)||(LA69_0>=MINUS && LA69_0<=PIPE)||LA69_0==LESS_AND||(LA69_0>=SASS_VAR && LA69_0<=SASS_INCLUDE)||(LA69_0>=SASS_DEBUG && LA69_0<=SASS_IF)||(LA69_0>=SASS_FOR && LA69_0<=SASS_FUNCTION)) ) {
                alt69=1;
            }
            } finally {dbg.exitDecision(69);}

            switch (alt69) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:3: body
                    {
                    dbg.location(461,3);
                    pushFollow(FOLLOW_body_in_moz_document1473);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(462,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document1478); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "moz_document");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document"


    // $ANTLR start "moz_document_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(465, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(466,2);
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
        dbg.location(468, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(471, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(473,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1519); if (state.failed) return ;
            dbg.location(473,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:23: ws
                    {
                    dbg.location(473,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1521);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(473,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1524);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(473,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:36: ws
                    {
                    dbg.location(473,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1526);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(474,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1531); if (state.failed) return ;
            dbg.location(474,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:9: ws
                    {
                    dbg.location(474,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1533);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(475,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(74);

            loop74:
            do {
                int alt74=2;
                try { dbg.enterDecision(74, decisionCanBacktrack[74]);

                int LA74_0 = input.LA(1);

                if ( (LA74_0==IDENT||LA74_0==PERCENTAGE) ) {
                    alt74=1;
                }


                } finally {dbg.exitDecision(74);}

                switch (alt74) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(475,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1540);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(475,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:26: ws
            	            {
            	            dbg.location(475,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1542);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(73);}


            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);
            } finally {dbg.exitSubRule(74);}

            dbg.location(476,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1549); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(479, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(481,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1562);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(481,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:26: ws
                    {
                    dbg.location(481,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1564);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(75);}

            dbg.location(483,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1569); if (state.failed) return ;
            dbg.location(483,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:10: ws
                    {
                    dbg.location(483,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1572);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(483,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1575);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(484,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1579);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(485,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1582); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframesBlock");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframesBlock"


    // $ANTLR start "webkitKeyframeSelectors"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(488, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(490,2);
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

            dbg.location(490,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            try { dbg.enterSubRule(79);

            loop79:
            do {
                int alt79=2;
                try { dbg.enterDecision(79, decisionCanBacktrack[79]);

                try {
                    isCyclicDecision = true;
                    alt79 = dfa79.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(79);}

                switch (alt79) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(490,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:27: ws
            	            {
            	            dbg.location(490,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1609);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(77);}

            	    dbg.location(490,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1612); if (state.failed) return ;
            	    dbg.location(490,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:37: ws
            	            {
            	            dbg.location(490,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1614);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(78);}

            	    dbg.location(490,41);
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
            	    break loop79;
                }
            } while (true);
            } finally {dbg.exitSubRule(79);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(491, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(493, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(494,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1646); if (state.failed) return ;
            dbg.location(494,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:16: ws
                    {
                    dbg.location(494,16);
                    pushFollow(FOLLOW_ws_in_page1648);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(494,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:20: ( IDENT ( ws )? )?
            int alt82=2;
            try { dbg.enterSubRule(82);
            try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            int LA82_0 = input.LA(1);

            if ( (LA82_0==IDENT) ) {
                alt82=1;
            }
            } finally {dbg.exitDecision(82);}

            switch (alt82) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:22: IDENT ( ws )?
                    {
                    dbg.location(494,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1653); if (state.failed) return ;
                    dbg.location(494,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:28: ws
                            {
                            dbg.location(494,28);
                            pushFollow(FOLLOW_ws_in_page1655);
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

            dbg.location(494,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:35: ( pseudoPage ( ws )? )?
            int alt84=2;
            try { dbg.enterSubRule(84);
            try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            int LA84_0 = input.LA(1);

            if ( (LA84_0==COLON) ) {
                alt84=1;
            }
            } finally {dbg.exitDecision(84);}

            switch (alt84) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:36: pseudoPage ( ws )?
                    {
                    dbg.location(494,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1662);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(494,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:47: ws
                            {
                            dbg.location(494,47);
                            pushFollow(FOLLOW_ws_in_page1664);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(83);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(84);}

            dbg.location(495,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1677); if (state.failed) return ;
            dbg.location(495,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:16: ws
                    {
                    dbg.location(495,16);
                    pushFollow(FOLLOW_ws_in_page1679);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(85);}

            dbg.location(499,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:13: ( declaration | margin ( ws )? )?
            int alt87=3;
            try { dbg.enterSubRule(87);
            try { dbg.enterDecision(87, decisionCanBacktrack[87]);

            int LA87_0 = input.LA(1);

            if ( (LA87_0==IDENT||LA87_0==MEDIA_SYM||LA87_0==GEN||LA87_0==AT_IDENT||(LA87_0>=MINUS && LA87_0<=DOT)||LA87_0==STAR||LA87_0==SASS_VAR) ) {
                alt87=1;
            }
            else if ( ((LA87_0>=TOPLEFTCORNER_SYM && LA87_0<=RIGHTBOTTOM_SYM)) ) {
                alt87=2;
            }
            } finally {dbg.exitDecision(87);}

            switch (alt87) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:14: declaration
                    {
                    dbg.location(499,14);
                    pushFollow(FOLLOW_declaration_in_page1734);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:26: margin ( ws )?
                    {
                    dbg.location(499,26);
                    pushFollow(FOLLOW_margin_in_page1736);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(499,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:33: ws
                            {
                            dbg.location(499,33);
                            pushFollow(FOLLOW_ws_in_page1738);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(86);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(499,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(91);

            loop91:
            do {
                int alt91=2;
                try { dbg.enterDecision(91, decisionCanBacktrack[91]);

                int LA91_0 = input.LA(1);

                if ( (LA91_0==SEMI) ) {
                    alt91=1;
                }


                } finally {dbg.exitDecision(91);}

                switch (alt91) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(499,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1744); if (state.failed) return ;
            	    dbg.location(499,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:45: ws
            	            {
            	            dbg.location(499,45);
            	            pushFollow(FOLLOW_ws_in_page1746);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(88);}

            	    dbg.location(499,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:49: ( declaration | margin ( ws )? )?
            	    int alt90=3;
            	    try { dbg.enterSubRule(90);
            	    try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            	    int LA90_0 = input.LA(1);

            	    if ( (LA90_0==IDENT||LA90_0==MEDIA_SYM||LA90_0==GEN||LA90_0==AT_IDENT||(LA90_0>=MINUS && LA90_0<=DOT)||LA90_0==STAR||LA90_0==SASS_VAR) ) {
            	        alt90=1;
            	    }
            	    else if ( ((LA90_0>=TOPLEFTCORNER_SYM && LA90_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt90=2;
            	    }
            	    } finally {dbg.exitDecision(90);}

            	    switch (alt90) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:50: declaration
            	            {
            	            dbg.location(499,50);
            	            pushFollow(FOLLOW_declaration_in_page1750);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:62: margin ( ws )?
            	            {
            	            dbg.location(499,62);
            	            pushFollow(FOLLOW_margin_in_page1752);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(499,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:69: ws
            	                    {
            	                    dbg.location(499,69);
            	                    pushFollow(FOLLOW_ws_in_page1754);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(89);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(90);}


            	    }
            	    break;

            	default :
            	    break loop91;
                }
            } while (true);
            } finally {dbg.exitSubRule(91);}

            dbg.location(500,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1769); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(503, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(504,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1790); if (state.failed) return ;
            dbg.location(504,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:25: ws
                    {
                    dbg.location(504,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1792);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(504,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1795); if (state.failed) return ;
            dbg.location(504,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:35: ws
                    {
                    dbg.location(504,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1797);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(505,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1808); if (state.failed) return ;
            dbg.location(505,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:16: ws
                    {
                    dbg.location(505,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1810);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(505,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1813);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(506,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1817);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(507,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1827); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "counterStyle");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "counterStyle"


    // $ANTLR start "fontFace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(510, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(511,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1848); if (state.failed) return ;
            dbg.location(511,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:21: ws
                    {
                    dbg.location(511,21);
                    pushFollow(FOLLOW_ws_in_fontFace1850);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}

            dbg.location(512,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1861); if (state.failed) return ;
            dbg.location(512,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:16: ws
                    {
                    dbg.location(512,16);
                    pushFollow(FOLLOW_ws_in_fontFace1863);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}

            dbg.location(512,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1866);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(513,3);
            pushFollow(FOLLOW_declarations_in_fontFace1870);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(514,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1880); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(517, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(518,4);
            pushFollow(FOLLOW_margin_sym_in_margin1895);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(518,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:15: ws
                    {
                    dbg.location(518,15);
                    pushFollow(FOLLOW_ws_in_margin1897);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(518,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1900); if (state.failed) return ;
            dbg.location(518,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:26: ws
                    {
                    dbg.location(518,26);
                    pushFollow(FOLLOW_ws_in_margin1902);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}

            dbg.location(518,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1905);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(518,53);
            pushFollow(FOLLOW_declarations_in_margin1907);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(518,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1909); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(519, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(521, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(522,2);
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
        dbg.location(539, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(541, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:7: COLON IDENT
            {
            dbg.location(542,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage2138); if (state.failed) return ;
            dbg.location(542,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage2140); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(545, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(546,5);
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
        dbg.location(548, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(550, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt102=4;
            try { dbg.enterDecision(102, decisionCanBacktrack[102]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt102=1;
                }
                break;
            case GREATER:
                {
                alt102=2;
                }
                break;
            case TILDE:
                {
                alt102=3;
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
                alt102=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(102);}

            switch (alt102) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:7: PLUS ( ws )?
                    {
                    dbg.location(551,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator2190); if (state.failed) return ;
                    dbg.location(551,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:12: ws
                            {
                            dbg.location(551,12);
                            pushFollow(FOLLOW_ws_in_combinator2192);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(99);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:7: GREATER ( ws )?
                    {
                    dbg.location(552,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator2201); if (state.failed) return ;
                    dbg.location(552,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:15: ws
                            {
                            dbg.location(552,15);
                            pushFollow(FOLLOW_ws_in_combinator2203);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(100);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:7: TILDE ( ws )?
                    {
                    dbg.location(553,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator2212); if (state.failed) return ;
                    dbg.location(553,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:13: ws
                            {
                            dbg.location(553,13);
                            pushFollow(FOLLOW_ws_in_combinator2214);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(101);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:5: 
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
        dbg.location(555, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(557, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(558,5);
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
        dbg.location(560, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:1: property : ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(562, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:5: ( ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:5: ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(564,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:5: ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable )
            int alt103=4;
            try { dbg.enterSubRule(103);
            try { dbg.enterDecision(103, decisionCanBacktrack[103]);

            int LA103_0 = input.LA(1);

            if ( (LA103_0==HASH_SYMBOL) && (synpred4_Css3())) {
                alt103=1;
            }
            else if ( (LA103_0==IDENT) ) {
                int LA103_2 = input.LA(2);

                if ( (synpred4_Css3()) ) {
                    alt103=1;
                }
                else if ( (true) ) {
                    alt103=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 103, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA103_0==MINUS||(LA103_0>=HASH && LA103_0<=DOT)) && (synpred4_Css3())) {
                alt103=1;
            }
            else if ( (LA103_0==GEN) ) {
                alt103=3;
            }
            else if ( (LA103_0==MEDIA_SYM||LA103_0==AT_IDENT||LA103_0==SASS_VAR) ) {
                alt103=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(103);}

            switch (alt103) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:9: ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(567,53);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_property2321);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:11: IDENT
                    {
                    dbg.location(568,11);
                    match(input,IDENT,FOLLOW_IDENT_in_property2333); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:11: GEN
                    {
                    dbg.location(569,11);
                    match(input,GEN,FOLLOW_GEN_in_property2346); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:11: {...}? cp_variable
                    {
                    dbg.location(570,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(570,40);
                    pushFollow(FOLLOW_cp_variable_in_property2361);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(103);}

            dbg.location(571,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:7: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:7: ws
                    {
                    dbg.location(571,7);
                    pushFollow(FOLLOW_ws_in_property2369);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(104);}


            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(COLON)); 
                
        }
        finally {
        }
        dbg.location(572, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(577, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(578,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
            int alt105=2;
            try { dbg.enterSubRule(105);
            try { dbg.enterDecision(105, decisionCanBacktrack[105]);

            try {
                isCyclicDecision = true;
                alt105 = dfa105.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(105);}

            switch (alt105) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(579,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(579,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(579,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule2418);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:13: ( selectorsGroup )
                    {
                    dbg.location(581,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:15: selectorsGroup
                    {
                    dbg.location(581,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule2451);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(584,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule2474); if (state.failed) return ;
            dbg.location(584,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:16: ws
                    {
                    dbg.location(584,16);
                    pushFollow(FOLLOW_ws_in_rule2476);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}

            dbg.location(584,20);
            pushFollow(FOLLOW_syncToFollow_in_rule2479);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(585,13);
            pushFollow(FOLLOW_declarations_in_rule2493);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(586,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule2503); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(587, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:1: declarations : ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(595, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: ( ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )?
            {
            dbg.location(597,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )*
            try { dbg.enterSubRule(118);

            loop118:
            do {
                int alt118=13;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:17: ( cp_variable_declaration )=> cp_variable_declaration ( ws )?
            	    {
            	    dbg.location(598,44);
            	    pushFollow(FOLLOW_cp_variable_declaration_in_declarations2569);
            	    cp_variable_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(598,68);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:68: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:68: ws
            	            {
            	            dbg.location(598,68);
            	            pushFollow(FOLLOW_ws_in_declarations2571);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(107);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:3: ( declaration SEMI )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(604,23);
            	    pushFollow(FOLLOW_declaration_in_declarations2652);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(604,35);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2654); if (state.failed) return ;
            	    dbg.location(604,40);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:40: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:40: ws
            	            {
            	            dbg.location(604,40);
            	            pushFollow(FOLLOW_ws_in_declarations2656);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(108);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(608,69);
            	    pushFollow(FOLLOW_declaration_in_declarations2741);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(608,81);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2743); if (state.failed) return ;
            	    dbg.location(608,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:86: ws
            	            {
            	            dbg.location(608,86);
            	            pushFollow(FOLLOW_ws_in_declarations2745);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(109);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:3: ( scss_nested_properties )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(610,29);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2758);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(610,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:52: ws
            	            {
            	            dbg.location(610,52);
            	            pushFollow(FOLLOW_ws_in_declarations2760);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(110);}


            	    }
            	    break;
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:17: ( rule )=> rule ( ws )?
            	    {
            	    dbg.location(612,25);
            	    pushFollow(FOLLOW_rule_in_declarations2787);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(612,30);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:30: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:30: ws
            	            {
            	            dbg.location(612,30);
            	            pushFollow(FOLLOW_ws_in_declarations2789);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(111);}


            	    }
            	    break;
            	case 6 :
            	    dbg.enterAlt(6);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:17: {...}? sass_extend ( ws )?
            	    {
            	    dbg.location(614,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(614,35);
            	    pushFollow(FOLLOW_sass_extend_in_declarations2828);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(614,47);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:47: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:47: ws
            	            {
            	            dbg.location(614,47);
            	            pushFollow(FOLLOW_ws_in_declarations2830);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(112);}


            	    }
            	    break;
            	case 7 :
            	    dbg.enterAlt(7);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:17: {...}? sass_debug ( ws )?
            	    {
            	    dbg.location(616,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(616,35);
            	    pushFollow(FOLLOW_sass_debug_in_declarations2869);
            	    sass_debug();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(616,46);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: ws
            	            {
            	            dbg.location(616,46);
            	            pushFollow(FOLLOW_ws_in_declarations2871);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(113);}


            	    }
            	    break;
            	case 8 :
            	    dbg.enterAlt(8);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:17: {...}? sass_control ( ws )?
            	    {
            	    dbg.location(618,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(618,35);
            	    pushFollow(FOLLOW_sass_control_in_declarations2910);
            	    sass_control();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(618,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:48: ws
            	            {
            	            dbg.location(618,48);
            	            pushFollow(FOLLOW_ws_in_declarations2912);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(114);}


            	    }
            	    break;
            	case 9 :
            	    dbg.enterAlt(9);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:17: {...}? media ( ws )?
            	    {
            	    dbg.location(620,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(620,46);
            	    pushFollow(FOLLOW_media_in_declarations2951);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(620,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:52: ws
            	            {
            	            dbg.location(620,52);
            	            pushFollow(FOLLOW_ws_in_declarations2953);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(115);}


            	    }
            	    break;
            	case 10 :
            	    dbg.enterAlt(10);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(622,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(622,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2992);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(622,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:60: ws
            	            {
            	            dbg.location(622,60);
            	            pushFollow(FOLLOW_ws_in_declarations2994);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(116);}


            	    }
            	    break;
            	case 11 :
            	    dbg.enterAlt(11);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:17: {...}? sass_content ( ws )?
            	    {
            	    dbg.location(624,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(624,35);
            	    pushFollow(FOLLOW_sass_content_in_declarations3033);
            	    sass_content();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(624,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:48: ws
            	            {
            	            dbg.location(624,48);
            	            pushFollow(FOLLOW_ws_in_declarations3035);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(117);}


            	    }
            	    break;
            	case 12 :
            	    dbg.enterAlt(12);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:17: ( (~ SEMI )* SEMI )=> syncTo_SEMI
            	    {
            	    dbg.location(626,32);
            	    pushFollow(FOLLOW_syncTo_SEMI_in_declarations3080);
            	    syncTo_SEMI();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop118;
                }
            } while (true);
            } finally {dbg.exitSubRule(118);}

            dbg.location(628,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:13: ( declaration )?
            int alt119=2;
            try { dbg.enterSubRule(119);
            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

            int LA119_0 = input.LA(1);

            if ( (LA119_0==IDENT||LA119_0==MEDIA_SYM||LA119_0==GEN||LA119_0==AT_IDENT||(LA119_0>=MINUS && LA119_0<=DOT)||LA119_0==STAR||LA119_0==SASS_VAR) ) {
                alt119=1;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:13: declaration
                    {
                    dbg.location(628,13);
                    pushFollow(FOLLOW_declaration_in_declarations3110);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(629, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(631, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt123=2;
            try { dbg.enterDecision(123, decisionCanBacktrack[123]);

            try {
                isCyclicDecision = true;
                alt123 = dfa123.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(634,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup3170);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(634,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:99: ws
                            {
                            dbg.location(634,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup3172);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(636,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup3187);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(636,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(122);

                    loop122:
                    do {
                        int alt122=2;
                        try { dbg.enterDecision(122, decisionCanBacktrack[122]);

                        int LA122_0 = input.LA(1);

                        if ( (LA122_0==COMMA) ) {
                            alt122=1;
                        }


                        } finally {dbg.exitDecision(122);}

                        switch (alt122) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(636,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup3190); if (state.failed) return ;
                    	    dbg.location(636,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:25: ws
                    	            {
                    	            dbg.location(636,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup3192);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(121);}

                    	    dbg.location(636,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup3195);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop122;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(122);}


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
        dbg.location(637, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(639, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(640,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector3222);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(640,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(124);

            loop124:
            do {
                int alt124=2;
                try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                int LA124_0 = input.LA(1);

                if ( (LA124_0==IDENT||LA124_0==GEN||LA124_0==COLON||(LA124_0>=PLUS && LA124_0<=TILDE)||(LA124_0>=HASH_SYMBOL && LA124_0<=PIPE)||LA124_0==LESS_AND) ) {
                    alt124=1;
                }


                } finally {dbg.exitDecision(124);}

                switch (alt124) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(640,31);
            	    pushFollow(FOLLOW_combinator_in_selector3225);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(640,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector3227);
            	    simpleSelectorSequence();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(641, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(644, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt129=2;
            try { dbg.enterDecision(129, decisionCanBacktrack[129]);

            int LA129_0 = input.LA(1);

            if ( (LA129_0==IDENT||LA129_0==GEN||(LA129_0>=STAR && LA129_0<=PIPE)||LA129_0==LESS_AND) ) {
                alt129=1;
            }
            else if ( (LA129_0==COLON||(LA129_0>=HASH_SYMBOL && LA129_0<=SASS_EXTEND_ONLY_SELECTOR)) ) {
                alt129=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 129, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(129);}

            switch (alt129) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(647,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(647,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence3260);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(647,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(126);

                    loop126:
                    do {
                        int alt126=2;
                        try { dbg.enterDecision(126, decisionCanBacktrack[126]);

                        try {
                            isCyclicDecision = true;
                            alt126 = dfa126.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(126);}

                        switch (alt126) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(647,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence3267);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(647,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:46: ( ws )?
                    	    int alt125=2;
                    	    try { dbg.enterSubRule(125);
                    	    try { dbg.enterDecision(125, decisionCanBacktrack[125]);

                    	    int LA125_0 = input.LA(1);

                    	    if ( (LA125_0==WS||(LA125_0>=NL && LA125_0<=COMMENT)) ) {
                    	        alt125=1;
                    	    }
                    	    } finally {dbg.exitDecision(125);}

                    	    switch (alt125) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:46: ws
                    	            {
                    	            dbg.location(647,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence3269);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(125);}


                    	    }
                    	    break;

                    	default :
                    	    break loop126;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(126);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(649,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(649,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt128=0;
                    try { dbg.enterSubRule(128);

                    loop128:
                    do {
                        int alt128=2;
                        try { dbg.enterDecision(128, decisionCanBacktrack[128]);

                        switch ( input.LA(1) ) {
                        case SASS_EXTEND_ONLY_SELECTOR:
                            {
                            int LA128_2 = input.LA(2);

                            if ( ((synpred13_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                                alt128=1;
                            }


                            }
                            break;
                        case HASH:
                            {
                            int LA128_3 = input.LA(2);

                            if ( (synpred13_Css3()) ) {
                                alt128=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA128_4 = input.LA(2);

                            if ( (synpred13_Css3()) ) {
                                alt128=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA128_5 = input.LA(2);

                            if ( (synpred13_Css3()) ) {
                                alt128=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA128_6 = input.LA(2);

                            if ( (synpred13_Css3()) ) {
                                alt128=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA128_7 = input.LA(2);

                            if ( (synpred13_Css3()) ) {
                                alt128=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(128);}

                        switch (alt128) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(649,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence3288);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(649,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:33: ws
                    	            {
                    	            dbg.location(649,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence3290);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(127);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt128 >= 1 ) break loop128;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(128, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt128++;
                    } while (true);
                    } finally {dbg.exitSubRule(128);}


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
        dbg.location(650, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(657, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:658:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(658,5);
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
        dbg.location(659, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(661, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(663,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt130=2;
            try { dbg.enterSubRule(130);
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            int LA130_0 = input.LA(1);

            if ( (LA130_0==IDENT) ) {
                int LA130_1 = input.LA(2);

                if ( (synpred14_Css3()) ) {
                    alt130=1;
                }
            }
            else if ( (LA130_0==STAR) ) {
                int LA130_2 = input.LA(2);

                if ( (synpred14_Css3()) ) {
                    alt130=1;
                }
            }
            else if ( (LA130_0==PIPE) && (synpred14_Css3())) {
                alt130=1;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(663,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector3406);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(130);}

            dbg.location(663,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:51: elementName ( ws )?
            {
            dbg.location(663,51);
            pushFollow(FOLLOW_elementName_in_typeSelector3412);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(663,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:63: ws
                    {
                    dbg.location(663,63);
                    pushFollow(FOLLOW_ws_in_typeSelector3414);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(131);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(664, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(666, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(667,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:5: ( namespacePrefixName | STAR )?
            int alt132=3;
            try { dbg.enterSubRule(132);
            try { dbg.enterDecision(132, decisionCanBacktrack[132]);

            int LA132_0 = input.LA(1);

            if ( (LA132_0==IDENT) ) {
                alt132=1;
            }
            else if ( (LA132_0==STAR) ) {
                alt132=2;
            }
            } finally {dbg.exitDecision(132);}

            switch (alt132) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:7: namespacePrefixName
                    {
                    dbg.location(667,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix3432);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:29: STAR
                    {
                    dbg.location(667,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix3436); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(132);}

            dbg.location(667,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix3440); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(668, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:1: elementSubsequent : ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(671, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:5: ( ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(673,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            int alt133=5;
            try { dbg.enterSubRule(133);
            try { dbg.enterDecision(133, decisionCanBacktrack[133]);

            switch ( input.LA(1) ) {
            case SASS_EXTEND_ONLY_SELECTOR:
                {
                alt133=1;
                }
                break;
            case HASH_SYMBOL:
            case HASH:
                {
                alt133=2;
                }
                break;
            case DOT:
                {
                alt133=3;
                }
                break;
            case LBRACKET:
                {
                alt133=4;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt133=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 133, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(133);}

            switch (alt133) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:9: {...}? sass_extend_only_selector
                    {
                    dbg.location(674,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "elementSubsequent", "isScssSource()");
                    }
                    dbg.location(674,27);
                    pushFollow(FOLLOW_sass_extend_only_selector_in_elementSubsequent3479);
                    sass_extend_only_selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:8: cssId
                    {
                    dbg.location(675,8);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent3488);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:8: cssClass
                    {
                    dbg.location(676,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent3497);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:11: slAttribute
                    {
                    dbg.location(677,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent3509);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:11: pseudo
                    {
                    dbg.location(678,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent3521);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(133);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(680, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(683, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt134=2;
            try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            int LA134_0 = input.LA(1);

            if ( (LA134_0==HASH) ) {
                alt134=1;
            }
            else if ( (LA134_0==HASH_SYMBOL) ) {
                alt134=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 134, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(134);}

            switch (alt134) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:7: HASH
                    {
                    dbg.location(684,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId3549); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(684,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:16: HASH_SYMBOL NAME
                    {
                    dbg.location(684,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId3555); if (state.failed) return ;
                    dbg.location(684,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId3557); if (state.failed) return ;

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
        dbg.location(685, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(691, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:7: DOT ( IDENT | GEN )
            {
            dbg.location(692,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass3585); if (state.failed) return ;
            dbg.location(692,11);
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
        dbg.location(693, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(700, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(701,5);
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
        dbg.location(702, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(704, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(705,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute3659); if (state.failed) return ;
            dbg.location(706,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:6: ( namespacePrefix )?
            int alt135=2;
            try { dbg.enterSubRule(135);
            try { dbg.enterDecision(135, decisionCanBacktrack[135]);

            int LA135_0 = input.LA(1);

            if ( (LA135_0==IDENT) ) {
                int LA135_1 = input.LA(2);

                if ( (LA135_1==PIPE) ) {
                    alt135=1;
                }
            }
            else if ( ((LA135_0>=STAR && LA135_0<=PIPE)) ) {
                alt135=1;
            }
            } finally {dbg.exitDecision(135);}

            switch (alt135) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:6: namespacePrefix
                    {
                    dbg.location(706,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute3666);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(135);}

            dbg.location(706,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:23: ws
                    {
                    dbg.location(706,23);
                    pushFollow(FOLLOW_ws_in_slAttribute3669);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}

            dbg.location(707,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute3680);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(707,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:25: ws
                    {
                    dbg.location(707,25);
                    pushFollow(FOLLOW_ws_in_slAttribute3682);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(709,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt140=2;
            try { dbg.enterSubRule(140);
            try { dbg.enterDecision(140, decisionCanBacktrack[140]);

            int LA140_0 = input.LA(1);

            if ( ((LA140_0>=OPEQ && LA140_0<=CONTAINS)) ) {
                alt140=1;
            }
            } finally {dbg.exitDecision(140);}

            switch (alt140) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(710,17);
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

                    dbg.location(718,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:17: ws
                            {
                            dbg.location(718,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3904);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(138);}

                    dbg.location(719,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute3923);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(720,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:17: ws
                            {
                            dbg.location(720,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3941);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(139);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(723,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3970); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(724, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(731, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:4: IDENT
            {
            dbg.location(732,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3986); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(733, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(735, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:2: ( IDENT | STRING )
            {
            dbg.location(737,2);
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
        dbg.location(741, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(743, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(744,7);
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

            dbg.location(745,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt148=2;
            try { dbg.enterSubRule(148);
            try { dbg.enterDecision(148, decisionCanBacktrack[148]);

            int LA148_0 = input.LA(1);

            if ( (LA148_0==IDENT||LA148_0==GEN) ) {
                alt148=1;
            }
            else if ( (LA148_0==NOT) ) {
                alt148=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 148, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(148);}

            switch (alt148) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(746,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(747,21);
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

                    dbg.location(748,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    int alt144=2;
                    try { dbg.enterSubRule(144);
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(749,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:25: ws
                                    {
                                    dbg.location(749,25);
                                    pushFollow(FOLLOW_ws_in_pseudo4181);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(141);}

                            dbg.location(749,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo4184); if (state.failed) return ;
                            dbg.location(749,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:36: ws
                                    {
                                    dbg.location(749,36);
                                    pushFollow(FOLLOW_ws_in_pseudo4186);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(142);}

                            dbg.location(749,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:40: ( expression | STAR )?
                            int alt143=3;
                            try { dbg.enterSubRule(143);
                            try { dbg.enterDecision(143, decisionCanBacktrack[143]);

                            int LA143_0 = input.LA(1);

                            if ( ((LA143_0>=IDENT && LA143_0<=URI)||LA143_0==MEDIA_SYM||LA143_0==GEN||LA143_0==AT_IDENT||LA143_0==PERCENTAGE||LA143_0==PLUS||LA143_0==MINUS||LA143_0==HASH||(LA143_0>=NUMBER && LA143_0<=DIMENSION)||LA143_0==SASS_VAR) ) {
                                alt143=1;
                            }
                            else if ( (LA143_0==STAR) ) {
                                alt143=2;
                            }
                            } finally {dbg.exitDecision(143);}

                            switch (alt143) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:42: expression
                                    {
                                    dbg.location(749,42);
                                    pushFollow(FOLLOW_expression_in_pseudo4191);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:55: STAR
                                    {
                                    dbg.location(749,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo4195); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(143);}

                            dbg.location(749,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo4200); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(144);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(753,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(753,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo4279); if (state.failed) return ;
                    dbg.location(753,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:23: ws
                            {
                            dbg.location(753,23);
                            pushFollow(FOLLOW_ws_in_pseudo4281);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(145);}

                    dbg.location(753,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo4284); if (state.failed) return ;
                    dbg.location(753,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:34: ws
                            {
                            dbg.location(753,34);
                            pushFollow(FOLLOW_ws_in_pseudo4286);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(146);}

                    dbg.location(753,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:38: ( simpleSelectorSequence )?
                    int alt147=2;
                    try { dbg.enterSubRule(147);
                    try { dbg.enterDecision(147, decisionCanBacktrack[147]);

                    int LA147_0 = input.LA(1);

                    if ( (LA147_0==IDENT||LA147_0==GEN||LA147_0==COLON||(LA147_0>=HASH_SYMBOL && LA147_0<=PIPE)||LA147_0==LESS_AND) ) {
                        alt147=1;
                    }
                    } finally {dbg.exitDecision(147);}

                    switch (alt147) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:38: simpleSelectorSequence
                            {
                            dbg.location(753,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo4289);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(147);}

                    dbg.location(753,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo4292); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(148);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(755, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:757:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(757, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(759,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: ( STAR )?
            int alt149=2;
            try { dbg.enterSubRule(149);
            try { dbg.enterDecision(149, decisionCanBacktrack[149]);

            int LA149_0 = input.LA(1);

            if ( (LA149_0==STAR) ) {
                alt149=1;
            }
            } finally {dbg.exitDecision(149);}

            switch (alt149) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: STAR
                    {
                    dbg.location(759,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration4331); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(149);}

            dbg.location(759,11);
            pushFollow(FOLLOW_property_in_declaration4334);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(759,20);
            match(input,COLON,FOLLOW_COLON_in_declaration4336); if (state.failed) return ;
            dbg.location(759,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:26: ws
                    {
                    dbg.location(759,26);
                    pushFollow(FOLLOW_ws_in_declaration4338);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}

            dbg.location(759,30);
            pushFollow(FOLLOW_propertyValue_in_declaration4341);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(759,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:44: ( prio ( ws )? )?
            int alt152=2;
            try { dbg.enterSubRule(152);
            try { dbg.enterDecision(152, decisionCanBacktrack[152]);

            int LA152_0 = input.LA(1);

            if ( (LA152_0==IMPORTANT_SYM) ) {
                alt152=1;
            }
            } finally {dbg.exitDecision(152);}

            switch (alt152) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:45: prio ( ws )?
                    {
                    dbg.location(759,45);
                    pushFollow(FOLLOW_prio_in_declaration4344);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(759,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:50: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:50: ws
                            {
                            dbg.location(759,50);
                            pushFollow(FOLLOW_ws_in_declaration4346);
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
        dbg.location(760, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:1: propertyValue : ( ( (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(768, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:2: ( ( (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) )
            int alt153=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:9: ( (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression
                    {
                    dbg.location(772,52);
                    pushFollow(FOLLOW_scss_declaration_property_value_interpolation_expression_in_propertyValue4412);
                    scss_declaration_property_value_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(773,34);
                    pushFollow(FOLLOW_expression_in_propertyValue4428);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:9: ({...}? cp_expression )
                    {
                    dbg.location(783,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:11: {...}? cp_expression
                    {
                    dbg.location(783,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(783,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue4469);
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
        dbg.location(784, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(787, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:789:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(790,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt154=0;
            try { dbg.enterSubRule(154);

            loop154:
            do {
                int alt154=2;
                try { dbg.enterDecision(154, decisionCanBacktrack[154]);

                int LA154_0 = input.LA(1);

                if ( (LA154_0==NAMESPACE_SYM||(LA154_0>=IDENT && LA154_0<=MEDIA_SYM)||(LA154_0>=AND && LA154_0<=RPAREN)||(LA154_0>=WS && LA154_0<=RIGHTBOTTOM_SYM)||(LA154_0>=PLUS && LA154_0<=SASS_EXTEND_ONLY_SELECTOR)||(LA154_0>=PIPE && LA154_0<=LINE_COMMENT)) ) {
                    alt154=1;
                }


                } finally {dbg.exitDecision(154);}

                switch (alt154) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(790,7);
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
            	    if ( cnt154 >= 1 ) break loop154;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(154, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt154++;
            } while (true);
            } finally {dbg.exitSubRule(154);}

            dbg.location(790,65);
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
        dbg.location(791, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(795, 1);

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
            dbg.exitRule(getGrammarFileName(), "syncToDeclarationsRule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToDeclarationsRule"


    // $ANTLR start "syncTo_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(803, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:6: 
            {
            }

        }
        finally {
        }
        dbg.location(808, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(810, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:13: SEMI
            {
            dbg.location(815,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI4654); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(816, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(819, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:6: 
            {
            }

        }
        finally {
        }
        dbg.location(824, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(826, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:7: IMPORTANT_SYM
            {
            dbg.location(827,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio4709); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(830, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(831,7);
            pushFollow(FOLLOW_term_in_expression4730);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(831,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(157);

            loop157:
            do {
                int alt157=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(831,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:14: ( operator ( ws )? )?
            	    int alt156=2;
            	    try { dbg.enterSubRule(156);
            	    try { dbg.enterDecision(156, decisionCanBacktrack[156]);

            	    int LA156_0 = input.LA(1);

            	    if ( (LA156_0==COMMA||LA156_0==SOLIDUS) ) {
            	        alt156=1;
            	    }
            	    } finally {dbg.exitDecision(156);}

            	    switch (alt156) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:15: operator ( ws )?
            	            {
            	            dbg.location(831,15);
            	            pushFollow(FOLLOW_operator_in_expression4735);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(831,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:24: ws
            	                    {
            	                    dbg.location(831,24);
            	                    pushFollow(FOLLOW_ws_in_expression4737);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(155);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(156);}

            	    dbg.location(831,30);
            	    pushFollow(FOLLOW_term_in_expression4742);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop157;
                }
            } while (true);
            } finally {dbg.exitSubRule(157);}


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
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(834, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(835,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:7: ( unaryOperator ( ws )? )?
            int alt159=2;
            try { dbg.enterSubRule(159);
            try { dbg.enterDecision(159, decisionCanBacktrack[159]);

            int LA159_0 = input.LA(1);

            if ( (LA159_0==PLUS||LA159_0==MINUS) ) {
                alt159=1;
            }
            } finally {dbg.exitDecision(159);}

            switch (alt159) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:9: unaryOperator ( ws )?
                    {
                    dbg.location(835,9);
                    pushFollow(FOLLOW_unaryOperator_in_term4767);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(835,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:835:23: ws
                            {
                            dbg.location(835,23);
                            pushFollow(FOLLOW_ws_in_term4769);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(158);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(159);}

            dbg.location(836,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt160=8;
            try { dbg.enterSubRule(160);
            try { dbg.enterDecision(160, decisionCanBacktrack[160]);

            try {
                isCyclicDecision = true;
                alt160 = dfa160.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(160);}

            switch (alt160) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(837,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:850:7: STRING
                    {
                    dbg.location(850,7);
                    match(input,STRING,FOLLOW_STRING_in_term4993); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:7: IDENT
                    {
                    dbg.location(851,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term5001); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:852:7: GEN
                    {
                    dbg.location(852,7);
                    match(input,GEN,FOLLOW_GEN_in_term5009); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:853:7: URI
                    {
                    dbg.location(853,7);
                    match(input,URI,FOLLOW_URI_in_term5017); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:7: hexColor
                    {
                    dbg.location(854,7);
                    pushFollow(FOLLOW_hexColor_in_term5025);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:7: function
                    {
                    dbg.location(855,7);
                    pushFollow(FOLLOW_function_in_term5033);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:7: {...}? cp_variable
                    {
                    dbg.location(856,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(856,36);
                    pushFollow(FOLLOW_cp_variable_in_term5043);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(858,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:5: ws
                    {
                    dbg.location(858,5);
                    pushFollow(FOLLOW_ws_in_term5055);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(161);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(859, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(861, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN
            {
            dbg.location(862,5);
            pushFollow(FOLLOW_functionName_in_function5071);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(862,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:18: ws
                    {
                    dbg.location(862,18);
                    pushFollow(FOLLOW_ws_in_function5073);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(162);}

            dbg.location(863,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function5078); if (state.failed) return ;
            dbg.location(863,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:10: ws
                    {
                    dbg.location(863,10);
                    pushFollow(FOLLOW_ws_in_function5080);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(163);}

            dbg.location(864,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?)
            int alt166=3;
            try { dbg.enterSubRule(166);
            try { dbg.enterDecision(166, decisionCanBacktrack[166]);

            try {
                isCyclicDecision = true;
                alt166 = dfa166.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(166);}

            switch (alt166) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:4: expression
                    {
                    dbg.location(865,4);
                    pushFollow(FOLLOW_expression_in_function5090);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(867,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(868,5);
                    pushFollow(FOLLOW_fnAttribute_in_function5108);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(868,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(165);

                    loop165:
                    do {
                        int alt165=2;
                        try { dbg.enterDecision(165, decisionCanBacktrack[165]);

                        int LA165_0 = input.LA(1);

                        if ( (LA165_0==COMMA) ) {
                            alt165=1;
                        }


                        } finally {dbg.exitDecision(165);}

                        switch (alt165) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(868,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function5111); if (state.failed) return ;
                    	    dbg.location(868,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:24: ws
                    	            {
                    	            dbg.location(868,24);
                    	            pushFollow(FOLLOW_ws_in_function5113);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(164);}

                    	    dbg.location(868,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function5116);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop165;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(165);}


                    }


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:17: {...}?
                    {
                    dbg.location(871,17);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "function", "isCssPreprocessorSource()");
                    }

                    }
                    break;

            }
            } finally {dbg.exitSubRule(166);}

            dbg.location(873,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function5174); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(874, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(880, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(884,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:4: ( IDENT COLON )?
            int alt167=2;
            try { dbg.enterSubRule(167);
            try { dbg.enterDecision(167, decisionCanBacktrack[167]);

            int LA167_0 = input.LA(1);

            if ( (LA167_0==IDENT) ) {
                int LA167_1 = input.LA(2);

                if ( (LA167_1==COLON) ) {
                    alt167=1;
                }
            }
            } finally {dbg.exitDecision(167);}

            switch (alt167) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:5: IDENT COLON
                    {
                    dbg.location(884,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName5222); if (state.failed) return ;
                    dbg.location(884,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName5224); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(167);}

            dbg.location(884,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName5228); if (state.failed) return ;
            dbg.location(884,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:25: ( DOT IDENT )*
            try { dbg.enterSubRule(168);

            loop168:
            do {
                int alt168=2;
                try { dbg.enterDecision(168, decisionCanBacktrack[168]);

                int LA168_0 = input.LA(1);

                if ( (LA168_0==DOT) ) {
                    alt168=1;
                }


                } finally {dbg.exitDecision(168);}

                switch (alt168) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:26: DOT IDENT
            	    {
            	    dbg.location(884,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName5231); if (state.failed) return ;
            	    dbg.location(884,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName5233); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop168;
                }
            } while (true);
            } finally {dbg.exitSubRule(168);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(886, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(888, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(889,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute5256);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(889,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:20: ws
                    {
                    dbg.location(889,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute5258);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(169);}

            dbg.location(889,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute5261); if (state.failed) return ;
            dbg.location(889,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:29: ws
                    {
                    dbg.location(889,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute5263);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(170);}

            dbg.location(889,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute5266);
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
        dbg.location(890, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(892, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:4: IDENT ( DOT IDENT )*
            {
            dbg.location(893,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName5281); if (state.failed) return ;
            dbg.location(893,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:10: ( DOT IDENT )*
            try { dbg.enterSubRule(171);

            loop171:
            do {
                int alt171=2;
                try { dbg.enterDecision(171, decisionCanBacktrack[171]);

                int LA171_0 = input.LA(1);

                if ( (LA171_0==DOT) ) {
                    alt171=1;
                }


                } finally {dbg.exitDecision(171);}

                switch (alt171) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:11: DOT IDENT
            	    {
            	    dbg.location(893,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName5284); if (state.failed) return ;
            	    dbg.location(893,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName5286); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop171;
                }
            } while (true);
            } finally {dbg.exitSubRule(171);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(894, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(896, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:4: expression
            {
            dbg.location(897,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue5300);
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
        dbg.location(898, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(900, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:7: HASH
            {
            dbg.location(901,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor5318); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(902, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(904, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:7: ( WS | NL | COMMENT )+
            {
            dbg.location(905,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:7: ( WS | NL | COMMENT )+
            int cnt172=0;
            try { dbg.enterSubRule(172);

            loop172:
            do {
                int alt172=2;
                try { dbg.enterDecision(172, decisionCanBacktrack[172]);

                int LA172_0 = input.LA(1);

                if ( (LA172_0==WS||(LA172_0>=NL && LA172_0<=COMMENT)) ) {
                    alt172=1;
                }


                } finally {dbg.exitDecision(172);}

                switch (alt172) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(905,7);
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
            	    if ( cnt172 >= 1 ) break loop172;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(172, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt172++;
            } while (true);
            } finally {dbg.exitSubRule(172);}


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
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"


    // $ANTLR start "cp_variable_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(911, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt179=2;
            try { dbg.enterDecision(179, decisionCanBacktrack[179]);

            int LA179_0 = input.LA(1);

            if ( (LA179_0==MEDIA_SYM||LA179_0==AT_IDENT) ) {
                int LA179_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt179=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt179=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 179, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA179_0==SASS_VAR) ) {
                int LA179_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt179=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt179=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 179, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 179, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(179);}

            switch (alt179) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI
                    {
                    dbg.location(913,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(913,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5387);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(913,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:39: ws
                            {
                            dbg.location(913,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5389);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(173);}

                    dbg.location(913,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5392); if (state.failed) return ;
                    dbg.location(913,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:49: ws
                            {
                            dbg.location(913,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5394);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(174);}

                    dbg.location(913,53);
                    pushFollow(FOLLOW_cp_variable_value_in_cp_variable_declaration5397);
                    cp_variable_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(913,71);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5399); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(915,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(915,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5426);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(915,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:39: ws
                            {
                            dbg.location(915,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5428);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(175);}

                    dbg.location(915,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5431); if (state.failed) return ;
                    dbg.location(915,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:49: ws
                            {
                            dbg.location(915,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5433);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(176);}

                    dbg.location(915,53);
                    pushFollow(FOLLOW_cp_variable_value_in_cp_variable_declaration5436);
                    cp_variable_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(915,71);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:71: ( SASS_DEFAULT ( ws )? )?
                    int alt178=2;
                    try { dbg.enterSubRule(178);
                    try { dbg.enterDecision(178, decisionCanBacktrack[178]);

                    int LA178_0 = input.LA(1);

                    if ( (LA178_0==SASS_DEFAULT) ) {
                        alt178=1;
                    }
                    } finally {dbg.exitDecision(178);}

                    switch (alt178) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:72: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(915,72);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5439); if (state.failed) return ;
                            dbg.location(915,85);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:85: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:85: ws
                                    {
                                    dbg.location(915,85);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration5441);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(177);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(178);}

                    dbg.location(915,91);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5446); if (state.failed) return ;

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
        dbg.location(916, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(919, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt180=2;
            try { dbg.enterDecision(180, decisionCanBacktrack[180]);

            int LA180_0 = input.LA(1);

            if ( (LA180_0==MEDIA_SYM||LA180_0==AT_IDENT) ) {
                alt180=1;
            }
            else if ( (LA180_0==SASS_VAR) ) {
                alt180=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 180, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(180);}

            switch (alt180) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(921,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(921,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(923,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(923,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:29: SASS_VAR
                    {
                    dbg.location(923,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable5511); if (state.failed) return ;

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
        dbg.location(925, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable"


    // $ANTLR start "cp_variable_value"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:1: cp_variable_value : cp_expression ( COMMA ( ws )? cp_expression )* ;
    public final void cp_variable_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(927, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:928:5: ( cp_expression ( COMMA ( ws )? cp_expression )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:5: cp_expression ( COMMA ( ws )? cp_expression )*
            {
            dbg.location(929,5);
            pushFollow(FOLLOW_cp_expression_in_cp_variable_value5535);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(929,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:19: ( COMMA ( ws )? cp_expression )*
            try { dbg.enterSubRule(182);

            loop182:
            do {
                int alt182=2;
                try { dbg.enterDecision(182, decisionCanBacktrack[182]);

                int LA182_0 = input.LA(1);

                if ( (LA182_0==COMMA) ) {
                    alt182=1;
                }


                } finally {dbg.exitDecision(182);}

                switch (alt182) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:21: COMMA ( ws )? cp_expression
            	    {
            	    dbg.location(929,21);
            	    match(input,COMMA,FOLLOW_COMMA_in_cp_variable_value5539); if (state.failed) return ;
            	    dbg.location(929,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:27: ws
            	            {
            	            dbg.location(929,27);
            	            pushFollow(FOLLOW_ws_in_cp_variable_value5541);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(181);}

            	    dbg.location(929,31);
            	    pushFollow(FOLLOW_cp_expression_in_cp_variable_value5544);
            	    cp_expression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop182;
                }
            } while (true);
            } finally {dbg.exitSubRule(182);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(930, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_variable_value");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable_value"


    // $ANTLR start "cp_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(933, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:10: cp_additionExp
            {
            dbg.location(934,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression5572);
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
        dbg.location(935, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(937, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(938,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5592);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(939,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(185);

            loop185:
            do {
                int alt185=3;
                try { dbg.enterDecision(185, decisionCanBacktrack[185]);

                int LA185_0 = input.LA(1);

                if ( (LA185_0==PLUS) ) {
                    alt185=1;
                }
                else if ( (LA185_0==MINUS) ) {
                    alt185=2;
                }


                } finally {dbg.exitDecision(185);}

                switch (alt185) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(939,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp5606); if (state.failed) return ;
            	    dbg.location(939,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:17: ws
            	            {
            	            dbg.location(939,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5608);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(183);}

            	    dbg.location(939,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5611);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(940,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp5624); if (state.failed) return ;
            	    dbg.location(940,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:18: ws
            	            {
            	            dbg.location(940,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5626);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(184);}

            	    dbg.location(940,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5629);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop185;
                }
            } while (true);
            } finally {dbg.exitSubRule(185);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(942, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(944, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(945,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5662);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(946,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(188);

            loop188:
            do {
                int alt188=3;
                try { dbg.enterDecision(188, decisionCanBacktrack[188]);

                int LA188_0 = input.LA(1);

                if ( (LA188_0==STAR) ) {
                    alt188=1;
                }
                else if ( (LA188_0==SOLIDUS) ) {
                    alt188=2;
                }


                } finally {dbg.exitDecision(188);}

                switch (alt188) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(946,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp5675); if (state.failed) return ;
            	    dbg.location(946,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:17: ws
            	            {
            	            dbg.location(946,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5677);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(186);}

            	    dbg.location(946,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5680);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(947,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp5694); if (state.failed) return ;
            	    dbg.location(947,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:20: ws
            	            {
            	            dbg.location(947,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5696);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(187);}

            	    dbg.location(947,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5699);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop188;
                }
            } while (true);
            } finally {dbg.exitSubRule(188);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(949, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:951:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(951, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt192=2;
            try { dbg.enterDecision(192, decisionCanBacktrack[192]);

            int LA192_0 = input.LA(1);

            if ( ((LA192_0>=IDENT && LA192_0<=URI)||LA192_0==MEDIA_SYM||LA192_0==GEN||LA192_0==AT_IDENT||LA192_0==PERCENTAGE||LA192_0==PLUS||LA192_0==MINUS||LA192_0==HASH||(LA192_0>=NUMBER && LA192_0<=DIMENSION)||LA192_0==SASS_VAR) ) {
                alt192=1;
            }
            else if ( (LA192_0==LPAREN) ) {
                alt192=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 192, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(192);}

            switch (alt192) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:10: term ( ( term )=> term )*
                    {
                    dbg.location(952,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp5732);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(952,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(189);

                    loop189:
                    do {
                        int alt189=2;
                        try { dbg.enterDecision(189, decisionCanBacktrack[189]);

                        try {
                            isCyclicDecision = true;
                            alt189 = dfa189.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(189);}

                        switch (alt189) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:16: ( term )=> term
                    	    {
                    	    dbg.location(952,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp5739);
                    	    term();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop189;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(189);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(953,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp5753); if (state.failed) return ;
                    dbg.location(953,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:17: ( ws )?
                    int alt190=2;
                    try { dbg.enterSubRule(190);
                    try { dbg.enterDecision(190, decisionCanBacktrack[190]);

                    int LA190_0 = input.LA(1);

                    if ( (LA190_0==WS||(LA190_0>=NL && LA190_0<=COMMENT)) ) {
                        alt190=1;
                    }
                    } finally {dbg.exitDecision(190);}

                    switch (alt190) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:17: ws
                            {
                            dbg.location(953,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5755);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(190);}

                    dbg.location(953,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp5758);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(953,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp5760); if (state.failed) return ;
                    dbg.location(953,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:43: ws
                            {
                            dbg.location(953,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5762);
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
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(954, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(957, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(959,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt193=8;
            try { dbg.enterSubRule(193);
            try { dbg.enterDecision(193, decisionCanBacktrack[193]);

            try {
                isCyclicDecision = true;
                alt193 = dfa193.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(193);}

            switch (alt193) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:960:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(960,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:7: STRING
                    {
                    dbg.location(973,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term6000); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:7: IDENT
                    {
                    dbg.location(974,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term6008); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:7: GEN
                    {
                    dbg.location(975,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term6016); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:976:7: URI
                    {
                    dbg.location(976,7);
                    match(input,URI,FOLLOW_URI_in_cp_term6024); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:7: hexColor
                    {
                    dbg.location(977,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term6032);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:7: function
                    {
                    dbg.location(978,7);
                    pushFollow(FOLLOW_function_in_cp_term6040);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:979:7: cp_variable
                    {
                    dbg.location(979,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term6048);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(193);}

            dbg.location(981,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:5: ( ws )?
            int alt194=2;
            try { dbg.enterSubRule(194);
            try { dbg.enterDecision(194, decisionCanBacktrack[194]);

            int LA194_0 = input.LA(1);

            if ( (LA194_0==WS||(LA194_0>=NL && LA194_0<=COMMENT)) ) {
                alt194=1;
            }
            } finally {dbg.exitDecision(194);}

            switch (alt194) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:5: ws
                    {
                    dbg.location(981,5);
                    pushFollow(FOLLOW_ws_in_cp_term6060);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(194);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(982, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( cp_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(991, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( cp_args_list )? RPAREN ( ws )? )? )
            int alt204=2;
            try { dbg.enterDecision(204, decisionCanBacktrack[204]);

            int LA204_0 = input.LA(1);

            if ( (LA204_0==DOT) ) {
                alt204=1;
            }
            else if ( (LA204_0==SASS_MIXIN) ) {
                alt204=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 204, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(204);}

            switch (alt204) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(993,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(993,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration6091); if (state.failed) return ;
                    dbg.location(993,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration6093);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(993,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:41: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:41: ws
                            {
                            dbg.location(993,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6095);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(195);}

                    dbg.location(993,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration6098); if (state.failed) return ;
                    dbg.location(993,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:52: ( cp_args_list )?
                    int alt196=2;
                    try { dbg.enterSubRule(196);
                    try { dbg.enterDecision(196, decisionCanBacktrack[196]);

                    int LA196_0 = input.LA(1);

                    if ( (LA196_0==MEDIA_SYM||LA196_0==AT_IDENT||LA196_0==SASS_VAR||(LA196_0>=LESS_DOTS && LA196_0<=LESS_REST)) ) {
                        alt196=1;
                    }
                    } finally {dbg.exitDecision(196);}

                    switch (alt196) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:52: cp_args_list
                            {
                            dbg.location(993,52);
                            pushFollow(FOLLOW_cp_args_list_in_cp_mixin_declaration6100);
                            cp_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(196);}

                    dbg.location(993,66);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration6103); if (state.failed) return ;
                    dbg.location(993,73);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:73: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:73: ws
                            {
                            dbg.location(993,73);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6105);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(197);}

                    dbg.location(993,77);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:77: ( less_mixin_guarded ( ws )? )?
                    int alt199=2;
                    try { dbg.enterSubRule(199);
                    try { dbg.enterDecision(199, decisionCanBacktrack[199]);

                    int LA199_0 = input.LA(1);

                    if ( (LA199_0==LESS_WHEN) ) {
                        alt199=1;
                    }
                    } finally {dbg.exitDecision(199);}

                    switch (alt199) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:78: less_mixin_guarded ( ws )?
                            {
                            dbg.location(993,78);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration6109);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(993,97);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:97: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:97: ws
                                    {
                                    dbg.location(993,97);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6111);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(198);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(199);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( cp_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(995,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(995,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration6128); if (state.failed) return ;
                    dbg.location(995,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6130);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(995,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration6132);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(995,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:51: ws
                            {
                            dbg.location(995,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6134);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(200);}

                    dbg.location(995,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:55: ( LPAREN ( cp_args_list )? RPAREN ( ws )? )?
                    int alt203=2;
                    try { dbg.enterSubRule(203);
                    try { dbg.enterDecision(203, decisionCanBacktrack[203]);

                    int LA203_0 = input.LA(1);

                    if ( (LA203_0==LPAREN) ) {
                        alt203=1;
                    }
                    } finally {dbg.exitDecision(203);}

                    switch (alt203) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:56: LPAREN ( cp_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(995,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration6138); if (state.failed) return ;
                            dbg.location(995,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:63: ( cp_args_list )?
                            int alt201=2;
                            try { dbg.enterSubRule(201);
                            try { dbg.enterDecision(201, decisionCanBacktrack[201]);

                            int LA201_0 = input.LA(1);

                            if ( (LA201_0==MEDIA_SYM||LA201_0==AT_IDENT||LA201_0==SASS_VAR||(LA201_0>=LESS_DOTS && LA201_0<=LESS_REST)) ) {
                                alt201=1;
                            }
                            } finally {dbg.exitDecision(201);}

                            switch (alt201) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:63: cp_args_list
                                    {
                                    dbg.location(995,63);
                                    pushFollow(FOLLOW_cp_args_list_in_cp_mixin_declaration6140);
                                    cp_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(201);}

                            dbg.location(995,77);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration6143); if (state.failed) return ;
                            dbg.location(995,84);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:84: ( ws )?
                            int alt202=2;
                            try { dbg.enterSubRule(202);
                            try { dbg.enterDecision(202, decisionCanBacktrack[202]);

                            int LA202_0 = input.LA(1);

                            if ( (LA202_0==WS||(LA202_0>=NL && LA202_0<=COMMENT)) ) {
                                alt202=1;
                            }
                            } finally {dbg.exitDecision(202);}

                            switch (alt202) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:84: ws
                                    {
                                    dbg.location(995,84);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6145);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(202);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(203);}


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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_declaration"


    // $ANTLR start "cp_mixin_call"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1000, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(1002,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt205=2;
            try { dbg.enterSubRule(205);
            try { dbg.enterDecision(205, decisionCanBacktrack[205]);

            int LA205_0 = input.LA(1);

            if ( (LA205_0==DOT) ) {
                alt205=1;
            }
            else if ( (LA205_0==SASS_INCLUDE) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1003:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(1003,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(1003,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call6187); if (state.failed) return ;
                    dbg.location(1003,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call6189);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(1005,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(1005,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call6211); if (state.failed) return ;
                    dbg.location(1005,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call6213);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1005,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call6215);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(205);}

            dbg.location(1007,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:5: ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )?
            int alt209=2;
            try { dbg.enterSubRule(209);
            try { dbg.enterDecision(209, decisionCanBacktrack[209]);

            try {
                isCyclicDecision = true;
                alt209 = dfa209.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(209);}

            switch (alt209) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:6: ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(1007,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:6: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:6: ws
                            {
                            dbg.location(1007,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call6228);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(206);}

                    dbg.location(1007,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call6231); if (state.failed) return ;
                    dbg.location(1007,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:17: ( ws )?
                    int alt207=2;
                    try { dbg.enterSubRule(207);
                    try { dbg.enterDecision(207, decisionCanBacktrack[207]);

                    int LA207_0 = input.LA(1);

                    if ( (LA207_0==WS||(LA207_0>=NL && LA207_0<=COMMENT)) ) {
                        alt207=1;
                    }
                    } finally {dbg.exitDecision(207);}

                    switch (alt207) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:17: ws
                            {
                            dbg.location(1007,17);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call6233);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(207);}

                    dbg.location(1007,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:21: ( cp_mixin_call_args )?
                    int alt208=2;
                    try { dbg.enterSubRule(208);
                    try { dbg.enterDecision(208, decisionCanBacktrack[208]);

                    int LA208_0 = input.LA(1);

                    if ( ((LA208_0>=IDENT && LA208_0<=URI)||LA208_0==MEDIA_SYM||LA208_0==GEN||LA208_0==AT_IDENT||LA208_0==PERCENTAGE||LA208_0==PLUS||LA208_0==MINUS||LA208_0==HASH||(LA208_0>=NUMBER && LA208_0<=DIMENSION)||LA208_0==SASS_VAR) ) {
                        alt208=1;
                    }
                    } finally {dbg.exitDecision(208);}

                    switch (alt208) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:21: cp_mixin_call_args
                            {
                            dbg.location(1007,21);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call6236);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(208);}

                    dbg.location(1007,41);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call6239); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(209);}

            dbg.location(1007,50);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:50: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:50: ws
                    {
                    dbg.location(1007,50);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call6243);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(210);}

            dbg.location(1007,54);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call6246); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1008, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1010, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:5: IDENT
            {
            dbg.location(1012,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name6275); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_name"


    // $ANTLR start "cp_mixin_call_args"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:1: cp_mixin_call_args : cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1015, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:5: ( cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:5: cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )*
            {
            dbg.location(1019,5);
            pushFollow(FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6311);
            cp_mixin_call_arg();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1019,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:23: ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )*
            try { dbg.enterSubRule(212);

            loop212:
            do {
                int alt212=2;
                try { dbg.enterDecision(212, decisionCanBacktrack[212]);

                int LA212_0 = input.LA(1);

                if ( (LA212_0==SEMI||LA212_0==COMMA) ) {
                    alt212=1;
                }


                } finally {dbg.exitDecision(212);}

                switch (alt212) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:25: ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg
            	    {
            	    dbg.location(1019,25);
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

            	    dbg.location(1019,40);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:40: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:40: ws
            	            {
            	            dbg.location(1019,40);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args6323);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(211);}

            	    dbg.location(1019,44);
            	    pushFollow(FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6326);
            	    cp_mixin_call_arg();

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
        dbg.location(1020, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_args"


    // $ANTLR start "cp_mixin_call_arg"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:1: cp_mixin_call_arg : term ;
    public final void cp_mixin_call_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1022, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1023:5: ( term )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:5: term
            {
            dbg.location(1024,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_arg6358);
            term();

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
        dbg.location(1027, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_arg");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_arg"


    // $ANTLR start "cp_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1030:1: cp_args_list : ( ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void cp_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1030, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1031:5: ( ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt217=2;
            try { dbg.enterDecision(217, decisionCanBacktrack[217]);

            int LA217_0 = input.LA(1);

            if ( (LA217_0==MEDIA_SYM||LA217_0==AT_IDENT||LA217_0==SASS_VAR) ) {
                alt217=1;
            }
            else if ( ((LA217_0>=LESS_DOTS && LA217_0<=LESS_REST)) ) {
                alt217=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 217, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(217);}

            switch (alt217) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:5: ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(1034,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:5: ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:7: cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(1034,7);
                    pushFollow(FOLLOW_cp_arg_in_cp_args_list6395);
                    cp_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1034,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:14: ( ( COMMA | SEMI ) ( ws )? cp_arg )*
                    try { dbg.enterSubRule(214);

                    loop214:
                    do {
                        int alt214=2;
                        try { dbg.enterDecision(214, decisionCanBacktrack[214]);

                        try {
                            isCyclicDecision = true;
                            alt214 = dfa214.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(214);}

                        switch (alt214) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:16: ( COMMA | SEMI ) ( ws )? cp_arg
                    	    {
                    	    dbg.location(1034,16);
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

                    	    dbg.location(1034,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:33: ws
                    	            {
                    	            dbg.location(1034,33);
                    	            pushFollow(FOLLOW_ws_in_cp_args_list6409);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(213);}

                    	    dbg.location(1034,37);
                    	    pushFollow(FOLLOW_cp_arg_in_cp_args_list6412);
                    	    cp_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop214;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(214);}

                    dbg.location(1034,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:46: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt216=2;
                    try { dbg.enterSubRule(216);
                    try { dbg.enterDecision(216, decisionCanBacktrack[216]);

                    int LA216_0 = input.LA(1);

                    if ( (LA216_0==SEMI||LA216_0==COMMA) ) {
                        alt216=1;
                    }
                    } finally {dbg.exitDecision(216);}

                    switch (alt216) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:48: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(1034,48);
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

                            dbg.location(1034,65);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:65: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:65: ws
                                    {
                                    dbg.location(1034,65);
                                    pushFollow(FOLLOW_ws_in_cp_args_list6428);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(215);}

                            dbg.location(1034,69);
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
                    } finally {dbg.exitSubRule(216);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(1036,5);
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
        dbg.location(1037, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_args_list");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_args_list"


    // $ANTLR start "cp_arg"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1040:1: cp_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void cp_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1040, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(1042,5);
            pushFollow(FOLLOW_cp_variable_in_cp_arg6485);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1042,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:17: ( COLON ( ws )? cp_expression )?
            int alt219=2;
            try { dbg.enterSubRule(219);
            try { dbg.enterDecision(219, decisionCanBacktrack[219]);

            int LA219_0 = input.LA(1);

            if ( (LA219_0==COLON) ) {
                alt219=1;
            }
            } finally {dbg.exitDecision(219);}

            switch (alt219) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(1042,19);
                    match(input,COLON,FOLLOW_COLON_in_cp_arg6489); if (state.failed) return ;
                    dbg.location(1042,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:25: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:25: ws
                            {
                            dbg.location(1042,25);
                            pushFollow(FOLLOW_ws_in_cp_arg6491);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(218);}

                    dbg.location(1042,29);
                    pushFollow(FOLLOW_cp_expression_in_cp_arg6494);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(219);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1043, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_arg");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_arg"


    // $ANTLR start "less_mixin_guarded"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1047:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1047, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1048:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(1049,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded6520); if (state.failed) return ;
            dbg.location(1049,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:15: ws
                    {
                    dbg.location(1049,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded6522);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(220);}

            dbg.location(1049,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6525);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1049,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(222);

            loop222:
            do {
                int alt222=2;
                try { dbg.enterDecision(222, decisionCanBacktrack[222]);

                int LA222_0 = input.LA(1);

                if ( (LA222_0==COMMA||LA222_0==AND) ) {
                    alt222=1;
                }


                } finally {dbg.exitDecision(222);}

                switch (alt222) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(1049,36);
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

            	    dbg.location(1049,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:50: ws
            	            {
            	            dbg.location(1049,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded6537);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(221);}

            	    dbg.location(1049,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6540);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop222;
                }
            } while (true);
            } finally {dbg.exitSubRule(222);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1050, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1054, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(1056,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:5: ( NOT ( ws )? )?
            int alt224=2;
            try { dbg.enterSubRule(224);
            try { dbg.enterDecision(224, decisionCanBacktrack[224]);

            int LA224_0 = input.LA(1);

            if ( (LA224_0==NOT) ) {
                alt224=1;
            }
            } finally {dbg.exitDecision(224);}

            switch (alt224) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:6: NOT ( ws )?
                    {
                    dbg.location(1056,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition6570); if (state.failed) return ;
                    dbg.location(1056,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:10: ws
                            {
                            dbg.location(1056,10);
                            pushFollow(FOLLOW_ws_in_less_condition6572);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(223);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(224);}

            dbg.location(1057,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition6581); if (state.failed) return ;
            dbg.location(1057,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:12: ws
                    {
                    dbg.location(1057,12);
                    pushFollow(FOLLOW_ws_in_less_condition6583);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(225);}

            dbg.location(1058,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt230=2;
            try { dbg.enterSubRule(230);
            try { dbg.enterDecision(230, decisionCanBacktrack[230]);

            int LA230_0 = input.LA(1);

            if ( (LA230_0==IDENT) ) {
                alt230=1;
            }
            else if ( (LA230_0==MEDIA_SYM||LA230_0==AT_IDENT||LA230_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1059,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition6609);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1059,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:40: ( ws )?
                    int alt226=2;
                    try { dbg.enterSubRule(226);
                    try { dbg.enterDecision(226, decisionCanBacktrack[226]);

                    int LA226_0 = input.LA(1);

                    if ( (LA226_0==WS||(LA226_0>=NL && LA226_0<=COMMENT)) ) {
                        alt226=1;
                    }
                    } finally {dbg.exitDecision(226);}

                    switch (alt226) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:40: ws
                            {
                            dbg.location(1059,40);
                            pushFollow(FOLLOW_ws_in_less_condition6611);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(226);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1061,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1061,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition6642);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1061,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt229=2;
                    try { dbg.enterSubRule(229);
                    try { dbg.enterDecision(229, decisionCanBacktrack[229]);

                    int LA229_0 = input.LA(1);

                    if ( (LA229_0==WS||LA229_0==GREATER||LA229_0==OPEQ||(LA229_0>=NL && LA229_0<=COMMENT)||(LA229_0>=GREATER_OR_EQ && LA229_0<=LESS_OR_EQ)) ) {
                        alt229=1;
                    }
                    } finally {dbg.exitDecision(229);}

                    switch (alt229) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1061,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:28: ws
                                    {
                                    dbg.location(1061,28);
                                    pushFollow(FOLLOW_ws_in_less_condition6645);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(227);}

                            dbg.location(1061,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition6648);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1061,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:56: ( ws )?
                            int alt228=2;
                            try { dbg.enterSubRule(228);
                            try { dbg.enterDecision(228, decisionCanBacktrack[228]);

                            int LA228_0 = input.LA(1);

                            if ( (LA228_0==WS||(LA228_0>=NL && LA228_0<=COMMENT)) ) {
                                alt228=1;
                            }
                            } finally {dbg.exitDecision(228);}

                            switch (alt228) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:56: ws
                                    {
                                    dbg.location(1061,56);
                                    pushFollow(FOLLOW_ws_in_less_condition6650);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(228);}

                            dbg.location(1061,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition6653);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(229);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(230);}

            dbg.location(1063,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition6682); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1064, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1067, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1069,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition6708);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1069,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:18: ws
                    {
                    dbg.location(1069,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6710);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(231);}

            dbg.location(1069,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition6713); if (state.failed) return ;
            dbg.location(1069,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:29: ( ws )?
            int alt232=2;
            try { dbg.enterSubRule(232);
            try { dbg.enterDecision(232, decisionCanBacktrack[232]);

            int LA232_0 = input.LA(1);

            if ( (LA232_0==WS||(LA232_0>=NL && LA232_0<=COMMENT)) ) {
                alt232=1;
            }
            } finally {dbg.exitDecision(232);}

            switch (alt232) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:29: ws
                    {
                    dbg.location(1069,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6715);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(232);}

            dbg.location(1069,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition6718);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1069,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:45: ws
                    {
                    dbg.location(1069,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6720);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(233);}

            dbg.location(1069,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition6723); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1070, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1073, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1074:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1075:5: IDENT
            {
            dbg.location(1075,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name6745); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1076, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1078:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1078, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1079,5);
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
        dbg.location(1081, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1099:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1099, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*
            {
            dbg.location(1101,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1101:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1102,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6844);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1104:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND )
                    {
                    dbg.location(1104,13);
                    if ( input.LA(1)==IDENT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT)||input.LA(1)==LESS_AND ) {
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

            dbg.location(1106,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1106:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*
            try { dbg.enterSubRule(237);

            loop237:
            do {
                int alt237=2;
                try { dbg.enterDecision(237, decisionCanBacktrack[237]);

                try {
                    isCyclicDecision = true;
                    alt237 = dfa237.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(237);}

                switch (alt237) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
            	    {
            	    dbg.location(1107,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:13: ( ws )?
            	    int alt235=2;
            	    try { dbg.enterSubRule(235);
            	    try { dbg.enterDecision(235, decisionCanBacktrack[235]);

            	    int LA235_0 = input.LA(1);

            	    if ( (LA235_0==WS||(LA235_0>=NL && LA235_0<=COMMENT)) ) {
            	        alt235=1;
            	    }
            	    } finally {dbg.exitDecision(235);}

            	    switch (alt235) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1107:13: ws
            	            {
            	            dbg.location(1107,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression6933);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(235);}

            	    dbg.location(1108,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
            	    int alt236=2;
            	    try { dbg.enterSubRule(236);
            	    try { dbg.enterDecision(236, decisionCanBacktrack[236]);

            	    try {
            	        isCyclicDecision = true;
            	        alt236 = dfa236.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(236);}

            	    switch (alt236) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1109,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6972);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1111:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND )
            	            {
            	            dbg.location(1111,17);
            	            if ( input.LA(1)==IDENT||input.LA(1)==COLON||(input.LA(1)>=MINUS && input.LA(1)<=DOT)||input.LA(1)==LESS_AND ) {
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
            	    } finally {dbg.exitSubRule(236);}


            	    }
            	    break;

            	default :
            	    break loop237;
                }
            } while (true);
            } finally {dbg.exitSubRule(237);}


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
            dbg.exitRule(getGrammarFileName(), "scss_selector_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_selector_interpolation_expression"


    // $ANTLR start "scss_declaration_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1117:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1117, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1119,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt238=2;
            try { dbg.enterSubRule(238);
            try { dbg.enterDecision(238, decisionCanBacktrack[238]);

            int LA238_0 = input.LA(1);

            if ( (LA238_0==HASH_SYMBOL) ) {
                int LA238_1 = input.LA(2);

                if ( (LA238_1==LBRACE) && (synpred20_Css3())) {
                    alt238=1;
                }
                else if ( (LA238_1==IDENT||LA238_1==COLON||LA238_1==WS||(LA238_1>=MINUS && LA238_1<=DOT)||(LA238_1>=NL && LA238_1<=COMMENT)) ) {
                    alt238=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 238, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA238_0==IDENT||LA238_0==MINUS||(LA238_0>=HASH && LA238_0<=DOT)) ) {
                alt238=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 238, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(238);}

            switch (alt238) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1120,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7110);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1122:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1122,13);
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
            } finally {dbg.exitSubRule(238);}

            dbg.location(1124,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1124:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(241);

            loop241:
            do {
                int alt241=2;
                try { dbg.enterDecision(241, decisionCanBacktrack[241]);

                try {
                    isCyclicDecision = true;
                    alt241 = dfa241.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(241);}

                switch (alt241) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1125:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1125,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1125:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1125:13: ws
            	            {
            	            dbg.location(1125,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression7191);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(239);}

            	    dbg.location(1126,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1126:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt240=2;
            	    try { dbg.enterSubRule(240);
            	    try { dbg.enterDecision(240, decisionCanBacktrack[240]);

            	    int LA240_0 = input.LA(1);

            	    if ( (LA240_0==HASH_SYMBOL) ) {
            	        int LA240_1 = input.LA(2);

            	        if ( (LA240_1==LBRACE) && (synpred21_Css3())) {
            	            alt240=1;
            	        }
            	        else if ( (LA240_1==IDENT||LA240_1==COLON||LA240_1==WS||(LA240_1>=MINUS && LA240_1<=DOT)||(LA240_1>=NL && LA240_1<=COMMENT)) ) {
            	            alt240=2;
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return ;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 240, 1, input);

            	            dbg.recognitionException(nvae);
            	            throw nvae;
            	        }
            	    }
            	    else if ( (LA240_0==IDENT||LA240_0==MINUS||(LA240_0>=HASH && LA240_0<=DOT)) ) {
            	        alt240=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 240, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(240);}

            	    switch (alt240) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1127:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1127,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7230);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1129:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1129,17);
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
            	    } finally {dbg.exitSubRule(240);}


            	    }
            	    break;

            	default :
            	    break loop241;
                }
            } while (true);
            } finally {dbg.exitSubRule(241);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1133, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_declaration_interpolation_expression"


    // $ANTLR start "scss_declaration_property_value_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1135:1: scss_declaration_property_value_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )* ;
    public final void scss_declaration_property_value_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_property_value_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1135, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )*
            {
            dbg.location(1137,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            int alt242=2;
            try { dbg.enterSubRule(242);
            try { dbg.enterDecision(242, decisionCanBacktrack[242]);

            try {
                isCyclicDecision = true;
                alt242 = dfa242.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(242);}

            switch (alt242) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1138:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1138,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7356);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1140:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS )
                    {
                    dbg.location(1140,13);
                    if ( input.LA(1)==IDENT||input.LA(1)==SOLIDUS||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            } finally {dbg.exitSubRule(242);}

            dbg.location(1142,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1142:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )*
            try { dbg.enterSubRule(245);

            loop245:
            do {
                int alt245=2;
                try { dbg.enterDecision(245, decisionCanBacktrack[245]);

                int LA245_0 = input.LA(1);

                if ( (LA245_0==IDENT||LA245_0==WS||LA245_0==SOLIDUS||(LA245_0>=MINUS && LA245_0<=DOT)||(LA245_0>=NL && LA245_0<=COMMENT)) ) {
                    alt245=1;
                }


                } finally {dbg.exitDecision(245);}

                switch (alt245) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1143:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            	    {
            	    dbg.location(1143,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1143:13: ( ws )?
            	    int alt243=2;
            	    try { dbg.enterSubRule(243);
            	    try { dbg.enterDecision(243, decisionCanBacktrack[243]);

            	    int LA243_0 = input.LA(1);

            	    if ( (LA243_0==WS||(LA243_0>=NL && LA243_0<=COMMENT)) ) {
            	        alt243=1;
            	    }
            	    } finally {dbg.exitDecision(243);}

            	    switch (alt243) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1143:13: ws
            	            {
            	            dbg.location(1143,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_property_value_interpolation_expression7441);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(243);}

            	    dbg.location(1144,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1144:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            	    int alt244=2;
            	    try { dbg.enterSubRule(244);
            	    try { dbg.enterDecision(244, decisionCanBacktrack[244]);

            	    try {
            	        isCyclicDecision = true;
            	        alt244 = dfa244.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(244);}

            	    switch (alt244) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1145:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1145,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7480);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1147:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS )
            	            {
            	            dbg.location(1147,17);
            	            if ( input.LA(1)==IDENT||input.LA(1)==SOLIDUS||(input.LA(1)>=MINUS && input.LA(1)<=DOT) ) {
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
            	    } finally {dbg.exitSubRule(244);}


            	    }
            	    break;

            	default :
            	    break loop245;
                }
            } while (true);
            } finally {dbg.exitSubRule(245);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1151, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_declaration_property_value_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_declaration_property_value_interpolation_expression"


    // $ANTLR start "scss_mq_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1153:1: scss_mq_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* ;
    public final void scss_mq_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_mq_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1153, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1154:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1155:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            {
            dbg.location(1155,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1155:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            int alt246=2;
            try { dbg.enterSubRule(246);
            try { dbg.enterDecision(246, decisionCanBacktrack[246]);

            try {
                isCyclicDecision = true;
                alt246 = dfa246.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(246);}

            switch (alt246) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1156,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7614);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1158:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
                    {
                    dbg.location(1158,13);
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
            } finally {dbg.exitSubRule(246);}

            dbg.location(1160,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1160:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            try { dbg.enterSubRule(249);

            loop249:
            do {
                int alt249=2;
                try { dbg.enterDecision(249, decisionCanBacktrack[249]);

                try {
                    isCyclicDecision = true;
                    alt249 = dfa249.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(249);}

                switch (alt249) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    {
            	    dbg.location(1161,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:13: ( ws )?
            	    int alt247=2;
            	    try { dbg.enterSubRule(247);
            	    try { dbg.enterDecision(247, decisionCanBacktrack[247]);

            	    int LA247_0 = input.LA(1);

            	    if ( (LA247_0==WS||(LA247_0>=NL && LA247_0<=COMMENT)) ) {
            	        alt247=1;
            	    }
            	    } finally {dbg.exitDecision(247);}

            	    switch (alt247) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1161:13: ws
            	            {
            	            dbg.location(1161,13);
            	            pushFollow(FOLLOW_ws_in_scss_mq_interpolation_expression7707);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(247);}

            	    dbg.location(1162,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1162:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    int alt248=2;
            	    try { dbg.enterSubRule(248);
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1163,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7746);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1165:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
            	            {
            	            dbg.location(1165,17);
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
            	    } finally {dbg.exitSubRule(248);}


            	    }
            	    break;

            	default :
            	    break loop249;
                }
            } while (true);
            } finally {dbg.exitSubRule(249);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1169, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1171:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1171, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1172:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1173,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7867); if (state.failed) return ;
            dbg.location(1173,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var7869); if (state.failed) return ;
            dbg.location(1173,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:28: ( ws )?
            int alt250=2;
            try { dbg.enterSubRule(250);
            try { dbg.enterDecision(250, decisionCanBacktrack[250]);

            int LA250_0 = input.LA(1);

            if ( (LA250_0==WS||(LA250_0>=NL && LA250_0<=COMMENT)) ) {
                alt250=1;
            }
            } finally {dbg.exitDecision(250);}

            switch (alt250) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:28: ws
                    {
                    dbg.location(1173,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7871);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(250);}

            dbg.location(1173,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:32: ( cp_variable | less_function_in_condition )
            int alt251=2;
            try { dbg.enterSubRule(251);
            try { dbg.enterDecision(251, decisionCanBacktrack[251]);

            int LA251_0 = input.LA(1);

            if ( (LA251_0==MEDIA_SYM||LA251_0==AT_IDENT||LA251_0==SASS_VAR) ) {
                alt251=1;
            }
            else if ( (LA251_0==IDENT) ) {
                alt251=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 251, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(251);}

            switch (alt251) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:34: cp_variable
                    {
                    dbg.location(1173,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var7876);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:48: less_function_in_condition
                    {
                    dbg.location(1173,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7880);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(251);}

            dbg.location(1173,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:77: ( ws )?
            int alt252=2;
            try { dbg.enterSubRule(252);
            try { dbg.enterDecision(252, decisionCanBacktrack[252]);

            int LA252_0 = input.LA(1);

            if ( (LA252_0==WS||(LA252_0>=NL && LA252_0<=COMMENT)) ) {
                alt252=1;
            }
            } finally {dbg.exitDecision(252);}

            switch (alt252) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:77: ws
                    {
                    dbg.location(1173,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7884);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(252);}

            dbg.location(1173,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var7887); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1174, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1194:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1194, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1195:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1196,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties7931);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1196,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties7933); if (state.failed) return ;
            dbg.location(1196,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:20: ( ws )?
            int alt253=2;
            try { dbg.enterSubRule(253);
            try { dbg.enterDecision(253, decisionCanBacktrack[253]);

            int LA253_0 = input.LA(1);

            if ( (LA253_0==WS||(LA253_0>=NL && LA253_0<=COMMENT)) ) {
                alt253=1;
            }
            } finally {dbg.exitDecision(253);}

            switch (alt253) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:20: ws
                    {
                    dbg.location(1196,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7935);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(253);}

            dbg.location(1196,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:24: ( propertyValue )?
            int alt254=2;
            try { dbg.enterSubRule(254);
            try { dbg.enterDecision(254, decisionCanBacktrack[254]);

            int LA254_0 = input.LA(1);

            if ( ((LA254_0>=IDENT && LA254_0<=URI)||LA254_0==MEDIA_SYM||(LA254_0>=GEN && LA254_0<=LPAREN)||LA254_0==AT_IDENT||LA254_0==PERCENTAGE||(LA254_0>=SOLIDUS && LA254_0<=PLUS)||(LA254_0>=MINUS && LA254_0<=DOT)||(LA254_0>=NUMBER && LA254_0<=DIMENSION)||LA254_0==SASS_VAR) ) {
                alt254=1;
            }
            } finally {dbg.exitDecision(254);}

            switch (alt254) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:24: propertyValue
                    {
                    dbg.location(1196,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties7938);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(254);}

            dbg.location(1196,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties7941); if (state.failed) return ;
            dbg.location(1196,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:46: ( ws )?
            int alt255=2;
            try { dbg.enterSubRule(255);
            try { dbg.enterDecision(255, decisionCanBacktrack[255]);

            int LA255_0 = input.LA(1);

            if ( (LA255_0==WS||(LA255_0>=NL && LA255_0<=COMMENT)) ) {
                alt255=1;
            }
            } finally {dbg.exitDecision(255);}

            switch (alt255) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1196:46: ws
                    {
                    dbg.location(1196,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7943);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(255);}

            dbg.location(1196,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties7946);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1196,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties7948);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1196,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties7950); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1197, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1199:1: sass_extend : SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI ;
    public final void sass_extend() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1199, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1200:5: ( SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:5: SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI
            {
            dbg.location(1201,5);
            match(input,SASS_EXTEND,FOLLOW_SASS_EXTEND_in_sass_extend7971); if (state.failed) return ;
            dbg.location(1201,17);
            pushFollow(FOLLOW_ws_in_sass_extend7973);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1201,20);
            pushFollow(FOLLOW_simpleSelectorSequence_in_sass_extend7975);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1201,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:43: ( SASS_OPTIONAL ( ws )? )?
            int alt257=2;
            try { dbg.enterSubRule(257);
            try { dbg.enterDecision(257, decisionCanBacktrack[257]);

            int LA257_0 = input.LA(1);

            if ( (LA257_0==SASS_OPTIONAL) ) {
                alt257=1;
            }
            } finally {dbg.exitDecision(257);}

            switch (alt257) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:44: SASS_OPTIONAL ( ws )?
                    {
                    dbg.location(1201,44);
                    match(input,SASS_OPTIONAL,FOLLOW_SASS_OPTIONAL_in_sass_extend7978); if (state.failed) return ;
                    dbg.location(1201,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:58: ( ws )?
                    int alt256=2;
                    try { dbg.enterSubRule(256);
                    try { dbg.enterDecision(256, decisionCanBacktrack[256]);

                    int LA256_0 = input.LA(1);

                    if ( (LA256_0==WS||(LA256_0>=NL && LA256_0<=COMMENT)) ) {
                        alt256=1;
                    }
                    } finally {dbg.exitDecision(256);}

                    switch (alt256) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1201:58: ws
                            {
                            dbg.location(1201,58);
                            pushFollow(FOLLOW_ws_in_sass_extend7980);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(256);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(257);}

            dbg.location(1201,64);
            match(input,SEMI,FOLLOW_SEMI_in_sass_extend7985); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1202, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1204:1: sass_extend_only_selector : SASS_EXTEND_ONLY_SELECTOR ;
    public final void sass_extend_only_selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend_only_selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1204, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1205:5: ( SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1206:5: SASS_EXTEND_ONLY_SELECTOR
            {
            dbg.location(1206,5);
            match(input,SASS_EXTEND_ONLY_SELECTOR,FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector8010); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1207, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1209:1: sass_debug : ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI ;
    public final void sass_debug() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_debug");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1209, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1210:5: ( ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1211:5: ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI
            {
            dbg.location(1211,5);
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

            dbg.location(1211,32);
            pushFollow(FOLLOW_ws_in_sass_debug8041);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1211,35);
            pushFollow(FOLLOW_cp_expression_in_sass_debug8043);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1211,49);
            match(input,SEMI,FOLLOW_SEMI_in_sass_debug8045); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1212, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_debug");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_debug"


    // $ANTLR start "sass_control"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1214:1: sass_control : ( sass_if | sass_for | sass_each | sass_while );
    public final void sass_control() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1214, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1215:5: ( sass_if | sass_for | sass_each | sass_while )
            int alt258=4;
            try { dbg.enterDecision(258, decisionCanBacktrack[258]);

            switch ( input.LA(1) ) {
            case SASS_IF:
                {
                alt258=1;
                }
                break;
            case SASS_FOR:
                {
                alt258=2;
                }
                break;
            case SASS_EACH:
                {
                alt258=3;
                }
                break;
            case SASS_WHILE:
                {
                alt258=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 258, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(258);}

            switch (alt258) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:5: sass_if
                    {
                    dbg.location(1216,5);
                    pushFollow(FOLLOW_sass_if_in_sass_control8070);
                    sass_if();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:15: sass_for
                    {
                    dbg.location(1216,15);
                    pushFollow(FOLLOW_sass_for_in_sass_control8074);
                    sass_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:26: sass_each
                    {
                    dbg.location(1216,26);
                    pushFollow(FOLLOW_sass_each_in_sass_control8078);
                    sass_each();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:38: sass_while
                    {
                    dbg.location(1216,38);
                    pushFollow(FOLLOW_sass_while_in_sass_control8082);
                    sass_while();

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
        dbg.location(1217, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_control");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control"


    // $ANTLR start "sass_if"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1219:1: sass_if : SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )? ;
    public final void sass_if() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_if");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1219, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1220:5: ( SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:5: SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )?
            {
            dbg.location(1221,5);
            match(input,SASS_IF,FOLLOW_SASS_IF_in_sass_if8103); if (state.failed) return ;
            dbg.location(1221,13);
            pushFollow(FOLLOW_ws_in_sass_if8105);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1221,16);
            pushFollow(FOLLOW_sass_control_expression_in_sass_if8107);
            sass_control_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1221,40);
            pushFollow(FOLLOW_sass_control_block_in_sass_if8109);
            sass_control_block();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1221,59);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:59: ( ( ws )? sass_else )?
            int alt260=2;
            try { dbg.enterSubRule(260);
            try { dbg.enterDecision(260, decisionCanBacktrack[260]);

            try {
                isCyclicDecision = true;
                alt260 = dfa260.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(260);}

            switch (alt260) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:60: ( ws )? sass_else
                    {
                    dbg.location(1221,60);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:60: ( ws )?
                    int alt259=2;
                    try { dbg.enterSubRule(259);
                    try { dbg.enterDecision(259, decisionCanBacktrack[259]);

                    int LA259_0 = input.LA(1);

                    if ( (LA259_0==WS||(LA259_0>=NL && LA259_0<=COMMENT)) ) {
                        alt259=1;
                    }
                    } finally {dbg.exitDecision(259);}

                    switch (alt259) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:60: ws
                            {
                            dbg.location(1221,60);
                            pushFollow(FOLLOW_ws_in_sass_if8112);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(259);}

                    dbg.location(1221,64);
                    pushFollow(FOLLOW_sass_else_in_sass_if8115);
                    sass_else();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(260);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1222, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_if");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_if"


    // $ANTLR start "sass_else"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1224:1: sass_else : ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? );
    public final void sass_else() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_else");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1224, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1225:5: ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? )
            int alt266=2;
            try { dbg.enterDecision(266, decisionCanBacktrack[266]);

            try {
                isCyclicDecision = true;
                alt266 = dfa266.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(266);}

            switch (alt266) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:5: SASS_ELSE ( ws )? sass_control_block
                    {
                    dbg.location(1226,5);
                    match(input,SASS_ELSE,FOLLOW_SASS_ELSE_in_sass_else8142); if (state.failed) return ;
                    dbg.location(1226,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:15: ( ws )?
                    int alt261=2;
                    try { dbg.enterSubRule(261);
                    try { dbg.enterDecision(261, decisionCanBacktrack[261]);

                    int LA261_0 = input.LA(1);

                    if ( (LA261_0==WS||(LA261_0>=NL && LA261_0<=COMMENT)) ) {
                        alt261=1;
                    }
                    } finally {dbg.exitDecision(261);}

                    switch (alt261) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:15: ws
                            {
                            dbg.location(1226,15);
                            pushFollow(FOLLOW_ws_in_sass_else8144);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(261);}

                    dbg.location(1226,19);
                    pushFollow(FOLLOW_sass_control_block_in_sass_else8147);
                    sass_control_block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:5: SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )?
                    {
                    dbg.location(1228,5);
                    match(input,SASS_ELSE,FOLLOW_SASS_ELSE_in_sass_else8160); if (state.failed) return ;
                    dbg.location(1228,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:15: ( ws )?
                    int alt262=2;
                    try { dbg.enterSubRule(262);
                    try { dbg.enterDecision(262, decisionCanBacktrack[262]);

                    int LA262_0 = input.LA(1);

                    if ( (LA262_0==WS||(LA262_0>=NL && LA262_0<=COMMENT)) ) {
                        alt262=1;
                    }
                    } finally {dbg.exitDecision(262);}

                    switch (alt262) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:15: ws
                            {
                            dbg.location(1228,15);
                            pushFollow(FOLLOW_ws_in_sass_else8162);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(262);}

                    dbg.location(1228,19);
                    if ( !(evalPredicate("if".equalsIgnoreCase(input.LT(1).getText()),"\"if\".equalsIgnoreCase(input.LT(1).getText())")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "sass_else", "\"if\".equalsIgnoreCase(input.LT(1).getText())");
                    }
                    dbg.location(1228,67);
                    match(input,IDENT,FOLLOW_IDENT_in_sass_else8167); if (state.failed) return ;
                    dbg.location(1228,82);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:82: ( ws )?
                    int alt263=2;
                    try { dbg.enterSubRule(263);
                    try { dbg.enterDecision(263, decisionCanBacktrack[263]);

                    int LA263_0 = input.LA(1);

                    if ( (LA263_0==WS||(LA263_0>=NL && LA263_0<=COMMENT)) ) {
                        alt263=1;
                    }
                    } finally {dbg.exitDecision(263);}

                    switch (alt263) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:82: ws
                            {
                            dbg.location(1228,82);
                            pushFollow(FOLLOW_ws_in_sass_else8171);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(263);}

                    dbg.location(1228,86);
                    pushFollow(FOLLOW_sass_control_expression_in_sass_else8174);
                    sass_control_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1228,110);
                    pushFollow(FOLLOW_sass_control_block_in_sass_else8176);
                    sass_control_block();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1228,129);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:129: ( ( ws )? sass_else )?
                    int alt265=2;
                    try { dbg.enterSubRule(265);
                    try { dbg.enterDecision(265, decisionCanBacktrack[265]);

                    try {
                        isCyclicDecision = true;
                        alt265 = dfa265.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(265);}

                    switch (alt265) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:130: ( ws )? sass_else
                            {
                            dbg.location(1228,130);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:130: ( ws )?
                            int alt264=2;
                            try { dbg.enterSubRule(264);
                            try { dbg.enterDecision(264, decisionCanBacktrack[264]);

                            int LA264_0 = input.LA(1);

                            if ( (LA264_0==WS||(LA264_0>=NL && LA264_0<=COMMENT)) ) {
                                alt264=1;
                            }
                            } finally {dbg.exitDecision(264);}

                            switch (alt264) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1228:130: ws
                                    {
                                    dbg.location(1228,130);
                                    pushFollow(FOLLOW_ws_in_sass_else8179);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(264);}

                            dbg.location(1228,134);
                            pushFollow(FOLLOW_sass_else_in_sass_else8182);
                            sass_else();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(265);}


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
        dbg.location(1229, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_else");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_else"


    // $ANTLR start "sass_control_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1231:1: sass_control_expression : cp_expression ( ( CP_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )? ;
    public final void sass_control_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1231, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:5: ( cp_expression ( ( CP_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1233:5: cp_expression ( ( CP_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )?
            {
            dbg.location(1233,5);
            pushFollow(FOLLOW_cp_expression_in_sass_control_expression8205);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1233,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1233:19: ( ( CP_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )?
            int alt268=2;
            try { dbg.enterSubRule(268);
            try { dbg.enterDecision(268, decisionCanBacktrack[268]);

            int LA268_0 = input.LA(1);

            if ( (LA268_0==GREATER||(LA268_0>=GREATER_OR_EQ && LA268_0<=LESS_OR_EQ)||LA268_0==CP_EQ) ) {
                alt268=1;
            }
            } finally {dbg.exitDecision(268);}

            switch (alt268) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1233:20: ( CP_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression
                    {
                    dbg.location(1233,20);
                    if ( input.LA(1)==GREATER||(input.LA(1)>=GREATER_OR_EQ && input.LA(1)<=LESS_OR_EQ)||input.LA(1)==CP_EQ ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(1233,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1233:75: ( ws )?
                    int alt267=2;
                    try { dbg.enterSubRule(267);
                    try { dbg.enterDecision(267, decisionCanBacktrack[267]);

                    int LA267_0 = input.LA(1);

                    if ( (LA267_0==WS||(LA267_0>=NL && LA267_0<=COMMENT)) ) {
                        alt267=1;
                    }
                    } finally {dbg.exitDecision(267);}

                    switch (alt267) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1233:75: ws
                            {
                            dbg.location(1233,75);
                            pushFollow(FOLLOW_ws_in_sass_control_expression8229);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(267);}

                    dbg.location(1233,79);
                    pushFollow(FOLLOW_cp_expression_in_sass_control_expression8232);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(268);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1234, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_control_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control_expression"


    // $ANTLR start "sass_for"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1236:1: sass_for : SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block ;
    public final void sass_for() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_for");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1236, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1237:5: ( SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1238:5: SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block
            {
            dbg.location(1238,5);
            match(input,SASS_FOR,FOLLOW_SASS_FOR_in_sass_for8255); if (state.failed) return ;
            dbg.location(1238,14);
            pushFollow(FOLLOW_ws_in_sass_for8257);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,17);
            pushFollow(FOLLOW_cp_variable_in_sass_for8259);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,29);
            pushFollow(FOLLOW_ws_in_sass_for8261);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,32);
            match(input,IDENT,FOLLOW_IDENT_in_sass_for8263); if (state.failed) return ;
            dbg.location(1238,47);
            pushFollow(FOLLOW_ws_in_sass_for8267);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,50);
            pushFollow(FOLLOW_cp_term_in_sass_for8269);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,58);
            match(input,IDENT,FOLLOW_IDENT_in_sass_for8271); if (state.failed) return ;
            dbg.location(1238,71);
            pushFollow(FOLLOW_ws_in_sass_for8275);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,74);
            pushFollow(FOLLOW_cp_term_in_sass_for8277);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1238,82);
            pushFollow(FOLLOW_sass_control_block_in_sass_for8279);
            sass_control_block();

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
        dbg.location(1239, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_for");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_for"


    // $ANTLR start "sass_each"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1241:1: sass_each : SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block ;
    public final void sass_each() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_each");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1241, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1242:5: ( SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1243:5: SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block
            {
            dbg.location(1243,5);
            match(input,SASS_EACH,FOLLOW_SASS_EACH_in_sass_each8300); if (state.failed) return ;
            dbg.location(1243,15);
            pushFollow(FOLLOW_ws_in_sass_each8302);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1243,18);
            pushFollow(FOLLOW_cp_variable_in_sass_each8304);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1243,30);
            pushFollow(FOLLOW_ws_in_sass_each8306);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1243,33);
            match(input,IDENT,FOLLOW_IDENT_in_sass_each8308); if (state.failed) return ;
            dbg.location(1243,46);
            pushFollow(FOLLOW_ws_in_sass_each8312);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1243,49);
            pushFollow(FOLLOW_sass_each_list_in_sass_each8314);
            sass_each_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1243,64);
            pushFollow(FOLLOW_sass_control_block_in_sass_each8316);
            sass_control_block();

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
        dbg.location(1244, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_each");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_each"


    // $ANTLR start "sass_each_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1246:1: sass_each_list : cp_term ( COMMA ( ws )? cp_term )* ;
    public final void sass_each_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_each_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1246, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1247:5: ( cp_term ( COMMA ( ws )? cp_term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:5: cp_term ( COMMA ( ws )? cp_term )*
            {
            dbg.location(1248,5);
            pushFollow(FOLLOW_cp_term_in_sass_each_list8341);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1248,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:13: ( COMMA ( ws )? cp_term )*
            try { dbg.enterSubRule(270);

            loop270:
            do {
                int alt270=2;
                try { dbg.enterDecision(270, decisionCanBacktrack[270]);

                int LA270_0 = input.LA(1);

                if ( (LA270_0==COMMA) ) {
                    alt270=1;
                }


                } finally {dbg.exitDecision(270);}

                switch (alt270) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:14: COMMA ( ws )? cp_term
            	    {
            	    dbg.location(1248,14);
            	    match(input,COMMA,FOLLOW_COMMA_in_sass_each_list8344); if (state.failed) return ;
            	    dbg.location(1248,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:20: ( ws )?
            	    int alt269=2;
            	    try { dbg.enterSubRule(269);
            	    try { dbg.enterDecision(269, decisionCanBacktrack[269]);

            	    int LA269_0 = input.LA(1);

            	    if ( (LA269_0==WS||(LA269_0>=NL && LA269_0<=COMMENT)) ) {
            	        alt269=1;
            	    }
            	    } finally {dbg.exitDecision(269);}

            	    switch (alt269) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:20: ws
            	            {
            	            dbg.location(1248,20);
            	            pushFollow(FOLLOW_ws_in_sass_each_list8346);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(269);}

            	    dbg.location(1248,24);
            	    pushFollow(FOLLOW_cp_term_in_sass_each_list8349);
            	    cp_term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop270;
                }
            } while (true);
            } finally {dbg.exitSubRule(270);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1249, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_each_list");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_each_list"


    // $ANTLR start "sass_while"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1251:1: sass_while : SASS_WHILE ws sass_control_expression sass_control_block ;
    public final void sass_while() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_while");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1251, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1252:5: ( SASS_WHILE ws sass_control_expression sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1253:5: SASS_WHILE ws sass_control_expression sass_control_block
            {
            dbg.location(1253,5);
            match(input,SASS_WHILE,FOLLOW_SASS_WHILE_in_sass_while8376); if (state.failed) return ;
            dbg.location(1253,16);
            pushFollow(FOLLOW_ws_in_sass_while8378);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1253,19);
            pushFollow(FOLLOW_sass_control_expression_in_sass_while8380);
            sass_control_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1253,43);
            pushFollow(FOLLOW_sass_control_block_in_sass_while8382);
            sass_control_block();

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
        dbg.location(1254, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_while");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_while"


    // $ANTLR start "sass_control_block"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1256:1: sass_control_block : LBRACE ( ws )? declarations RBRACE ;
    public final void sass_control_block() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control_block");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1256, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1257:5: ( LBRACE ( ws )? declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1258:5: LBRACE ( ws )? declarations RBRACE
            {
            dbg.location(1258,5);
            match(input,LBRACE,FOLLOW_LBRACE_in_sass_control_block8403); if (state.failed) return ;
            dbg.location(1258,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1258:12: ( ws )?
            int alt271=2;
            try { dbg.enterSubRule(271);
            try { dbg.enterDecision(271, decisionCanBacktrack[271]);

            int LA271_0 = input.LA(1);

            if ( (LA271_0==WS||(LA271_0>=NL && LA271_0<=COMMENT)) ) {
                alt271=1;
            }
            } finally {dbg.exitDecision(271);}

            switch (alt271) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1258:12: ws
                    {
                    dbg.location(1258,12);
                    pushFollow(FOLLOW_ws_in_sass_control_block8405);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(271);}

            dbg.location(1258,16);
            pushFollow(FOLLOW_declarations_in_sass_control_block8408);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1258,29);
            match(input,RBRACE,FOLLOW_RBRACE_in_sass_control_block8410); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1259, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_control_block");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control_block"


    // $ANTLR start "sass_function_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1261:1: sass_function_declaration : SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? sass_function_return ( ws )? RBRACE ;
    public final void sass_function_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1261, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1262:5: ( SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? sass_function_return ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:5: SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? sass_function_return ( ws )? RBRACE
            {
            dbg.location(1265,5);
            match(input,SASS_FUNCTION,FOLLOW_SASS_FUNCTION_in_sass_function_declaration8446); if (state.failed) return ;
            dbg.location(1265,19);
            pushFollow(FOLLOW_ws_in_sass_function_declaration8448);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1265,22);
            pushFollow(FOLLOW_sass_function_name_in_sass_function_declaration8450);
            sass_function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1265,41);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:41: ( ws )?
            int alt272=2;
            try { dbg.enterSubRule(272);
            try { dbg.enterDecision(272, decisionCanBacktrack[272]);

            int LA272_0 = input.LA(1);

            if ( (LA272_0==WS||(LA272_0>=NL && LA272_0<=COMMENT)) ) {
                alt272=1;
            }
            } finally {dbg.exitDecision(272);}

            switch (alt272) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:41: ws
                    {
                    dbg.location(1265,41);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8452);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(272);}

            dbg.location(1265,45);
            match(input,LPAREN,FOLLOW_LPAREN_in_sass_function_declaration8455); if (state.failed) return ;
            dbg.location(1265,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:52: ( cp_args_list )?
            int alt273=2;
            try { dbg.enterSubRule(273);
            try { dbg.enterDecision(273, decisionCanBacktrack[273]);

            int LA273_0 = input.LA(1);

            if ( (LA273_0==MEDIA_SYM||LA273_0==AT_IDENT||LA273_0==SASS_VAR||(LA273_0>=LESS_DOTS && LA273_0<=LESS_REST)) ) {
                alt273=1;
            }
            } finally {dbg.exitDecision(273);}

            switch (alt273) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:52: cp_args_list
                    {
                    dbg.location(1265,52);
                    pushFollow(FOLLOW_cp_args_list_in_sass_function_declaration8457);
                    cp_args_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(273);}

            dbg.location(1265,66);
            match(input,RPAREN,FOLLOW_RPAREN_in_sass_function_declaration8460); if (state.failed) return ;
            dbg.location(1265,73);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:73: ( ws )?
            int alt274=2;
            try { dbg.enterSubRule(274);
            try { dbg.enterDecision(274, decisionCanBacktrack[274]);

            int LA274_0 = input.LA(1);

            if ( (LA274_0==WS||(LA274_0>=NL && LA274_0<=COMMENT)) ) {
                alt274=1;
            }
            } finally {dbg.exitDecision(274);}

            switch (alt274) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:73: ws
                    {
                    dbg.location(1265,73);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8462);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(274);}

            dbg.location(1265,77);
            match(input,LBRACE,FOLLOW_LBRACE_in_sass_function_declaration8465); if (state.failed) return ;
            dbg.location(1265,84);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:84: ( ws )?
            int alt275=2;
            try { dbg.enterSubRule(275);
            try { dbg.enterDecision(275, decisionCanBacktrack[275]);

            int LA275_0 = input.LA(1);

            if ( (LA275_0==WS||(LA275_0>=NL && LA275_0<=COMMENT)) ) {
                alt275=1;
            }
            } finally {dbg.exitDecision(275);}

            switch (alt275) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:84: ws
                    {
                    dbg.location(1265,84);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8467);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(275);}

            dbg.location(1265,88);
            pushFollow(FOLLOW_sass_function_return_in_sass_function_declaration8470);
            sass_function_return();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1265,109);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:109: ( ws )?
            int alt276=2;
            try { dbg.enterSubRule(276);
            try { dbg.enterDecision(276, decisionCanBacktrack[276]);

            int LA276_0 = input.LA(1);

            if ( (LA276_0==WS||(LA276_0>=NL && LA276_0<=COMMENT)) ) {
                alt276=1;
            }
            } finally {dbg.exitDecision(276);}

            switch (alt276) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:109: ws
                    {
                    dbg.location(1265,109);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8472);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(276);}

            dbg.location(1265,113);
            match(input,RBRACE,FOLLOW_RBRACE_in_sass_function_declaration8475); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1266, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_function_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_function_declaration"


    // $ANTLR start "sass_function_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1268:1: sass_function_name : IDENT ;
    public final void sass_function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1268, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1269:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1270:5: IDENT
            {
            dbg.location(1270,5);
            match(input,IDENT,FOLLOW_IDENT_in_sass_function_name8500); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1271, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_function_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_function_name"


    // $ANTLR start "sass_function_return"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1273:1: sass_function_return : SASS_RETURN ws cp_expression SEMI ;
    public final void sass_function_return() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_return");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1273, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1274:5: ( SASS_RETURN ws cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1275:5: SASS_RETURN ws cp_expression SEMI
            {
            dbg.location(1275,5);
            match(input,SASS_RETURN,FOLLOW_SASS_RETURN_in_sass_function_return8521); if (state.failed) return ;
            dbg.location(1275,17);
            pushFollow(FOLLOW_ws_in_sass_function_return8523);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1275,20);
            pushFollow(FOLLOW_cp_expression_in_sass_function_return8525);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1275,34);
            match(input,SEMI,FOLLOW_SEMI_in_sass_function_return8527); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1276, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_function_return");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_function_return"


    // $ANTLR start "sass_content"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1278:1: sass_content : SASS_CONTENT ( ws )? SEMI ;
    public final void sass_content() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_content");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1278, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1279:5: ( SASS_CONTENT ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1280:5: SASS_CONTENT ( ws )? SEMI
            {
            dbg.location(1280,5);
            match(input,SASS_CONTENT,FOLLOW_SASS_CONTENT_in_sass_content8552); if (state.failed) return ;
            dbg.location(1280,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1280:18: ( ws )?
            int alt277=2;
            try { dbg.enterSubRule(277);
            try { dbg.enterDecision(277, decisionCanBacktrack[277]);

            int LA277_0 = input.LA(1);

            if ( (LA277_0==WS||(LA277_0>=NL && LA277_0<=COMMENT)) ) {
                alt277=1;
            }
            } finally {dbg.exitDecision(277);}

            switch (alt277) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1280:18: ws
                    {
                    dbg.location(1280,18);
                    pushFollow(FOLLOW_ws_in_sass_content8554);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(277);}

            dbg.location(1280,22);
            match(input,SEMI,FOLLOW_SEMI_in_sass_content8557); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1281, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_content");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_content"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(368,15);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(278);

        loop278:
        do {
            int alt278=2;
            try { dbg.enterDecision(278, decisionCanBacktrack[278]);

            int LA278_0 = input.LA(1);

            if ( ((LA278_0>=NAMESPACE_SYM && LA278_0<=MEDIA_SYM)||(LA278_0>=RBRACE && LA278_0<=MINUS)||(LA278_0>=HASH && LA278_0<=LINE_COMMENT)) ) {
                alt278=1;
            }


            } finally {dbg.exitDecision(278);}

            switch (alt278) {
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
        	    break loop278;
            }
        } while (true);
        } finally {dbg.exitSubRule(278);}

        dbg.location(368,42);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred1_Css3488); if (state.failed) return ;
        dbg.location(368,54);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Css3490); if (state.failed) return ;

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
        pushFollow(FOLLOW_mediaQueryList_in_synpred2_Css3527);
        mediaQueryList();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:17: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt281=2;
        try { dbg.enterDecision(281, decisionCanBacktrack[281]);

        try {
            isCyclicDecision = true;
            alt281 = dfa281.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(281);}

        switch (alt281) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(376,18);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt279=0;
                try { dbg.enterSubRule(279);

                loop279:
                do {
                    int alt279=2;
                    try { dbg.enterDecision(279, decisionCanBacktrack[279]);

                    int LA279_0 = input.LA(1);

                    if ( (LA279_0==NAMESPACE_SYM||(LA279_0>=IDENT && LA279_0<=MEDIA_SYM)||(LA279_0>=AND && LA279_0<=LPAREN)||(LA279_0>=RPAREN && LA279_0<=LINE_COMMENT)) ) {
                        alt279=1;
                    }


                    } finally {dbg.exitDecision(279);}

                    switch (alt279) {
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
                	    if ( cnt279 >= 1 ) break loop279;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(279, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt279++;
                } while (true);
                } finally {dbg.exitSubRule(279);}

                dbg.location(376,47);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3625); if (state.failed) return ;
                dbg.location(376,53);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt280=0;
                try { dbg.enterSubRule(280);

                loop280:
                do {
                    int alt280=2;
                    try { dbg.enterDecision(280, decisionCanBacktrack[280]);

                    int LA280_0 = input.LA(1);

                    if ( (LA280_0==NAMESPACE_SYM||(LA280_0>=IDENT && LA280_0<=MEDIA_SYM)||(LA280_0>=AND && LA280_0<=LINE_COMMENT)) ) {
                        alt280=1;
                    }


                    } finally {dbg.exitDecision(280);}

                    switch (alt280) {
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
                	    if ( cnt280 >= 1 ) break loop280;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(280, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt280++;
                } while (true);
                } finally {dbg.exitSubRule(280);}

                dbg.location(376,76);
                match(input,SEMI,FOLLOW_SEMI_in_synpred3_Css3637); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:83: scss_declaration_interpolation_expression COLON
                {
                dbg.location(376,83);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3641);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(376,125);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3643); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:9: ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:10: (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(567,10);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:10: (~ ( HASH_SYMBOL | COLON ) )*
        try { dbg.enterSubRule(282);

        loop282:
        do {
            int alt282=2;
            try { dbg.enterDecision(282, decisionCanBacktrack[282]);

            int LA282_0 = input.LA(1);

            if ( ((LA282_0>=NAMESPACE_SYM && LA282_0<=LPAREN)||(LA282_0>=RPAREN && LA282_0<=MINUS)||(LA282_0>=HASH && LA282_0<=LINE_COMMENT)) ) {
                alt282=1;
            }


            } finally {dbg.exitDecision(282);}

            switch (alt282) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:10: ~ ( HASH_SYMBOL | COLON )
        	    {
        	    dbg.location(567,10);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop282;
            }
        } while (true);
        } finally {dbg.exitSubRule(282);}

        dbg.location(567,32);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred4_Css32316); if (state.failed) return ;
        dbg.location(567,44);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred4_Css32318); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:17: ( cp_variable_declaration )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:18: cp_variable_declaration
        {
        dbg.location(598,18);
        pushFollow(FOLLOW_cp_variable_declaration_in_synpred5_Css32566);
        cp_variable_declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:3: ( declaration SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:4: declaration SEMI
        {
        dbg.location(604,4);
        pushFollow(FOLLOW_declaration_in_synpred6_Css32647);
        declaration();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(604,16);
        match(input,SEMI,FOLLOW_SEMI_in_synpred6_Css32649); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI
        {
        dbg.location(608,4);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )*
        try { dbg.enterSubRule(283);

        loop283:
        do {
            int alt283=2;
            try { dbg.enterDecision(283, decisionCanBacktrack[283]);

            int LA283_0 = input.LA(1);

            if ( (LA283_0==NAMESPACE_SYM||(LA283_0>=IDENT && LA283_0<=MEDIA_SYM)||(LA283_0>=AND && LA283_0<=LPAREN)||(LA283_0>=RPAREN && LA283_0<=LINE_COMMENT)) ) {
                alt283=1;
            }


            } finally {dbg.exitDecision(283);}

            switch (alt283) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
        	    {
        	    dbg.location(608,4);
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
        	    break loop283;
            }
        } while (true);
        } finally {dbg.exitSubRule(283);}

        dbg.location(608,33);
        match(input,COLON,FOLLOW_COLON_in_synpred7_Css32726); if (state.failed) return ;
        dbg.location(608,39);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:39: (~ ( SEMI | LBRACE | RBRACE ) )*
        try { dbg.enterSubRule(284);

        loop284:
        do {
            int alt284=2;
            try { dbg.enterDecision(284, decisionCanBacktrack[284]);

            int LA284_0 = input.LA(1);

            if ( (LA284_0==NAMESPACE_SYM||(LA284_0>=IDENT && LA284_0<=MEDIA_SYM)||(LA284_0>=AND && LA284_0<=LINE_COMMENT)) ) {
                alt284=1;
            }


            } finally {dbg.exitDecision(284);}

            switch (alt284) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:39: ~ ( SEMI | LBRACE | RBRACE )
        	    {
        	    dbg.location(608,39);
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
        	    break loop284;
            }
        } while (true);
        } finally {dbg.exitSubRule(284);}

        dbg.location(608,62);
        match(input,SEMI,FOLLOW_SEMI_in_synpred7_Css32738); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:3: ( scss_nested_properties )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:4: scss_nested_properties
        {
        dbg.location(610,4);
        pushFollow(FOLLOW_scss_nested_properties_in_synpred8_Css32755);
        scss_nested_properties();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:17: ( rule )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:18: rule
        {
        dbg.location(612,18);
        pushFollow(FOLLOW_rule_in_synpred9_Css32784);
        rule();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:17: ( (~ SEMI )* SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:18: (~ SEMI )* SEMI
        {
        dbg.location(626,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:18: (~ SEMI )*
        try { dbg.enterSubRule(285);

        loop285:
        do {
            int alt285=2;
            try { dbg.enterDecision(285, decisionCanBacktrack[285]);

            int LA285_0 = input.LA(1);

            if ( (LA285_0==NAMESPACE_SYM||(LA285_0>=IDENT && LA285_0<=LINE_COMMENT)) ) {
                alt285=1;
            }


            } finally {dbg.exitDecision(285);}

            switch (alt285) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:18: ~ SEMI
        	    {
        	    dbg.location(626,18);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop285;
            }
        } while (true);
        } finally {dbg.exitSubRule(285);}

        dbg.location(626,25);
        match(input,SEMI,FOLLOW_SEMI_in_synpred10_Css33077); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(634,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(286);

        loop286:
        do {
            int alt286=2;
            try { dbg.enterDecision(286, decisionCanBacktrack[286]);

            int LA286_0 = input.LA(1);

            if ( ((LA286_0>=NAMESPACE_SYM && LA286_0<=MEDIA_SYM)||(LA286_0>=RBRACE && LA286_0<=MINUS)||(LA286_0>=HASH && LA286_0<=LINE_COMMENT)) ) {
                alt286=1;
            }


            } finally {dbg.exitDecision(286);}

            switch (alt286) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(634,11);
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
        	    break loop286;
            }
        } while (true);
        } finally {dbg.exitSubRule(286);}

        dbg.location(634,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred11_Css33164); if (state.failed) return ;
        dbg.location(634,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred11_Css33166); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:19: esPred
        {
        dbg.location(647,19);
        pushFollow(FOLLOW_esPred_in_synpred12_Css33264);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:6: esPred
        {
        dbg.location(649,6);
        pushFollow(FOLLOW_esPred_in_synpred13_Css33285);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(663,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:8: ( IDENT | STAR )?
        int alt287=2;
        try { dbg.enterSubRule(287);
        try { dbg.enterDecision(287, decisionCanBacktrack[287]);

        int LA287_0 = input.LA(1);

        if ( (LA287_0==IDENT||LA287_0==STAR) ) {
            alt287=1;
        }
        } finally {dbg.exitDecision(287);}

        switch (alt287) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(663,8);
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
        } finally {dbg.exitSubRule(287);}

        dbg.location(663,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred14_Css33403); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:9: ( (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:10: (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(772,10);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:10: (~ ( HASH_SYMBOL | SEMI ) )*
        try { dbg.enterSubRule(288);

        loop288:
        do {
            int alt288=2;
            try { dbg.enterDecision(288, decisionCanBacktrack[288]);

            int LA288_0 = input.LA(1);

            if ( (LA288_0==NAMESPACE_SYM||(LA288_0>=IDENT && LA288_0<=MINUS)||(LA288_0>=HASH && LA288_0<=LINE_COMMENT)) ) {
                alt288=1;
            }


            } finally {dbg.exitDecision(288);}

            switch (alt288) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:10: ~ ( HASH_SYMBOL | SEMI )
        	    {
        	    dbg.location(772,10);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop288;
            }
        } while (true);
        } finally {dbg.exitSubRule(288);}

        dbg.location(772,31);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred15_Css34407); if (state.failed) return ;
        dbg.location(772,43);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred15_Css34409); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

    // $ANTLR start synpred16_Css3
    public final void synpred16_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:12: expressionPredicate
        {
        dbg.location(773,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred16_Css34425);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Css3

    // $ANTLR start synpred17_Css3
    public final void synpred17_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:952:17: term
        {
        dbg.location(952,17);
        pushFollow(FOLLOW_term_in_synpred17_Css35736);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Css3

    // $ANTLR start synpred18_Css3
    public final void synpred18_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1102,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred18_Css36839); if (state.failed) return ;
        dbg.location(1102,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred18_Css36841); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Css3

    // $ANTLR start synpred19_Css3
    public final void synpred19_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1109,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred19_Css36967); if (state.failed) return ;
        dbg.location(1109,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred19_Css36969); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Css3

    // $ANTLR start synpred20_Css3
    public final void synpred20_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1120,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred20_Css37105); if (state.failed) return ;
        dbg.location(1120,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred20_Css37107); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Css3

    // $ANTLR start synpred21_Css3
    public final void synpred21_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1127:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1127:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1127,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred21_Css37225); if (state.failed) return ;
        dbg.location(1127,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred21_Css37227); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_Css3

    // $ANTLR start synpred22_Css3
    public final void synpred22_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1138:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1138:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1138,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred22_Css37351); if (state.failed) return ;
        dbg.location(1138,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred22_Css37353); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred22_Css3

    // $ANTLR start synpred23_Css3
    public final void synpred23_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1145:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1145:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1145,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred23_Css37475); if (state.failed) return ;
        dbg.location(1145,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred23_Css37477); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred23_Css3

    // $ANTLR start synpred24_Css3
    public final void synpred24_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1156,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred24_Css37609); if (state.failed) return ;
        dbg.location(1156,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred24_Css37611); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred24_Css3

    // $ANTLR start synpred25_Css3
    public final void synpred25_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1163:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1163,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred25_Css37741); if (state.failed) return ;
        dbg.location(1163,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred25_Css37743); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_Css3

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
    public final boolean synpred25_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred25_Css3_fragment(); // can never throw exception
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
    public final boolean synpred24_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred24_Css3_fragment(); // can never throw exception
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
    public final boolean synpred21_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred21_Css3_fragment(); // can never throw exception
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
    public final boolean synpred23_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred23_Css3_fragment(); // can never throw exception
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
    public final boolean synpred22_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred22_Css3_fragment(); // can never throw exception
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


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA16 dfa16 = new DFA16(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA38 dfa38 = new DFA38(this);
    protected DFA58 dfa58 = new DFA58(this);
    protected DFA79 dfa79 = new DFA79(this);
    protected DFA105 dfa105 = new DFA105(this);
    protected DFA118 dfa118 = new DFA118(this);
    protected DFA123 dfa123 = new DFA123(this);
    protected DFA126 dfa126 = new DFA126(this);
    protected DFA144 dfa144 = new DFA144(this);
    protected DFA153 dfa153 = new DFA153(this);
    protected DFA157 dfa157 = new DFA157(this);
    protected DFA160 dfa160 = new DFA160(this);
    protected DFA166 dfa166 = new DFA166(this);
    protected DFA189 dfa189 = new DFA189(this);
    protected DFA193 dfa193 = new DFA193(this);
    protected DFA209 dfa209 = new DFA209(this);
    protected DFA214 dfa214 = new DFA214(this);
    protected DFA234 dfa234 = new DFA234(this);
    protected DFA237 dfa237 = new DFA237(this);
    protected DFA236 dfa236 = new DFA236(this);
    protected DFA241 dfa241 = new DFA241(this);
    protected DFA242 dfa242 = new DFA242(this);
    protected DFA244 dfa244 = new DFA244(this);
    protected DFA246 dfa246 = new DFA246(this);
    protected DFA249 dfa249 = new DFA249(this);
    protected DFA248 dfa248 = new DFA248(this);
    protected DFA260 dfa260 = new DFA260(this);
    protected DFA266 dfa266 = new DFA266(this);
    protected DFA265 dfa265 = new DFA265(this);
    protected DFA281 dfa281 = new DFA281(this);
    static final String DFA4_eotS =
        "\42\uffff";
    static final String DFA4_eofS =
        "\1\2\41\uffff";
    static final String DFA4_minS =
        "\1\4\1\0\40\uffff";
    static final String DFA4_maxS =
        "\1\150\1\0\40\uffff";
    static final String DFA4_acceptS =
        "\2\uffff\1\2\36\uffff\1\1";
    static final String DFA4_specialS =
        "\1\uffff\1\0\40\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\2\1\uffff\1\2\3\uffff\1\1\1\uffff\1\2\5\uffff\1\2\1\uffff"+
            "\1\2\1\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\3\2\24\uffff"+
            "\11\2\1\uffff\1\2\25\uffff\3\2\10\uffff\3\2\2\uffff\4\2",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "321:9: ( imports )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_1 = input.LA(1);

                         
                        int index4_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(evalPredicate((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")||(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))),""))) ) {s = 33;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")||(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()")))) ) {s = 2;}

                         
                        input.seek(index4_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA16_eotS =
        "\42\uffff";
    static final String DFA16_eofS =
        "\1\1\41\uffff";
    static final String DFA16_minS =
        "\1\4\30\uffff\1\0\10\uffff";
    static final String DFA16_maxS =
        "\1\150\30\uffff\1\0\10\uffff";
    static final String DFA16_acceptS =
        "\1\uffff\1\2\37\uffff\1\1";
    static final String DFA16_specialS =
        "\31\uffff\1\0\10\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\1\1\uffff\1\1\3\uffff\1\31\1\uffff\1\1\5\uffff\1\1\1\uffff"+
            "\1\1\1\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\3\1\24\uffff"+
            "\11\1\1\uffff\1\1\25\uffff\3\1\10\uffff\3\1\2\uffff\4\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "()+ loopback of 354:2: ( importItem ( ws )? )+";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA16_25 = input.LA(1);

                         
                        int index16_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(evalPredicate((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")||(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))),""))) ) {s = 33;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")||(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()")))) ) {s = 1;}

                         
                        input.seek(index16_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 16, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA26_eotS =
        "\16\uffff";
    static final String DFA26_eofS =
        "\16\uffff";
    static final String DFA26_minS =
        "\1\6\1\uffff\1\6\1\0\5\uffff\1\6\1\uffff\1\0\2\uffff";
    static final String DFA26_maxS =
        "\1\70\1\uffff\1\123\1\0\5\uffff\1\123\1\uffff\1\0\2\uffff";
    static final String DFA26_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\1\3\2\1\uffff\1\1\1\uffff\2\1";
    static final String DFA26_specialS =
        "\1\4\1\uffff\1\2\1\3\5\uffff\1\1\1\uffff\1\0\2\uffff}>";
    static final String[] DFA26_transitionS = {
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

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = DFA26_eot;
            this.eof = DFA26_eof;
            this.min = DFA26_min;
            this.max = DFA26_max;
            this.accept = DFA26_accept;
            this.special = DFA26_special;
            this.transition = DFA26_transition;
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
                        int LA26_11 = input.LA(1);

                         
                        int index26_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index26_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_9 = input.LA(1);

                         
                        int index26_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_9==HASH_SYMBOL) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA26_9==IDENT) ) {s = 11;}

                        else if ( (LA26_9==WS||(LA26_9>=NL && LA26_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA26_9==LBRACE) && (synpred1_Css3())) {s = 12;}

                        else if ( (LA26_9==AND||LA26_9==NOT||LA26_9==COLON||LA26_9==MINUS||(LA26_9>=HASH && LA26_9<=DOT)) && (synpred1_Css3())) {s = 13;}

                        else if ( (LA26_9==GEN) && (synpred2_Css3())) {s = 6;}

                         
                        input.seek(index26_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA26_2 = input.LA(1);

                         
                        int index26_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_2==WS||(LA26_2>=NL && LA26_2<=COMMENT)) ) {s = 9;}

                        else if ( (LA26_2==HASH_SYMBOL) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA26_2==IDENT) ) {s = 11;}

                        else if ( (LA26_2==LBRACE) && (synpred1_Css3())) {s = 12;}

                        else if ( (LA26_2==AND||LA26_2==NOT||LA26_2==COLON||LA26_2==MINUS||(LA26_2>=HASH && LA26_2<=DOT)) && (synpred1_Css3())) {s = 13;}

                        else if ( (LA26_2==GEN) && (synpred2_Css3())) {s = 6;}

                         
                        input.seek(index26_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA26_3 = input.LA(1);

                         
                        int index26_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index26_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA26_0 = input.LA(1);

                         
                        int index26_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_0==HASH_SYMBOL) && (synpred1_Css3())) {s = 1;}

                        else if ( (LA26_0==NOT) ) {s = 2;}

                        else if ( (LA26_0==IDENT) ) {s = 3;}

                        else if ( (LA26_0==ONLY) && (synpred2_Css3())) {s = 4;}

                        else if ( (LA26_0==AND||LA26_0==COLON||LA26_0==MINUS||(LA26_0>=HASH && LA26_0<=DOT)) && (synpred1_Css3())) {s = 5;}

                        else if ( (LA26_0==GEN) && (synpred2_Css3())) {s = 6;}

                        else if ( (LA26_0==LPAREN) && (synpred2_Css3())) {s = 7;}

                        else if ( (LA26_0==LBRACE) && (synpred2_Css3())) {s = 8;}

                         
                        input.seek(index26_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 26, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA38_eotS =
        "\37\uffff";
    static final String DFA38_eofS =
        "\37\uffff";
    static final String DFA38_minS =
        "\1\6\1\uffff\6\0\10\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA38_maxS =
        "\1\152\1\uffff\6\0\10\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA38_acceptS =
        "\1\uffff\1\13\6\uffff\1\1\1\2\1\3\1\4\3\uffff\1\5\1\uffff\1\6\7"+
        "\uffff\1\7\1\10\1\11\2\uffff\1\12";
    static final String DFA38_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\10\uffff\1\7\5\uffff\1\10\6"+
        "\uffff\1\11\1\uffff}>";
    static final String[] DFA38_transitionS = {
            "\1\4\5\uffff\1\35\1\uffff\1\1\3\uffff\1\6\1\uffff\1\21\1\uffff"+
            "\1\7\1\uffff\1\33\3\uffff\1\33\1\uffff\1\31\1\uffff\1\32\24"+
            "\uffff\1\26\1\3\1\20\1\5\3\21\1\2\1\21\1\uffff\1\21\25\uffff"+
            "\1\10\1\21\7\uffff\1\11\1\uffff\2\12\1\13\2\uffff\3\13\2\uffff"+
            "\1\17",
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

    static final short[] DFA38_eot = DFA.unpackEncodedString(DFA38_eotS);
    static final short[] DFA38_eof = DFA.unpackEncodedString(DFA38_eofS);
    static final char[] DFA38_min = DFA.unpackEncodedStringToUnsignedChars(DFA38_minS);
    static final char[] DFA38_max = DFA.unpackEncodedStringToUnsignedChars(DFA38_maxS);
    static final short[] DFA38_accept = DFA.unpackEncodedString(DFA38_acceptS);
    static final short[] DFA38_special = DFA.unpackEncodedString(DFA38_specialS);
    static final short[][] DFA38_transition;

    static {
        int numStates = DFA38_transitionS.length;
        DFA38_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA38_transition[i] = DFA.unpackEncodedString(DFA38_transitionS[i]);
        }
    }

    class DFA38 extends DFA {

        public DFA38(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 38;
            this.eot = DFA38_eot;
            this.eof = DFA38_eof;
            this.min = DFA38_min;
            this.max = DFA38_max;
            this.accept = DFA38_accept;
            this.special = DFA38_special;
            this.transition = DFA38_transition;
        }
        public String getDescription() {
            return "()* loopback of 374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA38_0 = input.LA(1);

                         
                        int index38_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_0==RBRACE) ) {s = 1;}

                        else if ( (LA38_0==STAR) ) {s = 2;}

                        else if ( (LA38_0==HASH_SYMBOL) ) {s = 3;}

                        else if ( (LA38_0==IDENT) ) {s = 4;}

                        else if ( (LA38_0==DOT) ) {s = 5;}

                        else if ( (LA38_0==GEN) ) {s = 6;}

                        else if ( (LA38_0==AT_IDENT) ) {s = 7;}

                        else if ( (LA38_0==SASS_VAR) && (synpred3_Css3())) {s = 8;}

                        else if ( (LA38_0==SASS_EXTEND) ) {s = 9;}

                        else if ( ((LA38_0>=SASS_DEBUG && LA38_0<=SASS_WARN)) ) {s = 10;}

                        else if ( (LA38_0==SASS_IF||(LA38_0>=SASS_FOR && LA38_0<=SASS_WHILE)) ) {s = 11;}

                        else if ( (LA38_0==SASS_CONTENT) ) {s = 15;}

                        else if ( (LA38_0==HASH) ) {s = 16;}

                        else if ( (LA38_0==COLON||(LA38_0>=LBRACKET && LA38_0<=SASS_EXTEND_ONLY_SELECTOR)||LA38_0==PIPE||LA38_0==LESS_AND||LA38_0==SASS_MIXIN) ) {s = 17;}

                        else if ( (LA38_0==MINUS) ) {s = 22;}

                        else if ( (LA38_0==PAGE_SYM) ) {s = 25;}

                        else if ( (LA38_0==FONT_FACE_SYM) ) {s = 26;}

                        else if ( (LA38_0==MOZ_DOCUMENT_SYM||LA38_0==WEBKIT_KEYFRAMES_SYM) ) {s = 27;}

                        else if ( (LA38_0==MEDIA_SYM) ) {s = 29;}

                         
                        input.seek(index38_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA38_2 = input.LA(1);

                         
                        int index38_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA38_3 = input.LA(1);

                         
                        int index38_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA38_4 = input.LA(1);

                         
                        int index38_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA38_5 = input.LA(1);

                         
                        int index38_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA38_6 = input.LA(1);

                         
                        int index38_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA38_7 = input.LA(1);

                         
                        int index38_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index38_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA38_16 = input.LA(1);

                         
                        int index38_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_16);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA38_22 = input.LA(1);

                         
                        int index38_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 17;}

                         
                        input.seek(index38_22);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA38_29 = input.LA(1);

                         
                        int index38_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 30;}

                         
                        input.seek(index38_29);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 38, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA58_eotS =
        "\161\uffff";
    static final String DFA58_eofS =
        "\161\uffff";
    static final String DFA58_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\6\uffff\1\5\1\6\1\uffff\2\6\1\5\3\6"+
        "\5\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\1\5\2\6\2\5\2\6\1\5\1\6\1\5\2\6"+
        "\2\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\2\5\4\6\2\5\1\6\1\5\1\6\1\5\2\6"+
        "\1\5\1\6\2\5\1\6\1\5\3\6\1\5\1\6\1\5\4\6\2\5\1\6\1\5\1\6\1\5\6\6"+
        "\1\5\1\6\1\5\7\6";
    static final String DFA58_maxS =
        "\1\150\1\123\1\uffff\1\123\4\uffff\1\123\6\uffff\2\123\1\uffff\1"+
        "\125\2\123\1\131\4\125\2\123\1\132\1\125\1\123\4\125\1\123\1\125"+
        "\1\131\1\132\1\123\4\125\1\123\1\125\1\123\1\125\1\123\1\131\4\123"+
        "\4\125\1\123\4\125\3\123\3\125\1\123\1\125\1\123\1\125\2\123\4\125"+
        "\1\123\1\125\3\123\1\125\1\123\1\125\3\123\3\125\1\123\1\125\1\123"+
        "\1\125\6\123\1\125\1\123\1\125\7\123";
    static final String DFA58_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\1\11\1\12"+
        "\1\13\1\14\2\uffff\1\2\137\uffff";
    static final String DFA58_specialS =
        "\161\uffff}>";
    static final String[] DFA58_transitionS = {
            "\1\2\3\uffff\1\13\1\uffff\1\3\5\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\uffff\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3"+
            "\2\1\1\5\2\1\uffff\1\2\25\uffff\1\11\1\2\1\12\10\uffff\2\14"+
            "\1\15\2\uffff\3\15\1\16",
            "\1\17\6\uffff\1\2\4\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\6\uffff\1\2\22\uffff\2\2",
            "",
            "\1\21\6\uffff\1\21\1\uffff\5\21\1\22\2\uffff\1\20\35\uffff"+
            "\4\21\31\uffff\2\20",
            "",
            "",
            "",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\23\72\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\25\1\2\2\uffff"+
            "\1\24\32\uffff\14\2\1\uffff\1\2\22\uffff\2\24",
            "\1\21\6\uffff\1\21\1\uffff\5\21\1\22\2\uffff\1\20\35\uffff"+
            "\4\21\31\uffff\2\20",
            "",
            "\1\30\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\26\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\27\1\21\1\31\1\21\17\uffff\12\11\2\26\1\uffff\1\11",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\23\72\uffff\2\11",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\25\1\2\2\uffff"+
            "\1\24\32\uffff\14\2\1\uffff\1\2\22\uffff\2\24",
            "\3\12\3\uffff\1\32\5\uffff\1\12\2\uffff\1\34\1\32\1\12\5\uffff"+
            "\1\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\14\12"+
            "\1\uffff\1\33\2\uffff\2\2",
            "\1\30\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\26\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\27\1\21\1\31\1\21\17\uffff\12\11\2\26\1\uffff\1\11",
            "\1\30\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\1\11"+
            "\1\uffff\1\21\1\uffff\1\11\1\35\5\uffff\1\11\27\uffff\2\21\1"+
            "\31\1\21\17\uffff\12\11\2\35\1\uffff\1\11",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\36\1\uffff\1\11\1\37\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\40\1\21\1\42\1\43\3\uffff\1\11\13\uffff\12\11\2\37\2\11",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\44\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\44\2\11",
            "\1\45\5\uffff\1\45\10\uffff\1\2\1\34\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\45\5\uffff\1\45\10\uffff\1\2\1\34\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\12\7\uffff\1\2\11\uffff\1\46\72\uffff\2\46\6\uffff\1\2",
            "\1\30\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\1\11"+
            "\1\uffff\1\21\1\uffff\1\11\1\35\5\uffff\1\11\27\uffff\2\21\1"+
            "\31\1\21\17\uffff\12\11\2\35\1\uffff\1\11",
            "\1\47\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21\2"+
            "\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\37\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11\2\37\2\11",
            "\1\51\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\50\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\53\1\21\1\52\1\21\17\uffff\12\11\2\50\1\uffff\1\11",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\54\1\uffff\1\11\1\55\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\40\1\21\1\42\1\56\3\uffff\1\11\13\uffff\12\11\2\55\2\11",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\57\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\57\2\11",
            "\1\60\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21\2"+
            "\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\44\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\44\2\11",
            "\3\12\3\uffff\1\62\5\uffff\1\12\3\uffff\1\62\1\61\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\61\1\uffff\1\63\2\uffff\2\2",
            "\1\12\7\uffff\1\2\11\uffff\1\46\72\uffff\2\46\6\uffff\1\2",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\64\35\uffff\3\21\1\43\31\uffff\2\64",
            "\1\51\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\50\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\53\1\21\1\52\1\21\17\uffff\12\11\2\50\1\uffff\1\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\65\1\uffff\1\11\1\66\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\67\1\21\1\71\1\72\3\uffff\1\11\13\uffff\12\11\2\66\2\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\73\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\73\2\11",
            "\1\75\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\1\11"+
            "\1\uffff\1\21\1\uffff\1\11\1\74\5\uffff\1\11\27\uffff\2\21\1"+
            "\76\1\21\17\uffff\12\11\2\74\1\uffff\1\11",
            "\1\77\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21\2"+
            "\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\55\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11\2\55\2\11",
            "\1\100\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\41\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\57\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\40\1\21\1\42\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\57\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\64\35\uffff\3\21\1\43\31\uffff\2\64",
            "\3\12\3\uffff\1\62\5\uffff\1\12\3\uffff\1\62\1\61\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\20\uffff\12\12\2"+
            "\61\1\uffff\1\63\2\uffff\2\2",
            "\1\45\5\uffff\1\45\10\uffff\1\2\1\34\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\45\5\uffff\1\45\10\uffff\1\2\1\34\1\uffff\1\12\72\uffff"+
            "\2\12",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\64\35\uffff\4\21\31\uffff\2\64",
            "\1\101\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\66\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11\2\66\2\11",
            "\1\103\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\102\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\53\1\21\1\104\1\21\17\uffff\12\11\2\102\1\uffff\1\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\105\1\uffff\1\11\1\106\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\67\1\21\1\71\1\107\3\uffff\1\11\13\uffff\12\11\2\106"+
            "\2\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\110\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\110\2\11",
            "\1\111\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\73\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\73\2\11",
            "\1\75\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\1\11"+
            "\1\uffff\1\21\1\uffff\1\11\1\74\5\uffff\1\11\27\uffff\2\21\1"+
            "\76\1\21\17\uffff\12\11\2\74\1\uffff\1\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\112\1\uffff\1\11\1\113\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\114\1\21\1\116\1\117\3\uffff\1\11\13\uffff\12\11\2"+
            "\113\2\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\120\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\121\35\uffff\3\21\1\56\31\uffff\2\121",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\121\35\uffff\3\21\1\56\31\uffff\2\121",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\122\35\uffff\3\21\1\72\31\uffff\2\122",
            "\1\103\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\102\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\53\1\21\1\104\1\21\17\uffff\12\11\2\102\1\uffff\1\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\123\1\uffff\1\11\1\124\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\67\1\21\1\71\1\125\3\uffff\1\11\13\uffff\12\11\2\124"+
            "\2\11",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\126\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\126\2\11",
            "\1\127\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\106\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11\2\106\2\11",
            "\1\130\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\110\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\110\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\122\35\uffff\3\21\1\72\31\uffff\2\122",
            "\1\131\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\113\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12\11\2\113\2\11",
            "\1\133\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\132\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\53\1\21\1\134\1\21\17\uffff\12\11\2\132\1\uffff\1\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\135\1\uffff\1\11\1\136\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\114\1\21\1\116\1\137\3\uffff\1\11\13\uffff\12\11\2"+
            "\136\2\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\140\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\140\2\11",
            "\1\141\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\120\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\121\35\uffff\4\21\31\uffff\2\121",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\122\35\uffff\4\21\31\uffff\2\122",
            "\1\142\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\124\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11\2\124\2\11",
            "\1\143\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\70\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\126\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\67\1\21\1\71\1\21\3\uffff\1\11\13\uffff\12\11"+
            "\2\126\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\144\35\uffff\3\21\1\107\31\uffff\2\144",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\144\35\uffff\3\21\1\107\31\uffff\2\144",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\145\35\uffff\3\21\1\117\31\uffff\2\145",
            "\1\133\2\11\3\uffff\1\11\1\21\1\uffff\1\21\1\uffff\1\21\2\11"+
            "\1\21\1\uffff\1\11\1\132\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\53\1\21\1\134\1\21\17\uffff\12\11\2\132\1\uffff\1\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\146\1\uffff\1\11\1\147\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\114\1\21\1\116\1\150\3\uffff\1\11\13\uffff\12\11\2"+
            "\147\2\11",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\151\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\151\2\11",
            "\1\152\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\136\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12\11\2\136\2\11",
            "\1\153\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\140\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\140\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\145\35\uffff\3\21\1\117\31\uffff\2\145",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\154\35\uffff\3\21\1\125\31\uffff\2\154",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\154\35\uffff\3\21\1\125\31\uffff\2\154",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\144\35\uffff\4\21\31\uffff\2\144",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\145\35\uffff\4\21\31\uffff\2\145",
            "\1\155\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\2\11\1\21\1\uffff\1\11\1\147\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12\11\2\147\2\11",
            "\1\156\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\2\uffff\1\21"+
            "\2\uffff\1\21\35\uffff\4\21\31\uffff\2\21",
            "\1\11\1\115\2\11\2\uffff\2\11\1\21\1\uffff\1\21\1\uffff\1\21"+
            "\1\11\1\uffff\1\21\1\uffff\1\11\1\151\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\114\1\21\1\116\1\21\3\uffff\1\11\13\uffff\12"+
            "\11\2\151\2\11",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\157\35\uffff\3\21\1\137\31\uffff\2\157",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\157\35\uffff\3\21\1\137\31\uffff\2\157",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\154\35\uffff\4\21\31\uffff\2\154",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\160\35\uffff\3\21\1\150\31\uffff\2\160",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\160\35\uffff\3\21\1\150\31\uffff\2\160",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\157\35\uffff\4\21\31\uffff\2\157",
            "\1\21\6\uffff\1\21\1\uffff\1\21\1\uffff\1\21\1\uffff\1\11\1"+
            "\21\2\uffff\1\160\35\uffff\4\21\31\uffff\2\160"
    };

    static final short[] DFA58_eot = DFA.unpackEncodedString(DFA58_eotS);
    static final short[] DFA58_eof = DFA.unpackEncodedString(DFA58_eofS);
    static final char[] DFA58_min = DFA.unpackEncodedStringToUnsignedChars(DFA58_minS);
    static final char[] DFA58_max = DFA.unpackEncodedStringToUnsignedChars(DFA58_maxS);
    static final short[] DFA58_accept = DFA.unpackEncodedString(DFA58_acceptS);
    static final short[] DFA58_special = DFA.unpackEncodedString(DFA58_specialS);
    static final short[][] DFA58_transition;

    static {
        int numStates = DFA58_transitionS.length;
        DFA58_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA58_transition[i] = DFA.unpackEncodedString(DFA58_transitionS[i]);
        }
    }

    class DFA58 extends DFA {

        public DFA58(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 58;
            this.eot = DFA58_eot;
            this.eof = DFA58_eof;
            this.min = DFA58_min;
            this.max = DFA58_max;
            this.accept = DFA58_accept;
            this.special = DFA58_special;
            this.transition = DFA58_transition;
        }
        public String getDescription() {
            return "421:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA79_eotS =
        "\4\uffff";
    static final String DFA79_eofS =
        "\4\uffff";
    static final String DFA79_minS =
        "\2\13\2\uffff";
    static final String DFA79_maxS =
        "\2\123\2\uffff";
    static final String DFA79_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA79_specialS =
        "\4\uffff}>";
    static final String[] DFA79_transitionS = {
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "\1\3\1\uffff\1\2\11\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA79_eot = DFA.unpackEncodedString(DFA79_eotS);
    static final short[] DFA79_eof = DFA.unpackEncodedString(DFA79_eofS);
    static final char[] DFA79_min = DFA.unpackEncodedStringToUnsignedChars(DFA79_minS);
    static final char[] DFA79_max = DFA.unpackEncodedStringToUnsignedChars(DFA79_maxS);
    static final short[] DFA79_accept = DFA.unpackEncodedString(DFA79_acceptS);
    static final short[] DFA79_special = DFA.unpackEncodedString(DFA79_specialS);
    static final short[][] DFA79_transition;

    static {
        int numStates = DFA79_transitionS.length;
        DFA79_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA79_transition[i] = DFA.unpackEncodedString(DFA79_transitionS[i]);
        }
    }

    class DFA79 extends DFA {

        public DFA79(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 79;
            this.eot = DFA79_eot;
            this.eof = DFA79_eof;
            this.min = DFA79_min;
            this.max = DFA79_max;
            this.accept = DFA79_accept;
            this.special = DFA79_special;
            this.transition = DFA79_transition;
        }
        public String getDescription() {
            return "()* loopback of 490:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA105_eotS =
        "\6\uffff";
    static final String DFA105_eofS =
        "\6\uffff";
    static final String DFA105_minS =
        "\2\6\2\uffff\2\6";
    static final String DFA105_maxS =
        "\1\126\1\123\2\uffff\2\123";
    static final String DFA105_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA105_specialS =
        "\6\uffff}>";
    static final String[] DFA105_transitionS = {
            "\1\3\13\uffff\1\3\1\uffff\1\3\40\uffff\3\3\1\1\5\3\1\uffff\1"+
            "\3\26\uffff\1\2",
            "\1\4\6\uffff\1\3\4\uffff\1\3\1\uffff\1\3\2\uffff\1\3\35\uffff"+
            "\4\3\6\uffff\1\3\22\uffff\2\3",
            "",
            "",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5"
    };

    static final short[] DFA105_eot = DFA.unpackEncodedString(DFA105_eotS);
    static final short[] DFA105_eof = DFA.unpackEncodedString(DFA105_eofS);
    static final char[] DFA105_min = DFA.unpackEncodedStringToUnsignedChars(DFA105_minS);
    static final char[] DFA105_max = DFA.unpackEncodedStringToUnsignedChars(DFA105_maxS);
    static final short[] DFA105_accept = DFA.unpackEncodedString(DFA105_acceptS);
    static final short[] DFA105_special = DFA.unpackEncodedString(DFA105_specialS);
    static final short[][] DFA105_transition;

    static {
        int numStates = DFA105_transitionS.length;
        DFA105_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA105_transition[i] = DFA.unpackEncodedString(DFA105_transitionS[i]);
        }
    }

    class DFA105 extends DFA {

        public DFA105(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 105;
            this.eot = DFA105_eot;
            this.eof = DFA105_eof;
            this.min = DFA105_min;
            this.max = DFA105_max;
            this.accept = DFA105_accept;
            this.special = DFA105_special;
            this.transition = DFA105_transition;
        }
        public String getDescription() {
            return "578:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA118_eotS =
        "\41\uffff";
    static final String DFA118_eofS =
        "\41\uffff";
    static final String DFA118_minS =
        "\1\5\7\0\1\uffff\1\0\5\uffff\1\0\10\uffff\1\0\10\uffff";
    static final String DFA118_maxS =
        "\1\152\7\0\1\uffff\1\0\5\uffff\1\0\10\uffff\1\0\10\uffff";
    static final String DFA118_acceptS =
        "\10\uffff\1\15\1\uffff\5\5\1\uffff\2\5\1\6\1\7\1\10\4\uffff\1\12"+
        "\1\13\1\14\1\2\1\3\1\4\1\1\1\11";
    static final String DFA118_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\5\uffff\1\11\10\uffff"+
        "\1\12\10\uffff}>";
    static final String[] DFA118_transitionS = {
            "\1\33\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\15"+
            "\1\uffff\1\30\36\uffff\1\17\1\2\1\11\1\4\1\20\1\21\1\16\1\1"+
            "\1\14\1\uffff\1\13\25\uffff\1\7\1\12\1\31\6\uffff\1\22\1\uffff"+
            "\2\23\1\24\2\uffff\3\24\2\uffff\1\32",
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
            return "()* loopback of 597:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA118_0 = input.LA(1);

                         
                        int index118_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA118_0==STAR) ) {s = 1;}

                        else if ( (LA118_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA118_0==IDENT) ) {s = 3;}

                        else if ( (LA118_0==DOT) ) {s = 4;}

                        else if ( (LA118_0==GEN) ) {s = 5;}

                        else if ( (LA118_0==MEDIA_SYM) ) {s = 6;}

                        else if ( (LA118_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA118_0==RBRACE) ) {s = 8;}

                        else if ( (LA118_0==HASH) ) {s = 9;}

                        else if ( (LA118_0==SASS_MIXIN) && (synpred9_Css3())) {s = 10;}

                        else if ( (LA118_0==LESS_AND) && (synpred9_Css3())) {s = 11;}

                        else if ( (LA118_0==PIPE) && (synpred9_Css3())) {s = 12;}

                        else if ( (LA118_0==COLON) && (synpred9_Css3())) {s = 13;}

                        else if ( (LA118_0==SASS_EXTEND_ONLY_SELECTOR) && (synpred9_Css3())) {s = 14;}

                        else if ( (LA118_0==MINUS) ) {s = 15;}

                        else if ( (LA118_0==LBRACKET) && (synpred9_Css3())) {s = 16;}

                        else if ( (LA118_0==DCOLON) && (synpred9_Css3())) {s = 17;}

                        else if ( (LA118_0==SASS_EXTEND) ) {s = 18;}

                        else if ( ((LA118_0>=SASS_DEBUG && LA118_0<=SASS_WARN)) ) {s = 19;}

                        else if ( (LA118_0==SASS_IF||(LA118_0>=SASS_FOR && LA118_0<=SASS_WHILE)) ) {s = 20;}

                        else if ( (LA118_0==AT_IDENT) ) {s = 24;}

                        else if ( (LA118_0==SASS_INCLUDE) ) {s = 25;}

                        else if ( (LA118_0==SASS_CONTENT) ) {s = 26;}

                        else if ( (LA118_0==SEMI) && (synpred10_Css3())) {s = 27;}

                         
                        input.seek(index118_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA118_1 = input.LA(1);

                         
                        int index118_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA118_2 = input.LA(1);

                         
                        int index118_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA118_3 = input.LA(1);

                         
                        int index118_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA118_4 = input.LA(1);

                         
                        int index118_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( ((synpred9_Css3()||((synpred9_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 17;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 25;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA118_5 = input.LA(1);

                         
                        int index118_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA118_6 = input.LA(1);

                         
                        int index118_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred5_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((synpred5_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 31;}

                        else if ( (((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 28;}

                        else if ( (((synpred7_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 29;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 30;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 32;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index118_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA118_7 = input.LA(1);

                         
                        int index118_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred5_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))||((synpred5_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isScssSource(),"isScssSource()")))) ) {s = 31;}

                        else if ( (((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 28;}

                        else if ( (((synpred7_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 29;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 30;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index118_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA118_9 = input.LA(1);

                         
                        int index118_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA118_15 = input.LA(1);

                         
                        int index118_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 28;}

                        else if ( (synpred7_Css3()) ) {s = 29;}

                        else if ( (synpred8_Css3()) ) {s = 30;}

                        else if ( (synpred9_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index118_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA118_24 = input.LA(1);

                         
                        int index118_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred5_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((synpred5_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 31;}

                        else if ( (((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 28;}

                        else if ( (((synpred7_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 29;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 30;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index118_24);
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
    static final String DFA123_eotS =
        "\20\uffff";
    static final String DFA123_eofS =
        "\20\uffff";
    static final String DFA123_minS =
        "\2\6\2\0\1\uffff\1\0\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA123_maxS =
        "\1\77\1\123\2\0\1\uffff\1\0\2\123\5\uffff\1\0\1\uffff\1\0";
    static final String DFA123_acceptS =
        "\4\uffff\1\2\3\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA123_specialS =
        "\1\7\1\5\1\10\1\6\1\uffff\1\2\1\3\1\4\5\uffff\1\1\1\uffff\1\0}>";
    static final String[] DFA123_transitionS = {
            "\1\2\13\uffff\1\4\1\uffff\1\7\40\uffff\1\10\1\1\1\5\1\6\5\4"+
            "\1\uffff\1\3",
            "\1\14\6\uffff\1\11\6\uffff\1\14\2\uffff\1\12\35\uffff\1\14"+
            "\1\13\2\14\5\uffff\1\4\1\14\22\uffff\2\12",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\15\6\uffff\1\16\4\uffff\1\4\1\uffff\1\14\2\uffff\1\12\35"+
            "\uffff\1\14\1\13\2\14\6\uffff\1\14\22\uffff\2\12",
            "\1\17\6\uffff\1\16\3\uffff\2\4\1\uffff\1\14\2\uffff\1\12\35"+
            "\uffff\1\14\1\13\2\14\6\uffff\1\14\22\uffff\2\12",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff"
    };

    static final short[] DFA123_eot = DFA.unpackEncodedString(DFA123_eotS);
    static final short[] DFA123_eof = DFA.unpackEncodedString(DFA123_eofS);
    static final char[] DFA123_min = DFA.unpackEncodedStringToUnsignedChars(DFA123_minS);
    static final char[] DFA123_max = DFA.unpackEncodedStringToUnsignedChars(DFA123_maxS);
    static final short[] DFA123_accept = DFA.unpackEncodedString(DFA123_acceptS);
    static final short[] DFA123_special = DFA.unpackEncodedString(DFA123_specialS);
    static final short[][] DFA123_transition;

    static {
        int numStates = DFA123_transitionS.length;
        DFA123_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA123_transition[i] = DFA.unpackEncodedString(DFA123_transitionS[i]);
        }
    }

    class DFA123 extends DFA {

        public DFA123(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 123;
            this.eot = DFA123_eot;
            this.eof = DFA123_eof;
            this.min = DFA123_min;
            this.max = DFA123_max;
            this.accept = DFA123_accept;
            this.special = DFA123_special;
            this.transition = DFA123_transition;
        }
        public String getDescription() {
            return "631:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA123_15 = input.LA(1);

                         
                        int index123_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index123_15);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA123_13 = input.LA(1);

                         
                        int index123_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index123_13);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA123_5 = input.LA(1);

                         
                        int index123_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index123_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA123_6 = input.LA(1);

                         
                        int index123_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA123_6==IDENT) ) {s = 13;}

                        else if ( (LA123_6==WS||(LA123_6>=NL && LA123_6<=COMMENT)) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA123_6==HASH_SYMBOL) && (synpred11_Css3())) {s = 11;}

                        else if ( (LA123_6==GEN) ) {s = 4;}

                        else if ( (LA123_6==COLON||LA123_6==MINUS||(LA123_6>=HASH && LA123_6<=DOT)||LA123_6==LESS_AND) && (synpred11_Css3())) {s = 12;}

                        else if ( (LA123_6==LBRACE) && (synpred11_Css3())) {s = 14;}

                         
                        input.seek(index123_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA123_7 = input.LA(1);

                         
                        int index123_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA123_7==WS||(LA123_7>=NL && LA123_7<=COMMENT)) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA123_7==HASH_SYMBOL) && (synpred11_Css3())) {s = 11;}

                        else if ( (LA123_7==IDENT) ) {s = 15;}

                        else if ( (LA123_7==LBRACE) && (synpred11_Css3())) {s = 14;}

                        else if ( (LA123_7==COLON||LA123_7==MINUS||(LA123_7>=HASH && LA123_7<=DOT)||LA123_7==LESS_AND) && (synpred11_Css3())) {s = 12;}

                        else if ( ((LA123_7>=NOT && LA123_7<=GEN)) ) {s = 4;}

                         
                        input.seek(index123_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA123_1 = input.LA(1);

                         
                        int index123_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA123_1==LBRACE) && (synpred11_Css3())) {s = 9;}

                        else if ( (LA123_1==NAME) ) {s = 4;}

                        else if ( (LA123_1==WS||(LA123_1>=NL && LA123_1<=COMMENT)) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA123_1==HASH_SYMBOL) && (synpred11_Css3())) {s = 11;}

                        else if ( (LA123_1==IDENT||LA123_1==COLON||LA123_1==MINUS||(LA123_1>=HASH && LA123_1<=DOT)||LA123_1==LESS_AND) && (synpred11_Css3())) {s = 12;}

                         
                        input.seek(index123_1);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA123_3 = input.LA(1);

                         
                        int index123_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index123_3);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA123_0 = input.LA(1);

                         
                        int index123_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA123_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA123_0==IDENT) ) {s = 2;}

                        else if ( (LA123_0==LESS_AND) ) {s = 3;}

                        else if ( (LA123_0==GEN||(LA123_0>=LBRACKET && LA123_0<=PIPE)) ) {s = 4;}

                        else if ( (LA123_0==HASH) ) {s = 5;}

                        else if ( (LA123_0==DOT) ) {s = 6;}

                        else if ( (LA123_0==COLON) ) {s = 7;}

                        else if ( (LA123_0==MINUS) && (synpred11_Css3())) {s = 8;}

                         
                        input.seek(index123_0);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA123_2 = input.LA(1);

                         
                        int index123_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index123_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 123, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA126_eotS =
        "\24\uffff";
    static final String DFA126_eofS =
        "\24\uffff";
    static final String DFA126_minS =
        "\1\5\7\uffff\6\0\6\uffff";
    static final String DFA126_maxS =
        "\1\137\7\uffff\6\0\6\uffff";
    static final String DFA126_acceptS =
        "\1\uffff\1\2\21\uffff\1\1";
    static final String DFA126_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\1\5\6\uffff}>";
    static final String[] DFA126_transitionS = {
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

    static final short[] DFA126_eot = DFA.unpackEncodedString(DFA126_eotS);
    static final short[] DFA126_eof = DFA.unpackEncodedString(DFA126_eofS);
    static final char[] DFA126_min = DFA.unpackEncodedStringToUnsignedChars(DFA126_minS);
    static final char[] DFA126_max = DFA.unpackEncodedStringToUnsignedChars(DFA126_maxS);
    static final short[] DFA126_accept = DFA.unpackEncodedString(DFA126_acceptS);
    static final short[] DFA126_special = DFA.unpackEncodedString(DFA126_specialS);
    static final short[][] DFA126_transition;

    static {
        int numStates = DFA126_transitionS.length;
        DFA126_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA126_transition[i] = DFA.unpackEncodedString(DFA126_transitionS[i]);
        }
    }

    class DFA126 extends DFA {

        public DFA126(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 126;
            this.eot = DFA126_eot;
            this.eof = DFA126_eof;
            this.min = DFA126_min;
            this.max = DFA126_max;
            this.accept = DFA126_accept;
            this.special = DFA126_special;
            this.transition = DFA126_transition;
        }
        public String getDescription() {
            return "()* loopback of 647:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA126_8 = input.LA(1);

                         
                        int index126_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred12_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 1;}

                         
                        input.seek(index126_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA126_9 = input.LA(1);

                         
                        int index126_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index126_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA126_10 = input.LA(1);

                         
                        int index126_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index126_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA126_11 = input.LA(1);

                         
                        int index126_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index126_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA126_12 = input.LA(1);

                         
                        int index126_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index126_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA126_13 = input.LA(1);

                         
                        int index126_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index126_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 126, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA144_eotS =
        "\4\uffff";
    static final String DFA144_eofS =
        "\4\uffff";
    static final String DFA144_minS =
        "\2\5\2\uffff";
    static final String DFA144_maxS =
        "\2\137\2\uffff";
    static final String DFA144_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA144_specialS =
        "\4\uffff}>";
    static final String[] DFA144_transitionS = {
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "",
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
            return "748:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA153_eotS =
        "\35\uffff";
    static final String DFA153_eofS =
        "\35\uffff";
    static final String DFA153_minS =
        "\1\6\1\uffff\1\5\1\0\1\6\5\0\1\uffff\2\0\1\uffff\1\6\1\uffff\1\0"+
        "\4\uffff\1\0\1\uffff\2\0\1\uffff\1\6\2\0";
    static final String DFA153_maxS =
        "\1\125\1\uffff\1\125\1\0\1\125\5\0\1\uffff\2\0\1\uffff\1\125\1\uffff"+
        "\1\0\4\uffff\1\0\1\uffff\2\0\1\uffff\1\125\2\0";
    static final String DFA153_acceptS =
        "\1\uffff\1\1\10\uffff\1\1\2\uffff\1\3\1\uffff\1\1\1\uffff\4\1\1"+
        "\uffff\1\1\2\uffff\1\2\3\uffff";
    static final String DFA153_specialS =
        "\1\15\1\uffff\1\4\1\10\1\uffff\1\2\1\3\1\6\1\5\1\7\1\uffff\1\12"+
        "\1\17\1\uffff\1\1\1\uffff\1\0\4\uffff\1\16\1\uffff\1\13\1\20\2\uffff"+
        "\1\11\1\14}>";
    static final String[] DFA153_transitionS = {
            "\1\3\1\6\1\11\3\uffff\1\13\5\uffff\1\10\1\15\2\uffff\1\13\6"+
            "\uffff\1\5\23\uffff\1\12\1\4\2\uffff\1\2\1\1\1\7\1\12\17\uffff"+
            "\12\5\3\uffff\1\14",
            "",
            "\1\22\1\20\1\6\1\11\3\uffff\1\27\1\24\1\23\3\uffff\1\10\3\uffff"+
            "\1\27\1\16\5\uffff\1\5\23\uffff\1\26\3\uffff\1\26\1\17\1\25"+
            "\1\26\16\uffff\1\21\12\5\2\16\1\uffff\1\30",
            "\1\uffff",
            "\1\33\1\6\1\11\3\uffff\1\27\5\uffff\1\10\3\uffff\1\27\1\32"+
            "\5\uffff\1\5\31\uffff\1\34\20\uffff\12\5\2\32\1\uffff\1\30",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\20\1\6\1\11\3\uffff\1\27\5\uffff\1\10\3\uffff\1\27\1\16"+
            "\5\uffff\1\5\23\uffff\1\26\3\uffff\1\26\1\17\1\25\1\26\17\uffff"+
            "\12\5\2\16\1\uffff\1\30",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\33\1\6\1\11\3\uffff\1\27\5\uffff\1\10\3\uffff\1\27\1\32"+
            "\5\uffff\1\5\31\uffff\1\34\20\uffff\12\5\2\32\1\uffff\1\30",
            "\1\uffff",
            "\1\uffff"
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
            return "768:1: propertyValue : ( ( (~ ( HASH_SYMBOL | SEMI ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA153_16 = input.LA(1);

                         
                        int index153_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 22;}

                        else if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_16);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA153_14 = input.LA(1);

                         
                        int index153_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA153_14==HASH_SYMBOL) && (synpred15_Css3())) {s = 15;}

                        else if ( (LA153_14==IDENT) ) {s = 16;}

                        else if ( (LA153_14==WS||(LA153_14>=NL && LA153_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA153_14==PERCENTAGE||(LA153_14>=NUMBER && LA153_14<=DIMENSION)) ) {s = 5;}

                        else if ( (LA153_14==STRING) ) {s = 6;}

                        else if ( (LA153_14==HASH) ) {s = 21;}

                        else if ( (LA153_14==GEN) ) {s = 8;}

                        else if ( (LA153_14==URI) ) {s = 9;}

                        else if ( (LA153_14==SOLIDUS||LA153_14==MINUS||LA153_14==DOT) && (synpred15_Css3())) {s = 22;}

                        else if ( (LA153_14==MEDIA_SYM||LA153_14==AT_IDENT) ) {s = 23;}

                        else if ( (LA153_14==SASS_VAR) ) {s = 24;}

                         
                        input.seek(index153_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA153_5 = input.LA(1);

                         
                        int index153_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA153_6 = input.LA(1);

                         
                        int index153_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA153_2 = input.LA(1);

                         
                        int index153_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA153_2==WS||(LA153_2>=NL && LA153_2<=COMMENT)) ) {s = 14;}

                        else if ( (LA153_2==HASH_SYMBOL) && (synpred15_Css3())) {s = 15;}

                        else if ( (LA153_2==IDENT) ) {s = 16;}

                        else if ( (LA153_2==IMPORTANT_SYM) && (synpred15_Css3())) {s = 17;}

                        else if ( (LA153_2==SEMI) && (synpred15_Css3())) {s = 18;}

                        else if ( (LA153_2==RBRACE) && (synpred15_Css3())) {s = 19;}

                        else if ( (LA153_2==LBRACE) && (synpred15_Css3())) {s = 20;}

                        else if ( (LA153_2==PERCENTAGE||(LA153_2>=NUMBER && LA153_2<=DIMENSION)) ) {s = 5;}

                        else if ( (LA153_2==STRING) ) {s = 6;}

                        else if ( (LA153_2==HASH) ) {s = 21;}

                        else if ( (LA153_2==GEN) ) {s = 8;}

                        else if ( (LA153_2==URI) ) {s = 9;}

                        else if ( (LA153_2==SOLIDUS||LA153_2==MINUS||LA153_2==DOT) && (synpred15_Css3())) {s = 22;}

                        else if ( (LA153_2==MEDIA_SYM||LA153_2==AT_IDENT) ) {s = 23;}

                        else if ( (LA153_2==SASS_VAR) ) {s = 24;}

                         
                        input.seek(index153_2);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA153_8 = input.LA(1);

                         
                        int index153_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_8);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA153_7 = input.LA(1);

                         
                        int index153_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 22;}

                        else if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA153_9 = input.LA(1);

                         
                        int index153_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA153_3 = input.LA(1);

                         
                        int index153_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 22;}

                        else if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_3);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA153_27 = input.LA(1);

                         
                        int index153_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_27);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA153_11 = input.LA(1);

                         
                        int index153_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred16_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 25;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 13;}

                         
                        input.seek(index153_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA153_23 = input.LA(1);

                         
                        int index153_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_23);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA153_28 = input.LA(1);

                         
                        int index153_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_28);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA153_0 = input.LA(1);

                         
                        int index153_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA153_0==HASH_SYMBOL) && (synpred15_Css3())) {s = 1;}

                        else if ( (LA153_0==MINUS) ) {s = 2;}

                        else if ( (LA153_0==IDENT) ) {s = 3;}

                        else if ( (LA153_0==PLUS) ) {s = 4;}

                        else if ( (LA153_0==PERCENTAGE||(LA153_0>=NUMBER && LA153_0<=DIMENSION)) ) {s = 5;}

                        else if ( (LA153_0==STRING) ) {s = 6;}

                        else if ( (LA153_0==HASH) ) {s = 7;}

                        else if ( (LA153_0==GEN) ) {s = 8;}

                        else if ( (LA153_0==URI) ) {s = 9;}

                        else if ( (LA153_0==SOLIDUS||LA153_0==DOT) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA153_0==MEDIA_SYM||LA153_0==AT_IDENT) ) {s = 11;}

                        else if ( (LA153_0==SASS_VAR) ) {s = 12;}

                        else if ( (LA153_0==LPAREN) ) {s = 13;}

                         
                        input.seek(index153_0);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA153_21 = input.LA(1);

                         
                        int index153_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 22;}

                        else if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_21);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA153_12 = input.LA(1);

                         
                        int index153_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred16_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 25;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 13;}

                         
                        input.seek(index153_12);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA153_24 = input.LA(1);

                         
                        int index153_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index153_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 153, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA157_eotS =
        "\12\uffff";
    static final String DFA157_eofS =
        "\12\uffff";
    static final String DFA157_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA157_maxS =
        "\1\125\1\uffff\1\125\1\uffff\2\125\1\6\1\125\2\123";
    static final String DFA157_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA157_specialS =
        "\12\uffff}>";
    static final String[] DFA157_transitionS = {
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
            return "()* loopback of 831:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA160_eotS =
        "\13\uffff";
    static final String DFA160_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA160_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA160_maxS =
        "\1\125\2\uffff\1\144\5\uffff\1\144\1\uffff";
    static final String DFA160_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA160_specialS =
        "\13\uffff}>";
    static final String[] DFA160_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\20\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\2\10\2\12\1\11\5\uffff\1\12"+
            "\23\uffff\3\12\1\uffff\1\12\1\uffff\1\12\1\10\3\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12\5\uffff\3\12\6\uffff\1\12",
            "",
            "",
            "",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\1\10\1\uffff\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\3\12\1\uffff\1\12\1\uffff\1\12\4\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12\5\uffff\3\12\6\uffff\1\12",
            ""
    };

    static final short[] DFA160_eot = DFA.unpackEncodedString(DFA160_eotS);
    static final short[] DFA160_eof = DFA.unpackEncodedString(DFA160_eofS);
    static final char[] DFA160_min = DFA.unpackEncodedStringToUnsignedChars(DFA160_minS);
    static final char[] DFA160_max = DFA.unpackEncodedStringToUnsignedChars(DFA160_maxS);
    static final short[] DFA160_accept = DFA.unpackEncodedString(DFA160_acceptS);
    static final short[] DFA160_special = DFA.unpackEncodedString(DFA160_specialS);
    static final short[][] DFA160_transition;

    static {
        int numStates = DFA160_transitionS.length;
        DFA160_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA160_transition[i] = DFA.unpackEncodedString(DFA160_transitionS[i]);
        }
    }

    class DFA160 extends DFA {

        public DFA160(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 160;
            this.eot = DFA160_eot;
            this.eof = DFA160_eof;
            this.min = DFA160_min;
            this.max = DFA160_max;
            this.accept = DFA160_accept;
            this.special = DFA160_special;
            this.transition = DFA160_transition;
        }
        public String getDescription() {
            return "836:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA166_eotS =
        "\11\uffff";
    static final String DFA166_eofS =
        "\11\uffff";
    static final String DFA166_minS =
        "\1\6\1\uffff\1\6\1\uffff\2\6\1\uffff\2\23";
    static final String DFA166_maxS =
        "\1\125\1\uffff\1\125\1\uffff\1\125\1\6\1\uffff\2\123";
    static final String DFA166_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\2\uffff\1\2\2\uffff";
    static final String DFA166_specialS =
        "\11\uffff}>";
    static final String[] DFA166_transitionS = {
            "\1\2\2\1\3\uffff\1\1\5\uffff\1\1\2\uffff\1\3\1\1\6\uffff\1\1"+
            "\24\uffff\1\1\2\uffff\1\1\1\uffff\1\1\20\uffff\12\1\3\uffff"+
            "\1\1",
            "",
            "\3\1\2\uffff\2\1\5\uffff\5\1\1\4\5\uffff\1\1\23\uffff\2\1\2"+
            "\uffff\1\1\1\uffff\1\1\1\5\7\uffff\1\6\7\uffff\12\1\2\4\1\uffff"+
            "\1\1",
            "",
            "\3\1\2\uffff\2\1\5\uffff\2\1\1\uffff\2\1\1\4\5\uffff\1\1\23"+
            "\uffff\2\1\2\uffff\1\1\1\uffff\1\1\10\uffff\1\6\7\uffff\12\1"+
            "\2\4\1\uffff\1\1",
            "\1\7",
            "",
            "\1\1\3\uffff\1\10\40\uffff\1\5\7\uffff\1\6\21\uffff\2\10",
            "\1\1\3\uffff\1\10\50\uffff\1\6\21\uffff\2\10"
    };

    static final short[] DFA166_eot = DFA.unpackEncodedString(DFA166_eotS);
    static final short[] DFA166_eof = DFA.unpackEncodedString(DFA166_eofS);
    static final char[] DFA166_min = DFA.unpackEncodedStringToUnsignedChars(DFA166_minS);
    static final char[] DFA166_max = DFA.unpackEncodedStringToUnsignedChars(DFA166_maxS);
    static final short[] DFA166_accept = DFA.unpackEncodedString(DFA166_acceptS);
    static final short[] DFA166_special = DFA.unpackEncodedString(DFA166_specialS);
    static final short[][] DFA166_transition;

    static {
        int numStates = DFA166_transitionS.length;
        DFA166_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA166_transition[i] = DFA.unpackEncodedString(DFA166_transitionS[i]);
        }
    }

    class DFA166 extends DFA {

        public DFA166(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 166;
            this.eot = DFA166_eot;
            this.eof = DFA166_eof;
            this.min = DFA166_min;
            this.max = DFA166_max;
            this.accept = DFA166_accept;
            this.special = DFA166_special;
            this.transition = DFA166_transition;
        }
        public String getDescription() {
            return "864:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?)";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA189_eotS =
        "\36\uffff";
    static final String DFA189_eofS =
        "\36\uffff";
    static final String DFA189_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA189_maxS =
        "\1\144\1\uffff\2\125\10\uffff\1\125\10\0\1\125\10\0";
    static final String DFA189_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA189_specialS =
        "\1\13\14\uffff\1\16\1\17\1\14\1\0\1\2\1\12\1\15\1\10\1\uffff\1\1"+
        "\1\3\1\20\1\4\1\6\1\11\1\7\1\5}>";
    static final String[] DFA189_transitionS = {
            "\1\1\1\6\1\5\1\10\2\uffff\1\1\1\12\2\1\3\uffff\1\7\2\uffff\1"+
            "\1\1\12\6\uffff\1\4\23\uffff\1\1\1\2\1\1\1\uffff\1\3\1\uffff"+
            "\1\11\4\uffff\1\1\12\uffff\1\1\12\4\2\uffff\1\1\1\13\5\uffff"+
            "\3\1\6\uffff\1\1",
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

    static final short[] DFA189_eot = DFA.unpackEncodedString(DFA189_eotS);
    static final short[] DFA189_eof = DFA.unpackEncodedString(DFA189_eofS);
    static final char[] DFA189_min = DFA.unpackEncodedStringToUnsignedChars(DFA189_minS);
    static final char[] DFA189_max = DFA.unpackEncodedStringToUnsignedChars(DFA189_maxS);
    static final short[] DFA189_accept = DFA.unpackEncodedString(DFA189_acceptS);
    static final short[] DFA189_special = DFA.unpackEncodedString(DFA189_specialS);
    static final short[][] DFA189_transition;

    static {
        int numStates = DFA189_transitionS.length;
        DFA189_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA189_transition[i] = DFA.unpackEncodedString(DFA189_transitionS[i]);
        }
    }

    class DFA189 extends DFA {

        public DFA189(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 189;
            this.eot = DFA189_eot;
            this.eof = DFA189_eof;
            this.min = DFA189_min;
            this.max = DFA189_max;
            this.accept = DFA189_accept;
            this.special = DFA189_special;
            this.transition = DFA189_transition;
        }
        public String getDescription() {
            return "()* loopback of 952:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA189_16 = input.LA(1);

                         
                        int index189_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_16);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA189_22 = input.LA(1);

                         
                        int index189_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_22);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA189_17 = input.LA(1);

                         
                        int index189_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_17);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA189_23 = input.LA(1);

                         
                        int index189_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_23);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA189_25 = input.LA(1);

                         
                        int index189_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_25);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA189_29 = input.LA(1);

                         
                        int index189_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_29);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA189_26 = input.LA(1);

                         
                        int index189_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_26);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA189_28 = input.LA(1);

                         
                        int index189_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_28);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA189_20 = input.LA(1);

                         
                        int index189_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_20);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA189_27 = input.LA(1);

                         
                        int index189_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_27);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA189_18 = input.LA(1);

                         
                        int index189_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_18);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA189_0 = input.LA(1);

                         
                        int index189_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA189_0==SEMI||LA189_0==COMMA||(LA189_0>=LBRACE && LA189_0<=RBRACE)||LA189_0==RPAREN||LA189_0==SOLIDUS||LA189_0==GREATER||LA189_0==STAR||LA189_0==IMPORTANT_SYM||LA189_0==SASS_DEFAULT||(LA189_0>=GREATER_OR_EQ && LA189_0<=LESS_OR_EQ)||LA189_0==CP_EQ) ) {s = 1;}

                        else if ( (LA189_0==PLUS) ) {s = 2;}

                        else if ( (LA189_0==MINUS) ) {s = 3;}

                        else if ( (LA189_0==PERCENTAGE||(LA189_0>=NUMBER && LA189_0<=DIMENSION)) && (synpred17_Css3())) {s = 4;}

                        else if ( (LA189_0==STRING) && (synpred17_Css3())) {s = 5;}

                        else if ( (LA189_0==IDENT) && (synpred17_Css3())) {s = 6;}

                        else if ( (LA189_0==GEN) && (synpred17_Css3())) {s = 7;}

                        else if ( (LA189_0==URI) && (synpred17_Css3())) {s = 8;}

                        else if ( (LA189_0==HASH) && (synpred17_Css3())) {s = 9;}

                        else if ( (LA189_0==MEDIA_SYM||LA189_0==AT_IDENT) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA189_0==SASS_VAR) && (synpred17_Css3())) {s = 11;}

                         
                        input.seek(index189_0);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA189_15 = input.LA(1);

                         
                        int index189_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_15);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA189_19 = input.LA(1);

                         
                        int index189_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_19);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA189_13 = input.LA(1);

                         
                        int index189_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_13);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA189_14 = input.LA(1);

                         
                        int index189_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_14);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA189_24 = input.LA(1);

                         
                        int index189_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index189_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 189, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA193_eotS =
        "\13\uffff";
    static final String DFA193_eofS =
        "\13\uffff";
    static final String DFA193_minS =
        "\1\6\2\uffff\1\6\5\uffff\1\6\1\uffff";
    static final String DFA193_maxS =
        "\1\125\2\uffff\1\123\5\uffff\1\123\1\uffff";
    static final String DFA193_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA193_specialS =
        "\13\uffff}>";
    static final String[] DFA193_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\20\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\1\12\4\uffff\1\12\1\uffff\1\12\5\uffff\2\10\2\uffff\1\11\40"+
            "\uffff\1\10\31\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\12\4\uffff\1\12\1\uffff\1\12\5\uffff\1\10\3\uffff\1\11\72"+
            "\uffff\2\11",
            ""
    };

    static final short[] DFA193_eot = DFA.unpackEncodedString(DFA193_eotS);
    static final short[] DFA193_eof = DFA.unpackEncodedString(DFA193_eofS);
    static final char[] DFA193_min = DFA.unpackEncodedStringToUnsignedChars(DFA193_minS);
    static final char[] DFA193_max = DFA.unpackEncodedStringToUnsignedChars(DFA193_maxS);
    static final short[] DFA193_accept = DFA.unpackEncodedString(DFA193_acceptS);
    static final short[] DFA193_special = DFA.unpackEncodedString(DFA193_specialS);
    static final short[][] DFA193_transition;

    static {
        int numStates = DFA193_transitionS.length;
        DFA193_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA193_transition[i] = DFA.unpackEncodedString(DFA193_transitionS[i]);
        }
    }

    class DFA193 extends DFA {

        public DFA193(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 193;
            this.eot = DFA193_eot;
            this.eof = DFA193_eof;
            this.min = DFA193_min;
            this.max = DFA193_max;
            this.accept = DFA193_accept;
            this.special = DFA193_special;
            this.transition = DFA193_transition;
        }
        public String getDescription() {
            return "959:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA209_eotS =
        "\4\uffff";
    static final String DFA209_eofS =
        "\4\uffff";
    static final String DFA209_minS =
        "\2\5\2\uffff";
    static final String DFA209_maxS =
        "\2\123\2\uffff";
    static final String DFA209_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA209_specialS =
        "\4\uffff}>";
    static final String[] DFA209_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA209_eot = DFA.unpackEncodedString(DFA209_eotS);
    static final short[] DFA209_eof = DFA.unpackEncodedString(DFA209_eofS);
    static final char[] DFA209_min = DFA.unpackEncodedStringToUnsignedChars(DFA209_minS);
    static final char[] DFA209_max = DFA.unpackEncodedStringToUnsignedChars(DFA209_maxS);
    static final short[] DFA209_accept = DFA.unpackEncodedString(DFA209_acceptS);
    static final short[] DFA209_special = DFA.unpackEncodedString(DFA209_specialS);
    static final short[][] DFA209_transition;

    static {
        int numStates = DFA209_transitionS.length;
        DFA209_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA209_transition[i] = DFA.unpackEncodedString(DFA209_transitionS[i]);
        }
    }

    class DFA209 extends DFA {

        public DFA209(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 209;
            this.eot = DFA209_eot;
            this.eof = DFA209_eof;
            this.min = DFA209_min;
            this.max = DFA209_max;
            this.accept = DFA209_accept;
            this.special = DFA209_special;
            this.transition = DFA209_transition;
        }
        public String getDescription() {
            return "1007:5: ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA214_eotS =
        "\5\uffff";
    static final String DFA214_eofS =
        "\5\uffff";
    static final String DFA214_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA214_maxS =
        "\1\25\1\131\1\uffff\1\131\1\uffff";
    static final String DFA214_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA214_specialS =
        "\5\uffff}>";
    static final String[] DFA214_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
    };

    static final short[] DFA214_eot = DFA.unpackEncodedString(DFA214_eotS);
    static final short[] DFA214_eof = DFA.unpackEncodedString(DFA214_eofS);
    static final char[] DFA214_min = DFA.unpackEncodedStringToUnsignedChars(DFA214_minS);
    static final char[] DFA214_max = DFA.unpackEncodedStringToUnsignedChars(DFA214_maxS);
    static final short[] DFA214_accept = DFA.unpackEncodedString(DFA214_acceptS);
    static final short[] DFA214_special = DFA.unpackEncodedString(DFA214_specialS);
    static final short[][] DFA214_transition;

    static {
        int numStates = DFA214_transitionS.length;
        DFA214_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA214_transition[i] = DFA.unpackEncodedString(DFA214_transitionS[i]);
        }
    }

    class DFA214 extends DFA {

        public DFA214(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 214;
            this.eot = DFA214_eot;
            this.eof = DFA214_eof;
            this.min = DFA214_min;
            this.max = DFA214_max;
            this.accept = DFA214_accept;
            this.special = DFA214_special;
            this.transition = DFA214_transition;
        }
        public String getDescription() {
            return "()* loopback of 1034:14: ( ( COMMA | SEMI ) ( ws )? cp_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA234_eotS =
        "\16\uffff";
    static final String DFA234_eofS =
        "\16\uffff";
    static final String DFA234_minS =
        "\2\6\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA234_maxS =
        "\1\77\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff";
    static final String DFA234_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA234_specialS =
        "\5\uffff\1\6\1\0\1\5\1\3\1\2\1\uffff\1\1\1\4\1\uffff}>";
    static final String[] DFA234_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2\6\uffff\1\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\6\uffff"+
            "\1\2\22\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
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
            return "1101:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA234_6 = input.LA(1);

                         
                        int index234_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_6==WS||(LA234_6>=NL && LA234_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA234_6==RBRACE) && (synpred18_Css3())) {s = 10;}

                        else if ( (LA234_6==COLON) ) {s = 2;}

                         
                        input.seek(index234_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA234_11 = input.LA(1);

                         
                        int index234_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_11==RBRACE) && (synpred18_Css3())) {s = 10;}

                        else if ( (LA234_11==WS||(LA234_11>=NL && LA234_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA234_11==COLON) ) {s = 2;}

                         
                        input.seek(index234_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA234_9 = input.LA(1);

                         
                        int index234_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_9==RBRACE) && (synpred18_Css3())) {s = 10;}

                        else if ( (LA234_9==WS||(LA234_9>=NL && LA234_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA234_9==IDENT||LA234_9==LBRACE||(LA234_9>=AND && LA234_9<=COLON)||(LA234_9>=MINUS && LA234_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index234_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA234_8 = input.LA(1);

                         
                        int index234_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_8==WS||(LA234_8>=NL && LA234_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA234_8==RBRACE) && (synpred18_Css3())) {s = 10;}

                        else if ( (LA234_8==COLON) ) {s = 2;}

                         
                        input.seek(index234_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA234_12 = input.LA(1);

                         
                        int index234_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_12==LPAREN) && (synpred18_Css3())) {s = 13;}

                        else if ( (LA234_12==WS||(LA234_12>=NL && LA234_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA234_12==IDENT||LA234_12==COMMA||LA234_12==LBRACE||LA234_12==GEN||LA234_12==COLON||(LA234_12>=PLUS && LA234_12<=PIPE)||LA234_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index234_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA234_7 = input.LA(1);

                         
                        int index234_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_7==WS||(LA234_7>=NL && LA234_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA234_7==LPAREN) && (synpred18_Css3())) {s = 13;}

                        else if ( (LA234_7==IDENT||LA234_7==COMMA||LA234_7==LBRACE||LA234_7==GEN||LA234_7==COLON||(LA234_7>=PLUS && LA234_7<=PIPE)||LA234_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index234_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA234_5 = input.LA(1);

                         
                        int index234_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA234_5==WS||(LA234_5>=NL && LA234_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA234_5==RBRACE) && (synpred18_Css3())) {s = 10;}

                        else if ( (LA234_5==IDENT||LA234_5==LBRACE||(LA234_5>=AND && LA234_5<=COLON)||(LA234_5>=MINUS && LA234_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index234_5);
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
    static final String DFA237_eotS =
        "\4\uffff";
    static final String DFA237_eofS =
        "\4\uffff";
    static final String DFA237_minS =
        "\2\6\2\uffff";
    static final String DFA237_maxS =
        "\2\123\2\uffff";
    static final String DFA237_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA237_specialS =
        "\4\uffff}>";
    static final String[] DFA237_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\6\uffff"+
            "\1\3\22\uffff\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\6\uffff"+
            "\1\3\22\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA237_eot = DFA.unpackEncodedString(DFA237_eotS);
    static final short[] DFA237_eof = DFA.unpackEncodedString(DFA237_eofS);
    static final char[] DFA237_min = DFA.unpackEncodedStringToUnsignedChars(DFA237_minS);
    static final char[] DFA237_max = DFA.unpackEncodedStringToUnsignedChars(DFA237_maxS);
    static final short[] DFA237_accept = DFA.unpackEncodedString(DFA237_acceptS);
    static final short[] DFA237_special = DFA.unpackEncodedString(DFA237_specialS);
    static final short[][] DFA237_transition;

    static {
        int numStates = DFA237_transitionS.length;
        DFA237_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA237_transition[i] = DFA.unpackEncodedString(DFA237_transitionS[i]);
        }
    }

    class DFA237 extends DFA {

        public DFA237(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 237;
            this.eot = DFA237_eot;
            this.eof = DFA237_eof;
            this.min = DFA237_min;
            this.max = DFA237_max;
            this.accept = DFA237_accept;
            this.special = DFA237_special;
            this.transition = DFA237_transition;
        }
        public String getDescription() {
            return "()* loopback of 1106:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA236_eotS =
        "\16\uffff";
    static final String DFA236_eofS =
        "\16\uffff";
    static final String DFA236_minS =
        "\2\6\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA236_maxS =
        "\1\77\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff";
    static final String DFA236_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA236_specialS =
        "\5\uffff\1\6\1\0\1\5\1\3\1\2\1\uffff\1\1\1\4\1\uffff}>";
    static final String[] DFA236_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2\6\uffff\1\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\6\uffff"+
            "\1\2\22\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
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

    static final short[] DFA236_eot = DFA.unpackEncodedString(DFA236_eotS);
    static final short[] DFA236_eof = DFA.unpackEncodedString(DFA236_eofS);
    static final char[] DFA236_min = DFA.unpackEncodedStringToUnsignedChars(DFA236_minS);
    static final char[] DFA236_max = DFA.unpackEncodedStringToUnsignedChars(DFA236_maxS);
    static final short[] DFA236_accept = DFA.unpackEncodedString(DFA236_acceptS);
    static final short[] DFA236_special = DFA.unpackEncodedString(DFA236_specialS);
    static final short[][] DFA236_transition;

    static {
        int numStates = DFA236_transitionS.length;
        DFA236_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA236_transition[i] = DFA.unpackEncodedString(DFA236_transitionS[i]);
        }
    }

    class DFA236 extends DFA {

        public DFA236(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 236;
            this.eot = DFA236_eot;
            this.eof = DFA236_eof;
            this.min = DFA236_min;
            this.max = DFA236_max;
            this.accept = DFA236_accept;
            this.special = DFA236_special;
            this.transition = DFA236_transition;
        }
        public String getDescription() {
            return "1108:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA236_6 = input.LA(1);

                         
                        int index236_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_6==WS||(LA236_6>=NL && LA236_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA236_6==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA236_6==COLON) ) {s = 2;}

                         
                        input.seek(index236_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA236_11 = input.LA(1);

                         
                        int index236_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_11==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA236_11==WS||(LA236_11>=NL && LA236_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA236_11==COLON) ) {s = 2;}

                         
                        input.seek(index236_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA236_9 = input.LA(1);

                         
                        int index236_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_9==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA236_9==WS||(LA236_9>=NL && LA236_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA236_9==IDENT||LA236_9==LBRACE||(LA236_9>=AND && LA236_9<=COLON)||(LA236_9>=MINUS && LA236_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index236_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA236_8 = input.LA(1);

                         
                        int index236_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_8==WS||(LA236_8>=NL && LA236_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA236_8==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA236_8==COLON) ) {s = 2;}

                         
                        input.seek(index236_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA236_12 = input.LA(1);

                         
                        int index236_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_12==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA236_12==WS||(LA236_12>=NL && LA236_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA236_12==IDENT||LA236_12==COMMA||LA236_12==LBRACE||LA236_12==GEN||LA236_12==COLON||(LA236_12>=PLUS && LA236_12<=PIPE)||LA236_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index236_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA236_7 = input.LA(1);

                         
                        int index236_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_7==WS||(LA236_7>=NL && LA236_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA236_7==LPAREN) && (synpred19_Css3())) {s = 13;}

                        else if ( (LA236_7==IDENT||LA236_7==COMMA||LA236_7==LBRACE||LA236_7==GEN||LA236_7==COLON||(LA236_7>=PLUS && LA236_7<=PIPE)||LA236_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index236_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA236_5 = input.LA(1);

                         
                        int index236_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA236_5==WS||(LA236_5>=NL && LA236_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA236_5==RBRACE) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA236_5==IDENT||LA236_5==LBRACE||(LA236_5>=AND && LA236_5<=COLON)||(LA236_5>=MINUS && LA236_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index236_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 236, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA241_eotS =
        "\4\uffff";
    static final String DFA241_eofS =
        "\4\uffff";
    static final String DFA241_minS =
        "\2\6\2\uffff";
    static final String DFA241_maxS =
        "\2\123\2\uffff";
    static final String DFA241_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA241_specialS =
        "\4\uffff}>";
    static final String[] DFA241_transitionS = {
            "\1\3\15\uffff\1\2\2\uffff\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\15\uffff\1\2\2\uffff\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA241_eot = DFA.unpackEncodedString(DFA241_eotS);
    static final short[] DFA241_eof = DFA.unpackEncodedString(DFA241_eofS);
    static final char[] DFA241_min = DFA.unpackEncodedStringToUnsignedChars(DFA241_minS);
    static final char[] DFA241_max = DFA.unpackEncodedStringToUnsignedChars(DFA241_maxS);
    static final short[] DFA241_accept = DFA.unpackEncodedString(DFA241_acceptS);
    static final short[] DFA241_special = DFA.unpackEncodedString(DFA241_specialS);
    static final short[][] DFA241_transition;

    static {
        int numStates = DFA241_transitionS.length;
        DFA241_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA241_transition[i] = DFA.unpackEncodedString(DFA241_transitionS[i]);
        }
    }

    class DFA241 extends DFA {

        public DFA241(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 241;
            this.eot = DFA241_eot;
            this.eof = DFA241_eof;
            this.min = DFA241_min;
            this.max = DFA241_max;
            this.accept = DFA241_accept;
            this.special = DFA241_special;
            this.transition = DFA241_transition;
        }
        public String getDescription() {
            return "()* loopback of 1124:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA242_eotS =
        "\16\uffff";
    static final String DFA242_eofS =
        "\16\uffff";
    static final String DFA242_minS =
        "\1\6\1\5\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA242_maxS =
        "\1\70\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff";
    static final String DFA242_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA242_specialS =
        "\5\uffff\1\6\1\0\1\5\1\3\1\2\1\uffff\1\1\1\4\1\uffff}>";
    static final String[] DFA242_transitionS = {
            "\1\2\52\uffff\1\2\3\uffff\1\2\1\1\2\2",
            "\2\2\6\uffff\1\3\1\2\10\uffff\1\2\31\uffff\1\2\3\uffff\4\2"+
            "\16\uffff\1\2\12\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
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

    static final short[] DFA242_eot = DFA.unpackEncodedString(DFA242_eotS);
    static final short[] DFA242_eof = DFA.unpackEncodedString(DFA242_eofS);
    static final char[] DFA242_min = DFA.unpackEncodedStringToUnsignedChars(DFA242_minS);
    static final char[] DFA242_max = DFA.unpackEncodedStringToUnsignedChars(DFA242_maxS);
    static final short[] DFA242_accept = DFA.unpackEncodedString(DFA242_acceptS);
    static final short[] DFA242_special = DFA.unpackEncodedString(DFA242_specialS);
    static final short[][] DFA242_transition;

    static {
        int numStates = DFA242_transitionS.length;
        DFA242_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA242_transition[i] = DFA.unpackEncodedString(DFA242_transitionS[i]);
        }
    }

    class DFA242 extends DFA {

        public DFA242(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 242;
            this.eot = DFA242_eot;
            this.eof = DFA242_eof;
            this.min = DFA242_min;
            this.max = DFA242_max;
            this.accept = DFA242_accept;
            this.special = DFA242_special;
            this.transition = DFA242_transition;
        }
        public String getDescription() {
            return "1137:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA242_6 = input.LA(1);

                         
                        int index242_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_6==WS||(LA242_6>=NL && LA242_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA242_6==RBRACE) && (synpred22_Css3())) {s = 10;}

                        else if ( (LA242_6==COLON) ) {s = 2;}

                         
                        input.seek(index242_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA242_11 = input.LA(1);

                         
                        int index242_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_11==RBRACE) && (synpred22_Css3())) {s = 10;}

                        else if ( (LA242_11==WS||(LA242_11>=NL && LA242_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA242_11==COLON) ) {s = 2;}

                         
                        input.seek(index242_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA242_9 = input.LA(1);

                         
                        int index242_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_9==RBRACE) && (synpred22_Css3())) {s = 10;}

                        else if ( (LA242_9==WS||(LA242_9>=NL && LA242_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA242_9==IDENT||LA242_9==LBRACE||(LA242_9>=AND && LA242_9<=COLON)||(LA242_9>=MINUS && LA242_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index242_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA242_8 = input.LA(1);

                         
                        int index242_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_8==WS||(LA242_8>=NL && LA242_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA242_8==RBRACE) && (synpred22_Css3())) {s = 10;}

                        else if ( (LA242_8==COLON) ) {s = 2;}

                         
                        input.seek(index242_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA242_12 = input.LA(1);

                         
                        int index242_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_12==LPAREN) && (synpred22_Css3())) {s = 13;}

                        else if ( (LA242_12==WS||(LA242_12>=NL && LA242_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA242_12==IDENT||LA242_12==COMMA||LA242_12==LBRACE||LA242_12==GEN||LA242_12==COLON||(LA242_12>=PLUS && LA242_12<=PIPE)||LA242_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index242_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA242_7 = input.LA(1);

                         
                        int index242_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_7==WS||(LA242_7>=NL && LA242_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA242_7==LPAREN) && (synpred22_Css3())) {s = 13;}

                        else if ( (LA242_7==IDENT||LA242_7==COMMA||LA242_7==LBRACE||LA242_7==GEN||LA242_7==COLON||(LA242_7>=PLUS && LA242_7<=PIPE)||LA242_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index242_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA242_5 = input.LA(1);

                         
                        int index242_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA242_5==WS||(LA242_5>=NL && LA242_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA242_5==RBRACE) && (synpred22_Css3())) {s = 10;}

                        else if ( (LA242_5==IDENT||LA242_5==LBRACE||(LA242_5>=AND && LA242_5<=COLON)||(LA242_5>=MINUS && LA242_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index242_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 242, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA244_eotS =
        "\16\uffff";
    static final String DFA244_eofS =
        "\16\uffff";
    static final String DFA244_minS =
        "\1\6\1\5\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA244_maxS =
        "\1\70\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff";
    static final String DFA244_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA244_specialS =
        "\5\uffff\1\6\1\0\1\5\1\3\1\2\1\uffff\1\1\1\4\1\uffff}>";
    static final String[] DFA244_transitionS = {
            "\1\2\52\uffff\1\2\3\uffff\1\2\1\1\2\2",
            "\2\2\6\uffff\1\3\1\2\10\uffff\1\2\31\uffff\1\2\3\uffff\4\2"+
            "\16\uffff\1\2\12\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\2\uffff\3\2\2\uffff\1\2",
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

    static final short[] DFA244_eot = DFA.unpackEncodedString(DFA244_eotS);
    static final short[] DFA244_eof = DFA.unpackEncodedString(DFA244_eofS);
    static final char[] DFA244_min = DFA.unpackEncodedStringToUnsignedChars(DFA244_minS);
    static final char[] DFA244_max = DFA.unpackEncodedStringToUnsignedChars(DFA244_maxS);
    static final short[] DFA244_accept = DFA.unpackEncodedString(DFA244_acceptS);
    static final short[] DFA244_special = DFA.unpackEncodedString(DFA244_specialS);
    static final short[][] DFA244_transition;

    static {
        int numStates = DFA244_transitionS.length;
        DFA244_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA244_transition[i] = DFA.unpackEncodedString(DFA244_transitionS[i]);
        }
    }

    class DFA244 extends DFA {

        public DFA244(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 244;
            this.eot = DFA244_eot;
            this.eof = DFA244_eof;
            this.min = DFA244_min;
            this.max = DFA244_max;
            this.accept = DFA244_accept;
            this.special = DFA244_special;
            this.transition = DFA244_transition;
        }
        public String getDescription() {
            return "1144:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA244_6 = input.LA(1);

                         
                        int index244_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_6==WS||(LA244_6>=NL && LA244_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA244_6==RBRACE) && (synpred23_Css3())) {s = 10;}

                        else if ( (LA244_6==COLON) ) {s = 2;}

                         
                        input.seek(index244_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA244_11 = input.LA(1);

                         
                        int index244_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_11==RBRACE) && (synpred23_Css3())) {s = 10;}

                        else if ( (LA244_11==WS||(LA244_11>=NL && LA244_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA244_11==COLON) ) {s = 2;}

                         
                        input.seek(index244_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA244_9 = input.LA(1);

                         
                        int index244_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_9==RBRACE) && (synpred23_Css3())) {s = 10;}

                        else if ( (LA244_9==WS||(LA244_9>=NL && LA244_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA244_9==IDENT||LA244_9==LBRACE||(LA244_9>=AND && LA244_9<=COLON)||(LA244_9>=MINUS && LA244_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index244_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA244_8 = input.LA(1);

                         
                        int index244_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_8==WS||(LA244_8>=NL && LA244_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA244_8==RBRACE) && (synpred23_Css3())) {s = 10;}

                        else if ( (LA244_8==COLON) ) {s = 2;}

                         
                        input.seek(index244_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA244_12 = input.LA(1);

                         
                        int index244_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_12==LPAREN) && (synpred23_Css3())) {s = 13;}

                        else if ( (LA244_12==WS||(LA244_12>=NL && LA244_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA244_12==IDENT||LA244_12==COMMA||LA244_12==LBRACE||LA244_12==GEN||LA244_12==COLON||(LA244_12>=PLUS && LA244_12<=PIPE)||LA244_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index244_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA244_7 = input.LA(1);

                         
                        int index244_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_7==WS||(LA244_7>=NL && LA244_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA244_7==LPAREN) && (synpred23_Css3())) {s = 13;}

                        else if ( (LA244_7==IDENT||LA244_7==COMMA||LA244_7==LBRACE||LA244_7==GEN||LA244_7==COLON||(LA244_7>=PLUS && LA244_7<=PIPE)||LA244_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index244_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA244_5 = input.LA(1);

                         
                        int index244_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA244_5==WS||(LA244_5>=NL && LA244_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA244_5==RBRACE) && (synpred23_Css3())) {s = 10;}

                        else if ( (LA244_5==IDENT||LA244_5==LBRACE||(LA244_5>=AND && LA244_5<=COLON)||(LA244_5>=MINUS && LA244_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index244_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 244, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA246_eotS =
        "\17\uffff";
    static final String DFA246_eofS =
        "\17\uffff";
    static final String DFA246_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA246_maxS =
        "\1\70\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA246_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA246_specialS =
        "\5\uffff\1\7\1\2\1\1\1\4\1\5\1\uffff\1\6\1\3\1\uffff\1\0}>";
    static final String[] DFA246_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\2\uffff\3\2\2\uffff\1\2",
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

    static final short[] DFA246_eot = DFA.unpackEncodedString(DFA246_eotS);
    static final short[] DFA246_eof = DFA.unpackEncodedString(DFA246_eofS);
    static final char[] DFA246_min = DFA.unpackEncodedStringToUnsignedChars(DFA246_minS);
    static final char[] DFA246_max = DFA.unpackEncodedStringToUnsignedChars(DFA246_maxS);
    static final short[] DFA246_accept = DFA.unpackEncodedString(DFA246_acceptS);
    static final short[] DFA246_special = DFA.unpackEncodedString(DFA246_specialS);
    static final short[][] DFA246_transition;

    static {
        int numStates = DFA246_transitionS.length;
        DFA246_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA246_transition[i] = DFA.unpackEncodedString(DFA246_transitionS[i]);
        }
    }

    class DFA246 extends DFA {

        public DFA246(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 246;
            this.eot = DFA246_eot;
            this.eof = DFA246_eof;
            this.min = DFA246_min;
            this.max = DFA246_max;
            this.accept = DFA246_accept;
            this.special = DFA246_special;
            this.transition = DFA246_transition;
        }
        public String getDescription() {
            return "1155:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA246_14 = input.LA(1);

                         
                        int index246_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_14==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( (LA246_14==WS||(LA246_14>=NL && LA246_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA246_14==IDENT||LA246_14==LBRACE||(LA246_14>=AND && LA246_14<=COLON)||(LA246_14>=MINUS && LA246_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index246_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA246_7 = input.LA(1);

                         
                        int index246_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_7==WS||(LA246_7>=NL && LA246_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA246_7==LPAREN) && (synpred24_Css3())) {s = 13;}

                        else if ( (LA246_7==IDENT||LA246_7==COMMA||LA246_7==LBRACE||LA246_7==GEN||LA246_7==COLON||(LA246_7>=PLUS && LA246_7<=PIPE)||LA246_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index246_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA246_6 = input.LA(1);

                         
                        int index246_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_6==WS||(LA246_6>=NL && LA246_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA246_6==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( (LA246_6==COLON) ) {s = 2;}

                         
                        input.seek(index246_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA246_12 = input.LA(1);

                         
                        int index246_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_12==LPAREN) && (synpred24_Css3())) {s = 13;}

                        else if ( (LA246_12==WS||(LA246_12>=NL && LA246_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA246_12==IDENT||LA246_12==COMMA||LA246_12==LBRACE||LA246_12==GEN||LA246_12==COLON||(LA246_12>=PLUS && LA246_12<=PIPE)||LA246_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index246_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA246_8 = input.LA(1);

                         
                        int index246_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_8==WS||(LA246_8>=NL && LA246_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA246_8==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( (LA246_8==IDENT||LA246_8==LBRACE||(LA246_8>=AND && LA246_8<=COLON)||(LA246_8>=MINUS && LA246_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index246_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA246_9 = input.LA(1);

                         
                        int index246_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_9==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( (LA246_9==WS) ) {s = 9;}

                        else if ( ((LA246_9>=IDENT && LA246_9<=STRING)||LA246_9==LBRACE||LA246_9==COLON) ) {s = 2;}

                        else if ( ((LA246_9>=NL && LA246_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index246_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA246_11 = input.LA(1);

                         
                        int index246_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_11==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( (LA246_11==WS||(LA246_11>=NL && LA246_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA246_11==COLON) ) {s = 2;}

                         
                        input.seek(index246_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA246_5 = input.LA(1);

                         
                        int index246_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA246_5==WS) ) {s = 9;}

                        else if ( (LA246_5==RBRACE) && (synpred24_Css3())) {s = 10;}

                        else if ( ((LA246_5>=IDENT && LA246_5<=STRING)||LA246_5==LBRACE||LA246_5==COLON) ) {s = 2;}

                        else if ( ((LA246_5>=NL && LA246_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index246_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 246, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA249_eotS =
        "\4\uffff";
    static final String DFA249_eofS =
        "\4\uffff";
    static final String DFA249_minS =
        "\2\6\2\uffff";
    static final String DFA249_maxS =
        "\2\123\2\uffff";
    static final String DFA249_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA249_specialS =
        "\4\uffff}>";
    static final String[] DFA249_transitionS = {
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA249_eot = DFA.unpackEncodedString(DFA249_eotS);
    static final short[] DFA249_eof = DFA.unpackEncodedString(DFA249_eofS);
    static final char[] DFA249_min = DFA.unpackEncodedStringToUnsignedChars(DFA249_minS);
    static final char[] DFA249_max = DFA.unpackEncodedStringToUnsignedChars(DFA249_maxS);
    static final short[] DFA249_accept = DFA.unpackEncodedString(DFA249_acceptS);
    static final short[] DFA249_special = DFA.unpackEncodedString(DFA249_specialS);
    static final short[][] DFA249_transition;

    static {
        int numStates = DFA249_transitionS.length;
        DFA249_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA249_transition[i] = DFA.unpackEncodedString(DFA249_transitionS[i]);
        }
    }

    class DFA249 extends DFA {

        public DFA249(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 249;
            this.eot = DFA249_eot;
            this.eof = DFA249_eof;
            this.min = DFA249_min;
            this.max = DFA249_max;
            this.accept = DFA249_accept;
            this.special = DFA249_special;
            this.transition = DFA249_transition;
        }
        public String getDescription() {
            return "()* loopback of 1160:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA248_eotS =
        "\17\uffff";
    static final String DFA248_eofS =
        "\17\uffff";
    static final String DFA248_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA248_maxS =
        "\1\70\1\123\1\uffff\2\152\5\123\1\uffff\2\123\1\uffff\1\123";
    static final String DFA248_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA248_specialS =
        "\5\uffff\1\7\1\2\1\1\1\4\1\5\1\uffff\1\6\1\3\1\uffff\1\0}>";
    static final String[] DFA248_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\2\uffff\3\2\2\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\2\uffff\3\2\2\uffff\1\2",
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
            return "1162:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA248_14 = input.LA(1);

                         
                        int index248_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_14==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( (LA248_14==WS||(LA248_14>=NL && LA248_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA248_14==IDENT||LA248_14==LBRACE||(LA248_14>=AND && LA248_14<=COLON)||(LA248_14>=MINUS && LA248_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index248_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA248_7 = input.LA(1);

                         
                        int index248_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_7==WS||(LA248_7>=NL && LA248_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA248_7==LPAREN) && (synpred25_Css3())) {s = 13;}

                        else if ( (LA248_7==IDENT||LA248_7==COMMA||LA248_7==LBRACE||LA248_7==GEN||LA248_7==COLON||(LA248_7>=PLUS && LA248_7<=PIPE)||LA248_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index248_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA248_6 = input.LA(1);

                         
                        int index248_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_6==WS||(LA248_6>=NL && LA248_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA248_6==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( (LA248_6==COLON) ) {s = 2;}

                         
                        input.seek(index248_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA248_12 = input.LA(1);

                         
                        int index248_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_12==LPAREN) && (synpred25_Css3())) {s = 13;}

                        else if ( (LA248_12==WS||(LA248_12>=NL && LA248_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA248_12==IDENT||LA248_12==COMMA||LA248_12==LBRACE||LA248_12==GEN||LA248_12==COLON||(LA248_12>=PLUS && LA248_12<=PIPE)||LA248_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index248_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA248_8 = input.LA(1);

                         
                        int index248_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_8==WS||(LA248_8>=NL && LA248_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA248_8==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( (LA248_8==IDENT||LA248_8==LBRACE||(LA248_8>=AND && LA248_8<=COLON)||(LA248_8>=MINUS && LA248_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index248_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA248_9 = input.LA(1);

                         
                        int index248_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_9==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( (LA248_9==WS) ) {s = 9;}

                        else if ( ((LA248_9>=IDENT && LA248_9<=STRING)||LA248_9==LBRACE||LA248_9==COLON) ) {s = 2;}

                        else if ( ((LA248_9>=NL && LA248_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index248_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA248_11 = input.LA(1);

                         
                        int index248_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_11==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( (LA248_11==WS||(LA248_11>=NL && LA248_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA248_11==COLON) ) {s = 2;}

                         
                        input.seek(index248_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA248_5 = input.LA(1);

                         
                        int index248_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA248_5==WS) ) {s = 9;}

                        else if ( (LA248_5==RBRACE) && (synpred25_Css3())) {s = 10;}

                        else if ( ((LA248_5>=IDENT && LA248_5<=STRING)||LA248_5==LBRACE||LA248_5==COLON) ) {s = 2;}

                        else if ( ((LA248_5>=NL && LA248_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index248_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 248, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA260_eotS =
        "\4\uffff";
    static final String DFA260_eofS =
        "\2\3\2\uffff";
    static final String DFA260_minS =
        "\2\5\2\uffff";
    static final String DFA260_maxS =
        "\2\152\2\uffff";
    static final String DFA260_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA260_specialS =
        "\4\uffff}>";
    static final String[] DFA260_transitionS = {
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\1\uffff\4\3\1\uffff\1\3",
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\1\uffff\4\3\1\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA260_eot = DFA.unpackEncodedString(DFA260_eotS);
    static final short[] DFA260_eof = DFA.unpackEncodedString(DFA260_eofS);
    static final char[] DFA260_min = DFA.unpackEncodedStringToUnsignedChars(DFA260_minS);
    static final char[] DFA260_max = DFA.unpackEncodedStringToUnsignedChars(DFA260_maxS);
    static final short[] DFA260_accept = DFA.unpackEncodedString(DFA260_acceptS);
    static final short[] DFA260_special = DFA.unpackEncodedString(DFA260_specialS);
    static final short[][] DFA260_transition;

    static {
        int numStates = DFA260_transitionS.length;
        DFA260_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA260_transition[i] = DFA.unpackEncodedString(DFA260_transitionS[i]);
        }
    }

    class DFA260 extends DFA {

        public DFA260(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 260;
            this.eot = DFA260_eot;
            this.eof = DFA260_eof;
            this.min = DFA260_min;
            this.max = DFA260_max;
            this.accept = DFA260_accept;
            this.special = DFA260_special;
            this.transition = DFA260_transition;
        }
        public String getDescription() {
            return "1221:59: ( ( ws )? sass_else )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA266_eotS =
        "\5\uffff";
    static final String DFA266_eofS =
        "\5\uffff";
    static final String DFA266_minS =
        "\1\143\2\6\2\uffff";
    static final String DFA266_maxS =
        "\1\143\2\123\2\uffff";
    static final String DFA266_acceptS =
        "\3\uffff\1\1\1\2";
    static final String DFA266_specialS =
        "\5\uffff}>";
    static final String[] DFA266_transitionS = {
            "\1\1",
            "\1\4\6\uffff\1\3\11\uffff\1\2\72\uffff\2\2",
            "\1\4\6\uffff\1\3\11\uffff\1\2\72\uffff\2\2",
            "",
            ""
    };

    static final short[] DFA266_eot = DFA.unpackEncodedString(DFA266_eotS);
    static final short[] DFA266_eof = DFA.unpackEncodedString(DFA266_eofS);
    static final char[] DFA266_min = DFA.unpackEncodedStringToUnsignedChars(DFA266_minS);
    static final char[] DFA266_max = DFA.unpackEncodedStringToUnsignedChars(DFA266_maxS);
    static final short[] DFA266_accept = DFA.unpackEncodedString(DFA266_acceptS);
    static final short[] DFA266_special = DFA.unpackEncodedString(DFA266_specialS);
    static final short[][] DFA266_transition;

    static {
        int numStates = DFA266_transitionS.length;
        DFA266_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA266_transition[i] = DFA.unpackEncodedString(DFA266_transitionS[i]);
        }
    }

    class DFA266 extends DFA {

        public DFA266(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 266;
            this.eot = DFA266_eot;
            this.eof = DFA266_eof;
            this.min = DFA266_min;
            this.max = DFA266_max;
            this.accept = DFA266_accept;
            this.special = DFA266_special;
            this.transition = DFA266_transition;
        }
        public String getDescription() {
            return "1224:1: sass_else : ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA265_eotS =
        "\4\uffff";
    static final String DFA265_eofS =
        "\2\3\2\uffff";
    static final String DFA265_minS =
        "\2\5\2\uffff";
    static final String DFA265_maxS =
        "\2\152\2\uffff";
    static final String DFA265_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA265_specialS =
        "\4\uffff}>";
    static final String[] DFA265_transitionS = {
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\1\uffff\4\3\1\uffff\1\3",
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\1\uffff\4\3\1\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA265_eot = DFA.unpackEncodedString(DFA265_eotS);
    static final short[] DFA265_eof = DFA.unpackEncodedString(DFA265_eofS);
    static final char[] DFA265_min = DFA.unpackEncodedStringToUnsignedChars(DFA265_minS);
    static final char[] DFA265_max = DFA.unpackEncodedStringToUnsignedChars(DFA265_maxS);
    static final short[] DFA265_accept = DFA.unpackEncodedString(DFA265_acceptS);
    static final short[] DFA265_special = DFA.unpackEncodedString(DFA265_specialS);
    static final short[][] DFA265_transition;

    static {
        int numStates = DFA265_transitionS.length;
        DFA265_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA265_transition[i] = DFA.unpackEncodedString(DFA265_transitionS[i]);
        }
    }

    class DFA265 extends DFA {

        public DFA265(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 265;
            this.eot = DFA265_eot;
            this.eof = DFA265_eof;
            this.min = DFA265_min;
            this.max = DFA265_max;
            this.accept = DFA265_accept;
            this.special = DFA265_special;
            this.transition = DFA265_transition;
        }
        public String getDescription() {
            return "1228:129: ( ( ws )? sass_else )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA281_eotS =
        "\11\uffff";
    static final String DFA281_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA281_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA281_maxS =
        "\3\u008f\2\uffff\4\u008f";
    static final String DFA281_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA281_specialS =
        "\11\uffff}>";
    static final String[] DFA281_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\127"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\74\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\74\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\u0081\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\74\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\74\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\74\3"
    };

    static final short[] DFA281_eot = DFA.unpackEncodedString(DFA281_eotS);
    static final short[] DFA281_eof = DFA.unpackEncodedString(DFA281_eofS);
    static final char[] DFA281_min = DFA.unpackEncodedStringToUnsignedChars(DFA281_minS);
    static final char[] DFA281_max = DFA.unpackEncodedStringToUnsignedChars(DFA281_maxS);
    static final short[] DFA281_accept = DFA.unpackEncodedString(DFA281_acceptS);
    static final short[] DFA281_special = DFA.unpackEncodedString(DFA281_specialS);
    static final short[][] DFA281_transition;

    static {
        int numStates = DFA281_transitionS.length;
        DFA281_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA281_transition[i] = DFA.unpackEncodedString(DFA281_transitionS[i]);
        }
    }

    class DFA281 extends DFA {

        public DFA281(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 281;
            this.eot = DFA281_eot;
            this.eof = DFA281_eof;
            this.min = DFA281_min;
            this.max = DFA281_max;
            this.accept = DFA281_accept;
            this.special = DFA281_special;
            this.transition = DFA281_transition;
        }
        public String getDescription() {
            return "376:17: synpred3_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0xBFE00001D1541650L,0x000001E700E00000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0xBFE00001D1D41450L,0x000001E700EC0000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0xBFE00001D1541450L,0x000001E700E00000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0xBFE00001D1541450L,0x000001E700E00000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0xBFE00001D1541440L,0x000001E700E00000L});
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
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem404 = new BitSet(new long[]{0x00000000008F0860L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem406 = new BitSet(new long[]{0x00000000000F0860L});
    public static final BitSet FOLLOW_COMMA_in_importItem410 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_importItem412 = new BitSet(new long[]{0x00000000008001C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem415 = new BitSet(new long[]{0x00000000000F0860L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem419 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media437 = new BitSet(new long[]{0x01E00000009FA040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_media439 = new BitSet(new long[]{0x01E00000001FA040L});
    public static final BitSet FOLLOW_scss_mq_interpolation_expression_in_media494 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_media496 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_mediaQueryList_in_media530 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media559 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media561 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_declaration_in_media647 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_media649 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media651 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_sass_extend_in_media674 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media676 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_sass_debug_in_media699 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media701 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_sass_control_in_media724 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media726 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_sass_content_in_media749 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media751 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_rule_in_media789 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media792 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_page_in_media813 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media816 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_fontFace_in_media837 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media840 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media861 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media864 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_media_in_media887 = new BitSet(new long[]{0xBFE0000151D45040L,0x000004E7406C0000L});
    public static final BitSet FOLLOW_ws_in_media889 = new BitSet(new long[]{0xBFE0000151545040L,0x000004E740600000L});
    public static final BitSet FOLLOW_RBRACE_in_media933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList949 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList953 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList955 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList958 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery977 = new BitSet(new long[]{0x0000000000870040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery979 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery986 = new BitSet(new long[]{0x0000000000808002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery988 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery993 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery995 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery998 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery1006 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery1010 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery1012 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery1015 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression1070 = new BitSet(new long[]{0x0000000000C41040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1072 = new BitSet(new long[]{0x0000000000C41040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression1075 = new BitSet(new long[]{0x0000000000B00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1077 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression1082 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1084 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_mediaExpression1087 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression1092 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature1110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GEN_in_mediaFeature1114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_mediaFeature1120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body1136 = new BitSet(new long[]{0xBFE00001D1D41442L,0x000001E700EC0000L});
    public static final BitSet FOLLOW_ws_in_body1138 = new BitSet(new long[]{0xBFE00001D1541442L,0x000001E700E00000L});
    public static final BitSet FOLLOW_rule_in_bodyItem1163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem1175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem1187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem1199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem1211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem1223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_bodyItem1265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_debug_in_bodyItem1280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_control_in_bodyItem1294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_function_declaration_in_bodyItem1308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule1335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule1339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule1375 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1377 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule1382 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1384 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule1399 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule1411 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule1421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1437 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1439 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1444 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1446 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document1452 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1454 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1457 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1459 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document1466 = new BitSet(new long[]{0xBFE00001D1D45440L,0x000001E700EC0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1468 = new BitSet(new long[]{0xBFE00001D1545440L,0x000001E700E00000L});
    public static final BitSet FOLLOW_body_in_moz_document1473 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1519 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1521 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1524 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1526 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1531 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1533 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1540 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1542 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1562 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1564 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1569 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1572 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1575 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1579 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1597 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1609 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1612 = new BitSet(new long[]{0x0000000020800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1614 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1617 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1646 = new BitSet(new long[]{0x0000000000902040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1648 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1653 = new BitSet(new long[]{0x0000000000902000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1655 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1662 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1664 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1677 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1679 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1734 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1736 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1738 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1744 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1746 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1750 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1752 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1754 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1790 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1792 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1795 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1797 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1808 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1810 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1813 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1817 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1848 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1850 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1861 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1863 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1866 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1870 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1895 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_margin1897 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1900 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_margin1902 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1905 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_margin1907 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage2138 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage2140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator2190 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator2201 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator2212 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_property2321 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_property2333 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_property2346 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_property2361 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_property2369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule2418 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule2451 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule2474 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_rule2476 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule2479 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_rule2493 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule2503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_declarations2569 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2571 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2652 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2654 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2656 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2741 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2743 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2745 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2758 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2760 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_rule_in_declarations2787 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2789 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_sass_extend_in_declarations2828 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2830 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_sass_debug_in_declarations2869 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2871 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_sass_control_in_declarations2910 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2912 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_media_in_declarations2951 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2953 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2992 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2994 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_sass_content_in_declarations3033 = new BitSet(new long[]{0xBFE0000000D41062L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations3035 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncTo_SEMI_in_declarations3080 = new BitSet(new long[]{0xBFE0000000541062L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations3110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup3170 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup3172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup3187 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup3190 = new BitSet(new long[]{0xBFE0000000940040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup3192 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup3195 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector3222 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_combinator_in_selector3225 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector3227 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence3260 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence3267 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence3269 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence3288 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence3290 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector3406 = new BitSet(new long[]{0xB000000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector3412 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_typeSelector3414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix3432 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix3436 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix3440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_extend_only_selector_in_elementSubsequent3479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent3488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent3497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent3509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent3521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId3549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId3555 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId3557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass3585 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass3587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute3659 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute3666 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3669 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute3680 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C007FL});
    public static final BitSet FOLLOW_ws_in_slAttribute3682 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_slAttribute3724 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3904 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute3923 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0040L});
    public static final BitSet FOLLOW_ws_in_slAttribute3941 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue4000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo4060 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo4124 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4181 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo4184 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_pseudo4186 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_pseudo4191 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo4195 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo4200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo4279 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4281 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo4284 = new BitSet(new long[]{0xBFE0000000B40040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4286 = new BitSet(new long[]{0xBFE0000000340040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo4289 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo4292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration4331 = new BitSet(new long[]{0x11E0000000441040L,0x0000000000200000L});
    public static final BitSet FOLLOW_property_in_declaration4334 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration4336 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_declaration4338 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_declaration4341 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_prio_in_declaration4344 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_declaration4346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_property_value_interpolation_expression_in_propertyValue4412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue4428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue4469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4507 = new BitSet(new long[]{0xEFFDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_set_in_expressionPredicate4536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI4654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio4709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression4730 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_operator_in_expression4735 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_expression4737 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_expression4742 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_unaryOperator_in_term4767 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_term4769 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_set_in_term4793 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_term4993 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_term5001 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_term5009 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_term5017 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_term5025 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_term5033 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_term5043 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_term5055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function5071 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function5073 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function5078 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function5080 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_function5090 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function5108 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function5111 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function5113 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_fnAttribute_in_function5116 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function5174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName5222 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName5224 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName5228 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName5231 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName5233 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute5256 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0001L});
    public static final BitSet FOLLOW_ws_in_fnAttribute5258 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute5261 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_fnAttribute5263 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute5266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName5281 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName5284 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName5286 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue5300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor5318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws5339 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5387 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5389 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5392 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5394 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_variable_value_in_cp_variable_declaration5397 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5426 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5428 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5431 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5433 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_variable_value_in_cp_variable_declaration5436 = new BitSet(new long[]{0x0000000000000020L,0x0000000000100000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5439 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5441 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable5479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable5511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_value5535 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_cp_variable_value5539 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_value5541 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_value5544 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression5572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5592 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp5606 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5608 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5611 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp5624 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5626 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5629 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5662 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp5675 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5677 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5680 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp5694 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5696 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5699 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5732 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5739 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp5753 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5755 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp5758 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp5760 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term5800 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_cp_term6000 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term6008 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_cp_term6016 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_cp_term6024 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term6032 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_cp_term6040 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term6048 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_term6060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration6091 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration6093 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6095 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration6098 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_cp_mixin_declaration6100 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration6103 = new BitSet(new long[]{0x0000000000800002L,0x00000000040C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6105 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration6109 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration6128 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6130 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration6132 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6134 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration6138 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_cp_mixin_declaration6140 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration6143 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call6187 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call6189 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call6211 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6213 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call6215 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6228 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call6231 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6233 = new BitSet(new long[]{0x00A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call6236 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call6239 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6243 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call6246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name6275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6311 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args6315 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args6323 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6326 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_arg6358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_arg_in_cp_args_list6395 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_args_list6399 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_cp_args_list6409 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_arg_in_cp_args_list6412 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_args_list6418 = new BitSet(new long[]{0x0000000000800000L,0x00000000030C0000L});
    public static final BitSet FOLLOW_ws_in_cp_args_list6428 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_set_in_cp_args_list6431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_args_list6453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_arg6485 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_cp_arg6489 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_arg6491 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_arg6494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded6520 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6522 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6525 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded6529 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6537 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6540 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition6570 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6572 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition6581 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6583 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition6609 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6611 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition6642 = new BitSet(new long[]{0x0008000000A00000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_ws_in_less_condition6645 = new BitSet(new long[]{0x0008000000800000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition6648 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_condition6650 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition6653 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition6682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition6708 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6710 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition6713 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6715 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition6718 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6720 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition6723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name6745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6844 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6872 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression6933 = new BitSet(new long[]{0x81E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6972 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression7008 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7110 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression7138 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression7191 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7230 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression7266 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7356 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_property_value_interpolation_expression7384 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_property_value_interpolation_expression7441 = new BitSet(new long[]{0x01E2000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7480 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_property_value_interpolation_expression7516 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7614 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7642 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_mq_interpolation_expression7707 = new BitSet(new long[]{0x01E0000000128040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7746 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7782 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7867 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var7869 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7871 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var7876 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7880 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7884 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var7887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties7931 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties7933 = new BitSet(new long[]{0x01E6000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7935 = new BitSet(new long[]{0x01E6000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties7938 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties7941 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7943 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties7946 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties7948 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties7950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_in_sass_extend7971 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7973 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_sass_extend7975 = new BitSet(new long[]{0x0000000000000020L,0x0000000080000000L});
    public static final BitSet FOLLOW_SASS_OPTIONAL_in_sass_extend7978 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend7980 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_extend7985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector8010 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_sass_debug8031 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_debug8041 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_debug8043 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_debug8045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_if_in_sass_control8070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_for_in_sass_control8074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_each_in_sass_control8078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_while_in_sass_control8082 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_IF_in_sass_if8103 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_if8105 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_if8107 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_if8109 = new BitSet(new long[]{0x0000000000800002L,0x00000008000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_if8112 = new BitSet(new long[]{0x0000000000800000L,0x00000008000C0000L});
    public static final BitSet FOLLOW_sass_else_in_sass_if8115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_ELSE_in_sass_else8142 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8144 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_else8147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_ELSE_in_sass_else8160 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8162 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_else8167 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_else8171 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_else8174 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_else8176 = new BitSet(new long[]{0x0000000000800002L,0x00000008000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8179 = new BitSet(new long[]{0x0000000000800000L,0x00000008000C0000L});
    public static final BitSet FOLLOW_sass_else_in_sass_else8182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_sass_control_expression8205 = new BitSet(new long[]{0x0008000000000002L,0x0000001038000000L});
    public static final BitSet FOLLOW_set_in_sass_control_expression8208 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_control_expression8229 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_control_expression8232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_FOR_in_sass_for8255 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8257 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_sass_for8259 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8261 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_for8263 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8267 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_for8269 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_for8271 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8275 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_for8277 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_for8279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EACH_in_sass_each8300 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8302 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_sass_each8304 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8306 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_each8308 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8312 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_sass_each_list_in_sass_each8314 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_each8316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_term_in_sass_each_list8341 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_sass_each_list8344 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_each_list8346 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_each_list8349 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_SASS_WHILE_in_sass_while8376 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_while8378 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_while8380 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_while8382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_sass_control_block8403 = new BitSet(new long[]{0xBFE0000000D45060L,0x000004E740EC0000L});
    public static final BitSet FOLLOW_ws_in_sass_control_block8405 = new BitSet(new long[]{0xBFE0000000545060L,0x000004E740E00000L});
    public static final BitSet FOLLOW_declarations_in_sass_control_block8408 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_sass_control_block8410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_FUNCTION_in_sass_function_declaration8446 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8448 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_sass_function_name_in_sass_function_declaration8450 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8452 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_sass_function_declaration8455 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_sass_function_declaration8457 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_sass_function_declaration8460 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8462 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_sass_function_declaration8465 = new BitSet(new long[]{0x0000000000800000L,0x00000200000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8467 = new BitSet(new long[]{0x0000000000800000L,0x00000200000C0000L});
    public static final BitSet FOLLOW_sass_function_return_in_sass_function_declaration8470 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8472 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_sass_function_declaration8475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_sass_function_name8500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_RETURN_in_sass_function_return8521 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_return8523 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_function_return8525 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_function_return8527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_CONTENT_in_sass_content8552 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_content8554 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_content8557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css3476 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred1_Css3488 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css3490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQueryList_in_synpred2_Css3527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3613 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3625 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_set_in_synpred3_Css3627 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred3_Css3637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3641 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32308 = new BitSet(new long[]{0xFFFFFFFFFFEFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred4_Css32316 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred4_Css32318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_synpred5_Css32566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_synpred6_Css32647 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_synpred6_Css32649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred7_Css32714 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_COLON_in_synpred7_Css32726 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_set_in_synpred7_Css32728 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred7_Css32738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_nested_properties_in_synpred8_Css32755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_synpred9_Css32784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred10_Css33073 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred10_Css33077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred11_Css33152 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred11_Css33164 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred11_Css33166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred12_Css33264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred13_Css33285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred14_Css33394 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred14_Css33403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred15_Css34399 = new BitSet(new long[]{0xFFFFFFFFFFFFFFD0L,0xFFFFFFFFFFFFFFFFL,0x000000000000FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred15_Css34407 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred15_Css34409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred16_Css34425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred17_Css35736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred18_Css36839 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred18_Css36841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred19_Css36967 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred19_Css36969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred20_Css37105 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred20_Css37107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred21_Css37225 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred21_Css37227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred22_Css37351 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred22_Css37353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred23_Css37475 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred23_Css37477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred24_Css37609 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred24_Css37611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred25_Css37741 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred25_Css37743 = new BitSet(new long[]{0x0000000000000002L});

}