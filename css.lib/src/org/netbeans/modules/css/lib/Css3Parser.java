// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-20 15:40:06

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "SASS_EXTEND_ONLY_SELECTOR", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "SASS_EXTEND", "SASS_OPTIONAL", "SASS_DEBUG", "SASS_WARN", "SASS_IF", "SASS_ELSE", "OR", "CP_EQ", "CP_NOT_EQ", "SASS_FOR", "SASS_EACH", "SASS_WHILE", "SASS_FUNCTION", "SASS_RETURN", "SASS_CONTENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
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
    public static final int OR=100;
    public static final int CP_EQ=101;
    public static final int CP_NOT_EQ=102;
    public static final int SASS_FOR=103;
    public static final int SASS_EACH=104;
    public static final int SASS_WHILE=105;
    public static final int SASS_FUNCTION=106;
    public static final int SASS_RETURN=107;
    public static final int SASS_CONTENT=108;
    public static final int HEXCHAR=109;
    public static final int NONASCII=110;
    public static final int UNICODE=111;
    public static final int ESCAPE=112;
    public static final int NMSTART=113;
    public static final int NMCHAR=114;
    public static final int URL=115;
    public static final int A=116;
    public static final int B=117;
    public static final int C=118;
    public static final int D=119;
    public static final int E=120;
    public static final int F=121;
    public static final int G=122;
    public static final int H=123;
    public static final int I=124;
    public static final int J=125;
    public static final int K=126;
    public static final int L=127;
    public static final int M=128;
    public static final int N=129;
    public static final int O=130;
    public static final int P=131;
    public static final int Q=132;
    public static final int R=133;
    public static final int S=134;
    public static final int T=135;
    public static final int U=136;
    public static final int V=137;
    public static final int W=138;
    public static final int X=139;
    public static final int Y=140;
    public static final int Z=141;
    public static final int CDO=142;
    public static final int CDC=143;
    public static final int INVALID=144;
    public static final int LINE_COMMENT=145;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "declarations", "cp_multiplyExp", "esPred", "charSetValue", 
        "rule", "cp_mixin_call_args", "scss_declaration_interpolation_expression", 
        "elementSubsequent", "scss_declaration_property_value_interpolation_expression", 
        "cp_additionExp", "synpred17_Css3", "synpred13_Css3", "sass_control_expression", 
        "cp_arg", "sass_content", "prio", "sass_function_return", "counterStyle", 
        "namespacePrefix", "synpred15_Css3", "sass_debug", "fontFace", "pseudoPage", 
        "fnAttributeValue", "hexColor", "cp_variable_declaration", "scss_selector_interpolation_expression", 
        "synpred8_Css3", "cp_variable", "cp_variable_value", "atRuleId", 
        "cp_args_list", "charSet", "synpred9_Css3", "typeSelector", "resourceIdentifier", 
        "less_mixin_guarded", "namespaces", "body", "sass_each", "cp_atomExp", 
        "page", "synpred6_Css3", "sass_if", "cp_term", "margin", "selectorsGroup", 
        "namespacePrefixName", "synpred4_Css3", "unaryOperator", "mediaQuery", 
        "vendorAtRule", "function", "synpred14_Css3", "fnAttribute", "cssId", 
        "slAttributeValue", "propertyValue", "fnAttributeName", "cp_mixin_declaration", 
        "sass_extend", "scss_interpolation_expression_var", "combinator", 
        "sass_for", "imports", "cp_mixin_name", "syncToDeclarationsRule", 
        "slAttribute", "cp_mixin_call", "namespace", "cp_expression", "elementName", 
        "synpred7_Css3", "sass_while", "synpred2_Css3", "scss_nested_properties", 
        "importItem", "cssClass", "syncToFollow", "synpred3_Css3", "sass_extend_only_selector", 
        "mediaQueryList", "sass_function_declaration", "property", "slAttributeName", 
        "less_fn_name", "synpred16_Css3", "syncTo_SEMI", "cp_mixin_call_arg", 
        "margin_sym", "bodyItem", "less_condition", "mediaFeature", "webkitKeyframes", 
        "sass_function_name", "pseudo", "moz_document_function", "ws", "syncTo_RBRACE", 
        "selector", "mediaExpression", "operator", "expressionPredicate", 
        "simpleSelectorSequence", "sass_control_block", "moz_document", 
        "synpred19_Css3", "synpred5_Css3", "scss_mq_interpolation_expression", 
        "less_condition_operator", "generic_at_rule", "synpred18_Css3", 
        "less_function_in_condition", "declaration", "synpred1_Css3", "functionName", 
        "term", "sass_control_expression_condition", "media", "webkitKeyframeSelectors", 
        "synpred12_Css3", "sass_else", "sass_control", "synpred10_Css3", 
        "synpred11_Css3", "webkitKeyframesBlock", "styleSheet", "mediaQueryOperator", 
        "sass_each_list", "expression", "mediaType"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, false, false, false, false, true, false, false, true, 
            false, true, false, true, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
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
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | media ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | media ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | media ( ws )? )* RBRACE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | media ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:19: media ( ws )?
            	    {
            	    dbg.location(389,19);
            	    pushFollow(FOLLOW_media_in_media936);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(389,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:25: ws
            	            {
            	            dbg.location(389,25);
            	            pushFollow(FOLLOW_ws_in_media938);
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

            dbg.location(393,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media983); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(394, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(397,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(397,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList999);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(397,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:17: ( COMMA ( ws )? mediaQuery )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(397,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList1003); if (state.failed) return ;
                    	    dbg.location(397,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:25: ws
                    	            {
                    	            dbg.location(397,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList1005);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(39);}

                    	    dbg.location(397,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList1008);
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
        dbg.location(398, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(400, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(401,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:4: ( mediaQueryOperator ( ws )? )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(401,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery1027);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(401,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:24: ws
                                    {
                                    dbg.location(401,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery1029);
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

                    dbg.location(401,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery1036);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(401,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:42: ws
                            {
                            dbg.location(401,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery1038);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(44);}

                    dbg.location(401,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:46: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(401,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery1043); if (state.failed) return ;
                    	    dbg.location(401,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:52: ws
                    	            {
                    	            dbg.location(401,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery1045);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(45);}

                    	    dbg.location(401,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery1048);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(402,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery1056);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(402,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:20: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(402,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery1060); if (state.failed) return ;
                    	    dbg.location(402,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:26: ws
                    	            {
                    	            dbg.location(402,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery1062);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(47);}

                    	    dbg.location(402,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery1065);
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
        dbg.location(403, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(406,3);
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
        dbg.location(407, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(410,2);
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
        dbg.location(411, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(414,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression1120); if (state.failed) return ;
            dbg.location(414,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:14: ws
                    {
                    dbg.location(414,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression1122);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}

            dbg.location(414,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression1125);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(414,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:31: ws
                    {
                    dbg.location(414,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression1127);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}

            dbg.location(414,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:35: ( COLON ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:37: COLON ( ws )? expression
                    {
                    dbg.location(414,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression1132); if (state.failed) return ;
                    dbg.location(414,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:43: ws
                            {
                            dbg.location(414,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression1134);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(52);}

                    dbg.location(414,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression1137);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(414,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression1142); if (state.failed) return ;
            dbg.location(414,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:68: ws
                    {
                    dbg.location(414,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression1144);
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
        dbg.location(415, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:1: mediaFeature : ( IDENT | GEN | {...}? cp_variable );
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:2: ( IDENT | GEN | {...}? cp_variable )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:4: IDENT
                    {
                    dbg.location(418,4);
                    match(input,IDENT,FOLLOW_IDENT_in_mediaFeature1160); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:12: GEN
                    {
                    dbg.location(418,12);
                    match(input,GEN,FOLLOW_GEN_in_mediaFeature1164); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:18: {...}? cp_variable
                    {
                    dbg.location(418,18);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "mediaFeature", "isCssPreprocessorSource()");
                    }
                    dbg.location(418,47);
                    pushFollow(FOLLOW_cp_variable_in_mediaFeature1170);
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
        dbg.location(419, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:2: ( bodyItem ( ws )? )+
            {
            dbg.location(422,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:2: ( bodyItem ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:4: bodyItem ( ws )?
            	    {
            	    dbg.location(422,4);
            	    pushFollow(FOLLOW_bodyItem_in_body1186);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(422,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:13: ws
            	            {
            	            dbg.location(422,13);
            	            pushFollow(FOLLOW_ws_in_body1188);
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
        dbg.location(423, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:1: bodyItem : ( ( cp_mixin_call )=> cp_mixin_call | rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(425, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:5: ( ( cp_mixin_call )=> cp_mixin_call | rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:9: ( cp_mixin_call )=> cp_mixin_call
                    {
                    dbg.location(429,26);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem1230);
                    cp_mixin_call();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:8: rule
                    {
                    dbg.location(430,8);
                    pushFollow(FOLLOW_rule_in_bodyItem1239);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:11: media
                    {
                    dbg.location(431,11);
                    pushFollow(FOLLOW_media_in_bodyItem1251);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:11: page
                    {
                    dbg.location(432,11);
                    pushFollow(FOLLOW_page_in_bodyItem1263);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:11: counterStyle
                    {
                    dbg.location(433,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem1275);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:11: fontFace
                    {
                    dbg.location(434,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem1287);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:11: vendorAtRule
                    {
                    dbg.location(435,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem1299);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:11: {...}? cp_variable_declaration
                    {
                    dbg.location(436,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(436,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem1313);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:11: {...}? importItem
                    {
                    dbg.location(437,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(437,40);
                    pushFollow(FOLLOW_importItem_in_bodyItem1327);
                    importItem();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 10 :
                    dbg.enterAlt(10);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:11: {...}? sass_debug
                    {
                    dbg.location(438,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(438,29);
                    pushFollow(FOLLOW_sass_debug_in_bodyItem1342);
                    sass_debug();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 11 :
                    dbg.enterAlt(11);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:11: {...}? sass_control
                    {
                    dbg.location(439,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(439,29);
                    pushFollow(FOLLOW_sass_control_in_bodyItem1356);
                    sass_control();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 12 :
                    dbg.enterAlt(12);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:11: {...}? sass_function_declaration
                    {
                    dbg.location(440,11);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isScssSource()");
                    }
                    dbg.location(440,29);
                    pushFollow(FOLLOW_sass_function_declaration_in_bodyItem1370);
                    sass_function_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(NL)); 
                
        }
        finally {
        }
        dbg.location(441, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(446, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:1: ( moz_document | webkitKeyframes | generic_at_rule )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:3: moz_document
                    {
                    dbg.location(447,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule1392);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:18: webkitKeyframes
                    {
                    dbg.location(447,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule1396);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:36: generic_at_rule
                    {
                    dbg.location(447,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule1400);
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
        dbg.location(447, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(450,2);
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
        dbg.location(452, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(454, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(455,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule1436); if (state.failed) return ;
            dbg.location(455,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:16: WS
            	    {
            	    dbg.location(455,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule1438); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);
            } finally {dbg.exitSubRule(60);}

            dbg.location(455,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:20: ( atRuleId ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:22: atRuleId ( WS )*
                    {
                    dbg.location(455,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule1443);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(455,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:31: WS
                    	    {
                    	    dbg.location(455,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule1445); if (state.failed) return ;

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

            dbg.location(456,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule1460); if (state.failed) return ;
            dbg.location(457,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule1472);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(458,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule1482); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(459, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(460, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(462,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1498); if (state.failed) return ;
            dbg.location(462,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:19: ws
                    {
                    dbg.location(462,19);
                    pushFollow(FOLLOW_ws_in_moz_document1500);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(462,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:25: moz_document_function ( ws )?
            {
            dbg.location(462,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document1505);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(462,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:47: ws
                    {
                    dbg.location(462,47);
                    pushFollow(FOLLOW_ws_in_moz_document1507);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}


            }

            dbg.location(462,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(462,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document1513); if (state.failed) return ;
            	    dbg.location(462,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:60: ws
            	            {
            	            dbg.location(462,60);
            	            pushFollow(FOLLOW_ws_in_moz_document1515);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(65);}

            	    dbg.location(462,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document1518);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(462,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:86: ws
            	            {
            	            dbg.location(462,86);
            	            pushFollow(FOLLOW_ws_in_moz_document1520);
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

            dbg.location(463,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document1527); if (state.failed) return ;
            dbg.location(463,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:9: ws
                    {
                    dbg.location(463,9);
                    pushFollow(FOLLOW_ws_in_moz_document1529);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(464,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:3: ( body )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:3: body
                    {
                    dbg.location(464,3);
                    pushFollow(FOLLOW_body_in_moz_document1534);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(465,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document1539); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(466, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(468, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(469,2);
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
        dbg.location(471, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(474, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(476,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1580); if (state.failed) return ;
            dbg.location(476,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:23: ws
                    {
                    dbg.location(476,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1582);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(476,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1585);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(476,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:36: ws
                    {
                    dbg.location(476,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1587);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(477,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1592); if (state.failed) return ;
            dbg.location(477,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:9: ws
                    {
                    dbg.location(477,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1594);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(478,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:3: ( webkitKeyframesBlock ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(478,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1601);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(478,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:26: ws
            	            {
            	            dbg.location(478,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1603);
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

            dbg.location(479,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1610); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(480, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(482, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(484,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1623);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(484,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:26: ws
                    {
                    dbg.location(484,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1625);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(75);}

            dbg.location(486,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1630); if (state.failed) return ;
            dbg.location(486,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:10: ws
                    {
                    dbg.location(486,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1633);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(486,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1636);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(487,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1640);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(488,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1643); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(489, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(491, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(493,2);
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

            dbg.location(493,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(493,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:27: ws
            	            {
            	            dbg.location(493,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1670);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(77);}

            	    dbg.location(493,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1673); if (state.failed) return ;
            	    dbg.location(493,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:37: ws
            	            {
            	            dbg.location(493,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1675);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(78);}

            	    dbg.location(493,41);
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
        dbg.location(494, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(496, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(497,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1707); if (state.failed) return ;
            dbg.location(497,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ws
                    {
                    dbg.location(497,16);
                    pushFollow(FOLLOW_ws_in_page1709);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(497,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:20: ( IDENT ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:22: IDENT ( ws )?
                    {
                    dbg.location(497,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1714); if (state.failed) return ;
                    dbg.location(497,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:28: ws
                            {
                            dbg.location(497,28);
                            pushFollow(FOLLOW_ws_in_page1716);
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

            dbg.location(497,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:35: ( pseudoPage ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:36: pseudoPage ( ws )?
                    {
                    dbg.location(497,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1723);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(497,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:47: ws
                            {
                            dbg.location(497,47);
                            pushFollow(FOLLOW_ws_in_page1725);
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

            dbg.location(498,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1738); if (state.failed) return ;
            dbg.location(498,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:16: ws
                    {
                    dbg.location(498,16);
                    pushFollow(FOLLOW_ws_in_page1740);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(85);}

            dbg.location(502,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:13: ( declaration | margin ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:14: declaration
                    {
                    dbg.location(502,14);
                    pushFollow(FOLLOW_declaration_in_page1795);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:26: margin ( ws )?
                    {
                    dbg.location(502,26);
                    pushFollow(FOLLOW_margin_in_page1797);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(502,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:33: ws
                            {
                            dbg.location(502,33);
                            pushFollow(FOLLOW_ws_in_page1799);
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

            dbg.location(502,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(502,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1805); if (state.failed) return ;
            	    dbg.location(502,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:45: ws
            	            {
            	            dbg.location(502,45);
            	            pushFollow(FOLLOW_ws_in_page1807);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(88);}

            	    dbg.location(502,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:49: ( declaration | margin ( ws )? )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:50: declaration
            	            {
            	            dbg.location(502,50);
            	            pushFollow(FOLLOW_declaration_in_page1811);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:62: margin ( ws )?
            	            {
            	            dbg.location(502,62);
            	            pushFollow(FOLLOW_margin_in_page1813);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(502,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:69: ws
            	                    {
            	                    dbg.location(502,69);
            	                    pushFollow(FOLLOW_ws_in_page1815);
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

            dbg.location(503,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1830); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(504, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(506, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(507,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1851); if (state.failed) return ;
            dbg.location(507,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:25: ws
                    {
                    dbg.location(507,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1853);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(507,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1856); if (state.failed) return ;
            dbg.location(507,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:35: ws
                    {
                    dbg.location(507,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1858);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(508,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1869); if (state.failed) return ;
            dbg.location(508,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:16: ws
                    {
                    dbg.location(508,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1871);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(508,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1874);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(509,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1878);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(510,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1888); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "counterStyle");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "counterStyle"


    // $ANTLR start "fontFace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(513, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(514,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1909); if (state.failed) return ;
            dbg.location(514,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:21: ws
                    {
                    dbg.location(514,21);
                    pushFollow(FOLLOW_ws_in_fontFace1911);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}

            dbg.location(515,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1922); if (state.failed) return ;
            dbg.location(515,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:16: ws
                    {
                    dbg.location(515,16);
                    pushFollow(FOLLOW_ws_in_fontFace1924);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}

            dbg.location(515,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1927);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(516,3);
            pushFollow(FOLLOW_declarations_in_fontFace1931);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(517,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1941); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(520, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(521,4);
            pushFollow(FOLLOW_margin_sym_in_margin1956);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(521,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:15: ws
                    {
                    dbg.location(521,15);
                    pushFollow(FOLLOW_ws_in_margin1958);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(521,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1961); if (state.failed) return ;
            dbg.location(521,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:26: ws
                    {
                    dbg.location(521,26);
                    pushFollow(FOLLOW_ws_in_margin1963);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}

            dbg.location(521,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1966);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(521,53);
            pushFollow(FOLLOW_declarations_in_margin1968);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(521,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1970); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(522, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(524, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(525,2);
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
        dbg.location(542, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(544, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:7: COLON IDENT
            {
            dbg.location(545,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage2199); if (state.failed) return ;
            dbg.location(545,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage2201); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(546, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(548, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(549,5);
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
        dbg.location(551, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(553, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:7: PLUS ( ws )?
                    {
                    dbg.location(554,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator2251); if (state.failed) return ;
                    dbg.location(554,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:12: ws
                            {
                            dbg.location(554,12);
                            pushFollow(FOLLOW_ws_in_combinator2253);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:7: GREATER ( ws )?
                    {
                    dbg.location(555,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator2262); if (state.failed) return ;
                    dbg.location(555,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:15: ws
                            {
                            dbg.location(555,15);
                            pushFollow(FOLLOW_ws_in_combinator2264);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:7: TILDE ( ws )?
                    {
                    dbg.location(556,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator2273); if (state.failed) return ;
                    dbg.location(556,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:13: ws
                            {
                            dbg.location(556,13);
                            pushFollow(FOLLOW_ws_in_combinator2275);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:5: 
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
        dbg.location(558, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(560, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(561,5);
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
        dbg.location(563, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:1: property : ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(565, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:5: ( ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:5: ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(567,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:5: ( ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | IDENT | GEN | {...}? cp_variable )
            int alt103=4;
            try { dbg.enterSubRule(103);
            try { dbg.enterDecision(103, decisionCanBacktrack[103]);

            int LA103_0 = input.LA(1);

            if ( (LA103_0==HASH_SYMBOL) && (synpred5_Css3())) {
                alt103=1;
            }
            else if ( (LA103_0==IDENT) ) {
                int LA103_2 = input.LA(2);

                if ( (synpred5_Css3()) ) {
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
            else if ( (LA103_0==MINUS||(LA103_0>=HASH && LA103_0<=DOT)) && (synpred5_Css3())) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:9: ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(570,53);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_property2382);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:11: IDENT
                    {
                    dbg.location(571,11);
                    match(input,IDENT,FOLLOW_IDENT_in_property2394); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:572:11: GEN
                    {
                    dbg.location(572,11);
                    match(input,GEN,FOLLOW_GEN_in_property2407); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:11: {...}? cp_variable
                    {
                    dbg.location(573,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(573,40);
                    pushFollow(FOLLOW_cp_variable_in_property2422);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(103);}

            dbg.location(574,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:7: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:7: ws
                    {
                    dbg.location(574,7);
                    pushFollow(FOLLOW_ws_in_property2430);
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
        dbg.location(575, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:1: rule : ( ( cp_mixin_declaration )=> cp_mixin_declaration | selectorsGroup ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(580, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:5: ( ( ( cp_mixin_declaration )=> cp_mixin_declaration | selectorsGroup ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:9: ( ( cp_mixin_declaration )=> cp_mixin_declaration | selectorsGroup ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(581,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:9: ( ( cp_mixin_declaration )=> cp_mixin_declaration | selectorsGroup )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:13: ( cp_mixin_declaration )=> cp_mixin_declaration
                    {
                    dbg.location(584,37);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule2494);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:13: selectorsGroup
                    {
                    dbg.location(586,13);
                    pushFollow(FOLLOW_selectorsGroup_in_rule2524);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(589,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule2546); if (state.failed) return ;
            dbg.location(589,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:16: ws
                    {
                    dbg.location(589,16);
                    pushFollow(FOLLOW_ws_in_rule2548);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}

            dbg.location(589,20);
            pushFollow(FOLLOW_syncToFollow_in_rule2551);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(590,13);
            pushFollow(FOLLOW_declarations_in_rule2565);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(591,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule2575); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(592, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:1: declarations : ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | {...}? sass_function_return ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:5: ( ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | {...}? sass_function_return ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | {...}? sass_function_return ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )* ( declaration )?
            {
            dbg.location(602,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | {...}? sass_function_return ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )*
            try { dbg.enterSubRule(119);

            loop119:
            do {
                int alt119=14;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:17: ( cp_variable_declaration )=> cp_variable_declaration ( ws )?
            	    {
            	    dbg.location(603,44);
            	    pushFollow(FOLLOW_cp_variable_declaration_in_declarations2641);
            	    cp_variable_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(603,68);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:68: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:68: ws
            	            {
            	            dbg.location(603,68);
            	            pushFollow(FOLLOW_ws_in_declarations2643);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:3: ( declaration SEMI )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(609,23);
            	    pushFollow(FOLLOW_declaration_in_declarations2724);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(609,35);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2726); if (state.failed) return ;
            	    dbg.location(609,40);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:40: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:40: ws
            	            {
            	            dbg.location(609,40);
            	            pushFollow(FOLLOW_ws_in_declarations2728);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(613,69);
            	    pushFollow(FOLLOW_declaration_in_declarations2813);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(613,81);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2815); if (state.failed) return ;
            	    dbg.location(613,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:86: ws
            	            {
            	            dbg.location(613,86);
            	            pushFollow(FOLLOW_ws_in_declarations2817);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:3: ( scss_nested_properties )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(615,29);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2830);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(615,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:52: ws
            	            {
            	            dbg.location(615,52);
            	            pushFollow(FOLLOW_ws_in_declarations2832);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:17: ( rule )=> rule ( ws )?
            	    {
            	    dbg.location(617,25);
            	    pushFollow(FOLLOW_rule_in_declarations2859);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(617,30);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:30: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:30: ws
            	            {
            	            dbg.location(617,30);
            	            pushFollow(FOLLOW_ws_in_declarations2861);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:17: {...}? sass_extend ( ws )?
            	    {
            	    dbg.location(619,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(619,35);
            	    pushFollow(FOLLOW_sass_extend_in_declarations2900);
            	    sass_extend();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(619,47);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:47: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:47: ws
            	            {
            	            dbg.location(619,47);
            	            pushFollow(FOLLOW_ws_in_declarations2902);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:17: {...}? sass_debug ( ws )?
            	    {
            	    dbg.location(621,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(621,35);
            	    pushFollow(FOLLOW_sass_debug_in_declarations2941);
            	    sass_debug();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(621,46);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:46: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:46: ws
            	            {
            	            dbg.location(621,46);
            	            pushFollow(FOLLOW_ws_in_declarations2943);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:17: {...}? sass_control ( ws )?
            	    {
            	    dbg.location(623,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(623,35);
            	    pushFollow(FOLLOW_sass_control_in_declarations2982);
            	    sass_control();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(623,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:48: ws
            	            {
            	            dbg.location(623,48);
            	            pushFollow(FOLLOW_ws_in_declarations2984);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:17: {...}? media ( ws )?
            	    {
            	    dbg.location(625,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(625,46);
            	    pushFollow(FOLLOW_media_in_declarations3023);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(625,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:52: ws
            	            {
            	            dbg.location(625,52);
            	            pushFollow(FOLLOW_ws_in_declarations3025);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(627,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(627,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations3064);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(627,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:60: ws
            	            {
            	            dbg.location(627,60);
            	            pushFollow(FOLLOW_ws_in_declarations3066);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:17: {...}? sass_content ( ws )?
            	    {
            	    dbg.location(629,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(629,35);
            	    pushFollow(FOLLOW_sass_content_in_declarations3105);
            	    sass_content();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(629,48);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:48: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:48: ws
            	            {
            	            dbg.location(629,48);
            	            pushFollow(FOLLOW_ws_in_declarations3107);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:17: {...}? sass_function_return ( ws )?
            	    {
            	    dbg.location(631,17);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isScssSource()");
            	    }
            	    dbg.location(631,35);
            	    pushFollow(FOLLOW_sass_function_return_in_declarations3146);
            	    sass_function_return();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(631,56);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:56: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:56: ws
            	            {
            	            dbg.location(631,56);
            	            pushFollow(FOLLOW_ws_in_declarations3148);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(118);}


            	    }
            	    break;
            	case 13 :
            	    dbg.enterAlt(13);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:17: ( (~ SEMI )* SEMI )=> syncTo_SEMI
            	    {
            	    dbg.location(633,32);
            	    pushFollow(FOLLOW_syncTo_SEMI_in_declarations3194);
            	    syncTo_SEMI();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop119;
                }
            } while (true);
            } finally {dbg.exitSubRule(119);}

            dbg.location(635,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:13: ( declaration )?
            int alt120=2;
            try { dbg.enterSubRule(120);
            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

            int LA120_0 = input.LA(1);

            if ( (LA120_0==IDENT||LA120_0==MEDIA_SYM||LA120_0==GEN||LA120_0==AT_IDENT||(LA120_0>=MINUS && LA120_0<=DOT)||LA120_0==STAR||LA120_0==SASS_VAR) ) {
                alt120=1;
            }
            } finally {dbg.exitDecision(120);}

            switch (alt120) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:13: declaration
                    {
                    dbg.location(635,13);
                    pushFollow(FOLLOW_declaration_in_declarations3224);
                    declaration();

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
        dbg.location(636, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(638, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt124=2;
            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

            try {
                isCyclicDecision = true;
                alt124 = dfa124.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(124);}

            switch (alt124) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(641,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup3284);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(641,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:99: ws
                            {
                            dbg.location(641,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup3286);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(643,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup3301);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(643,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(123);

                    loop123:
                    do {
                        int alt123=2;
                        try { dbg.enterDecision(123, decisionCanBacktrack[123]);

                        int LA123_0 = input.LA(1);

                        if ( (LA123_0==COMMA) ) {
                            alt123=1;
                        }


                        } finally {dbg.exitDecision(123);}

                        switch (alt123) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(643,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup3304); if (state.failed) return ;
                    	    dbg.location(643,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:25: ws
                    	            {
                    	            dbg.location(643,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup3306);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(122);}

                    	    dbg.location(643,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup3309);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop123;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(123);}


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
        dbg.location(644, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(646, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(647,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector3336);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(647,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(125);

            loop125:
            do {
                int alt125=2;
                try { dbg.enterDecision(125, decisionCanBacktrack[125]);

                int LA125_0 = input.LA(1);

                if ( (LA125_0==IDENT||LA125_0==GEN||LA125_0==COLON||(LA125_0>=PLUS && LA125_0<=TILDE)||(LA125_0>=HASH_SYMBOL && LA125_0<=PIPE)||LA125_0==LESS_AND) ) {
                    alt125=1;
                }


                } finally {dbg.exitDecision(125);}

                switch (alt125) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(647,31);
            	    pushFollow(FOLLOW_combinator_in_selector3339);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(647,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector3341);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop125;
                }
            } while (true);
            } finally {dbg.exitSubRule(125);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(648, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(651, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt130=2;
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            int LA130_0 = input.LA(1);

            if ( (LA130_0==IDENT||LA130_0==GEN||(LA130_0>=STAR && LA130_0<=PIPE)||LA130_0==LESS_AND) ) {
                alt130=1;
            }
            else if ( (LA130_0==COLON||(LA130_0>=HASH_SYMBOL && LA130_0<=SASS_EXTEND_ONLY_SELECTOR)) ) {
                alt130=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 130, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(654,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(654,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence3374);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(654,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:17: ( ( esPred )=> elementSubsequent ( ws )? )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(654,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence3381);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(654,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:46: ws
                    	            {
                    	            dbg.location(654,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence3383);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(126);}


                    	    }
                    	    break;

                    	default :
                    	    break loop127;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(127);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(656,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(656,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt129=0;
                    try { dbg.enterSubRule(129);

                    loop129:
                    do {
                        int alt129=2;
                        try { dbg.enterDecision(129, decisionCanBacktrack[129]);

                        switch ( input.LA(1) ) {
                        case SASS_EXTEND_ONLY_SELECTOR:
                            {
                            int LA129_2 = input.LA(2);

                            if ( ((synpred15_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                                alt129=1;
                            }


                            }
                            break;
                        case HASH:
                            {
                            int LA129_3 = input.LA(2);

                            if ( (synpred15_Css3()) ) {
                                alt129=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA129_4 = input.LA(2);

                            if ( (synpred15_Css3()) ) {
                                alt129=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA129_5 = input.LA(2);

                            if ( (synpred15_Css3()) ) {
                                alt129=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA129_6 = input.LA(2);

                            if ( (synpred15_Css3()) ) {
                                alt129=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA129_7 = input.LA(2);

                            if ( (synpred15_Css3()) ) {
                                alt129=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(129);}

                        switch (alt129) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(656,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence3402);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(656,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:33: ws
                    	            {
                    	            dbg.location(656,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence3404);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(128);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt129 >= 1 ) break loop129;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(129, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt129++;
                    } while (true);
                    } finally {dbg.exitSubRule(129);}


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
        dbg.location(657, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(664, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(665,5);
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
        dbg.location(666, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(668, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(670,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt131=2;
            try { dbg.enterSubRule(131);
            try { dbg.enterDecision(131, decisionCanBacktrack[131]);

            int LA131_0 = input.LA(1);

            if ( (LA131_0==IDENT) ) {
                int LA131_1 = input.LA(2);

                if ( (synpred16_Css3()) ) {
                    alt131=1;
                }
            }
            else if ( (LA131_0==STAR) ) {
                int LA131_2 = input.LA(2);

                if ( (synpred16_Css3()) ) {
                    alt131=1;
                }
            }
            else if ( (LA131_0==PIPE) && (synpred16_Css3())) {
                alt131=1;
            }
            } finally {dbg.exitDecision(131);}

            switch (alt131) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(670,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector3520);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(131);}

            dbg.location(670,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:51: elementName ( ws )?
            {
            dbg.location(670,51);
            pushFollow(FOLLOW_elementName_in_typeSelector3526);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(670,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:63: ws
                    {
                    dbg.location(670,63);
                    pushFollow(FOLLOW_ws_in_typeSelector3528);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(132);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(671, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(673, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(674,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:5: ( namespacePrefixName | STAR )?
            int alt133=3;
            try { dbg.enterSubRule(133);
            try { dbg.enterDecision(133, decisionCanBacktrack[133]);

            int LA133_0 = input.LA(1);

            if ( (LA133_0==IDENT) ) {
                alt133=1;
            }
            else if ( (LA133_0==STAR) ) {
                alt133=2;
            }
            } finally {dbg.exitDecision(133);}

            switch (alt133) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:7: namespacePrefixName
                    {
                    dbg.location(674,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix3546);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:29: STAR
                    {
                    dbg.location(674,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix3550); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(133);}

            dbg.location(674,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix3554); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(675, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:1: elementSubsequent : ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(678, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:5: ( ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(680,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:5: ({...}? sass_extend_only_selector | cssId | cssClass | slAttribute | pseudo )
            int alt134=5;
            try { dbg.enterSubRule(134);
            try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            switch ( input.LA(1) ) {
            case SASS_EXTEND_ONLY_SELECTOR:
                {
                alt134=1;
                }
                break;
            case HASH_SYMBOL:
            case HASH:
                {
                alt134=2;
                }
                break;
            case DOT:
                {
                alt134=3;
                }
                break;
            case LBRACKET:
                {
                alt134=4;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt134=5;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:9: {...}? sass_extend_only_selector
                    {
                    dbg.location(681,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "elementSubsequent", "isScssSource()");
                    }
                    dbg.location(681,27);
                    pushFollow(FOLLOW_sass_extend_only_selector_in_elementSubsequent3593);
                    sass_extend_only_selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:8: cssId
                    {
                    dbg.location(682,8);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent3602);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:8: cssClass
                    {
                    dbg.location(683,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent3611);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:11: slAttribute
                    {
                    dbg.location(684,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent3623);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:11: pseudo
                    {
                    dbg.location(685,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent3635);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(134);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(687, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(690, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt135=2;
            try { dbg.enterDecision(135, decisionCanBacktrack[135]);

            int LA135_0 = input.LA(1);

            if ( (LA135_0==HASH) ) {
                alt135=1;
            }
            else if ( (LA135_0==HASH_SYMBOL) ) {
                alt135=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 135, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(135);}

            switch (alt135) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:7: HASH
                    {
                    dbg.location(691,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId3663); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(691,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:16: HASH_SYMBOL NAME
                    {
                    dbg.location(691,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId3669); if (state.failed) return ;
                    dbg.location(691,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId3671); if (state.failed) return ;

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
        dbg.location(692, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(698, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:7: DOT ( IDENT | GEN )
            {
            dbg.location(699,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass3699); if (state.failed) return ;
            dbg.location(699,11);
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
        dbg.location(700, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(707, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(708,5);
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
        dbg.location(709, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(711, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(712,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute3773); if (state.failed) return ;
            dbg.location(713,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:6: ( namespacePrefix )?
            int alt136=2;
            try { dbg.enterSubRule(136);
            try { dbg.enterDecision(136, decisionCanBacktrack[136]);

            int LA136_0 = input.LA(1);

            if ( (LA136_0==IDENT) ) {
                int LA136_1 = input.LA(2);

                if ( (LA136_1==PIPE) ) {
                    alt136=1;
                }
            }
            else if ( ((LA136_0>=STAR && LA136_0<=PIPE)) ) {
                alt136=1;
            }
            } finally {dbg.exitDecision(136);}

            switch (alt136) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:6: namespacePrefix
                    {
                    dbg.location(713,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute3780);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}

            dbg.location(713,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:23: ws
                    {
                    dbg.location(713,23);
                    pushFollow(FOLLOW_ws_in_slAttribute3783);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(714,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute3794);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(714,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:25: ws
                    {
                    dbg.location(714,25);
                    pushFollow(FOLLOW_ws_in_slAttribute3796);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(138);}

            dbg.location(716,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            int LA141_0 = input.LA(1);

            if ( ((LA141_0>=OPEQ && LA141_0<=CONTAINS)) ) {
                alt141=1;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(717,17);
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

                    dbg.location(725,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:17: ws
                            {
                            dbg.location(725,17);
                            pushFollow(FOLLOW_ws_in_slAttribute4018);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(139);}

                    dbg.location(726,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute4037);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(727,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:17: ws
                            {
                            dbg.location(727,17);
                            pushFollow(FOLLOW_ws_in_slAttribute4055);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(140);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(730,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute4084); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(731, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(738, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:4: IDENT
            {
            dbg.location(739,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName4100); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "slAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "slAttributeName"


    // $ANTLR start "slAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(742, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:2: ( IDENT | STRING )
            {
            dbg.location(744,2);
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
        dbg.location(748, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(750, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(751,7);
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

            dbg.location(752,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt149=2;
            try { dbg.enterSubRule(149);
            try { dbg.enterDecision(149, decisionCanBacktrack[149]);

            int LA149_0 = input.LA(1);

            if ( (LA149_0==IDENT||LA149_0==GEN) ) {
                alt149=1;
            }
            else if ( (LA149_0==NOT) ) {
                alt149=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 149, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(149);}

            switch (alt149) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(753,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(754,21);
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

                    dbg.location(755,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    int alt145=2;
                    try { dbg.enterSubRule(145);
                    try { dbg.enterDecision(145, decisionCanBacktrack[145]);

                    try {
                        isCyclicDecision = true;
                        alt145 = dfa145.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(145);}

                    switch (alt145) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(756,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:25: ws
                                    {
                                    dbg.location(756,25);
                                    pushFollow(FOLLOW_ws_in_pseudo4295);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(142);}

                            dbg.location(756,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo4298); if (state.failed) return ;
                            dbg.location(756,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:36: ws
                                    {
                                    dbg.location(756,36);
                                    pushFollow(FOLLOW_ws_in_pseudo4300);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(143);}

                            dbg.location(756,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:40: ( expression | STAR )?
                            int alt144=3;
                            try { dbg.enterSubRule(144);
                            try { dbg.enterDecision(144, decisionCanBacktrack[144]);

                            int LA144_0 = input.LA(1);

                            if ( ((LA144_0>=IDENT && LA144_0<=URI)||LA144_0==MEDIA_SYM||LA144_0==GEN||LA144_0==AT_IDENT||LA144_0==PERCENTAGE||LA144_0==PLUS||LA144_0==MINUS||LA144_0==HASH||(LA144_0>=NUMBER && LA144_0<=DIMENSION)||LA144_0==SASS_VAR) ) {
                                alt144=1;
                            }
                            else if ( (LA144_0==STAR) ) {
                                alt144=2;
                            }
                            } finally {dbg.exitDecision(144);}

                            switch (alt144) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:42: expression
                                    {
                                    dbg.location(756,42);
                                    pushFollow(FOLLOW_expression_in_pseudo4305);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:55: STAR
                                    {
                                    dbg.location(756,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo4309); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(144);}

                            dbg.location(756,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo4314); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(145);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(760,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(760,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo4393); if (state.failed) return ;
                    dbg.location(760,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:23: ws
                            {
                            dbg.location(760,23);
                            pushFollow(FOLLOW_ws_in_pseudo4395);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(146);}

                    dbg.location(760,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo4398); if (state.failed) return ;
                    dbg.location(760,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:34: ws
                            {
                            dbg.location(760,34);
                            pushFollow(FOLLOW_ws_in_pseudo4400);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(147);}

                    dbg.location(760,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:38: ( simpleSelectorSequence )?
                    int alt148=2;
                    try { dbg.enterSubRule(148);
                    try { dbg.enterDecision(148, decisionCanBacktrack[148]);

                    int LA148_0 = input.LA(1);

                    if ( (LA148_0==IDENT||LA148_0==GEN||LA148_0==COLON||(LA148_0>=HASH_SYMBOL && LA148_0<=PIPE)||LA148_0==LESS_AND) ) {
                        alt148=1;
                    }
                    } finally {dbg.exitDecision(148);}

                    switch (alt148) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:38: simpleSelectorSequence
                            {
                            dbg.location(760,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo4403);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(148);}

                    dbg.location(760,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo4406); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(149);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(762, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(764, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:5: ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(766,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:5: ( STAR )?
            int alt150=2;
            try { dbg.enterSubRule(150);
            try { dbg.enterDecision(150, decisionCanBacktrack[150]);

            int LA150_0 = input.LA(1);

            if ( (LA150_0==STAR) ) {
                alt150=1;
            }
            } finally {dbg.exitDecision(150);}

            switch (alt150) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:5: STAR
                    {
                    dbg.location(766,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration4445); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}

            dbg.location(766,11);
            pushFollow(FOLLOW_property_in_declaration4448);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(766,20);
            match(input,COLON,FOLLOW_COLON_in_declaration4450); if (state.failed) return ;
            dbg.location(766,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:26: ws
                    {
                    dbg.location(766,26);
                    pushFollow(FOLLOW_ws_in_declaration4452);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(766,30);
            pushFollow(FOLLOW_propertyValue_in_declaration4455);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(766,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:44: ( prio ( ws )? )?
            int alt153=2;
            try { dbg.enterSubRule(153);
            try { dbg.enterDecision(153, decisionCanBacktrack[153]);

            int LA153_0 = input.LA(1);

            if ( (LA153_0==IMPORTANT_SYM) ) {
                alt153=1;
            }
            } finally {dbg.exitDecision(153);}

            switch (alt153) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:45: prio ( ws )?
                    {
                    dbg.location(766,45);
                    pushFollow(FOLLOW_prio_in_declaration4458);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(766,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:50: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:50: ws
                            {
                            dbg.location(766,50);
                            pushFollow(FOLLOW_ws_in_declaration4460);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(152);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(153);}


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
        dbg.location(767, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:1: propertyValue : ( ( (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(775, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:2: ( ( (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) )
            int alt154=3;
            try { dbg.enterDecision(154, decisionCanBacktrack[154]);

            try {
                isCyclicDecision = true;
                alt154 = dfa154.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(154);}

            switch (alt154) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:9: ( (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression
                    {
                    dbg.location(779,66);
                    pushFollow(FOLLOW_scss_declaration_property_value_interpolation_expression_in_propertyValue4530);
                    scss_declaration_property_value_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(780,34);
                    pushFollow(FOLLOW_expression_in_propertyValue4546);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:9: ({...}? cp_expression )
                    {
                    dbg.location(790,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:11: {...}? cp_expression
                    {
                    dbg.location(790,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(790,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue4587);
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
        dbg.location(791, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:794:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(794, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(797,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt155=0;
            try { dbg.enterSubRule(155);

            loop155:
            do {
                int alt155=2;
                try { dbg.enterDecision(155, decisionCanBacktrack[155]);

                int LA155_0 = input.LA(1);

                if ( (LA155_0==NAMESPACE_SYM||(LA155_0>=IDENT && LA155_0<=MEDIA_SYM)||(LA155_0>=AND && LA155_0<=RPAREN)||(LA155_0>=WS && LA155_0<=RIGHTBOTTOM_SYM)||(LA155_0>=PLUS && LA155_0<=SASS_EXTEND_ONLY_SELECTOR)||(LA155_0>=PIPE && LA155_0<=LINE_COMMENT)) ) {
                    alt155=1;
                }


                } finally {dbg.exitDecision(155);}

                switch (alt155) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(797,7);
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
            	    if ( cnt155 >= 1 ) break loop155;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(155, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt155++;
            } while (true);
            } finally {dbg.exitSubRule(155);}

            dbg.location(797,65);
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
        dbg.location(798, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(802, 1);

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
            dbg.exitRule(getGrammarFileName(), "syncToDeclarationsRule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToDeclarationsRule"


    // $ANTLR start "syncTo_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(810, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:6: 
            {
            }

        }
        finally {
        }
        dbg.location(815, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(817, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:13: SEMI
            {
            dbg.location(822,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI4772); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(823, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(826, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:6: 
            {
            }

        }
        finally {
        }
        dbg.location(831, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(833, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:7: IMPORTANT_SYM
            {
            dbg.location(834,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio4827); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(835, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(837, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(838,7);
            pushFollow(FOLLOW_term_in_expression4848);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(838,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(158);

            loop158:
            do {
                int alt158=2;
                try { dbg.enterDecision(158, decisionCanBacktrack[158]);

                try {
                    isCyclicDecision = true;
                    alt158 = dfa158.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(158);}

                switch (alt158) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(838,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:14: ( operator ( ws )? )?
            	    int alt157=2;
            	    try { dbg.enterSubRule(157);
            	    try { dbg.enterDecision(157, decisionCanBacktrack[157]);

            	    int LA157_0 = input.LA(1);

            	    if ( (LA157_0==COMMA||LA157_0==SOLIDUS) ) {
            	        alt157=1;
            	    }
            	    } finally {dbg.exitDecision(157);}

            	    switch (alt157) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:15: operator ( ws )?
            	            {
            	            dbg.location(838,15);
            	            pushFollow(FOLLOW_operator_in_expression4853);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(838,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:24: ws
            	                    {
            	                    dbg.location(838,24);
            	                    pushFollow(FOLLOW_ws_in_expression4855);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(156);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(157);}

            	    dbg.location(838,30);
            	    pushFollow(FOLLOW_term_in_expression4860);
            	    term();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(839, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(841, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(842,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:7: ( unaryOperator ( ws )? )?
            int alt160=2;
            try { dbg.enterSubRule(160);
            try { dbg.enterDecision(160, decisionCanBacktrack[160]);

            int LA160_0 = input.LA(1);

            if ( (LA160_0==PLUS||LA160_0==MINUS) ) {
                alt160=1;
            }
            } finally {dbg.exitDecision(160);}

            switch (alt160) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:9: unaryOperator ( ws )?
                    {
                    dbg.location(842,9);
                    pushFollow(FOLLOW_unaryOperator_in_term4885);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(842,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:23: ( ws )?
                    int alt159=2;
                    try { dbg.enterSubRule(159);
                    try { dbg.enterDecision(159, decisionCanBacktrack[159]);

                    int LA159_0 = input.LA(1);

                    if ( (LA159_0==WS||(LA159_0>=NL && LA159_0<=COMMENT)) ) {
                        alt159=1;
                    }
                    } finally {dbg.exitDecision(159);}

                    switch (alt159) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:23: ws
                            {
                            dbg.location(842,23);
                            pushFollow(FOLLOW_ws_in_term4887);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(159);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(843,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt161=8;
            try { dbg.enterSubRule(161);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(844,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:7: STRING
                    {
                    dbg.location(857,7);
                    match(input,STRING,FOLLOW_STRING_in_term5111); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:7: IDENT
                    {
                    dbg.location(858,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term5119); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:7: GEN
                    {
                    dbg.location(859,7);
                    match(input,GEN,FOLLOW_GEN_in_term5127); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:7: URI
                    {
                    dbg.location(860,7);
                    match(input,URI,FOLLOW_URI_in_term5135); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:7: hexColor
                    {
                    dbg.location(861,7);
                    pushFollow(FOLLOW_hexColor_in_term5143);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:7: function
                    {
                    dbg.location(862,7);
                    pushFollow(FOLLOW_function_in_term5151);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:7: {...}? cp_variable
                    {
                    dbg.location(863,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(863,36);
                    pushFollow(FOLLOW_cp_variable_in_term5161);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(161);}

            dbg.location(865,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:5: ws
                    {
                    dbg.location(865,5);
                    pushFollow(FOLLOW_ws_in_term5173);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(162);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:1: function : functionName ( ws )? LPAREN ( ws )? ({...}? cp_variable_value | expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(868, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:2: ( functionName ( ws )? LPAREN ( ws )? ({...}? cp_variable_value | expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:5: functionName ( ws )? LPAREN ( ws )? ({...}? cp_variable_value | expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?) RPAREN
            {
            dbg.location(869,5);
            pushFollow(FOLLOW_functionName_in_function5189);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(869,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:18: ws
                    {
                    dbg.location(869,18);
                    pushFollow(FOLLOW_ws_in_function5191);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(163);}

            dbg.location(870,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function5196); if (state.failed) return ;
            dbg.location(870,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:10: ws
                    {
                    dbg.location(870,10);
                    pushFollow(FOLLOW_ws_in_function5198);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(164);}

            dbg.location(871,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:3: ({...}? cp_variable_value | expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?)
            int alt167=4;
            try { dbg.enterSubRule(167);
            try { dbg.enterDecision(167, decisionCanBacktrack[167]);

            try {
                isCyclicDecision = true;
                alt167 = dfa167.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(167);}

            switch (alt167) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:21: {...}? cp_variable_value
                    {
                    dbg.location(872,21);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "function", "isCssPreprocessorSource()");
                    }
                    dbg.location(872,50);
                    pushFollow(FOLLOW_cp_variable_value_in_function5227);
                    cp_variable_value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:21: expression
                    {
                    dbg.location(874,21);
                    pushFollow(FOLLOW_expression_in_function5271);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(876,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(877,5);
                    pushFollow(FOLLOW_fnAttribute_in_function5289);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(877,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(166);

                    loop166:
                    do {
                        int alt166=2;
                        try { dbg.enterDecision(166, decisionCanBacktrack[166]);

                        int LA166_0 = input.LA(1);

                        if ( (LA166_0==COMMA) ) {
                            alt166=1;
                        }


                        } finally {dbg.exitDecision(166);}

                        switch (alt166) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(877,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function5292); if (state.failed) return ;
                    	    dbg.location(877,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:24: ws
                    	            {
                    	            dbg.location(877,24);
                    	            pushFollow(FOLLOW_ws_in_function5294);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(165);}

                    	    dbg.location(877,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function5297);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop166;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(166);}


                    }


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:17: {...}?
                    {
                    dbg.location(880,17);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "function", "isCssPreprocessorSource()");
                    }

                    }
                    break;

            }
            } finally {dbg.exitSubRule(167);}

            dbg.location(882,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function5355); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(883, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(889, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(893,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:4: ( IDENT COLON )?
            int alt168=2;
            try { dbg.enterSubRule(168);
            try { dbg.enterDecision(168, decisionCanBacktrack[168]);

            int LA168_0 = input.LA(1);

            if ( (LA168_0==IDENT) ) {
                int LA168_1 = input.LA(2);

                if ( (LA168_1==COLON) ) {
                    alt168=1;
                }
            }
            } finally {dbg.exitDecision(168);}

            switch (alt168) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:5: IDENT COLON
                    {
                    dbg.location(893,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName5403); if (state.failed) return ;
                    dbg.location(893,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName5405); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(168);}

            dbg.location(893,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName5409); if (state.failed) return ;
            dbg.location(893,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:25: ( DOT IDENT )*
            try { dbg.enterSubRule(169);

            loop169:
            do {
                int alt169=2;
                try { dbg.enterDecision(169, decisionCanBacktrack[169]);

                int LA169_0 = input.LA(1);

                if ( (LA169_0==DOT) ) {
                    alt169=1;
                }


                } finally {dbg.exitDecision(169);}

                switch (alt169) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:26: DOT IDENT
            	    {
            	    dbg.location(893,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName5412); if (state.failed) return ;
            	    dbg.location(893,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName5414); if (state.failed) return ;

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
        dbg.location(895, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(897, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(898,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute5437);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(898,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:20: ws
                    {
                    dbg.location(898,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute5439);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(170);}

            dbg.location(898,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute5442); if (state.failed) return ;
            dbg.location(898,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:29: ( ws )?
            int alt171=2;
            try { dbg.enterSubRule(171);
            try { dbg.enterDecision(171, decisionCanBacktrack[171]);

            int LA171_0 = input.LA(1);

            if ( (LA171_0==WS||(LA171_0>=NL && LA171_0<=COMMENT)) ) {
                alt171=1;
            }
            } finally {dbg.exitDecision(171);}

            switch (alt171) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:29: ws
                    {
                    dbg.location(898,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute5444);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(171);}

            dbg.location(898,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute5447);
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
        dbg.location(899, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(901, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:4: IDENT ( DOT IDENT )*
            {
            dbg.location(902,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName5462); if (state.failed) return ;
            dbg.location(902,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:10: ( DOT IDENT )*
            try { dbg.enterSubRule(172);

            loop172:
            do {
                int alt172=2;
                try { dbg.enterDecision(172, decisionCanBacktrack[172]);

                int LA172_0 = input.LA(1);

                if ( (LA172_0==DOT) ) {
                    alt172=1;
                }


                } finally {dbg.exitDecision(172);}

                switch (alt172) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:11: DOT IDENT
            	    {
            	    dbg.location(902,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName5465); if (state.failed) return ;
            	    dbg.location(902,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName5467); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop172;
                }
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
        dbg.location(903, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:905:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(905, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:4: expression
            {
            dbg.location(906,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue5481);
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
        dbg.location(907, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(909, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:910:7: HASH
            {
            dbg.location(910,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor5499); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(911, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(913, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:7: ( WS | NL | COMMENT )+
            {
            dbg.location(914,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:7: ( WS | NL | COMMENT )+
            int cnt173=0;
            try { dbg.enterSubRule(173);

            loop173:
            do {
                int alt173=2;
                try { dbg.enterDecision(173, decisionCanBacktrack[173]);

                int LA173_0 = input.LA(1);

                if ( (LA173_0==WS||(LA173_0>=NL && LA173_0<=COMMENT)) ) {
                    alt173=1;
                }


                } finally {dbg.exitDecision(173);}

                switch (alt173) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(914,7);
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
            	    if ( cnt173 >= 1 ) break loop173;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(173, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt173++;
            } while (true);
            } finally {dbg.exitSubRule(173);}


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
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"


    // $ANTLR start "cp_variable_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(920, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt180=2;
            try { dbg.enterDecision(180, decisionCanBacktrack[180]);

            int LA180_0 = input.LA(1);

            if ( (LA180_0==MEDIA_SYM||LA180_0==AT_IDENT) ) {
                int LA180_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt180=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt180=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 180, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA180_0==SASS_VAR) ) {
                int LA180_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt180=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt180=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 180, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value SEMI
                    {
                    dbg.location(922,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(922,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5568);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(922,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:39: ws
                            {
                            dbg.location(922,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5570);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(174);}

                    dbg.location(922,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5573); if (state.failed) return ;
                    dbg.location(922,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:49: ws
                            {
                            dbg.location(922,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5575);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(175);}

                    dbg.location(922,53);
                    pushFollow(FOLLOW_cp_variable_value_in_cp_variable_declaration5578);
                    cp_variable_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(922,71);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5580); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_variable_value ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(924,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(924,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration5607);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(924,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:39: ws
                            {
                            dbg.location(924,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5609);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(176);}

                    dbg.location(924,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration5612); if (state.failed) return ;
                    dbg.location(924,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:49: ws
                            {
                            dbg.location(924,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration5614);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(177);}

                    dbg.location(924,53);
                    pushFollow(FOLLOW_cp_variable_value_in_cp_variable_declaration5617);
                    cp_variable_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(924,71);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:71: ( SASS_DEFAULT ( ws )? )?
                    int alt179=2;
                    try { dbg.enterSubRule(179);
                    try { dbg.enterDecision(179, decisionCanBacktrack[179]);

                    int LA179_0 = input.LA(1);

                    if ( (LA179_0==SASS_DEFAULT) ) {
                        alt179=1;
                    }
                    } finally {dbg.exitDecision(179);}

                    switch (alt179) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:72: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(924,72);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5620); if (state.failed) return ;
                            dbg.location(924,85);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:85: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:85: ws
                                    {
                                    dbg.location(924,85);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration5622);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(178);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(179);}

                    dbg.location(924,91);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration5627); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_variable_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable_declaration"


    // $ANTLR start "cp_variable"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:928:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(928, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:929:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt181=2;
            try { dbg.enterDecision(181, decisionCanBacktrack[181]);

            int LA181_0 = input.LA(1);

            if ( (LA181_0==MEDIA_SYM||LA181_0==AT_IDENT) ) {
                alt181=1;
            }
            else if ( (LA181_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(930,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(930,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(932,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(932,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:29: SASS_VAR
                    {
                    dbg.location(932,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable5692); if (state.failed) return ;

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
        dbg.location(934, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:1: cp_variable_value : cp_expression ( COMMA ( ws )? cp_expression )* ;
    public final void cp_variable_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(936, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:5: ( cp_expression ( COMMA ( ws )? cp_expression )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:5: cp_expression ( COMMA ( ws )? cp_expression )*
            {
            dbg.location(938,5);
            pushFollow(FOLLOW_cp_expression_in_cp_variable_value5716);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(938,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:19: ( COMMA ( ws )? cp_expression )*
            try { dbg.enterSubRule(183);

            loop183:
            do {
                int alt183=2;
                try { dbg.enterDecision(183, decisionCanBacktrack[183]);

                int LA183_0 = input.LA(1);

                if ( (LA183_0==COMMA) ) {
                    alt183=1;
                }


                } finally {dbg.exitDecision(183);}

                switch (alt183) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:21: COMMA ( ws )? cp_expression
            	    {
            	    dbg.location(938,21);
            	    match(input,COMMA,FOLLOW_COMMA_in_cp_variable_value5720); if (state.failed) return ;
            	    dbg.location(938,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:27: ws
            	            {
            	            dbg.location(938,27);
            	            pushFollow(FOLLOW_ws_in_cp_variable_value5722);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(182);}

            	    dbg.location(938,31);
            	    pushFollow(FOLLOW_cp_expression_in_cp_variable_value5725);
            	    cp_expression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop183;
                }
            } while (true);
            } finally {dbg.exitSubRule(183);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(939, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:942:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(942, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:10: cp_additionExp
            {
            dbg.location(943,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression5753);
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
        dbg.location(944, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(946, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:947:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(947,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5773);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(948,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(186);

            loop186:
            do {
                int alt186=3;
                try { dbg.enterDecision(186, decisionCanBacktrack[186]);

                int LA186_0 = input.LA(1);

                if ( (LA186_0==PLUS) ) {
                    alt186=1;
                }
                else if ( (LA186_0==MINUS) ) {
                    alt186=2;
                }


                } finally {dbg.exitDecision(186);}

                switch (alt186) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(948,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp5787); if (state.failed) return ;
            	    dbg.location(948,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:17: ws
            	            {
            	            dbg.location(948,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5789);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(184);}

            	    dbg.location(948,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5792);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:949:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(949,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp5805); if (state.failed) return ;
            	    dbg.location(949,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:949:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:949:18: ws
            	            {
            	            dbg.location(949,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5807);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(185);}

            	    dbg.location(949,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5810);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop186;
                }
            } while (true);
            } finally {dbg.exitSubRule(186);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(951, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(953, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(954,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5843);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(955,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(189);

            loop189:
            do {
                int alt189=3;
                try { dbg.enterDecision(189, decisionCanBacktrack[189]);

                int LA189_0 = input.LA(1);

                if ( (LA189_0==STAR) ) {
                    alt189=1;
                }
                else if ( (LA189_0==SOLIDUS) ) {
                    alt189=2;
                }


                } finally {dbg.exitDecision(189);}

                switch (alt189) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(955,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp5856); if (state.failed) return ;
            	    dbg.location(955,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:17: ws
            	            {
            	            dbg.location(955,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5858);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(187);}

            	    dbg.location(955,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5861);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(956,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp5875); if (state.failed) return ;
            	    dbg.location(956,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:20: ws
            	            {
            	            dbg.location(956,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5877);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(188);}

            	    dbg.location(956,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5880);
            	    cp_atomExp();

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

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(958, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:960:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(960, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt193=2;
            try { dbg.enterDecision(193, decisionCanBacktrack[193]);

            int LA193_0 = input.LA(1);

            if ( ((LA193_0>=IDENT && LA193_0<=URI)||LA193_0==MEDIA_SYM||LA193_0==GEN||LA193_0==AT_IDENT||LA193_0==PERCENTAGE||LA193_0==PLUS||LA193_0==MINUS||LA193_0==HASH||(LA193_0>=NUMBER && LA193_0<=DIMENSION)||LA193_0==SASS_VAR) ) {
                alt193=1;
            }
            else if ( (LA193_0==LPAREN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:10: term ( ( term )=> term )*
                    {
                    dbg.location(961,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp5913);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(961,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(190);

                    loop190:
                    do {
                        int alt190=2;
                        try { dbg.enterDecision(190, decisionCanBacktrack[190]);

                        try {
                            isCyclicDecision = true;
                            alt190 = dfa190.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(190);}

                        switch (alt190) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:16: ( term )=> term
                    	    {
                    	    dbg.location(961,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp5920);
                    	    term();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop190;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(190);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(962,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp5934); if (state.failed) return ;
                    dbg.location(962,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:17: ws
                            {
                            dbg.location(962,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5936);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(191);}

                    dbg.location(962,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp5939);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(962,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp5941); if (state.failed) return ;
                    dbg.location(962,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:43: ( ws )?
                    int alt192=2;
                    try { dbg.enterSubRule(192);
                    try { dbg.enterDecision(192, decisionCanBacktrack[192]);

                    int LA192_0 = input.LA(1);

                    if ( (LA192_0==WS||(LA192_0>=NL && LA192_0<=COMMENT)) ) {
                        alt192=1;
                    }
                    } finally {dbg.exitDecision(192);}

                    switch (alt192) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:43: ws
                            {
                            dbg.location(962,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5943);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

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
        dbg.location(963, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(966, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:967:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(968,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt194=8;
            try { dbg.enterSubRule(194);
            try { dbg.enterDecision(194, decisionCanBacktrack[194]);

            try {
                isCyclicDecision = true;
                alt194 = dfa194.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(194);}

            switch (alt194) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(969,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:7: STRING
                    {
                    dbg.location(982,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term6181); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:7: IDENT
                    {
                    dbg.location(983,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term6189); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:984:7: GEN
                    {
                    dbg.location(984,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term6197); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:7: URI
                    {
                    dbg.location(985,7);
                    match(input,URI,FOLLOW_URI_in_cp_term6205); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:7: hexColor
                    {
                    dbg.location(986,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term6213);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:987:7: function
                    {
                    dbg.location(987,7);
                    pushFollow(FOLLOW_function_in_cp_term6221);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:7: cp_variable
                    {
                    dbg.location(988,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term6229);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(194);}

            dbg.location(990,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:5: ws
                    {
                    dbg.location(990,5);
                    pushFollow(FOLLOW_ws_in_cp_term6241);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(195);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(991, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1000, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? )? )
            int alt207=2;
            try { dbg.enterDecision(207, decisionCanBacktrack[207]);

            int LA207_0 = input.LA(1);

            if ( (LA207_0==DOT) ) {
                alt207=1;
            }
            else if ( (LA207_0==SASS_MIXIN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(1002,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(1002,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration6272); if (state.failed) return ;
                    dbg.location(1002,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration6274);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1002,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:41: ( ws )?
                    int alt196=2;
                    try { dbg.enterSubRule(196);
                    try { dbg.enterDecision(196, decisionCanBacktrack[196]);

                    int LA196_0 = input.LA(1);

                    if ( (LA196_0==WS||(LA196_0>=NL && LA196_0<=COMMENT)) ) {
                        alt196=1;
                    }
                    } finally {dbg.exitDecision(196);}

                    switch (alt196) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:41: ws
                            {
                            dbg.location(1002,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6276);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(196);}

                    dbg.location(1002,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration6279); if (state.failed) return ;
                    dbg.location(1002,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:52: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:52: ws
                            {
                            dbg.location(1002,52);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6281);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(197);}

                    dbg.location(1002,56);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:56: ( cp_args_list )?
                    int alt198=2;
                    try { dbg.enterSubRule(198);
                    try { dbg.enterDecision(198, decisionCanBacktrack[198]);

                    int LA198_0 = input.LA(1);

                    if ( (LA198_0==MEDIA_SYM||LA198_0==AT_IDENT||LA198_0==SASS_VAR||(LA198_0>=LESS_DOTS && LA198_0<=LESS_REST)) ) {
                        alt198=1;
                    }
                    } finally {dbg.exitDecision(198);}

                    switch (alt198) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:56: cp_args_list
                            {
                            dbg.location(1002,56);
                            pushFollow(FOLLOW_cp_args_list_in_cp_mixin_declaration6284);
                            cp_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(198);}

                    dbg.location(1002,70);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration6287); if (state.failed) return ;
                    dbg.location(1002,77);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:77: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:77: ws
                            {
                            dbg.location(1002,77);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6289);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(199);}

                    dbg.location(1002,81);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:81: ( less_mixin_guarded ( ws )? )?
                    int alt201=2;
                    try { dbg.enterSubRule(201);
                    try { dbg.enterDecision(201, decisionCanBacktrack[201]);

                    int LA201_0 = input.LA(1);

                    if ( (LA201_0==LESS_WHEN) ) {
                        alt201=1;
                    }
                    } finally {dbg.exitDecision(201);}

                    switch (alt201) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:82: less_mixin_guarded ( ws )?
                            {
                            dbg.location(1002,82);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration6293);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1002,101);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:101: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:101: ws
                                    {
                                    dbg.location(1002,101);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6295);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(200);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(201);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(1004,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(1004,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration6312); if (state.failed) return ;
                    dbg.location(1004,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6314);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1004,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration6316);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1004,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:51: ws
                            {
                            dbg.location(1004,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration6318);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(202);}

                    dbg.location(1004,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:55: ( LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )? )?
                    int alt206=2;
                    try { dbg.enterSubRule(206);
                    try { dbg.enterDecision(206, decisionCanBacktrack[206]);

                    int LA206_0 = input.LA(1);

                    if ( (LA206_0==LPAREN) ) {
                        alt206=1;
                    }
                    } finally {dbg.exitDecision(206);}

                    switch (alt206) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:56: LPAREN ( ws )? ( cp_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(1004,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration6322); if (state.failed) return ;
                            dbg.location(1004,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:63: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:63: ws
                                    {
                                    dbg.location(1004,63);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6324);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(203);}

                            dbg.location(1004,67);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:67: ( cp_args_list )?
                            int alt204=2;
                            try { dbg.enterSubRule(204);
                            try { dbg.enterDecision(204, decisionCanBacktrack[204]);

                            int LA204_0 = input.LA(1);

                            if ( (LA204_0==MEDIA_SYM||LA204_0==AT_IDENT||LA204_0==SASS_VAR||(LA204_0>=LESS_DOTS && LA204_0<=LESS_REST)) ) {
                                alt204=1;
                            }
                            } finally {dbg.exitDecision(204);}

                            switch (alt204) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:67: cp_args_list
                                    {
                                    dbg.location(1004,67);
                                    pushFollow(FOLLOW_cp_args_list_in_cp_mixin_declaration6327);
                                    cp_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(204);}

                            dbg.location(1004,81);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration6330); if (state.failed) return ;
                            dbg.location(1004,88);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:88: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:88: ws
                                    {
                                    dbg.location(1004,88);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration6332);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(205);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(206);}


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
        dbg.location(1005, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1009:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1009, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(1011,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt208=2;
            try { dbg.enterSubRule(208);
            try { dbg.enterDecision(208, decisionCanBacktrack[208]);

            int LA208_0 = input.LA(1);

            if ( (LA208_0==DOT) ) {
                alt208=1;
            }
            else if ( (LA208_0==SASS_INCLUDE) ) {
                alt208=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 208, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(208);}

            switch (alt208) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(1012,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(1012,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call6374); if (state.failed) return ;
                    dbg.location(1012,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call6376);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(1014,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(1014,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call6398); if (state.failed) return ;
                    dbg.location(1014,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call6400);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1014,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call6402);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(208);}

            dbg.location(1016,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:5: ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )?
            int alt212=2;
            try { dbg.enterSubRule(212);
            try { dbg.enterDecision(212, decisionCanBacktrack[212]);

            try {
                isCyclicDecision = true;
                alt212 = dfa212.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(212);}

            switch (alt212) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:6: ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(1016,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:6: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:6: ws
                            {
                            dbg.location(1016,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call6415);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(209);}

                    dbg.location(1016,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call6418); if (state.failed) return ;
                    dbg.location(1016,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:17: ws
                            {
                            dbg.location(1016,17);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call6420);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(210);}

                    dbg.location(1016,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:21: ( cp_mixin_call_args )?
                    int alt211=2;
                    try { dbg.enterSubRule(211);
                    try { dbg.enterDecision(211, decisionCanBacktrack[211]);

                    int LA211_0 = input.LA(1);

                    if ( ((LA211_0>=IDENT && LA211_0<=URI)||LA211_0==MEDIA_SYM||(LA211_0>=GEN && LA211_0<=LPAREN)||LA211_0==AT_IDENT||LA211_0==PERCENTAGE||LA211_0==PLUS||LA211_0==MINUS||LA211_0==HASH||(LA211_0>=NUMBER && LA211_0<=DIMENSION)||LA211_0==SASS_VAR) ) {
                        alt211=1;
                    }
                    } finally {dbg.exitDecision(211);}

                    switch (alt211) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:21: cp_mixin_call_args
                            {
                            dbg.location(1016,21);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call6423);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(211);}

                    dbg.location(1016,41);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call6426); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(212);}

            dbg.location(1016,50);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:50: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:50: ws
                    {
                    dbg.location(1016,50);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call6430);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(213);}

            dbg.location(1016,54);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call6433); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1017, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1019:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1019, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1020:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1021:5: IDENT
            {
            dbg.location(1021,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name6462); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1022, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:1: cp_mixin_call_args : cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1024, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1025:5: ( cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:5: cp_mixin_call_arg ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )*
            {
            dbg.location(1028,5);
            pushFollow(FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6498);
            cp_mixin_call_arg();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1028,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:23: ( ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg )*
            try { dbg.enterSubRule(215);

            loop215:
            do {
                int alt215=2;
                try { dbg.enterDecision(215, decisionCanBacktrack[215]);

                int LA215_0 = input.LA(1);

                if ( (LA215_0==SEMI||LA215_0==COMMA) ) {
                    alt215=1;
                }


                } finally {dbg.exitDecision(215);}

                switch (alt215) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:25: ( COMMA | SEMI ) ( ws )? cp_mixin_call_arg
            	    {
            	    dbg.location(1028,25);
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

            	    dbg.location(1028,40);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:40: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:40: ws
            	            {
            	            dbg.location(1028,40);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args6510);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(214);}

            	    dbg.location(1028,44);
            	    pushFollow(FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6513);
            	    cp_mixin_call_arg();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop215;
                }
            } while (true);
            } finally {dbg.exitSubRule(215);}


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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_args"


    // $ANTLR start "cp_mixin_call_arg"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1031:1: cp_mixin_call_arg : ( cp_arg | cp_expression );
    public final void cp_mixin_call_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1031, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:5: ( cp_arg | cp_expression )
            int alt216=2;
            try { dbg.enterDecision(216, decisionCanBacktrack[216]);

            switch ( input.LA(1) ) {
            case MEDIA_SYM:
            case AT_IDENT:
                {
                int LA216_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt216=1;
                }
                else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt216=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 216, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                }
                break;
            case SASS_VAR:
                {
                int LA216_2 = input.LA(2);

                if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt216=1;
                }
                else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt216=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 216, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                }
                break;
            case IDENT:
            case STRING:
            case URI:
            case GEN:
            case LPAREN:
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
                alt216=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 216, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(216);}

            switch (alt216) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:5: cp_arg
                    {
                    dbg.location(1034,5);
                    pushFollow(FOLLOW_cp_arg_in_cp_mixin_call_arg6546);
                    cp_arg();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1035:7: cp_expression
                    {
                    dbg.location(1035,7);
                    pushFollow(FOLLOW_cp_expression_in_cp_mixin_call_arg6554);
                    cp_expression();

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
        dbg.location(1038, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:1: cp_args_list : ( ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void cp_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1041, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:5: ( ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt221=2;
            try { dbg.enterDecision(221, decisionCanBacktrack[221]);

            int LA221_0 = input.LA(1);

            if ( (LA221_0==MEDIA_SYM||LA221_0==AT_IDENT||LA221_0==SASS_VAR) ) {
                alt221=1;
            }
            else if ( ((LA221_0>=LESS_DOTS && LA221_0<=LESS_REST)) ) {
                alt221=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 221, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(221);}

            switch (alt221) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:5: ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(1045,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:5: ( cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:7: cp_arg ( ( COMMA | SEMI ) ( ws )? cp_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(1045,7);
                    pushFollow(FOLLOW_cp_arg_in_cp_args_list6591);
                    cp_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1045,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:14: ( ( COMMA | SEMI ) ( ws )? cp_arg )*
                    try { dbg.enterSubRule(218);

                    loop218:
                    do {
                        int alt218=2;
                        try { dbg.enterDecision(218, decisionCanBacktrack[218]);

                        try {
                            isCyclicDecision = true;
                            alt218 = dfa218.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(218);}

                        switch (alt218) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:16: ( COMMA | SEMI ) ( ws )? cp_arg
                    	    {
                    	    dbg.location(1045,16);
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

                    	    dbg.location(1045,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:33: ws
                    	            {
                    	            dbg.location(1045,33);
                    	            pushFollow(FOLLOW_ws_in_cp_args_list6605);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(217);}

                    	    dbg.location(1045,37);
                    	    pushFollow(FOLLOW_cp_arg_in_cp_args_list6608);
                    	    cp_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop218;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(218);}

                    dbg.location(1045,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:46: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt220=2;
                    try { dbg.enterSubRule(220);
                    try { dbg.enterDecision(220, decisionCanBacktrack[220]);

                    int LA220_0 = input.LA(1);

                    if ( (LA220_0==SEMI||LA220_0==COMMA) ) {
                        alt220=1;
                    }
                    } finally {dbg.exitDecision(220);}

                    switch (alt220) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:48: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(1045,48);
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

                            dbg.location(1045,65);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:65: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:65: ws
                                    {
                                    dbg.location(1045,65);
                                    pushFollow(FOLLOW_ws_in_cp_args_list6624);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(219);}

                            dbg.location(1045,69);
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
                    } finally {dbg.exitSubRule(220);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1047:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(1047,5);
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
        dbg.location(1048, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1051:1: cp_arg : cp_variable ( ( ws )? COLON ( ws )? cp_expression )? ;
    public final void cp_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1051, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:5: ( cp_variable ( ( ws )? COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:5: cp_variable ( ( ws )? COLON ( ws )? cp_expression )?
            {
            dbg.location(1053,5);
            pushFollow(FOLLOW_cp_variable_in_cp_arg6681);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1053,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:17: ( ( ws )? COLON ( ws )? cp_expression )?
            int alt224=2;
            try { dbg.enterSubRule(224);
            try { dbg.enterDecision(224, decisionCanBacktrack[224]);

            int LA224_0 = input.LA(1);

            if ( (LA224_0==COLON||LA224_0==WS||(LA224_0>=NL && LA224_0<=COMMENT)) ) {
                alt224=1;
            }
            } finally {dbg.exitDecision(224);}

            switch (alt224) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:19: ( ws )? COLON ( ws )? cp_expression
                    {
                    dbg.location(1053,19);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:19: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:19: ws
                            {
                            dbg.location(1053,19);
                            pushFollow(FOLLOW_ws_in_cp_arg6685);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(222);}

                    dbg.location(1053,23);
                    match(input,COLON,FOLLOW_COLON_in_cp_arg6688); if (state.failed) return ;
                    dbg.location(1053,29);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:29: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:29: ws
                            {
                            dbg.location(1053,29);
                            pushFollow(FOLLOW_ws_in_cp_arg6690);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(223);}

                    dbg.location(1053,33);
                    pushFollow(FOLLOW_cp_expression_in_cp_arg6693);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(224);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1054, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1058, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(1060,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded6719); if (state.failed) return ;
            dbg.location(1060,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:15: ws
                    {
                    dbg.location(1060,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded6721);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(225);}

            dbg.location(1060,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6724);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1060,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(227);

            loop227:
            do {
                int alt227=2;
                try { dbg.enterDecision(227, decisionCanBacktrack[227]);

                int LA227_0 = input.LA(1);

                if ( (LA227_0==COMMA||LA227_0==AND) ) {
                    alt227=1;
                }


                } finally {dbg.exitDecision(227);}

                switch (alt227) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(1060,36);
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

            	    dbg.location(1060,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:50: ws
            	            {
            	            dbg.location(1060,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded6736);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(226);}

            	    dbg.location(1060,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded6739);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(1061, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1065, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1066:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(1067,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:5: ( NOT ( ws )? )?
            int alt229=2;
            try { dbg.enterSubRule(229);
            try { dbg.enterDecision(229, decisionCanBacktrack[229]);

            int LA229_0 = input.LA(1);

            if ( (LA229_0==NOT) ) {
                alt229=1;
            }
            } finally {dbg.exitDecision(229);}

            switch (alt229) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:6: NOT ( ws )?
                    {
                    dbg.location(1067,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition6769); if (state.failed) return ;
                    dbg.location(1067,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:10: ws
                            {
                            dbg.location(1067,10);
                            pushFollow(FOLLOW_ws_in_less_condition6771);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(228);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(229);}

            dbg.location(1068,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition6780); if (state.failed) return ;
            dbg.location(1068,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:12: ( ws )?
            int alt230=2;
            try { dbg.enterSubRule(230);
            try { dbg.enterDecision(230, decisionCanBacktrack[230]);

            int LA230_0 = input.LA(1);

            if ( (LA230_0==WS||(LA230_0>=NL && LA230_0<=COMMENT)) ) {
                alt230=1;
            }
            } finally {dbg.exitDecision(230);}

            switch (alt230) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:12: ws
                    {
                    dbg.location(1068,12);
                    pushFollow(FOLLOW_ws_in_less_condition6782);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(230);}

            dbg.location(1069,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt235=2;
            try { dbg.enterSubRule(235);
            try { dbg.enterDecision(235, decisionCanBacktrack[235]);

            int LA235_0 = input.LA(1);

            if ( (LA235_0==IDENT) ) {
                alt235=1;
            }
            else if ( (LA235_0==MEDIA_SYM||LA235_0==AT_IDENT||LA235_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1070,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition6808);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1070,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:40: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1070:40: ws
                            {
                            dbg.location(1070,40);
                            pushFollow(FOLLOW_ws_in_less_condition6810);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(231);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1072,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1072,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition6841);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1072,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt234=2;
                    try { dbg.enterSubRule(234);
                    try { dbg.enterDecision(234, decisionCanBacktrack[234]);

                    int LA234_0 = input.LA(1);

                    if ( (LA234_0==WS||LA234_0==GREATER||LA234_0==OPEQ||(LA234_0>=NL && LA234_0<=COMMENT)||(LA234_0>=GREATER_OR_EQ && LA234_0<=LESS_OR_EQ)) ) {
                        alt234=1;
                    }
                    } finally {dbg.exitDecision(234);}

                    switch (alt234) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1072,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:28: ws
                                    {
                                    dbg.location(1072,28);
                                    pushFollow(FOLLOW_ws_in_less_condition6844);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(232);}

                            dbg.location(1072,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition6847);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1072,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:56: ws
                                    {
                                    dbg.location(1072,56);
                                    pushFollow(FOLLOW_ws_in_less_condition6849);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(233);}

                            dbg.location(1072,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition6852);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(234);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(235);}

            dbg.location(1074,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition6881); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1075, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1078:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1078, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1080,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition6907);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1080,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:18: ws
                    {
                    dbg.location(1080,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6909);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(236);}

            dbg.location(1080,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition6912); if (state.failed) return ;
            dbg.location(1080,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:29: ws
                    {
                    dbg.location(1080,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6914);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(237);}

            dbg.location(1080,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition6917);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1080,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:45: ws
                    {
                    dbg.location(1080,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6919);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(238);}

            dbg.location(1080,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition6922); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_function_in_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function_in_condition"


    // $ANTLR start "less_fn_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1084:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1084, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1085:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1086:5: IDENT
            {
            dbg.location(1086,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name6944); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1087, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1089:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1089, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1090,5);
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
        dbg.location(1092, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:1: scss_selector_interpolation_expression : ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1110, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1111:5: ( ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1112:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*
            {
            dbg.location(1112,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1112:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
            int alt239=2;
            try { dbg.enterSubRule(239);
            try { dbg.enterDecision(239, decisionCanBacktrack[239]);

            try {
                isCyclicDecision = true;
                alt239 = dfa239.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(239);}

            switch (alt239) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1113:13: scss_interpolation_expression_var
                    {
                    dbg.location(1113,13);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression7037);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1115:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND )
                    {
                    dbg.location(1115,13);
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
            } finally {dbg.exitSubRule(239);}

            dbg.location(1117,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1117:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*
            try { dbg.enterSubRule(242);

            loop242:
            do {
                int alt242=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:13: ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
            	    {
            	    dbg.location(1118,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1118:13: ws
            	            {
            	            dbg.location(1118,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression7126);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(240);}

            	    dbg.location(1119,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1119:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )
            	    int alt241=2;
            	    try { dbg.enterSubRule(241);
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1120:17: scss_interpolation_expression_var
            	            {
            	            dbg.location(1120,17);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression7159);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1122:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND )
            	            {
            	            dbg.location(1122,17);
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
            	    } finally {dbg.exitSubRule(241);}


            	    }
            	    break;

            	default :
            	    break loop242;
                }
            } while (true);
            } finally {dbg.exitSubRule(242);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1126, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1128:1: scss_declaration_interpolation_expression : ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1128, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1129:5: ( ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1130,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1130:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt243=2;
            try { dbg.enterSubRule(243);
            try { dbg.enterDecision(243, decisionCanBacktrack[243]);

            int LA243_0 = input.LA(1);

            if ( (LA243_0==HASH_SYMBOL) ) {
                int LA243_1 = input.LA(2);

                if ( (LA243_1==LBRACE) ) {
                    alt243=1;
                }
                else if ( (LA243_1==IDENT||LA243_1==COLON||LA243_1==WS||(LA243_1>=MINUS && LA243_1<=DOT)||(LA243_1>=NL && LA243_1<=COMMENT)) ) {
                    alt243=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 243, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA243_0==IDENT||LA243_0==MINUS||(LA243_0>=HASH && LA243_0<=DOT)) ) {
                alt243=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 243, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(243);}

            switch (alt243) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1131:13: scss_interpolation_expression_var
                    {
                    dbg.location(1131,13);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7291);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1133,13);
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
            } finally {dbg.exitSubRule(243);}

            dbg.location(1135,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1135:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(246);

            loop246:
            do {
                int alt246=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:13: ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1136,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:13: ( ws )?
            	    int alt244=2;
            	    try { dbg.enterSubRule(244);
            	    try { dbg.enterDecision(244, decisionCanBacktrack[244]);

            	    int LA244_0 = input.LA(1);

            	    if ( (LA244_0==WS||(LA244_0>=NL && LA244_0<=COMMENT)) ) {
            	        alt244=1;
            	    }
            	    } finally {dbg.exitDecision(244);}

            	    switch (alt244) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1136:13: ws
            	            {
            	            dbg.location(1136,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression7372);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(244);}

            	    dbg.location(1137,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1137:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt245=2;
            	    try { dbg.enterSubRule(245);
            	    try { dbg.enterDecision(245, decisionCanBacktrack[245]);

            	    int LA245_0 = input.LA(1);

            	    if ( (LA245_0==HASH_SYMBOL) ) {
            	        int LA245_1 = input.LA(2);

            	        if ( (LA245_1==LBRACE) ) {
            	            alt245=1;
            	        }
            	        else if ( (LA245_1==IDENT||LA245_1==COLON||LA245_1==WS||(LA245_1>=MINUS && LA245_1<=DOT)||(LA245_1>=NL && LA245_1<=COMMENT)) ) {
            	            alt245=2;
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return ;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 245, 1, input);

            	            dbg.recognitionException(nvae);
            	            throw nvae;
            	        }
            	    }
            	    else if ( (LA245_0==IDENT||LA245_0==MINUS||(LA245_0>=HASH && LA245_0<=DOT)) ) {
            	        alt245=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 245, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(245);}

            	    switch (alt245) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1138:17: scss_interpolation_expression_var
            	            {
            	            dbg.location(1138,17);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7405);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1140:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1140,17);
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
            	    } finally {dbg.exitSubRule(245);}


            	    }
            	    break;

            	default :
            	    break loop246;
                }
            } while (true);
            } finally {dbg.exitSubRule(246);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1144, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1146:1: scss_declaration_property_value_interpolation_expression : ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )* ;
    public final void scss_declaration_property_value_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_property_value_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1146, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1147:5: ( ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1148:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )*
            {
            dbg.location(1148,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1148:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            int alt247=2;
            try { dbg.enterSubRule(247);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1149:13: scss_interpolation_expression_var
                    {
                    dbg.location(1149,13);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7525);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1151:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS )
                    {
                    dbg.location(1151,13);
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
            } finally {dbg.exitSubRule(247);}

            dbg.location(1153,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1153:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) ) )*
            try { dbg.enterSubRule(250);

            loop250:
            do {
                int alt250=2;
                try { dbg.enterDecision(250, decisionCanBacktrack[250]);

                int LA250_0 = input.LA(1);

                if ( (LA250_0==IDENT||LA250_0==WS||LA250_0==SOLIDUS||(LA250_0>=MINUS && LA250_0<=DOT)||(LA250_0>=NL && LA250_0<=COMMENT)) ) {
                    alt250=1;
                }


                } finally {dbg.exitDecision(250);}

                switch (alt250) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1154:13: ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            	    {
            	    dbg.location(1154,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1154:13: ( ws )?
            	    int alt248=2;
            	    try { dbg.enterSubRule(248);
            	    try { dbg.enterDecision(248, decisionCanBacktrack[248]);

            	    int LA248_0 = input.LA(1);

            	    if ( (LA248_0==WS||(LA248_0>=NL && LA248_0<=COMMENT)) ) {
            	        alt248=1;
            	    }
            	    } finally {dbg.exitDecision(248);}

            	    switch (alt248) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1154:13: ws
            	            {
            	            dbg.location(1154,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_property_value_interpolation_expression7610);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(248);}

            	    dbg.location(1155,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1155:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )
            	    int alt249=2;
            	    try { dbg.enterSubRule(249);
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1156:17: scss_interpolation_expression_var
            	            {
            	            dbg.location(1156,17);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7643);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1158:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS )
            	            {
            	            dbg.location(1158,17);
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
            	    } finally {dbg.exitSubRule(249);}


            	    }
            	    break;

            	default :
            	    break loop250;
                }
            } while (true);
            } finally {dbg.exitSubRule(250);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1162, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1164:1: scss_mq_interpolation_expression : ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* ;
    public final void scss_mq_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_mq_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1164, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1165:5: ( ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1166:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            {
            dbg.location(1166,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1166:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            int alt251=2;
            try { dbg.enterSubRule(251);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1167:13: scss_interpolation_expression_var
                    {
                    dbg.location(1167,13);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7771);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1169:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
                    {
                    dbg.location(1169,13);
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
            } finally {dbg.exitSubRule(251);}

            dbg.location(1171,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1171:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            try { dbg.enterSubRule(254);

            loop254:
            do {
                int alt254=2;
                try { dbg.enterDecision(254, decisionCanBacktrack[254]);

                try {
                    isCyclicDecision = true;
                    alt254 = dfa254.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(254);}

                switch (alt254) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1172:13: ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    {
            	    dbg.location(1172,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1172:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1172:13: ws
            	            {
            	            dbg.location(1172,13);
            	            pushFollow(FOLLOW_ws_in_scss_mq_interpolation_expression7864);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(252);}

            	    dbg.location(1173,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1173:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    int alt253=2;
            	    try { dbg.enterSubRule(253);
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1174:17: scss_interpolation_expression_var
            	            {
            	            dbg.location(1174,17);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7897);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1176:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
            	            {
            	            dbg.location(1176,17);
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
            	    } finally {dbg.exitSubRule(253);}


            	    }
            	    break;

            	default :
            	    break loop254;
                }
            } while (true);
            } finally {dbg.exitSubRule(254);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1180, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1182:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition | IDENT ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1182, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1183:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition | IDENT ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition | IDENT ) ( ws )? RBRACE
            {
            dbg.location(1184,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var8018); if (state.failed) return ;
            dbg.location(1184,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var8020); if (state.failed) return ;
            dbg.location(1184,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:28: ws
                    {
                    dbg.location(1184,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var8022);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(255);}

            dbg.location(1184,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:32: ( cp_variable | less_function_in_condition | IDENT )
            int alt256=3;
            try { dbg.enterSubRule(256);
            try { dbg.enterDecision(256, decisionCanBacktrack[256]);

            try {
                isCyclicDecision = true;
                alt256 = dfa256.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(256);}

            switch (alt256) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:34: cp_variable
                    {
                    dbg.location(1184,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var8027);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:48: less_function_in_condition
                    {
                    dbg.location(1184,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var8031);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:77: IDENT
                    {
                    dbg.location(1184,77);
                    match(input,IDENT,FOLLOW_IDENT_in_scss_interpolation_expression_var8035); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(256);}

            dbg.location(1184,85);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:85: ( ws )?
            int alt257=2;
            try { dbg.enterSubRule(257);
            try { dbg.enterDecision(257, decisionCanBacktrack[257]);

            int LA257_0 = input.LA(1);

            if ( (LA257_0==WS||(LA257_0>=NL && LA257_0<=COMMENT)) ) {
                alt257=1;
            }
            } finally {dbg.exitDecision(257);}

            switch (alt257) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1184:85: ws
                    {
                    dbg.location(1184,85);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var8039);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(257);}

            dbg.location(1184,89);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var8042); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1185, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1205:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1205, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1206:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1207,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties8086);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1207,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties8088); if (state.failed) return ;
            dbg.location(1207,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:20: ( ws )?
            int alt258=2;
            try { dbg.enterSubRule(258);
            try { dbg.enterDecision(258, decisionCanBacktrack[258]);

            int LA258_0 = input.LA(1);

            if ( (LA258_0==WS||(LA258_0>=NL && LA258_0<=COMMENT)) ) {
                alt258=1;
            }
            } finally {dbg.exitDecision(258);}

            switch (alt258) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:20: ws
                    {
                    dbg.location(1207,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties8090);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(258);}

            dbg.location(1207,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:24: ( propertyValue )?
            int alt259=2;
            try { dbg.enterSubRule(259);
            try { dbg.enterDecision(259, decisionCanBacktrack[259]);

            int LA259_0 = input.LA(1);

            if ( ((LA259_0>=IDENT && LA259_0<=URI)||LA259_0==MEDIA_SYM||(LA259_0>=GEN && LA259_0<=LPAREN)||LA259_0==AT_IDENT||LA259_0==PERCENTAGE||(LA259_0>=SOLIDUS && LA259_0<=PLUS)||(LA259_0>=MINUS && LA259_0<=DOT)||(LA259_0>=NUMBER && LA259_0<=DIMENSION)||LA259_0==SASS_VAR) ) {
                alt259=1;
            }
            } finally {dbg.exitDecision(259);}

            switch (alt259) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:24: propertyValue
                    {
                    dbg.location(1207,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties8093);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(259);}

            dbg.location(1207,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties8096); if (state.failed) return ;
            dbg.location(1207,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:46: ( ws )?
            int alt260=2;
            try { dbg.enterSubRule(260);
            try { dbg.enterDecision(260, decisionCanBacktrack[260]);

            int LA260_0 = input.LA(1);

            if ( (LA260_0==WS||(LA260_0>=NL && LA260_0<=COMMENT)) ) {
                alt260=1;
            }
            } finally {dbg.exitDecision(260);}

            switch (alt260) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1207:46: ws
                    {
                    dbg.location(1207,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties8098);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(260);}

            dbg.location(1207,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties8101);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1207,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties8103);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1207,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties8105); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1208, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1210:1: sass_extend : SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI ;
    public final void sass_extend() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1210, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1211:5: ( SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1212:5: SASS_EXTEND ws simpleSelectorSequence ( SASS_OPTIONAL ( ws )? )? SEMI
            {
            dbg.location(1212,5);
            match(input,SASS_EXTEND,FOLLOW_SASS_EXTEND_in_sass_extend8126); if (state.failed) return ;
            dbg.location(1212,17);
            pushFollow(FOLLOW_ws_in_sass_extend8128);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1212,20);
            pushFollow(FOLLOW_simpleSelectorSequence_in_sass_extend8130);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1212,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1212:43: ( SASS_OPTIONAL ( ws )? )?
            int alt262=2;
            try { dbg.enterSubRule(262);
            try { dbg.enterDecision(262, decisionCanBacktrack[262]);

            int LA262_0 = input.LA(1);

            if ( (LA262_0==SASS_OPTIONAL) ) {
                alt262=1;
            }
            } finally {dbg.exitDecision(262);}

            switch (alt262) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1212:44: SASS_OPTIONAL ( ws )?
                    {
                    dbg.location(1212,44);
                    match(input,SASS_OPTIONAL,FOLLOW_SASS_OPTIONAL_in_sass_extend8133); if (state.failed) return ;
                    dbg.location(1212,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1212:58: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1212:58: ws
                            {
                            dbg.location(1212,58);
                            pushFollow(FOLLOW_ws_in_sass_extend8135);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(261);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(262);}

            dbg.location(1212,64);
            match(input,SEMI,FOLLOW_SEMI_in_sass_extend8140); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1213, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1215:1: sass_extend_only_selector : SASS_EXTEND_ONLY_SELECTOR ;
    public final void sass_extend_only_selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_extend_only_selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1215, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1216:5: ( SASS_EXTEND_ONLY_SELECTOR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1217:5: SASS_EXTEND_ONLY_SELECTOR
            {
            dbg.location(1217,5);
            match(input,SASS_EXTEND_ONLY_SELECTOR,FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector8165); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1218, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1220:1: sass_debug : ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI ;
    public final void sass_debug() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_debug");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1220, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1221:5: ( ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1222:5: ( SASS_DEBUG | SASS_WARN ) ws cp_expression SEMI
            {
            dbg.location(1222,5);
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

            dbg.location(1222,32);
            pushFollow(FOLLOW_ws_in_sass_debug8196);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1222,35);
            pushFollow(FOLLOW_cp_expression_in_sass_debug8198);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1222,49);
            match(input,SEMI,FOLLOW_SEMI_in_sass_debug8200); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1223, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1225:1: sass_control : ( sass_if | sass_for | sass_each | sass_while );
    public final void sass_control() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1225, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1226:5: ( sass_if | sass_for | sass_each | sass_while )
            int alt263=4;
            try { dbg.enterDecision(263, decisionCanBacktrack[263]);

            switch ( input.LA(1) ) {
            case SASS_IF:
                {
                alt263=1;
                }
                break;
            case SASS_FOR:
                {
                alt263=2;
                }
                break;
            case SASS_EACH:
                {
                alt263=3;
                }
                break;
            case SASS_WHILE:
                {
                alt263=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 263, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(263);}

            switch (alt263) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1227:5: sass_if
                    {
                    dbg.location(1227,5);
                    pushFollow(FOLLOW_sass_if_in_sass_control8225);
                    sass_if();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1227:15: sass_for
                    {
                    dbg.location(1227,15);
                    pushFollow(FOLLOW_sass_for_in_sass_control8229);
                    sass_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1227:26: sass_each
                    {
                    dbg.location(1227,26);
                    pushFollow(FOLLOW_sass_each_in_sass_control8233);
                    sass_each();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1227:38: sass_while
                    {
                    dbg.location(1227,38);
                    pushFollow(FOLLOW_sass_while_in_sass_control8237);
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
        dbg.location(1228, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1230:1: sass_if : SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )? ;
    public final void sass_if() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_if");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1230, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1231:5: ( SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:5: SASS_IF ws sass_control_expression sass_control_block ( ( ws )? sass_else )?
            {
            dbg.location(1232,5);
            match(input,SASS_IF,FOLLOW_SASS_IF_in_sass_if8258); if (state.failed) return ;
            dbg.location(1232,13);
            pushFollow(FOLLOW_ws_in_sass_if8260);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1232,16);
            pushFollow(FOLLOW_sass_control_expression_in_sass_if8262);
            sass_control_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1232,40);
            pushFollow(FOLLOW_sass_control_block_in_sass_if8264);
            sass_control_block();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1232,59);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:59: ( ( ws )? sass_else )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:60: ( ws )? sass_else
                    {
                    dbg.location(1232,60);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:60: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1232:60: ws
                            {
                            dbg.location(1232,60);
                            pushFollow(FOLLOW_ws_in_sass_if8267);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(264);}

                    dbg.location(1232,64);
                    pushFollow(FOLLOW_sass_else_in_sass_if8270);
                    sass_else();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(265);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1233, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1235:1: sass_else : ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? );
    public final void sass_else() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_else");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1235, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1236:5: ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? )
            int alt271=2;
            try { dbg.enterDecision(271, decisionCanBacktrack[271]);

            try {
                isCyclicDecision = true;
                alt271 = dfa271.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(271);}

            switch (alt271) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1237:5: SASS_ELSE ( ws )? sass_control_block
                    {
                    dbg.location(1237,5);
                    match(input,SASS_ELSE,FOLLOW_SASS_ELSE_in_sass_else8297); if (state.failed) return ;
                    dbg.location(1237,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1237:15: ( ws )?
                    int alt266=2;
                    try { dbg.enterSubRule(266);
                    try { dbg.enterDecision(266, decisionCanBacktrack[266]);

                    int LA266_0 = input.LA(1);

                    if ( (LA266_0==WS||(LA266_0>=NL && LA266_0<=COMMENT)) ) {
                        alt266=1;
                    }
                    } finally {dbg.exitDecision(266);}

                    switch (alt266) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1237:15: ws
                            {
                            dbg.location(1237,15);
                            pushFollow(FOLLOW_ws_in_sass_else8299);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(266);}

                    dbg.location(1237,19);
                    pushFollow(FOLLOW_sass_control_block_in_sass_else8302);
                    sass_control_block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:5: SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )?
                    {
                    dbg.location(1239,5);
                    match(input,SASS_ELSE,FOLLOW_SASS_ELSE_in_sass_else8315); if (state.failed) return ;
                    dbg.location(1239,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:15: ws
                            {
                            dbg.location(1239,15);
                            pushFollow(FOLLOW_ws_in_sass_else8317);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(267);}

                    dbg.location(1239,19);
                    if ( !(evalPredicate("if".equalsIgnoreCase(input.LT(1).getText()),"\"if\".equalsIgnoreCase(input.LT(1).getText())")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "sass_else", "\"if\".equalsIgnoreCase(input.LT(1).getText())");
                    }
                    dbg.location(1239,67);
                    match(input,IDENT,FOLLOW_IDENT_in_sass_else8322); if (state.failed) return ;
                    dbg.location(1239,82);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:82: ( ws )?
                    int alt268=2;
                    try { dbg.enterSubRule(268);
                    try { dbg.enterDecision(268, decisionCanBacktrack[268]);

                    int LA268_0 = input.LA(1);

                    if ( (LA268_0==WS||(LA268_0>=NL && LA268_0<=COMMENT)) ) {
                        alt268=1;
                    }
                    } finally {dbg.exitDecision(268);}

                    switch (alt268) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:82: ws
                            {
                            dbg.location(1239,82);
                            pushFollow(FOLLOW_ws_in_sass_else8326);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(268);}

                    dbg.location(1239,86);
                    pushFollow(FOLLOW_sass_control_expression_in_sass_else8329);
                    sass_control_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1239,110);
                    pushFollow(FOLLOW_sass_control_block_in_sass_else8331);
                    sass_control_block();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1239,129);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:129: ( ( ws )? sass_else )?
                    int alt270=2;
                    try { dbg.enterSubRule(270);
                    try { dbg.enterDecision(270, decisionCanBacktrack[270]);

                    try {
                        isCyclicDecision = true;
                        alt270 = dfa270.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(270);}

                    switch (alt270) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:130: ( ws )? sass_else
                            {
                            dbg.location(1239,130);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:130: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1239:130: ws
                                    {
                                    dbg.location(1239,130);
                                    pushFollow(FOLLOW_ws_in_sass_else8334);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(269);}

                            dbg.location(1239,134);
                            pushFollow(FOLLOW_sass_else_in_sass_else8337);
                            sass_else();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(270);}


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
        dbg.location(1240, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1242:1: sass_control_expression : sass_control_expression_condition ( ( OR | AND ) ( ws )? sass_control_expression_condition )* ;
    public final void sass_control_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1242, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1243:5: ( sass_control_expression_condition ( ( OR | AND ) ( ws )? sass_control_expression_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1244:5: sass_control_expression_condition ( ( OR | AND ) ( ws )? sass_control_expression_condition )*
            {
            dbg.location(1244,5);
            pushFollow(FOLLOW_sass_control_expression_condition_in_sass_control_expression8360);
            sass_control_expression_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1244,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1244:39: ( ( OR | AND ) ( ws )? sass_control_expression_condition )*
            try { dbg.enterSubRule(273);

            loop273:
            do {
                int alt273=2;
                try { dbg.enterDecision(273, decisionCanBacktrack[273]);

                int LA273_0 = input.LA(1);

                if ( (LA273_0==AND||LA273_0==OR) ) {
                    alt273=1;
                }


                } finally {dbg.exitDecision(273);}

                switch (alt273) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1244:41: ( OR | AND ) ( ws )? sass_control_expression_condition
            	    {
            	    dbg.location(1244,41);
            	    if ( input.LA(1)==AND||input.LA(1)==OR ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(1244,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1244:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1244:52: ws
            	            {
            	            dbg.location(1244,52);
            	            pushFollow(FOLLOW_ws_in_sass_control_expression8372);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(272);}

            	    dbg.location(1244,56);
            	    pushFollow(FOLLOW_sass_control_expression_condition_in_sass_control_expression8375);
            	    sass_control_expression_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop273;
                }
            } while (true);
            } finally {dbg.exitSubRule(273);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1246, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_control_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control_expression"


    // $ANTLR start "sass_control_expression_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1248:1: sass_control_expression_condition : ( NOT ( ws )? )? cp_expression ( ( CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )? ;
    public final void sass_control_expression_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control_expression_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1248, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1249:5: ( ( NOT ( ws )? )? cp_expression ( ( CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:5: ( NOT ( ws )? )? cp_expression ( ( CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )?
            {
            dbg.location(1250,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:5: ( NOT ( ws )? )?
            int alt275=2;
            try { dbg.enterSubRule(275);
            try { dbg.enterDecision(275, decisionCanBacktrack[275]);

            int LA275_0 = input.LA(1);

            if ( (LA275_0==NOT) ) {
                alt275=1;
            }
            } finally {dbg.exitDecision(275);}

            switch (alt275) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:6: NOT ( ws )?
                    {
                    dbg.location(1250,6);
                    match(input,NOT,FOLLOW_NOT_in_sass_control_expression_condition8404); if (state.failed) return ;
                    dbg.location(1250,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:10: ws
                            {
                            dbg.location(1250,10);
                            pushFollow(FOLLOW_ws_in_sass_control_expression_condition8406);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(274);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(275);}

            dbg.location(1250,16);
            pushFollow(FOLLOW_cp_expression_in_sass_control_expression_condition8411);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1250,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:30: ( ( CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression )?
            int alt277=2;
            try { dbg.enterSubRule(277);
            try { dbg.enterDecision(277, decisionCanBacktrack[277]);

            int LA277_0 = input.LA(1);

            if ( (LA277_0==GREATER||(LA277_0>=GREATER_OR_EQ && LA277_0<=LESS_OR_EQ)||(LA277_0>=CP_EQ && LA277_0<=CP_NOT_EQ)) ) {
                alt277=1;
            }
            } finally {dbg.exitDecision(277);}

            switch (alt277) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:31: ( CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ ) ( ws )? cp_expression
                    {
                    dbg.location(1250,31);
                    if ( input.LA(1)==GREATER||(input.LA(1)>=GREATER_OR_EQ && input.LA(1)<=LESS_OR_EQ)||(input.LA(1)>=CP_EQ && input.LA(1)<=CP_NOT_EQ) ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(1250,98);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:98: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1250:98: ws
                            {
                            dbg.location(1250,98);
                            pushFollow(FOLLOW_ws_in_sass_control_expression_condition8439);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(276);}

                    dbg.location(1250,102);
                    pushFollow(FOLLOW_cp_expression_in_sass_control_expression_condition8442);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(277);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1251, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "sass_control_expression_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control_expression_condition"


    // $ANTLR start "sass_for"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1253:1: sass_for : SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block ;
    public final void sass_for() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_for");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1253, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1254:5: ( SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1255:5: SASS_FOR ws cp_variable ws IDENT ws cp_term IDENT ws cp_term sass_control_block
            {
            dbg.location(1255,5);
            match(input,SASS_FOR,FOLLOW_SASS_FOR_in_sass_for8465); if (state.failed) return ;
            dbg.location(1255,14);
            pushFollow(FOLLOW_ws_in_sass_for8467);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,17);
            pushFollow(FOLLOW_cp_variable_in_sass_for8469);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,29);
            pushFollow(FOLLOW_ws_in_sass_for8471);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,32);
            match(input,IDENT,FOLLOW_IDENT_in_sass_for8473); if (state.failed) return ;
            dbg.location(1255,47);
            pushFollow(FOLLOW_ws_in_sass_for8477);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,50);
            pushFollow(FOLLOW_cp_term_in_sass_for8479);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,58);
            match(input,IDENT,FOLLOW_IDENT_in_sass_for8481); if (state.failed) return ;
            dbg.location(1255,71);
            pushFollow(FOLLOW_ws_in_sass_for8485);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,74);
            pushFollow(FOLLOW_cp_term_in_sass_for8487);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1255,82);
            pushFollow(FOLLOW_sass_control_block_in_sass_for8489);
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
        dbg.location(1256, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1258:1: sass_each : SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block ;
    public final void sass_each() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_each");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1258, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1259:5: ( SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1260:5: SASS_EACH ws cp_variable ws IDENT ws sass_each_list sass_control_block
            {
            dbg.location(1260,5);
            match(input,SASS_EACH,FOLLOW_SASS_EACH_in_sass_each8510); if (state.failed) return ;
            dbg.location(1260,15);
            pushFollow(FOLLOW_ws_in_sass_each8512);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1260,18);
            pushFollow(FOLLOW_cp_variable_in_sass_each8514);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1260,30);
            pushFollow(FOLLOW_ws_in_sass_each8516);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1260,33);
            match(input,IDENT,FOLLOW_IDENT_in_sass_each8518); if (state.failed) return ;
            dbg.location(1260,46);
            pushFollow(FOLLOW_ws_in_sass_each8522);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1260,49);
            pushFollow(FOLLOW_sass_each_list_in_sass_each8524);
            sass_each_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1260,64);
            pushFollow(FOLLOW_sass_control_block_in_sass_each8526);
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
        dbg.location(1261, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1263:1: sass_each_list : cp_term ( COMMA ( ws )? cp_term )* ;
    public final void sass_each_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_each_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1263, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1264:5: ( cp_term ( COMMA ( ws )? cp_term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:5: cp_term ( COMMA ( ws )? cp_term )*
            {
            dbg.location(1265,5);
            pushFollow(FOLLOW_cp_term_in_sass_each_list8551);
            cp_term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1265,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:13: ( COMMA ( ws )? cp_term )*
            try { dbg.enterSubRule(279);

            loop279:
            do {
                int alt279=2;
                try { dbg.enterDecision(279, decisionCanBacktrack[279]);

                int LA279_0 = input.LA(1);

                if ( (LA279_0==COMMA) ) {
                    alt279=1;
                }


                } finally {dbg.exitDecision(279);}

                switch (alt279) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:14: COMMA ( ws )? cp_term
            	    {
            	    dbg.location(1265,14);
            	    match(input,COMMA,FOLLOW_COMMA_in_sass_each_list8554); if (state.failed) return ;
            	    dbg.location(1265,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:20: ( ws )?
            	    int alt278=2;
            	    try { dbg.enterSubRule(278);
            	    try { dbg.enterDecision(278, decisionCanBacktrack[278]);

            	    int LA278_0 = input.LA(1);

            	    if ( (LA278_0==WS||(LA278_0>=NL && LA278_0<=COMMENT)) ) {
            	        alt278=1;
            	    }
            	    } finally {dbg.exitDecision(278);}

            	    switch (alt278) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1265:20: ws
            	            {
            	            dbg.location(1265,20);
            	            pushFollow(FOLLOW_ws_in_sass_each_list8556);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(278);}

            	    dbg.location(1265,24);
            	    pushFollow(FOLLOW_cp_term_in_sass_each_list8559);
            	    cp_term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop279;
                }
            } while (true);
            } finally {dbg.exitSubRule(279);}


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
            dbg.exitRule(getGrammarFileName(), "sass_each_list");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_each_list"


    // $ANTLR start "sass_while"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1268:1: sass_while : SASS_WHILE ws sass_control_expression sass_control_block ;
    public final void sass_while() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_while");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1268, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1269:5: ( SASS_WHILE ws sass_control_expression sass_control_block )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1270:5: SASS_WHILE ws sass_control_expression sass_control_block
            {
            dbg.location(1270,5);
            match(input,SASS_WHILE,FOLLOW_SASS_WHILE_in_sass_while8586); if (state.failed) return ;
            dbg.location(1270,16);
            pushFollow(FOLLOW_ws_in_sass_while8588);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1270,19);
            pushFollow(FOLLOW_sass_control_expression_in_sass_while8590);
            sass_control_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1270,43);
            pushFollow(FOLLOW_sass_control_block_in_sass_while8592);
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
        dbg.location(1271, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1273:1: sass_control_block : LBRACE ( ws )? declarations RBRACE ;
    public final void sass_control_block() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_control_block");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1273, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1274:5: ( LBRACE ( ws )? declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1275:5: LBRACE ( ws )? declarations RBRACE
            {
            dbg.location(1275,5);
            match(input,LBRACE,FOLLOW_LBRACE_in_sass_control_block8613); if (state.failed) return ;
            dbg.location(1275,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1275:12: ( ws )?
            int alt280=2;
            try { dbg.enterSubRule(280);
            try { dbg.enterDecision(280, decisionCanBacktrack[280]);

            int LA280_0 = input.LA(1);

            if ( (LA280_0==WS||(LA280_0>=NL && LA280_0<=COMMENT)) ) {
                alt280=1;
            }
            } finally {dbg.exitDecision(280);}

            switch (alt280) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1275:12: ws
                    {
                    dbg.location(1275,12);
                    pushFollow(FOLLOW_ws_in_sass_control_block8615);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(280);}

            dbg.location(1275,16);
            pushFollow(FOLLOW_declarations_in_sass_control_block8618);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1275,29);
            match(input,RBRACE,FOLLOW_RBRACE_in_sass_control_block8620); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "sass_control_block");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "sass_control_block"


    // $ANTLR start "sass_function_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1278:1: sass_function_declaration : SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? declarations RBRACE ;
    public final void sass_function_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1278, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1279:5: ( SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:5: SASS_FUNCTION ws sass_function_name ( ws )? LPAREN ( cp_args_list )? RPAREN ( ws )? LBRACE ( ws )? declarations RBRACE
            {
            dbg.location(1282,5);
            match(input,SASS_FUNCTION,FOLLOW_SASS_FUNCTION_in_sass_function_declaration8656); if (state.failed) return ;
            dbg.location(1282,19);
            pushFollow(FOLLOW_ws_in_sass_function_declaration8658);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1282,22);
            pushFollow(FOLLOW_sass_function_name_in_sass_function_declaration8660);
            sass_function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1282,41);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:41: ( ws )?
            int alt281=2;
            try { dbg.enterSubRule(281);
            try { dbg.enterDecision(281, decisionCanBacktrack[281]);

            int LA281_0 = input.LA(1);

            if ( (LA281_0==WS||(LA281_0>=NL && LA281_0<=COMMENT)) ) {
                alt281=1;
            }
            } finally {dbg.exitDecision(281);}

            switch (alt281) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:41: ws
                    {
                    dbg.location(1282,41);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8662);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(281);}

            dbg.location(1282,45);
            match(input,LPAREN,FOLLOW_LPAREN_in_sass_function_declaration8665); if (state.failed) return ;
            dbg.location(1282,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:52: ( cp_args_list )?
            int alt282=2;
            try { dbg.enterSubRule(282);
            try { dbg.enterDecision(282, decisionCanBacktrack[282]);

            int LA282_0 = input.LA(1);

            if ( (LA282_0==MEDIA_SYM||LA282_0==AT_IDENT||LA282_0==SASS_VAR||(LA282_0>=LESS_DOTS && LA282_0<=LESS_REST)) ) {
                alt282=1;
            }
            } finally {dbg.exitDecision(282);}

            switch (alt282) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:52: cp_args_list
                    {
                    dbg.location(1282,52);
                    pushFollow(FOLLOW_cp_args_list_in_sass_function_declaration8667);
                    cp_args_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(282);}

            dbg.location(1282,66);
            match(input,RPAREN,FOLLOW_RPAREN_in_sass_function_declaration8670); if (state.failed) return ;
            dbg.location(1282,73);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:73: ( ws )?
            int alt283=2;
            try { dbg.enterSubRule(283);
            try { dbg.enterDecision(283, decisionCanBacktrack[283]);

            int LA283_0 = input.LA(1);

            if ( (LA283_0==WS||(LA283_0>=NL && LA283_0<=COMMENT)) ) {
                alt283=1;
            }
            } finally {dbg.exitDecision(283);}

            switch (alt283) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:73: ws
                    {
                    dbg.location(1282,73);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8672);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(283);}

            dbg.location(1282,77);
            match(input,LBRACE,FOLLOW_LBRACE_in_sass_function_declaration8675); if (state.failed) return ;
            dbg.location(1282,84);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:84: ( ws )?
            int alt284=2;
            try { dbg.enterSubRule(284);
            try { dbg.enterDecision(284, decisionCanBacktrack[284]);

            int LA284_0 = input.LA(1);

            if ( (LA284_0==WS||(LA284_0>=NL && LA284_0<=COMMENT)) ) {
                alt284=1;
            }
            } finally {dbg.exitDecision(284);}

            switch (alt284) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1282:84: ws
                    {
                    dbg.location(1282,84);
                    pushFollow(FOLLOW_ws_in_sass_function_declaration8677);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(284);}

            dbg.location(1282,88);
            pushFollow(FOLLOW_declarations_in_sass_function_declaration8680);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1282,101);
            match(input,RBRACE,FOLLOW_RBRACE_in_sass_function_declaration8682); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1283, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1285:1: sass_function_name : IDENT ;
    public final void sass_function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1285, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1286:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1287:5: IDENT
            {
            dbg.location(1287,5);
            match(input,IDENT,FOLLOW_IDENT_in_sass_function_name8707); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1288, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1290:1: sass_function_return : SASS_RETURN ws cp_expression SEMI ;
    public final void sass_function_return() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_function_return");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1290, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1291:5: ( SASS_RETURN ws cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1292:5: SASS_RETURN ws cp_expression SEMI
            {
            dbg.location(1292,5);
            match(input,SASS_RETURN,FOLLOW_SASS_RETURN_in_sass_function_return8728); if (state.failed) return ;
            dbg.location(1292,17);
            pushFollow(FOLLOW_ws_in_sass_function_return8730);
            ws();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1292,20);
            pushFollow(FOLLOW_cp_expression_in_sass_function_return8732);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1292,34);
            match(input,SEMI,FOLLOW_SEMI_in_sass_function_return8734); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1293, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1295:1: sass_content : SASS_CONTENT ( ws )? SEMI ;
    public final void sass_content() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "sass_content");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1295, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1296:5: ( SASS_CONTENT ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1297:5: SASS_CONTENT ( ws )? SEMI
            {
            dbg.location(1297,5);
            match(input,SASS_CONTENT,FOLLOW_SASS_CONTENT_in_sass_content8759); if (state.failed) return ;
            dbg.location(1297,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1297:18: ( ws )?
            int alt285=2;
            try { dbg.enterSubRule(285);
            try { dbg.enterDecision(285, decisionCanBacktrack[285]);

            int LA285_0 = input.LA(1);

            if ( (LA285_0==WS||(LA285_0>=NL && LA285_0<=COMMENT)) ) {
                alt285=1;
            }
            } finally {dbg.exitDecision(285);}

            switch (alt285) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1297:18: ws
                    {
                    dbg.location(1297,18);
                    pushFollow(FOLLOW_ws_in_sass_content8761);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(285);}

            dbg.location(1297,22);
            match(input,SEMI,FOLLOW_SEMI_in_sass_content8764); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1298, 5);

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
        	    break loop286;
            }
        } while (true);
        } finally {dbg.exitSubRule(286);}

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
        int alt289=2;
        try { dbg.enterDecision(289, decisionCanBacktrack[289]);

        try {
            isCyclicDecision = true;
            alt289 = dfa289.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(289);}

        switch (alt289) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(376,18);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt287=0;
                try { dbg.enterSubRule(287);

                loop287:
                do {
                    int alt287=2;
                    try { dbg.enterDecision(287, decisionCanBacktrack[287]);

                    int LA287_0 = input.LA(1);

                    if ( (LA287_0==NAMESPACE_SYM||(LA287_0>=IDENT && LA287_0<=MEDIA_SYM)||(LA287_0>=AND && LA287_0<=LPAREN)||(LA287_0>=RPAREN && LA287_0<=LINE_COMMENT)) ) {
                        alt287=1;
                    }


                    } finally {dbg.exitDecision(287);}

                    switch (alt287) {
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
                	    if ( cnt287 >= 1 ) break loop287;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(287, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt287++;
                } while (true);
                } finally {dbg.exitSubRule(287);}

                dbg.location(376,47);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3625); if (state.failed) return ;
                dbg.location(376,53);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt288=0;
                try { dbg.enterSubRule(288);

                loop288:
                do {
                    int alt288=2;
                    try { dbg.enterDecision(288, decisionCanBacktrack[288]);

                    int LA288_0 = input.LA(1);

                    if ( (LA288_0==NAMESPACE_SYM||(LA288_0>=IDENT && LA288_0<=MEDIA_SYM)||(LA288_0>=AND && LA288_0<=LINE_COMMENT)) ) {
                        alt288=1;
                    }


                    } finally {dbg.exitDecision(288);}

                    switch (alt288) {
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
                	    if ( cnt288 >= 1 ) break loop288;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(288, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt288++;
                } while (true);
                } finally {dbg.exitSubRule(288);}

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:9: ( cp_mixin_call )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:10: cp_mixin_call
        {
        dbg.location(429,10);
        pushFollow(FOLLOW_cp_mixin_call_in_synpred4_Css31227);
        cp_mixin_call();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:9: ( (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:10: (~ ( HASH_SYMBOL | COLON ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(570,10);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:10: (~ ( HASH_SYMBOL | COLON ) )*
        try { dbg.enterSubRule(290);

        loop290:
        do {
            int alt290=2;
            try { dbg.enterDecision(290, decisionCanBacktrack[290]);

            int LA290_0 = input.LA(1);

            if ( ((LA290_0>=NAMESPACE_SYM && LA290_0<=LPAREN)||(LA290_0>=RPAREN && LA290_0<=MINUS)||(LA290_0>=HASH && LA290_0<=LINE_COMMENT)) ) {
                alt290=1;
            }


            } finally {dbg.exitDecision(290);}

            switch (alt290) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:10: ~ ( HASH_SYMBOL | COLON )
        	    {
        	    dbg.location(570,10);
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
        	    break loop290;
            }
        } while (true);
        } finally {dbg.exitSubRule(290);}

        dbg.location(570,32);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred5_Css32377); if (state.failed) return ;
        dbg.location(570,44);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32379); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:13: ( cp_mixin_declaration )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:14: cp_mixin_declaration
        {
        dbg.location(584,14);
        pushFollow(FOLLOW_cp_mixin_declaration_in_synpred6_Css32491);
        cp_mixin_declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:17: ( cp_variable_declaration )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:18: cp_variable_declaration
        {
        dbg.location(603,18);
        pushFollow(FOLLOW_cp_variable_declaration_in_synpred7_Css32638);
        cp_variable_declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:3: ( declaration SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:4: declaration SEMI
        {
        dbg.location(609,4);
        pushFollow(FOLLOW_declaration_in_synpred8_Css32719);
        declaration();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(609,16);
        match(input,SEMI,FOLLOW_SEMI_in_synpred8_Css32721); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI
        {
        dbg.location(613,4);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )*
        try { dbg.enterSubRule(291);

        loop291:
        do {
            int alt291=2;
            try { dbg.enterDecision(291, decisionCanBacktrack[291]);

            int LA291_0 = input.LA(1);

            if ( (LA291_0==NAMESPACE_SYM||(LA291_0>=IDENT && LA291_0<=MEDIA_SYM)||(LA291_0>=AND && LA291_0<=LPAREN)||(LA291_0>=RPAREN && LA291_0<=LINE_COMMENT)) ) {
                alt291=1;
            }


            } finally {dbg.exitDecision(291);}

            switch (alt291) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
        	    {
        	    dbg.location(613,4);
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
        	    break loop291;
            }
        } while (true);
        } finally {dbg.exitSubRule(291);}

        dbg.location(613,33);
        match(input,COLON,FOLLOW_COLON_in_synpred9_Css32798); if (state.failed) return ;
        dbg.location(613,39);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:39: (~ ( SEMI | LBRACE | RBRACE ) )*
        try { dbg.enterSubRule(292);

        loop292:
        do {
            int alt292=2;
            try { dbg.enterDecision(292, decisionCanBacktrack[292]);

            int LA292_0 = input.LA(1);

            if ( (LA292_0==NAMESPACE_SYM||(LA292_0>=IDENT && LA292_0<=MEDIA_SYM)||(LA292_0>=AND && LA292_0<=LINE_COMMENT)) ) {
                alt292=1;
            }


            } finally {dbg.exitDecision(292);}

            switch (alt292) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:39: ~ ( SEMI | LBRACE | RBRACE )
        	    {
        	    dbg.location(613,39);
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
        	    break loop292;
            }
        } while (true);
        } finally {dbg.exitSubRule(292);}

        dbg.location(613,62);
        match(input,SEMI,FOLLOW_SEMI_in_synpred9_Css32810); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:3: ( scss_nested_properties )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:4: scss_nested_properties
        {
        dbg.location(615,4);
        pushFollow(FOLLOW_scss_nested_properties_in_synpred10_Css32827);
        scss_nested_properties();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:17: ( rule )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:18: rule
        {
        dbg.location(617,18);
        pushFollow(FOLLOW_rule_in_synpred11_Css32856);
        rule();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:17: ( (~ SEMI )* SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:18: (~ SEMI )* SEMI
        {
        dbg.location(633,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:18: (~ SEMI )*
        try { dbg.enterSubRule(293);

        loop293:
        do {
            int alt293=2;
            try { dbg.enterDecision(293, decisionCanBacktrack[293]);

            int LA293_0 = input.LA(1);

            if ( (LA293_0==NAMESPACE_SYM||(LA293_0>=IDENT && LA293_0<=LINE_COMMENT)) ) {
                alt293=1;
            }


            } finally {dbg.exitDecision(293);}

            switch (alt293) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:18: ~ SEMI
        	    {
        	    dbg.location(633,18);
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
        	    break loop293;
            }
        } while (true);
        } finally {dbg.exitSubRule(293);}

        dbg.location(633,25);
        match(input,SEMI,FOLLOW_SEMI_in_synpred12_Css33191); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(641,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(294);

        loop294:
        do {
            int alt294=2;
            try { dbg.enterDecision(294, decisionCanBacktrack[294]);

            int LA294_0 = input.LA(1);

            if ( ((LA294_0>=NAMESPACE_SYM && LA294_0<=MEDIA_SYM)||(LA294_0>=RBRACE && LA294_0<=MINUS)||(LA294_0>=HASH && LA294_0<=LINE_COMMENT)) ) {
                alt294=1;
            }


            } finally {dbg.exitDecision(294);}

            switch (alt294) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(641,11);
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
        	    break loop294;
            }
        } while (true);
        } finally {dbg.exitSubRule(294);}

        dbg.location(641,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred13_Css33278); if (state.failed) return ;
        dbg.location(641,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred13_Css33280); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:19: esPred
        {
        dbg.location(654,19);
        pushFollow(FOLLOW_esPred_in_synpred14_Css33378);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:6: esPred
        {
        dbg.location(656,6);
        pushFollow(FOLLOW_esPred_in_synpred15_Css33399);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

    // $ANTLR start synpred16_Css3
    public final void synpred16_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(670,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:8: ( IDENT | STAR )?
        int alt295=2;
        try { dbg.enterSubRule(295);
        try { dbg.enterDecision(295, decisionCanBacktrack[295]);

        int LA295_0 = input.LA(1);

        if ( (LA295_0==IDENT||LA295_0==STAR) ) {
            alt295=1;
        }
        } finally {dbg.exitDecision(295);}

        switch (alt295) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(670,8);
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
        } finally {dbg.exitSubRule(295);}

        dbg.location(670,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred16_Css33517); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Css3

    // $ANTLR start synpred17_Css3
    public final void synpred17_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:9: ( (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:10: (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(779,10);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:10: (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )*
        try { dbg.enterSubRule(296);

        loop296:
        do {
            int alt296=2;
            try { dbg.enterDecision(296, decisionCanBacktrack[296]);

            int LA296_0 = input.LA(1);

            if ( (LA296_0==NAMESPACE_SYM||(LA296_0>=IDENT && LA296_0<=MEDIA_SYM)||(LA296_0>=AND && LA296_0<=MINUS)||(LA296_0>=HASH && LA296_0<=LINE_COMMENT)) ) {
                alt296=1;
            }


            } finally {dbg.exitDecision(296);}

            switch (alt296) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:10: ~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE )
        	    {
        	    dbg.location(779,10);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
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
        	    break loop296;
            }
        } while (true);
        } finally {dbg.exitSubRule(296);}

        dbg.location(779,45);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred17_Css34525); if (state.failed) return ;
        dbg.location(779,57);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred17_Css34527); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Css3

    // $ANTLR start synpred18_Css3
    public final void synpred18_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:12: expressionPredicate
        {
        dbg.location(780,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred18_Css34543);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Css3

    // $ANTLR start synpred19_Css3
    public final void synpred19_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:17: term
        {
        dbg.location(961,17);
        pushFollow(FOLLOW_term_in_synpred19_Css35917);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Css3

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


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA16 dfa16 = new DFA16(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA38 dfa38 = new DFA38(this);
    protected DFA58 dfa58 = new DFA58(this);
    protected DFA79 dfa79 = new DFA79(this);
    protected DFA105 dfa105 = new DFA105(this);
    protected DFA119 dfa119 = new DFA119(this);
    protected DFA124 dfa124 = new DFA124(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA145 dfa145 = new DFA145(this);
    protected DFA154 dfa154 = new DFA154(this);
    protected DFA158 dfa158 = new DFA158(this);
    protected DFA161 dfa161 = new DFA161(this);
    protected DFA167 dfa167 = new DFA167(this);
    protected DFA190 dfa190 = new DFA190(this);
    protected DFA194 dfa194 = new DFA194(this);
    protected DFA212 dfa212 = new DFA212(this);
    protected DFA218 dfa218 = new DFA218(this);
    protected DFA239 dfa239 = new DFA239(this);
    protected DFA242 dfa242 = new DFA242(this);
    protected DFA241 dfa241 = new DFA241(this);
    protected DFA246 dfa246 = new DFA246(this);
    protected DFA247 dfa247 = new DFA247(this);
    protected DFA249 dfa249 = new DFA249(this);
    protected DFA251 dfa251 = new DFA251(this);
    protected DFA254 dfa254 = new DFA254(this);
    protected DFA253 dfa253 = new DFA253(this);
    protected DFA256 dfa256 = new DFA256(this);
    protected DFA265 dfa265 = new DFA265(this);
    protected DFA271 dfa271 = new DFA271(this);
    protected DFA270 dfa270 = new DFA270(this);
    protected DFA289 dfa289 = new DFA289(this);
    static final String DFA4_eotS =
        "\42\uffff";
    static final String DFA4_eofS =
        "\1\2\41\uffff";
    static final String DFA4_minS =
        "\1\4\1\0\40\uffff";
    static final String DFA4_maxS =
        "\1\152\1\0\40\uffff";
    static final String DFA4_acceptS =
        "\2\uffff\1\2\36\uffff\1\1";
    static final String DFA4_specialS =
        "\1\uffff\1\0\40\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\2\1\uffff\1\2\3\uffff\1\1\1\uffff\1\2\5\uffff\1\2\1\uffff"+
            "\1\2\1\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\3\2\24\uffff"+
            "\11\2\1\uffff\1\2\25\uffff\3\2\10\uffff\3\2\4\uffff\4\2",
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
                        if ( (!(evalPredicate(((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))||evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")),""))) ) {s = 33;}

                        else if ( (((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))||evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))) ) {s = 2;}

                         
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
        "\1\152\30\uffff\1\0\10\uffff";
    static final String DFA16_acceptS =
        "\1\uffff\1\2\37\uffff\1\1";
    static final String DFA16_specialS =
        "\31\uffff\1\0\10\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\1\1\uffff\1\1\3\uffff\1\31\1\uffff\1\1\5\uffff\1\1\1\uffff"+
            "\1\1\1\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\3\1\24\uffff"+
            "\11\1\1\uffff\1\1\25\uffff\3\1\10\uffff\3\1\4\uffff\4\1",
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
        "\1\4\1\uffff\1\0\1\3\5\uffff\1\1\1\uffff\1\2\2\uffff}>";
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
                        int LA26_11 = input.LA(1);

                         
                        int index26_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index26_11);
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
        "\1\154\1\uffff\6\0\10\uffff\1\0\5\uffff\1\0\6\uffff\1\0\1\uffff";
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
            "\1\10\1\21\7\uffff\1\11\1\uffff\2\12\1\13\4\uffff\3\13\2\uffff"+
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
            return "()* loopback of 374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? sass_content ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | media ( ws )? )*";
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

                        else if ( (true) ) {s = 30;}

                         
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
        "\40\uffff";
    static final String DFA58_eofS =
        "\40\uffff";
    static final String DFA58_minS =
        "\1\6\1\0\16\uffff\1\0\5\uffff\1\0\11\uffff";
    static final String DFA58_maxS =
        "\1\152\1\0\16\uffff\1\0\5\uffff\1\0\11\uffff";
    static final String DFA58_acceptS =
        "\2\uffff\1\1\1\2\15\uffff\1\4\1\5\1\6\1\7\2\uffff\1\10\1\11\1\12"+
        "\1\13\3\uffff\1\14\1\3";
    static final String DFA58_specialS =
        "\1\0\1\1\16\uffff\1\2\5\uffff\1\3\11\uffff}>";
    static final String[] DFA58_transitionS = {
            "\1\3\3\uffff\1\30\1\uffff\1\20\5\uffff\1\3\1\uffff\1\3\1\uffff"+
            "\1\26\1\uffff\1\24\3\uffff\1\24\1\uffff\1\21\1\22\1\23\24\uffff"+
            "\3\3\1\1\5\3\1\uffff\1\3\25\uffff\1\27\1\3\1\2\10\uffff\2\31"+
            "\1\32\4\uffff\3\32\1\36",
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
            ""
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
            return "425:1: bodyItem : ( ( cp_mixin_call )=> cp_mixin_call | rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? importItem | {...}? sass_debug | {...}? sass_control | {...}? sass_function_declaration );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA58_0 = input.LA(1);

                         
                        int index58_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA58_0==DOT) ) {s = 1;}

                        else if ( (LA58_0==SASS_INCLUDE) && (synpred4_Css3())) {s = 2;}

                        else if ( (LA58_0==IDENT||LA58_0==GEN||LA58_0==COLON||(LA58_0>=MINUS && LA58_0<=HASH)||(LA58_0>=LBRACKET && LA58_0<=PIPE)||LA58_0==LESS_AND||LA58_0==SASS_MIXIN) ) {s = 3;}

                        else if ( (LA58_0==MEDIA_SYM) ) {s = 16;}

                        else if ( (LA58_0==PAGE_SYM) ) {s = 17;}

                        else if ( (LA58_0==COUNTER_STYLE_SYM) ) {s = 18;}

                        else if ( (LA58_0==FONT_FACE_SYM) ) {s = 19;}

                        else if ( (LA58_0==MOZ_DOCUMENT_SYM||LA58_0==WEBKIT_KEYFRAMES_SYM) ) {s = 20;}

                        else if ( (LA58_0==AT_IDENT) ) {s = 22;}

                        else if ( (LA58_0==SASS_VAR) ) {s = 23;}

                        else if ( (LA58_0==IMPORT_SYM) ) {s = 24;}

                        else if ( ((LA58_0>=SASS_DEBUG && LA58_0<=SASS_WARN)) ) {s = 25;}

                        else if ( (LA58_0==SASS_IF||(LA58_0>=SASS_FOR && LA58_0<=SASS_WHILE)) ) {s = 26;}

                        else if ( (LA58_0==SASS_FUNCTION) ) {s = 30;}

                         
                        input.seek(index58_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA58_1 = input.LA(1);

                         
                        int index58_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred4_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 2;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index58_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA58_16 = input.LA(1);

                         
                        int index58_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(evalPredicate((((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))),""))) ) {s = 31;}

                        else if ( ((((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 23;}

                         
                        input.seek(index58_16);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA58_22 = input.LA(1);

                         
                        int index58_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(evalPredicate((((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))),""))) ) {s = 20;}

                        else if ( ((((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 23;}

                         
                        input.seek(index58_22);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 58, _s, input);
            error(nvae);
            throw nvae;
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
            return "()* loopback of 493:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA105_eotS =
        "\7\uffff";
    static final String DFA105_eofS =
        "\7\uffff";
    static final String DFA105_minS =
        "\2\6\2\uffff\2\6\1\uffff";
    static final String DFA105_maxS =
        "\1\126\1\123\2\uffff\2\123\1\uffff";
    static final String DFA105_acceptS =
        "\2\uffff\1\1\1\2\2\uffff\1\1";
    static final String DFA105_specialS =
        "\1\0\3\uffff\1\2\1\1\1\uffff}>";
    static final String[] DFA105_transitionS = {
            "\1\3\13\uffff\1\3\1\uffff\1\3\40\uffff\3\3\1\1\5\3\1\uffff\1"+
            "\3\26\uffff\1\2",
            "\1\4\6\uffff\1\3\4\uffff\1\3\1\uffff\1\3\2\uffff\1\3\35\uffff"+
            "\4\3\6\uffff\1\3\22\uffff\2\3",
            "",
            "",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\6\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\6\1\3\2\uffff\1"+
            "\5\32\uffff\14\3\1\uffff\1\3\22\uffff\2\5",
            ""
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
            return "581:9: ( ( cp_mixin_declaration )=> cp_mixin_declaration | selectorsGroup )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA105_0 = input.LA(1);

                         
                        int index105_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA105_0==DOT) ) {s = 1;}

                        else if ( (LA105_0==SASS_MIXIN) && (synpred6_Css3())) {s = 2;}

                        else if ( (LA105_0==IDENT||LA105_0==GEN||LA105_0==COLON||(LA105_0>=MINUS && LA105_0<=HASH)||(LA105_0>=LBRACKET && LA105_0<=PIPE)||LA105_0==LESS_AND) ) {s = 3;}

                         
                        input.seek(index105_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA105_5 = input.LA(1);

                         
                        int index105_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA105_5==IDENT||LA105_5==COMMA||LA105_5==LBRACE||LA105_5==GEN||LA105_5==COLON||(LA105_5>=PLUS && LA105_5<=PIPE)||LA105_5==LESS_AND) ) {s = 3;}

                        else if ( (LA105_5==WS||(LA105_5>=NL && LA105_5<=COMMENT)) ) {s = 5;}

                        else if ( (LA105_5==LPAREN) && (synpred6_Css3())) {s = 6;}

                         
                        input.seek(index105_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA105_4 = input.LA(1);

                         
                        int index105_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA105_4==WS||(LA105_4>=NL && LA105_4<=COMMENT)) ) {s = 5;}

                        else if ( (LA105_4==IDENT||LA105_4==COMMA||LA105_4==LBRACE||LA105_4==GEN||LA105_4==COLON||(LA105_4>=PLUS && LA105_4<=PIPE)||LA105_4==LESS_AND) ) {s = 3;}

                        else if ( (LA105_4==LPAREN) && (synpred6_Css3())) {s = 6;}

                         
                        input.seek(index105_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 105, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA119_eotS =
        "\42\uffff";
    static final String DFA119_eofS =
        "\42\uffff";
    static final String DFA119_minS =
        "\1\5\7\0\1\uffff\1\0\5\uffff\1\0\10\uffff\1\0\11\uffff";
    static final String DFA119_maxS =
        "\1\154\7\0\1\uffff\1\0\5\uffff\1\0\10\uffff\1\0\11\uffff";
    static final String DFA119_acceptS =
        "\10\uffff\1\16\1\uffff\5\5\1\uffff\2\5\1\6\1\7\1\10\4\uffff\1\12"+
        "\1\13\1\14\1\15\1\2\1\3\1\4\1\1\1\11";
    static final String DFA119_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\5\uffff\1\11\10\uffff"+
        "\1\12\11\uffff}>";
    static final String[] DFA119_transitionS = {
            "\1\34\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\15"+
            "\1\uffff\1\30\36\uffff\1\17\1\2\1\11\1\4\1\20\1\21\1\16\1\1"+
            "\1\14\1\uffff\1\13\25\uffff\1\7\1\12\1\31\6\uffff\1\22\1\uffff"+
            "\2\23\1\24\4\uffff\3\24\1\uffff\1\33\1\32",
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
            "",
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
            return "()* loopback of 602:13: ( ( cp_variable_declaration )=> cp_variable_declaration ( ws )? | ( declaration SEMI )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )* COLON (~ ( SEMI | LBRACE | RBRACE ) )* SEMI )=> declaration SEMI ( ws )? | ( scss_nested_properties )=> scss_nested_properties ( ws )? | ( rule )=> rule ( ws )? | {...}? sass_extend ( ws )? | {...}? sass_debug ( ws )? | {...}? sass_control ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? | {...}? sass_content ( ws )? | {...}? sass_function_return ( ws )? | ( (~ SEMI )* SEMI )=> syncTo_SEMI )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA119_0 = input.LA(1);

                         
                        int index119_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA119_0==STAR) ) {s = 1;}

                        else if ( (LA119_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA119_0==IDENT) ) {s = 3;}

                        else if ( (LA119_0==DOT) ) {s = 4;}

                        else if ( (LA119_0==GEN) ) {s = 5;}

                        else if ( (LA119_0==MEDIA_SYM) ) {s = 6;}

                        else if ( (LA119_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA119_0==RBRACE) ) {s = 8;}

                        else if ( (LA119_0==HASH) ) {s = 9;}

                        else if ( (LA119_0==SASS_MIXIN) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA119_0==LESS_AND) && (synpred11_Css3())) {s = 11;}

                        else if ( (LA119_0==PIPE) && (synpred11_Css3())) {s = 12;}

                        else if ( (LA119_0==COLON) && (synpred11_Css3())) {s = 13;}

                        else if ( (LA119_0==SASS_EXTEND_ONLY_SELECTOR) && (synpred11_Css3())) {s = 14;}

                        else if ( (LA119_0==MINUS) ) {s = 15;}

                        else if ( (LA119_0==LBRACKET) && (synpred11_Css3())) {s = 16;}

                        else if ( (LA119_0==DCOLON) && (synpred11_Css3())) {s = 17;}

                        else if ( (LA119_0==SASS_EXTEND) ) {s = 18;}

                        else if ( ((LA119_0>=SASS_DEBUG && LA119_0<=SASS_WARN)) ) {s = 19;}

                        else if ( (LA119_0==SASS_IF||(LA119_0>=SASS_FOR && LA119_0<=SASS_WHILE)) ) {s = 20;}

                        else if ( (LA119_0==AT_IDENT) ) {s = 24;}

                        else if ( (LA119_0==SASS_INCLUDE) ) {s = 25;}

                        else if ( (LA119_0==SASS_CONTENT) ) {s = 26;}

                        else if ( (LA119_0==SASS_RETURN) ) {s = 27;}

                        else if ( (LA119_0==SEMI) && (synpred12_Css3())) {s = 28;}

                         
                        input.seek(index119_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA119_1 = input.LA(1);

                         
                        int index119_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA119_2 = input.LA(1);

                         
                        int index119_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA119_3 = input.LA(1);

                         
                        int index119_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA119_4 = input.LA(1);

                         
                        int index119_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( ((synpred11_Css3()||(synpred11_Css3()&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 17;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 25;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA119_5 = input.LA(1);

                         
                        int index119_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA119_6 = input.LA(1);

                         
                        int index119_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred7_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((synpred7_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 32;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 29;}

                        else if ( (((synpred9_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 30;}

                        else if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 31;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 33;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index119_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA119_7 = input.LA(1);

                         
                        int index119_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred7_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))||((synpred7_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isScssSource(),"isScssSource()")))) ) {s = 32;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 29;}

                        else if ( (((synpred9_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 30;}

                        else if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 31;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index119_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA119_9 = input.LA(1);

                         
                        int index119_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA119_15 = input.LA(1);

                         
                        int index119_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 29;}

                        else if ( (synpred9_Css3()) ) {s = 30;}

                        else if ( (synpred10_Css3()) ) {s = 31;}

                        else if ( (synpred11_Css3()) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index119_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA119_24 = input.LA(1);

                         
                        int index119_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((((synpred7_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||((synpred7_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 32;}

                        else if ( (((synpred8_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 29;}

                        else if ( (((synpred9_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 30;}

                        else if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 31;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index119_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 119, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA124_eotS =
        "\20\uffff";
    static final String DFA124_eofS =
        "\20\uffff";
    static final String DFA124_minS =
        "\2\6\2\0\1\uffff\1\0\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA124_maxS =
        "\1\77\1\123\2\0\1\uffff\1\0\2\123\5\uffff\1\0\1\uffff\1\0";
    static final String DFA124_acceptS =
        "\4\uffff\1\2\3\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA124_specialS =
        "\1\5\1\6\1\3\1\7\1\uffff\1\10\1\0\1\4\5\uffff\1\2\1\uffff\1\1}>";
    static final String[] DFA124_transitionS = {
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

    static final short[] DFA124_eot = DFA.unpackEncodedString(DFA124_eotS);
    static final short[] DFA124_eof = DFA.unpackEncodedString(DFA124_eofS);
    static final char[] DFA124_min = DFA.unpackEncodedStringToUnsignedChars(DFA124_minS);
    static final char[] DFA124_max = DFA.unpackEncodedStringToUnsignedChars(DFA124_maxS);
    static final short[] DFA124_accept = DFA.unpackEncodedString(DFA124_acceptS);
    static final short[] DFA124_special = DFA.unpackEncodedString(DFA124_specialS);
    static final short[][] DFA124_transition;

    static {
        int numStates = DFA124_transitionS.length;
        DFA124_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA124_transition[i] = DFA.unpackEncodedString(DFA124_transitionS[i]);
        }
    }

    class DFA124 extends DFA {

        public DFA124(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 124;
            this.eot = DFA124_eot;
            this.eof = DFA124_eof;
            this.min = DFA124_min;
            this.max = DFA124_max;
            this.accept = DFA124_accept;
            this.special = DFA124_special;
            this.transition = DFA124_transition;
        }
        public String getDescription() {
            return "638:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA124_6 = input.LA(1);

                         
                        int index124_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA124_6==IDENT) ) {s = 13;}

                        else if ( (LA124_6==WS||(LA124_6>=NL && LA124_6<=COMMENT)) && (synpred13_Css3())) {s = 10;}

                        else if ( (LA124_6==HASH_SYMBOL) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA124_6==GEN) ) {s = 4;}

                        else if ( (LA124_6==COLON||LA124_6==MINUS||(LA124_6>=HASH && LA124_6<=DOT)||LA124_6==LESS_AND) && (synpred13_Css3())) {s = 12;}

                        else if ( (LA124_6==LBRACE) && (synpred13_Css3())) {s = 14;}

                         
                        input.seek(index124_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA124_15 = input.LA(1);

                         
                        int index124_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index124_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA124_13 = input.LA(1);

                         
                        int index124_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index124_13);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA124_2 = input.LA(1);

                         
                        int index124_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index124_2);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA124_7 = input.LA(1);

                         
                        int index124_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA124_7==WS||(LA124_7>=NL && LA124_7<=COMMENT)) && (synpred13_Css3())) {s = 10;}

                        else if ( (LA124_7==HASH_SYMBOL) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA124_7==IDENT) ) {s = 15;}

                        else if ( (LA124_7==LBRACE) && (synpred13_Css3())) {s = 14;}

                        else if ( (LA124_7==COLON||LA124_7==MINUS||(LA124_7>=HASH && LA124_7<=DOT)||LA124_7==LESS_AND) && (synpred13_Css3())) {s = 12;}

                        else if ( ((LA124_7>=NOT && LA124_7<=GEN)) ) {s = 4;}

                         
                        input.seek(index124_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA124_0 = input.LA(1);

                         
                        int index124_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA124_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA124_0==IDENT) ) {s = 2;}

                        else if ( (LA124_0==LESS_AND) ) {s = 3;}

                        else if ( (LA124_0==GEN||(LA124_0>=LBRACKET && LA124_0<=PIPE)) ) {s = 4;}

                        else if ( (LA124_0==HASH) ) {s = 5;}

                        else if ( (LA124_0==DOT) ) {s = 6;}

                        else if ( (LA124_0==COLON) ) {s = 7;}

                        else if ( (LA124_0==MINUS) && (synpred13_Css3())) {s = 8;}

                         
                        input.seek(index124_0);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA124_1 = input.LA(1);

                         
                        int index124_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA124_1==LBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA124_1==NAME) ) {s = 4;}

                        else if ( (LA124_1==WS||(LA124_1>=NL && LA124_1<=COMMENT)) && (synpred13_Css3())) {s = 10;}

                        else if ( (LA124_1==HASH_SYMBOL) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA124_1==IDENT||LA124_1==COLON||LA124_1==MINUS||(LA124_1>=HASH && LA124_1<=DOT)||LA124_1==LESS_AND) && (synpred13_Css3())) {s = 12;}

                         
                        input.seek(index124_1);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA124_3 = input.LA(1);

                         
                        int index124_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index124_3);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA124_5 = input.LA(1);

                         
                        int index124_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index124_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 124, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA127_eotS =
        "\24\uffff";
    static final String DFA127_eofS =
        "\24\uffff";
    static final String DFA127_minS =
        "\1\5\7\uffff\6\0\6\uffff";
    static final String DFA127_maxS =
        "\1\137\7\uffff\6\0\6\uffff";
    static final String DFA127_acceptS =
        "\1\uffff\1\2\21\uffff\1\1";
    static final String DFA127_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\1\5\6\uffff}>";
    static final String[] DFA127_transitionS = {
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
            return "()* loopback of 654:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA127_8 = input.LA(1);

                         
                        int index127_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred14_Css3()&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 1;}

                         
                        input.seek(index127_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA127_9 = input.LA(1);

                         
                        int index127_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index127_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA127_10 = input.LA(1);

                         
                        int index127_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index127_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA127_11 = input.LA(1);

                         
                        int index127_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index127_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA127_12 = input.LA(1);

                         
                        int index127_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index127_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA127_13 = input.LA(1);

                         
                        int index127_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_Css3()) ) {s = 19;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index127_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 127, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA145_eotS =
        "\4\uffff";
    static final String DFA145_eofS =
        "\4\uffff";
    static final String DFA145_minS =
        "\2\5\2\uffff";
    static final String DFA145_maxS =
        "\2\137\2\uffff";
    static final String DFA145_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA145_specialS =
        "\4\uffff}>";
    static final String[] DFA145_transitionS = {
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "\2\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\10\3\1\uffff\1\3\22\uffff\2\1\13\uffff"+
            "\1\3",
            "",
            ""
    };

    static final short[] DFA145_eot = DFA.unpackEncodedString(DFA145_eotS);
    static final short[] DFA145_eof = DFA.unpackEncodedString(DFA145_eofS);
    static final char[] DFA145_min = DFA.unpackEncodedStringToUnsignedChars(DFA145_minS);
    static final char[] DFA145_max = DFA.unpackEncodedStringToUnsignedChars(DFA145_maxS);
    static final short[] DFA145_accept = DFA.unpackEncodedString(DFA145_acceptS);
    static final short[] DFA145_special = DFA.unpackEncodedString(DFA145_specialS);
    static final short[][] DFA145_transition;

    static {
        int numStates = DFA145_transitionS.length;
        DFA145_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA145_transition[i] = DFA.unpackEncodedString(DFA145_transitionS[i]);
        }
    }

    class DFA145 extends DFA {

        public DFA145(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 145;
            this.eot = DFA145_eot;
            this.eof = DFA145_eof;
            this.min = DFA145_min;
            this.max = DFA145_max;
            this.accept = DFA145_accept;
            this.special = DFA145_special;
            this.transition = DFA145_transition;
        }
        public String getDescription() {
            return "755:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA154_eotS =
        "\35\uffff";
    static final String DFA154_eofS =
        "\35\uffff";
    static final String DFA154_minS =
        "\1\6\1\uffff\1\5\1\0\1\6\5\0\1\uffff\2\0\1\uffff\1\6\1\uffff\1\0"+
        "\4\uffff\1\0\1\uffff\2\0\1\uffff\1\6\2\0";
    static final String DFA154_maxS =
        "\1\125\1\uffff\1\125\1\0\1\125\5\0\1\uffff\2\0\1\uffff\1\125\1\uffff"+
        "\1\0\4\uffff\1\0\1\uffff\2\0\1\uffff\1\125\2\0";
    static final String DFA154_acceptS =
        "\1\uffff\1\1\10\uffff\1\1\2\uffff\1\3\1\uffff\1\1\1\uffff\4\1\1"+
        "\uffff\1\1\2\uffff\1\2\3\uffff";
    static final String DFA154_specialS =
        "\1\5\1\uffff\1\15\1\10\1\uffff\1\16\1\2\1\7\1\0\1\1\1\uffff\1\11"+
        "\1\20\1\uffff\1\6\1\uffff\1\14\4\uffff\1\3\1\uffff\1\12\1\17\2\uffff"+
        "\1\4\1\13}>";
    static final String[] DFA154_transitionS = {
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

    static final short[] DFA154_eot = DFA.unpackEncodedString(DFA154_eotS);
    static final short[] DFA154_eof = DFA.unpackEncodedString(DFA154_eofS);
    static final char[] DFA154_min = DFA.unpackEncodedStringToUnsignedChars(DFA154_minS);
    static final char[] DFA154_max = DFA.unpackEncodedStringToUnsignedChars(DFA154_maxS);
    static final short[] DFA154_accept = DFA.unpackEncodedString(DFA154_acceptS);
    static final short[] DFA154_special = DFA.unpackEncodedString(DFA154_specialS);
    static final short[][] DFA154_transition;

    static {
        int numStates = DFA154_transitionS.length;
        DFA154_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA154_transition[i] = DFA.unpackEncodedString(DFA154_transitionS[i]);
        }
    }

    class DFA154 extends DFA {

        public DFA154(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 154;
            this.eot = DFA154_eot;
            this.eof = DFA154_eof;
            this.min = DFA154_min;
            this.max = DFA154_max;
            this.accept = DFA154_accept;
            this.special = DFA154_special;
            this.transition = DFA154_transition;
        }
        public String getDescription() {
            return "775:1: propertyValue : ( ( (~ ( HASH_SYMBOL | SEMI | RBRACE | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_property_value_interpolation_expression | ( expressionPredicate )=> expression | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA154_8 = input.LA(1);

                         
                        int index154_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA154_9 = input.LA(1);

                         
                        int index154_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA154_6 = input.LA(1);

                         
                        int index154_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA154_21 = input.LA(1);

                         
                        int index154_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 22;}

                        else if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_21);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA154_27 = input.LA(1);

                         
                        int index154_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_27);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA154_0 = input.LA(1);

                         
                        int index154_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA154_0==HASH_SYMBOL) && (synpred17_Css3())) {s = 1;}

                        else if ( (LA154_0==MINUS) ) {s = 2;}

                        else if ( (LA154_0==IDENT) ) {s = 3;}

                        else if ( (LA154_0==PLUS) ) {s = 4;}

                        else if ( (LA154_0==PERCENTAGE||(LA154_0>=NUMBER && LA154_0<=DIMENSION)) ) {s = 5;}

                        else if ( (LA154_0==STRING) ) {s = 6;}

                        else if ( (LA154_0==HASH) ) {s = 7;}

                        else if ( (LA154_0==GEN) ) {s = 8;}

                        else if ( (LA154_0==URI) ) {s = 9;}

                        else if ( (LA154_0==SOLIDUS||LA154_0==DOT) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA154_0==MEDIA_SYM||LA154_0==AT_IDENT) ) {s = 11;}

                        else if ( (LA154_0==SASS_VAR) ) {s = 12;}

                        else if ( (LA154_0==LPAREN) ) {s = 13;}

                         
                        input.seek(index154_0);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA154_14 = input.LA(1);

                         
                        int index154_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA154_14==HASH_SYMBOL) && (synpred17_Css3())) {s = 15;}

                        else if ( (LA154_14==IDENT) ) {s = 16;}

                        else if ( (LA154_14==WS||(LA154_14>=NL && LA154_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA154_14==PERCENTAGE||(LA154_14>=NUMBER && LA154_14<=DIMENSION)) ) {s = 5;}

                        else if ( (LA154_14==STRING) ) {s = 6;}

                        else if ( (LA154_14==HASH) ) {s = 21;}

                        else if ( (LA154_14==GEN) ) {s = 8;}

                        else if ( (LA154_14==URI) ) {s = 9;}

                        else if ( (LA154_14==SOLIDUS||LA154_14==MINUS||LA154_14==DOT) && (synpred17_Css3())) {s = 22;}

                        else if ( (LA154_14==MEDIA_SYM||LA154_14==AT_IDENT) ) {s = 23;}

                        else if ( (LA154_14==SASS_VAR) ) {s = 24;}

                         
                        input.seek(index154_14);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA154_7 = input.LA(1);

                         
                        int index154_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 22;}

                        else if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA154_3 = input.LA(1);

                         
                        int index154_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 22;}

                        else if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_3);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA154_11 = input.LA(1);

                         
                        int index154_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred18_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 25;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 13;}

                         
                        input.seek(index154_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA154_23 = input.LA(1);

                         
                        int index154_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_23);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA154_28 = input.LA(1);

                         
                        int index154_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_28);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA154_16 = input.LA(1);

                         
                        int index154_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_Css3()) ) {s = 22;}

                        else if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_16);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA154_2 = input.LA(1);

                         
                        int index154_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA154_2==WS||(LA154_2>=NL && LA154_2<=COMMENT)) ) {s = 14;}

                        else if ( (LA154_2==HASH_SYMBOL) && (synpred17_Css3())) {s = 15;}

                        else if ( (LA154_2==IDENT) ) {s = 16;}

                        else if ( (LA154_2==IMPORTANT_SYM) && (synpred17_Css3())) {s = 17;}

                        else if ( (LA154_2==SEMI) && (synpred17_Css3())) {s = 18;}

                        else if ( (LA154_2==RBRACE) && (synpred17_Css3())) {s = 19;}

                        else if ( (LA154_2==LBRACE) && (synpred17_Css3())) {s = 20;}

                        else if ( (LA154_2==PERCENTAGE||(LA154_2>=NUMBER && LA154_2<=DIMENSION)) ) {s = 5;}

                        else if ( (LA154_2==STRING) ) {s = 6;}

                        else if ( (LA154_2==HASH) ) {s = 21;}

                        else if ( (LA154_2==GEN) ) {s = 8;}

                        else if ( (LA154_2==URI) ) {s = 9;}

                        else if ( (LA154_2==SOLIDUS||LA154_2==MINUS||LA154_2==DOT) && (synpred17_Css3())) {s = 22;}

                        else if ( (LA154_2==MEDIA_SYM||LA154_2==AT_IDENT) ) {s = 23;}

                        else if ( (LA154_2==SASS_VAR) ) {s = 24;}

                         
                        input.seek(index154_2);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA154_5 = input.LA(1);

                         
                        int index154_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_5);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA154_24 = input.LA(1);

                         
                        int index154_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Css3()) ) {s = 25;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 13;}

                         
                        input.seek(index154_24);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA154_12 = input.LA(1);

                         
                        int index154_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred18_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 25;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 13;}

                         
                        input.seek(index154_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 154, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA158_eotS =
        "\12\uffff";
    static final String DFA158_eofS =
        "\12\uffff";
    static final String DFA158_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA158_maxS =
        "\1\125\1\uffff\1\125\1\uffff\2\125\1\6\1\125\2\123";
    static final String DFA158_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA158_specialS =
        "\12\uffff}>";
    static final String[] DFA158_transitionS = {
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

    static final short[] DFA158_eot = DFA.unpackEncodedString(DFA158_eotS);
    static final short[] DFA158_eof = DFA.unpackEncodedString(DFA158_eofS);
    static final char[] DFA158_min = DFA.unpackEncodedStringToUnsignedChars(DFA158_minS);
    static final char[] DFA158_max = DFA.unpackEncodedStringToUnsignedChars(DFA158_maxS);
    static final short[] DFA158_accept = DFA.unpackEncodedString(DFA158_acceptS);
    static final short[] DFA158_special = DFA.unpackEncodedString(DFA158_specialS);
    static final short[][] DFA158_transition;

    static {
        int numStates = DFA158_transitionS.length;
        DFA158_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA158_transition[i] = DFA.unpackEncodedString(DFA158_transitionS[i]);
        }
    }

    class DFA158 extends DFA {

        public DFA158(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 158;
            this.eot = DFA158_eot;
            this.eof = DFA158_eof;
            this.min = DFA158_min;
            this.max = DFA158_max;
            this.accept = DFA158_accept;
            this.special = DFA158_special;
            this.transition = DFA158_transition;
        }
        public String getDescription() {
            return "()* loopback of 838:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA161_eotS =
        "\13\uffff";
    static final String DFA161_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA161_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA161_maxS =
        "\1\125\2\uffff\1\146\5\uffff\1\146\1\uffff";
    static final String DFA161_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA161_specialS =
        "\13\uffff}>";
    static final String[] DFA161_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\20\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\4\12\2\uffff\5\12\2\uffff\1\12\2\10\2\12\1\11\5\uffff\1\12"+
            "\23\uffff\3\12\1\uffff\1\12\1\uffff\1\12\1\10\3\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12\5\uffff\3\12\6\uffff\3\12",
            "",
            "",
            "",
            "",
            "",
            "\4\12\2\uffff\5\12\2\uffff\1\12\1\10\1\uffff\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\3\12\1\uffff\1\12\1\uffff\1\12\4\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12\5\uffff\3\12\6\uffff\3\12",
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
            return "843:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA167_eotS =
        "\22\uffff";
    static final String DFA167_eofS =
        "\22\uffff";
    static final String DFA167_minS =
        "\2\6\2\0\1\27\5\0\2\uffff\1\6\3\0\2\uffff";
    static final String DFA167_maxS =
        "\2\125\2\0\1\123\5\0\2\uffff\1\125\3\0\2\uffff";
    static final String DFA167_acceptS =
        "\12\uffff\1\1\1\4\4\uffff\1\2\1\3";
    static final String DFA167_specialS =
        "\2\uffff\1\12\1\1\1\11\1\6\1\5\1\2\1\4\1\10\3\uffff\1\0\1\3\1\7"+
        "\2\uffff}>";
    static final String[] DFA167_transitionS = {
            "\1\4\1\3\1\6\3\uffff\1\10\5\uffff\1\5\1\12\1\uffff\1\13\1\10"+
            "\6\uffff\1\2\24\uffff\1\1\2\uffff\1\1\1\uffff\1\7\20\uffff\12"+
            "\2\3\uffff\1\11",
            "\1\15\1\3\1\6\3\uffff\1\16\5\uffff\1\5\3\uffff\1\16\1\14\5"+
            "\uffff\1\2\31\uffff\1\7\20\uffff\12\2\2\14\1\uffff\1\17",
            "\1\uffff",
            "\1\uffff",
            "\1\21\40\uffff\1\21\7\uffff\1\21\21\uffff\2\21",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\15\1\3\1\6\3\uffff\1\16\5\uffff\1\5\3\uffff\1\16\1\14\5"+
            "\uffff\1\2\31\uffff\1\7\20\uffff\12\2\2\14\1\uffff\1\17",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA167_eot = DFA.unpackEncodedString(DFA167_eotS);
    static final short[] DFA167_eof = DFA.unpackEncodedString(DFA167_eofS);
    static final char[] DFA167_min = DFA.unpackEncodedStringToUnsignedChars(DFA167_minS);
    static final char[] DFA167_max = DFA.unpackEncodedStringToUnsignedChars(DFA167_maxS);
    static final short[] DFA167_accept = DFA.unpackEncodedString(DFA167_acceptS);
    static final short[] DFA167_special = DFA.unpackEncodedString(DFA167_specialS);
    static final short[][] DFA167_transition;

    static {
        int numStates = DFA167_transitionS.length;
        DFA167_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA167_transition[i] = DFA.unpackEncodedString(DFA167_transitionS[i]);
        }
    }

    class DFA167 extends DFA {

        public DFA167(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 167;
            this.eot = DFA167_eot;
            this.eof = DFA167_eof;
            this.min = DFA167_min;
            this.max = DFA167_max;
            this.accept = DFA167_accept;
            this.special = DFA167_special;
            this.transition = DFA167_transition;
        }
        public String getDescription() {
            return "871:3: ({...}? cp_variable_value | expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) | {...}?)";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA167_13 = input.LA(1);

                         
                        int index167_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA167_3 = input.LA(1);

                         
                        int index167_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA167_7 = input.LA(1);

                         
                        int index167_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA167_14 = input.LA(1);

                         
                        int index167_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA167_8 = input.LA(1);

                         
                        int index167_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 16;}

                         
                        input.seek(index167_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA167_6 = input.LA(1);

                         
                        int index167_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA167_5 = input.LA(1);

                         
                        int index167_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_5);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA167_15 = input.LA(1);

                         
                        int index167_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_15);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA167_9 = input.LA(1);

                         
                        int index167_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 16;}

                         
                        input.seek(index167_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA167_4 = input.LA(1);

                         
                        int index167_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA167_4==WS||LA167_4==DOT||LA167_4==OPEQ||(LA167_4>=NL && LA167_4<=COMMENT)) ) {s = 17;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_4);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA167_2 = input.LA(1);

                         
                        int index167_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index167_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 167, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA190_eotS =
        "\36\uffff";
    static final String DFA190_eofS =
        "\36\uffff";
    static final String DFA190_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA190_maxS =
        "\1\146\1\uffff\2\125\10\uffff\1\125\10\0\1\125\10\0";
    static final String DFA190_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA190_specialS =
        "\1\3\14\uffff\1\1\1\2\1\10\1\17\1\15\1\13\1\6\1\7\1\uffff\1\16\1"+
        "\14\1\5\1\20\1\12\1\4\1\11\1\0}>";
    static final String[] DFA190_transitionS = {
            "\1\1\1\6\1\5\1\10\2\uffff\1\1\1\12\3\1\2\uffff\1\7\2\uffff\1"+
            "\1\1\12\6\uffff\1\4\23\uffff\1\1\1\2\1\1\1\uffff\1\3\1\uffff"+
            "\1\11\4\uffff\1\1\12\uffff\1\1\12\4\2\uffff\1\1\1\13\5\uffff"+
            "\3\1\6\uffff\3\1",
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

    static final short[] DFA190_eot = DFA.unpackEncodedString(DFA190_eotS);
    static final short[] DFA190_eof = DFA.unpackEncodedString(DFA190_eofS);
    static final char[] DFA190_min = DFA.unpackEncodedStringToUnsignedChars(DFA190_minS);
    static final char[] DFA190_max = DFA.unpackEncodedStringToUnsignedChars(DFA190_maxS);
    static final short[] DFA190_accept = DFA.unpackEncodedString(DFA190_acceptS);
    static final short[] DFA190_special = DFA.unpackEncodedString(DFA190_specialS);
    static final short[][] DFA190_transition;

    static {
        int numStates = DFA190_transitionS.length;
        DFA190_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA190_transition[i] = DFA.unpackEncodedString(DFA190_transitionS[i]);
        }
    }

    class DFA190 extends DFA {

        public DFA190(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 190;
            this.eot = DFA190_eot;
            this.eof = DFA190_eof;
            this.min = DFA190_min;
            this.max = DFA190_max;
            this.accept = DFA190_accept;
            this.special = DFA190_special;
            this.transition = DFA190_transition;
        }
        public String getDescription() {
            return "()* loopback of 961:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA190_29 = input.LA(1);

                         
                        int index190_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_29);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA190_13 = input.LA(1);

                         
                        int index190_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_13);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA190_14 = input.LA(1);

                         
                        int index190_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_14);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA190_0 = input.LA(1);

                         
                        int index190_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA190_0==SEMI||LA190_0==COMMA||(LA190_0>=LBRACE && LA190_0<=AND)||LA190_0==RPAREN||LA190_0==SOLIDUS||LA190_0==GREATER||LA190_0==STAR||LA190_0==IMPORTANT_SYM||LA190_0==SASS_DEFAULT||(LA190_0>=GREATER_OR_EQ && LA190_0<=LESS_OR_EQ)||(LA190_0>=OR && LA190_0<=CP_NOT_EQ)) ) {s = 1;}

                        else if ( (LA190_0==PLUS) ) {s = 2;}

                        else if ( (LA190_0==MINUS) ) {s = 3;}

                        else if ( (LA190_0==PERCENTAGE||(LA190_0>=NUMBER && LA190_0<=DIMENSION)) && (synpred19_Css3())) {s = 4;}

                        else if ( (LA190_0==STRING) && (synpred19_Css3())) {s = 5;}

                        else if ( (LA190_0==IDENT) && (synpred19_Css3())) {s = 6;}

                        else if ( (LA190_0==GEN) && (synpred19_Css3())) {s = 7;}

                        else if ( (LA190_0==URI) && (synpred19_Css3())) {s = 8;}

                        else if ( (LA190_0==HASH) && (synpred19_Css3())) {s = 9;}

                        else if ( (LA190_0==MEDIA_SYM||LA190_0==AT_IDENT) && (synpred19_Css3())) {s = 10;}

                        else if ( (LA190_0==SASS_VAR) && (synpred19_Css3())) {s = 11;}

                         
                        input.seek(index190_0);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA190_27 = input.LA(1);

                         
                        int index190_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_27);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA190_24 = input.LA(1);

                         
                        int index190_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_24);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA190_19 = input.LA(1);

                         
                        int index190_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_19);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA190_20 = input.LA(1);

                         
                        int index190_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_20);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA190_15 = input.LA(1);

                         
                        int index190_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_15);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA190_28 = input.LA(1);

                         
                        int index190_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_28);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA190_26 = input.LA(1);

                         
                        int index190_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_26);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA190_18 = input.LA(1);

                         
                        int index190_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_18);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA190_23 = input.LA(1);

                         
                        int index190_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_23);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA190_17 = input.LA(1);

                         
                        int index190_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_17);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA190_22 = input.LA(1);

                         
                        int index190_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_22);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA190_16 = input.LA(1);

                         
                        int index190_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA190_25 = input.LA(1);

                         
                        int index190_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index190_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 190, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA194_eotS =
        "\13\uffff";
    static final String DFA194_eofS =
        "\13\uffff";
    static final String DFA194_minS =
        "\1\6\2\uffff\1\6\5\uffff\1\6\1\uffff";
    static final String DFA194_maxS =
        "\1\125\2\uffff\1\123\5\uffff\1\123\1\uffff";
    static final String DFA194_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA194_specialS =
        "\13\uffff}>";
    static final String[] DFA194_transitionS = {
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

    static final short[] DFA194_eot = DFA.unpackEncodedString(DFA194_eotS);
    static final short[] DFA194_eof = DFA.unpackEncodedString(DFA194_eofS);
    static final char[] DFA194_min = DFA.unpackEncodedStringToUnsignedChars(DFA194_minS);
    static final char[] DFA194_max = DFA.unpackEncodedStringToUnsignedChars(DFA194_maxS);
    static final short[] DFA194_accept = DFA.unpackEncodedString(DFA194_acceptS);
    static final short[] DFA194_special = DFA.unpackEncodedString(DFA194_specialS);
    static final short[][] DFA194_transition;

    static {
        int numStates = DFA194_transitionS.length;
        DFA194_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA194_transition[i] = DFA.unpackEncodedString(DFA194_transitionS[i]);
        }
    }

    class DFA194 extends DFA {

        public DFA194(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 194;
            this.eot = DFA194_eot;
            this.eof = DFA194_eof;
            this.min = DFA194_min;
            this.max = DFA194_max;
            this.accept = DFA194_accept;
            this.special = DFA194_special;
            this.transition = DFA194_transition;
        }
        public String getDescription() {
            return "968:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA212_eotS =
        "\4\uffff";
    static final String DFA212_eofS =
        "\4\uffff";
    static final String DFA212_minS =
        "\2\5\2\uffff";
    static final String DFA212_maxS =
        "\2\123\2\uffff";
    static final String DFA212_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA212_specialS =
        "\4\uffff}>";
    static final String[] DFA212_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA212_eot = DFA.unpackEncodedString(DFA212_eotS);
    static final short[] DFA212_eof = DFA.unpackEncodedString(DFA212_eofS);
    static final char[] DFA212_min = DFA.unpackEncodedStringToUnsignedChars(DFA212_minS);
    static final char[] DFA212_max = DFA.unpackEncodedStringToUnsignedChars(DFA212_maxS);
    static final short[] DFA212_accept = DFA.unpackEncodedString(DFA212_acceptS);
    static final short[] DFA212_special = DFA.unpackEncodedString(DFA212_specialS);
    static final short[][] DFA212_transition;

    static {
        int numStates = DFA212_transitionS.length;
        DFA212_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA212_transition[i] = DFA.unpackEncodedString(DFA212_transitionS[i]);
        }
    }

    class DFA212 extends DFA {

        public DFA212(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 212;
            this.eot = DFA212_eot;
            this.eof = DFA212_eof;
            this.min = DFA212_min;
            this.max = DFA212_max;
            this.accept = DFA212_accept;
            this.special = DFA212_special;
            this.transition = DFA212_transition;
        }
        public String getDescription() {
            return "1016:5: ( ( ws )? LPAREN ( ws )? ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA218_eotS =
        "\5\uffff";
    static final String DFA218_eofS =
        "\5\uffff";
    static final String DFA218_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA218_maxS =
        "\1\25\1\131\1\uffff\1\131\1\uffff";
    static final String DFA218_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA218_specialS =
        "\5\uffff}>";
    static final String[] DFA218_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\72\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
    };

    static final short[] DFA218_eot = DFA.unpackEncodedString(DFA218_eotS);
    static final short[] DFA218_eof = DFA.unpackEncodedString(DFA218_eofS);
    static final char[] DFA218_min = DFA.unpackEncodedStringToUnsignedChars(DFA218_minS);
    static final char[] DFA218_max = DFA.unpackEncodedStringToUnsignedChars(DFA218_maxS);
    static final short[] DFA218_accept = DFA.unpackEncodedString(DFA218_acceptS);
    static final short[] DFA218_special = DFA.unpackEncodedString(DFA218_specialS);
    static final short[][] DFA218_transition;

    static {
        int numStates = DFA218_transitionS.length;
        DFA218_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA218_transition[i] = DFA.unpackEncodedString(DFA218_transitionS[i]);
        }
    }

    class DFA218 extends DFA {

        public DFA218(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 218;
            this.eot = DFA218_eot;
            this.eof = DFA218_eof;
            this.min = DFA218_min;
            this.max = DFA218_max;
            this.accept = DFA218_accept;
            this.special = DFA218_special;
            this.transition = DFA218_transition;
        }
        public String getDescription() {
            return "()* loopback of 1045:14: ( ( COMMA | SEMI ) ( ws )? cp_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA239_eotS =
        "\15\uffff";
    static final String DFA239_eofS =
        "\15\uffff";
    static final String DFA239_minS =
        "\2\6\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6";
    static final String DFA239_maxS =
        "\1\77\1\123\1\uffff\2\154\5\123\1\uffff\2\123";
    static final String DFA239_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff";
    static final String DFA239_specialS =
        "\15\uffff}>";
    static final String[] DFA239_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2\6\uffff\1\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\6\uffff"+
            "\1\2\22\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14"
    };

    static final short[] DFA239_eot = DFA.unpackEncodedString(DFA239_eotS);
    static final short[] DFA239_eof = DFA.unpackEncodedString(DFA239_eofS);
    static final char[] DFA239_min = DFA.unpackEncodedStringToUnsignedChars(DFA239_minS);
    static final char[] DFA239_max = DFA.unpackEncodedStringToUnsignedChars(DFA239_maxS);
    static final short[] DFA239_accept = DFA.unpackEncodedString(DFA239_acceptS);
    static final short[] DFA239_special = DFA.unpackEncodedString(DFA239_specialS);
    static final short[][] DFA239_transition;

    static {
        int numStates = DFA239_transitionS.length;
        DFA239_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA239_transition[i] = DFA.unpackEncodedString(DFA239_transitionS[i]);
        }
    }

    class DFA239 extends DFA {

        public DFA239(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 239;
            this.eot = DFA239_eot;
            this.eof = DFA239_eof;
            this.min = DFA239_min;
            this.max = DFA239_max;
            this.accept = DFA239_accept;
            this.special = DFA239_special;
            this.transition = DFA239_transition;
        }
        public String getDescription() {
            return "1112:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA242_eotS =
        "\4\uffff";
    static final String DFA242_eofS =
        "\4\uffff";
    static final String DFA242_minS =
        "\2\6\2\uffff";
    static final String DFA242_maxS =
        "\2\123\2\uffff";
    static final String DFA242_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA242_specialS =
        "\4\uffff}>";
    static final String[] DFA242_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\6\uffff"+
            "\1\3\22\uffff\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\6\uffff"+
            "\1\3\22\uffff\2\1",
            "",
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
            return "()* loopback of 1117:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA241_eotS =
        "\15\uffff";
    static final String DFA241_eofS =
        "\15\uffff";
    static final String DFA241_minS =
        "\2\6\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6";
    static final String DFA241_maxS =
        "\1\77\1\123\1\uffff\2\154\5\123\1\uffff\2\123";
    static final String DFA241_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff";
    static final String DFA241_specialS =
        "\15\uffff}>";
    static final String[] DFA241_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2\6\uffff\1\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\6\uffff"+
            "\1\2\22\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14"
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
            return "1119:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA246_eotS =
        "\4\uffff";
    static final String DFA246_eofS =
        "\4\uffff";
    static final String DFA246_minS =
        "\2\6\2\uffff";
    static final String DFA246_maxS =
        "\2\123\2\uffff";
    static final String DFA246_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA246_specialS =
        "\4\uffff}>";
    static final String[] DFA246_transitionS = {
            "\1\3\15\uffff\1\2\2\uffff\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\15\uffff\1\2\2\uffff\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
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
            return "()* loopback of 1135:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA247_eotS =
        "\15\uffff";
    static final String DFA247_eofS =
        "\15\uffff";
    static final String DFA247_minS =
        "\1\6\1\5\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6";
    static final String DFA247_maxS =
        "\1\70\1\123\1\uffff\2\154\5\123\1\uffff\2\123";
    static final String DFA247_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff";
    static final String DFA247_specialS =
        "\15\uffff}>";
    static final String[] DFA247_transitionS = {
            "\1\2\52\uffff\1\2\3\uffff\1\2\1\1\2\2",
            "\2\2\6\uffff\1\3\1\2\10\uffff\1\2\31\uffff\1\2\3\uffff\4\2"+
            "\16\uffff\1\2\12\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14"
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
            return "1148:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA249_eotS =
        "\15\uffff";
    static final String DFA249_eofS =
        "\15\uffff";
    static final String DFA249_minS =
        "\1\6\1\5\1\uffff\2\5\1\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6";
    static final String DFA249_maxS =
        "\1\70\1\123\1\uffff\2\154\5\123\1\uffff\2\123";
    static final String DFA249_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff";
    static final String DFA249_specialS =
        "\15\uffff}>";
    static final String[] DFA249_transitionS = {
            "\1\2\52\uffff\1\2\3\uffff\1\2\1\1\2\2",
            "\2\2\6\uffff\1\3\1\2\10\uffff\1\2\31\uffff\1\2\3\uffff\4\2"+
            "\16\uffff\1\2\12\uffff\2\2",
            "",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1"+
            "\uffff\1\10\1\4\35\uffff\11\2\1\uffff\1\2\22\uffff\2\4\1\uffff"+
            "\1\6\2\2\6\uffff\1\2\1\uffff\3\2\4\uffff\3\2\1\uffff\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\31\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14"
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
            return "1155:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | SOLIDUS ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA251_eotS =
        "\16\uffff";
    static final String DFA251_eofS =
        "\16\uffff";
    static final String DFA251_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\2\6";
    static final String DFA251_maxS =
        "\1\70\1\123\1\uffff\2\154\5\123\1\uffff\3\123";
    static final String DFA251_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\3\uffff";
    static final String DFA251_specialS =
        "\16\uffff}>";
    static final String[] DFA251_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\4\uffff\3\2\2\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\4\uffff\3\2\2\uffff\1\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\15\35\uffff\4\2\31\uffff"+
            "\2\15",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\15\35\uffff\4\2\31\uffff"+
            "\2\15"
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
            return "1166:9: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA254_eotS =
        "\4\uffff";
    static final String DFA254_eofS =
        "\4\uffff";
    static final String DFA254_minS =
        "\2\6\2\uffff";
    static final String DFA254_maxS =
        "\2\123\2\uffff";
    static final String DFA254_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA254_specialS =
        "\4\uffff}>";
    static final String[] DFA254_transitionS = {
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\31\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA254_eot = DFA.unpackEncodedString(DFA254_eotS);
    static final short[] DFA254_eof = DFA.unpackEncodedString(DFA254_eofS);
    static final char[] DFA254_min = DFA.unpackEncodedStringToUnsignedChars(DFA254_minS);
    static final char[] DFA254_max = DFA.unpackEncodedStringToUnsignedChars(DFA254_maxS);
    static final short[] DFA254_accept = DFA.unpackEncodedString(DFA254_acceptS);
    static final short[] DFA254_special = DFA.unpackEncodedString(DFA254_specialS);
    static final short[][] DFA254_transition;

    static {
        int numStates = DFA254_transitionS.length;
        DFA254_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA254_transition[i] = DFA.unpackEncodedString(DFA254_transitionS[i]);
        }
    }

    class DFA254 extends DFA {

        public DFA254(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 254;
            this.eot = DFA254_eot;
            this.eof = DFA254_eof;
            this.min = DFA254_min;
            this.max = DFA254_max;
            this.accept = DFA254_accept;
            this.special = DFA254_special;
            this.transition = DFA254_transition;
        }
        public String getDescription() {
            return "()* loopback of 1171:9: ( ( ws )? ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA253_eotS =
        "\16\uffff";
    static final String DFA253_eofS =
        "\16\uffff";
    static final String DFA253_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\2\6";
    static final String DFA253_maxS =
        "\1\70\1\123\1\uffff\2\154\5\123\1\uffff\3\123";
    static final String DFA253_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\3\uffff";
    static final String DFA253_specialS =
        "\16\uffff}>";
    static final String[] DFA253_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\31\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\4\uffff\3\2\2\uffff\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\11"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2\7\uffff\1\2\1\uffff"+
            "\3\2\4\uffff\3\2\2\uffff\1\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\15\35\uffff\4\2\31\uffff"+
            "\2\15",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\72\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\72\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\1\12\3\uffff\1\2\1\12\1\2\2\uffff"+
            "\1\14\32\uffff\14\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\15\35\uffff\4\2\31\uffff"+
            "\2\15"
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
            return "1173:13: ( scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA256_eotS =
        "\6\uffff";
    static final String DFA256_eofS =
        "\6\uffff";
    static final String DFA256_minS =
        "\1\6\1\uffff\2\16\2\uffff";
    static final String DFA256_maxS =
        "\1\125\1\uffff\2\123\2\uffff";
    static final String DFA256_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\3";
    static final String DFA256_specialS =
        "\6\uffff}>";
    static final String[] DFA256_transitionS = {
            "\1\2\5\uffff\1\1\11\uffff\1\1\76\uffff\1\1",
            "",
            "\1\5\4\uffff\1\4\3\uffff\1\3\72\uffff\2\3",
            "\1\5\4\uffff\1\4\3\uffff\1\3\72\uffff\2\3",
            "",
            ""
    };

    static final short[] DFA256_eot = DFA.unpackEncodedString(DFA256_eotS);
    static final short[] DFA256_eof = DFA.unpackEncodedString(DFA256_eofS);
    static final char[] DFA256_min = DFA.unpackEncodedStringToUnsignedChars(DFA256_minS);
    static final char[] DFA256_max = DFA.unpackEncodedStringToUnsignedChars(DFA256_maxS);
    static final short[] DFA256_accept = DFA.unpackEncodedString(DFA256_acceptS);
    static final short[] DFA256_special = DFA.unpackEncodedString(DFA256_specialS);
    static final short[][] DFA256_transition;

    static {
        int numStates = DFA256_transitionS.length;
        DFA256_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA256_transition[i] = DFA.unpackEncodedString(DFA256_transitionS[i]);
        }
    }

    class DFA256 extends DFA {

        public DFA256(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 256;
            this.eot = DFA256_eot;
            this.eof = DFA256_eof;
            this.min = DFA256_min;
            this.max = DFA256_max;
            this.accept = DFA256_accept;
            this.special = DFA256_special;
            this.transition = DFA256_transition;
        }
        public String getDescription() {
            return "1184:32: ( cp_variable | less_function_in_condition | IDENT )";
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
        "\2\154\2\uffff";
    static final String DFA265_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA265_specialS =
        "\4\uffff}>";
    static final String[] DFA265_transitionS = {
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\3\uffff\6\3",
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\3\uffff\6\3",
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
            return "1232:59: ( ( ws )? sass_else )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA271_eotS =
        "\5\uffff";
    static final String DFA271_eofS =
        "\5\uffff";
    static final String DFA271_minS =
        "\1\143\2\6\2\uffff";
    static final String DFA271_maxS =
        "\1\143\2\123\2\uffff";
    static final String DFA271_acceptS =
        "\3\uffff\1\1\1\2";
    static final String DFA271_specialS =
        "\5\uffff}>";
    static final String[] DFA271_transitionS = {
            "\1\1",
            "\1\4\6\uffff\1\3\11\uffff\1\2\72\uffff\2\2",
            "\1\4\6\uffff\1\3\11\uffff\1\2\72\uffff\2\2",
            "",
            ""
    };

    static final short[] DFA271_eot = DFA.unpackEncodedString(DFA271_eotS);
    static final short[] DFA271_eof = DFA.unpackEncodedString(DFA271_eofS);
    static final char[] DFA271_min = DFA.unpackEncodedStringToUnsignedChars(DFA271_minS);
    static final char[] DFA271_max = DFA.unpackEncodedStringToUnsignedChars(DFA271_maxS);
    static final short[] DFA271_accept = DFA.unpackEncodedString(DFA271_acceptS);
    static final short[] DFA271_special = DFA.unpackEncodedString(DFA271_specialS);
    static final short[][] DFA271_transition;

    static {
        int numStates = DFA271_transitionS.length;
        DFA271_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA271_transition[i] = DFA.unpackEncodedString(DFA271_transitionS[i]);
        }
    }

    class DFA271 extends DFA {

        public DFA271(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 271;
            this.eot = DFA271_eot;
            this.eof = DFA271_eof;
            this.min = DFA271_min;
            this.max = DFA271_max;
            this.accept = DFA271_accept;
            this.special = DFA271_special;
            this.transition = DFA271_transition;
        }
        public String getDescription() {
            return "1235:1: sass_else : ( SASS_ELSE ( ws )? sass_control_block | SASS_ELSE ( ws )? {...}? IDENT ( ws )? sass_control_expression sass_control_block ( ( ws )? sass_else )? );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA270_eotS =
        "\4\uffff";
    static final String DFA270_eofS =
        "\2\3\2\uffff";
    static final String DFA270_minS =
        "\2\5\2\uffff";
    static final String DFA270_maxS =
        "\2\154\2\uffff";
    static final String DFA270_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA270_specialS =
        "\4\uffff}>";
    static final String[] DFA270_transitionS = {
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\3\uffff\6\3",
            "\2\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\1\3\1\1\1\3\3\uffff\1\3\1\uffff\3\3\24\uffff\11"+
            "\3\1\uffff\1\3\22\uffff\2\1\1\uffff\3\3\6\uffff\1\3\1\uffff"+
            "\3\3\1\2\3\uffff\6\3",
            "",
            ""
    };

    static final short[] DFA270_eot = DFA.unpackEncodedString(DFA270_eotS);
    static final short[] DFA270_eof = DFA.unpackEncodedString(DFA270_eofS);
    static final char[] DFA270_min = DFA.unpackEncodedStringToUnsignedChars(DFA270_minS);
    static final char[] DFA270_max = DFA.unpackEncodedStringToUnsignedChars(DFA270_maxS);
    static final short[] DFA270_accept = DFA.unpackEncodedString(DFA270_acceptS);
    static final short[] DFA270_special = DFA.unpackEncodedString(DFA270_specialS);
    static final short[][] DFA270_transition;

    static {
        int numStates = DFA270_transitionS.length;
        DFA270_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA270_transition[i] = DFA.unpackEncodedString(DFA270_transitionS[i]);
        }
    }

    class DFA270 extends DFA {

        public DFA270(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 270;
            this.eot = DFA270_eot;
            this.eof = DFA270_eof;
            this.min = DFA270_min;
            this.max = DFA270_max;
            this.accept = DFA270_accept;
            this.special = DFA270_special;
            this.transition = DFA270_transition;
        }
        public String getDescription() {
            return "1239:129: ( ( ws )? sass_else )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA289_eotS =
        "\11\uffff";
    static final String DFA289_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA289_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA289_maxS =
        "\3\u0091\2\uffff\4\u0091";
    static final String DFA289_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA289_specialS =
        "\11\uffff}>";
    static final String[] DFA289_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\131"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\76\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\76\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\u0083\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\31"+
            "\3\2\6\76\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\31\3\2\6\76\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\31\3\2\6\76\3"
    };

    static final short[] DFA289_eot = DFA.unpackEncodedString(DFA289_eotS);
    static final short[] DFA289_eof = DFA.unpackEncodedString(DFA289_eofS);
    static final char[] DFA289_min = DFA.unpackEncodedStringToUnsignedChars(DFA289_minS);
    static final char[] DFA289_max = DFA.unpackEncodedStringToUnsignedChars(DFA289_maxS);
    static final short[] DFA289_accept = DFA.unpackEncodedString(DFA289_acceptS);
    static final short[] DFA289_special = DFA.unpackEncodedString(DFA289_specialS);
    static final short[][] DFA289_transition;

    static {
        int numStates = DFA289_transitionS.length;
        DFA289_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA289_transition[i] = DFA.unpackEncodedString(DFA289_transitionS[i]);
        }
    }

    class DFA289 extends DFA {

        public DFA289(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 289;
            this.eot = DFA289_eot;
            this.eof = DFA289_eof;
            this.min = DFA289_min;
            this.max = DFA289_max;
            this.accept = DFA289_accept;
            this.special = DFA289_special;
            this.transition = DFA289_transition;
        }
        public String getDescription() {
            return "376:17: synpred3_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0xBFE00001D1541650L,0x0000078700E00000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0xBFE00001D1D41450L,0x0000078700EC0000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0xBFE00001D1541450L,0x0000078700E00000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0xBFE00001D1541450L,0x0000078700E00000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0xBFE00001D1541440L,0x0000078700E00000L});
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
    public static final BitSet FOLLOW_LBRACE_in_media559 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media561 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_declaration_in_media647 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_media649 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media651 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_sass_extend_in_media674 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media676 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_sass_debug_in_media699 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media701 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_sass_control_in_media724 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media726 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_sass_content_in_media749 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media751 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_rule_in_media789 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media792 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_page_in_media813 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media816 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_fontFace_in_media837 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media840 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media861 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media864 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_media_in_media936 = new BitSet(new long[]{0xBFE0000151D45040L,0x00001387406C0000L});
    public static final BitSet FOLLOW_ws_in_media938 = new BitSet(new long[]{0xBFE0000151545040L,0x0000138740600000L});
    public static final BitSet FOLLOW_RBRACE_in_media983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList999 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList1003 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList1005 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList1008 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery1027 = new BitSet(new long[]{0x0000000000870040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery1029 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery1036 = new BitSet(new long[]{0x0000000000808002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery1038 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery1043 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery1045 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery1048 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery1056 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery1060 = new BitSet(new long[]{0x00000000008F0040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery1062 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery1065 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression1120 = new BitSet(new long[]{0x0000000000C41040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1122 = new BitSet(new long[]{0x0000000000C41040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression1125 = new BitSet(new long[]{0x0000000000B00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1127 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression1132 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1134 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_mediaExpression1137 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression1142 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression1144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature1160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GEN_in_mediaFeature1164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_mediaFeature1170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body1186 = new BitSet(new long[]{0xBFE00001D1D41442L,0x0000078700EC0000L});
    public static final BitSet FOLLOW_ws_in_body1188 = new BitSet(new long[]{0xBFE00001D1541442L,0x0000078700E00000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem1230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_bodyItem1239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem1275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem1287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem1299 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem1313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_bodyItem1327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_debug_in_bodyItem1342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_control_in_bodyItem1356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_function_declaration_in_bodyItem1370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule1396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule1400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule1436 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1438 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule1443 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1445 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule1460 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule1472 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule1482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1498 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1500 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1505 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1507 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document1513 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1515 = new BitSet(new long[]{0x000000000E800100L,0x00000000000C0000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1518 = new BitSet(new long[]{0x0000000000802800L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1520 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document1527 = new BitSet(new long[]{0xBFE00001D1D45440L,0x0000078700EC0000L});
    public static final BitSet FOLLOW_ws_in_moz_document1529 = new BitSet(new long[]{0xBFE00001D1545440L,0x0000078700E00000L});
    public static final BitSet FOLLOW_body_in_moz_document1534 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document1539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1580 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1582 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1585 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1587 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1592 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1594 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1601 = new BitSet(new long[]{0x0000000020804040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1603 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1623 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1625 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1630 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1633 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1636 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1640 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1658 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1670 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1673 = new BitSet(new long[]{0x0000000020800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1675 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1678 = new BitSet(new long[]{0x0000000000800802L,0x00000000000C0000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1707 = new BitSet(new long[]{0x0000000000902040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1709 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1714 = new BitSet(new long[]{0x0000000000902000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1716 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1723 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1725 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1738 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1740 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1795 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1797 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1799 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1805 = new BitSet(new long[]{0x11E1FFFE00C45060L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_page1807 = new BitSet(new long[]{0x11E1FFFE00445060L,0x0000000000200000L});
    public static final BitSet FOLLOW_declaration_in_page1811 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1813 = new BitSet(new long[]{0x0000000000804020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_page1815 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1851 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1853 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1856 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1858 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1869 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1871 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1874 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1878 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1909 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1911 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1922 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_fontFace1924 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1927 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1931 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1956 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_margin1958 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1961 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_margin1963 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1966 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_margin1968 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage2199 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage2201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator2251 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator2262 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator2273 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_combinator2275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_property2382 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_property2394 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_property2407 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_property2422 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_property2430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule2494 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule2524 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule2546 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_rule2548 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule2551 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_rule2565 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule2575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_declarations2641 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2643 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2724 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2726 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2728 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations2813 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2815 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2817 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2830 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2832 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_rule_in_declarations2859 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2861 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_sass_extend_in_declarations2900 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2902 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_sass_debug_in_declarations2941 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2943 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_sass_control_in_declarations2982 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations2984 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_media_in_declarations3023 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations3025 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations3064 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations3066 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_sass_content_in_declarations3105 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations3107 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_sass_function_return_in_declarations3146 = new BitSet(new long[]{0xBFE0000000D41062L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_declarations3148 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncTo_SEMI_in_declarations3194 = new BitSet(new long[]{0xBFE0000000541062L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declaration_in_declarations3224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup3284 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup3286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup3301 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup3304 = new BitSet(new long[]{0xBFE0000000940040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup3306 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup3309 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector3336 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_combinator_in_selector3339 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector3341 = new BitSet(new long[]{0xBFFC000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence3374 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence3381 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence3383 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence3402 = new BitSet(new long[]{0xBFE0000000940042L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence3404 = new BitSet(new long[]{0xBFE0000000140042L,0x0000000000400000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector3520 = new BitSet(new long[]{0xB000000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector3526 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_typeSelector3528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix3546 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix3550 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix3554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_extend_only_selector_in_elementSubsequent3593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent3602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent3611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent3623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent3635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId3663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId3669 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId3671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass3699 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass3701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute3773 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute3780 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3783 = new BitSet(new long[]{0x3000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute3794 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C007FL});
    public static final BitSet FOLLOW_ws_in_slAttribute3796 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_set_in_slAttribute3838 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_slAttribute4018 = new BitSet(new long[]{0x00000000008000C0L,0x00000000000C0000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute4037 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0040L});
    public static final BitSet FOLLOW_ws_in_slAttribute4055 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute4084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName4100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue4114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo4174 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo4238 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4295 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo4298 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_pseudo4300 = new BitSet(new long[]{0x10A4000020E411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_expression_in_pseudo4305 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo4309 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo4314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo4393 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4395 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo4398 = new BitSet(new long[]{0xBFE0000000B40040L,0x00000000004C0000L});
    public static final BitSet FOLLOW_ws_in_pseudo4400 = new BitSet(new long[]{0xBFE0000000340040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo4403 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo4406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration4445 = new BitSet(new long[]{0x11E0000000441040L,0x0000000000200000L});
    public static final BitSet FOLLOW_property_in_declaration4448 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration4450 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_declaration4452 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_declaration4455 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_prio_in_declaration4458 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_declaration4460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_property_value_interpolation_expression_in_propertyValue4530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue4546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue4587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4625 = new BitSet(new long[]{0xEFFDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_set_in_expressionPredicate4654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI4772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio4827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression4848 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_operator_in_expression4853 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_expression4855 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_expression4860 = new BitSet(new long[]{0x00A6000020C419C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_unaryOperator_in_term4885 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_term4887 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_set_in_term4911 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_term5111 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_term5119 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_term5127 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_term5135 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_term5143 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_term5151 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_term5161 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_term5173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function5189 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function5191 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function5196 = new BitSet(new long[]{0x01E6000020EC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_function5198 = new BitSet(new long[]{0x01E6000020EC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_variable_value_in_function5227 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_expression_in_function5271 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function5289 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function5292 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_function5294 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_fnAttribute_in_function5297 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function5355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName5403 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName5405 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName5409 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName5412 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName5414 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute5437 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0001L});
    public static final BitSet FOLLOW_ws_in_fnAttribute5439 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute5442 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_fnAttribute5444 = new BitSet(new long[]{0x00A4000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute5447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName5462 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName5465 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName5467 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue5481 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor5499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws5520 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5568 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5570 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5573 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5575 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_variable_value_in_cp_variable_declaration5578 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration5607 = new BitSet(new long[]{0x0000000000900000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5609 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration5612 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5614 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_variable_value_in_cp_variable_declaration5617 = new BitSet(new long[]{0x0000000000000020L,0x0000000000100000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration5620 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration5622 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration5627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable5660 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable5692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_value5716 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_cp_variable_value5720 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_variable_value5722 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_value5725 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression5753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5773 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp5787 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5789 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5792 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp5805 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5807 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5810 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5843 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp5856 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5858 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5861 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp5875 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5877 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5880 = new BitSet(new long[]{0x1002000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5913 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5920 = new BitSet(new long[]{0x00A4000020C411C2L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp5934 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5936 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp5939 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp5941 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term5981 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_STRING_in_cp_term6181 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term6189 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_GEN_in_cp_term6197 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_URI_in_cp_term6205 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term6213 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_function_in_cp_term6221 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term6229 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_term6241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration6272 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration6274 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6276 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration6279 = new BitSet(new long[]{0x0000000000E01000L,0x00000000032C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6281 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_cp_mixin_declaration6284 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration6287 = new BitSet(new long[]{0x0000000000800002L,0x00000000040C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6289 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration6293 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration6312 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6314 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration6316 = new BitSet(new long[]{0x0000000000880002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6318 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration6322 = new BitSet(new long[]{0x0000000000E01000L,0x00000000032C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6324 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_cp_mixin_declaration6327 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration6330 = new BitSet(new long[]{0x0000000000800002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration6332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call6374 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call6376 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call6398 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6400 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call6402 = new BitSet(new long[]{0x0000000000880020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6415 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call6418 = new BitSet(new long[]{0x01E6000020EC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6420 = new BitSet(new long[]{0x01E6000020EC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call6423 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call6426 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call6430 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call6433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name6462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6498 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args6502 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args6510 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_mixin_call_arg_in_cp_mixin_call_args6513 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_cp_arg_in_cp_mixin_call_arg6546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_cp_mixin_call_arg6554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_arg_in_cp_args_list6591 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_args_list6595 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_cp_args_list6605 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_arg_in_cp_args_list6608 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_args_list6614 = new BitSet(new long[]{0x0000000000800000L,0x00000000030C0000L});
    public static final BitSet FOLLOW_ws_in_cp_args_list6624 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_set_in_cp_args_list6627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_args_list6649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_arg6681 = new BitSet(new long[]{0x0000000000900002L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_cp_arg6685 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_arg6688 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_cp_arg6690 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_cp_arg6693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded6719 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6721 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6724 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded6728 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded6736 = new BitSet(new long[]{0x00000000008A0000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded6739 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition6769 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6771 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition6780 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6782 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition6808 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_condition6810 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition6841 = new BitSet(new long[]{0x0008000000A00000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_ws_in_less_condition6844 = new BitSet(new long[]{0x0008000000800000L,0x00000000380C0001L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition6847 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_less_condition6849 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition6852 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition6881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition6907 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6909 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition6912 = new BitSet(new long[]{0x0000000000C01000L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6914 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition6917 = new BitSet(new long[]{0x0000000000A00000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6919 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition6922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name6944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression7037 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression7065 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression7126 = new BitSet(new long[]{0x81E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression7159 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression7195 = new BitSet(new long[]{0x81E0000000900042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7291 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression7319 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression7372 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression7405 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression7441 = new BitSet(new long[]{0x01E0000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7525 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_property_value_interpolation_expression7553 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_property_value_interpolation_expression7610 = new BitSet(new long[]{0x01E2000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_property_value_interpolation_expression7643 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_property_value_interpolation_expression7679 = new BitSet(new long[]{0x01E2000000800042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7771 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7799 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_mq_interpolation_expression7864 = new BitSet(new long[]{0x01E0000000128040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression7897 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression7933 = new BitSet(new long[]{0x01E0000000928042L,0x00000000000C0000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var8018 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var8020 = new BitSet(new long[]{0x0000000000C01040L,0x00000000002C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var8022 = new BitSet(new long[]{0x0000000000401040L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var8027 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var8031 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_IDENT_in_scss_interpolation_expression_var8035 = new BitSet(new long[]{0x0000000000804000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var8039 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var8042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties8086 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties8088 = new BitSet(new long[]{0x01E6000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties8090 = new BitSet(new long[]{0x01E6000020CC31C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties8093 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties8096 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties8098 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties8101 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties8103 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties8105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_in_sass_extend8126 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend8128 = new BitSet(new long[]{0xBFE0000000140040L,0x0000000000400000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_sass_extend8130 = new BitSet(new long[]{0x0000000000000020L,0x0000000080000000L});
    public static final BitSet FOLLOW_SASS_OPTIONAL_in_sass_extend8133 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_extend8135 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_extend8140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EXTEND_ONLY_SELECTOR_in_sass_extend_only_selector8165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_sass_debug8186 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_debug8196 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_debug8198 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_debug8200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_if_in_sass_control8225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_for_in_sass_control8229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_each_in_sass_control8233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_while_in_sass_control8237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_IF_in_sass_if8258 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_if8260 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_if8262 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_if8264 = new BitSet(new long[]{0x0000000000800002L,0x00000008000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_if8267 = new BitSet(new long[]{0x0000000000800000L,0x00000008000C0000L});
    public static final BitSet FOLLOW_sass_else_in_sass_if8270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_ELSE_in_sass_else8297 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8299 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_else8302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_ELSE_in_sass_else8315 = new BitSet(new long[]{0x0000000000800040L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8317 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_else8322 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_else8326 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_else8329 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_else8331 = new BitSet(new long[]{0x0000000000800002L,0x00000008000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_else8334 = new BitSet(new long[]{0x0000000000800000L,0x00000008000C0000L});
    public static final BitSet FOLLOW_sass_else_in_sass_else8337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sass_control_expression_condition_in_sass_control_expression8360 = new BitSet(new long[]{0x0000000000008002L,0x0000001000000000L});
    public static final BitSet FOLLOW_set_in_sass_control_expression8364 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_control_expression8372 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_condition_in_sass_control_expression8375 = new BitSet(new long[]{0x0000000000008002L,0x0000001000000000L});
    public static final BitSet FOLLOW_NOT_in_sass_control_expression_condition8404 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_control_expression_condition8406 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_control_expression_condition8411 = new BitSet(new long[]{0x0008000000000002L,0x0000006038000000L});
    public static final BitSet FOLLOW_set_in_sass_control_expression_condition8414 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_control_expression_condition8439 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_control_expression_condition8442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_FOR_in_sass_for8465 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8467 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_sass_for8469 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8471 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_for8473 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8477 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_for8479 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_for8481 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_for8485 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_for8487 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_for8489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_EACH_in_sass_each8510 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8512 = new BitSet(new long[]{0x0000000000401000L,0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_sass_each8514 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8516 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_sass_each8518 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_each8522 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_sass_each_list_in_sass_each8524 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_each8526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_term_in_sass_each_list8551 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_sass_each_list8554 = new BitSet(new long[]{0x0080000020C411C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_ws_in_sass_each_list8556 = new BitSet(new long[]{0x00800000204411C0L,0x000000000023FF00L});
    public static final BitSet FOLLOW_cp_term_in_sass_each_list8559 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_SASS_WHILE_in_sass_while8586 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_while8588 = new BitSet(new long[]{0x01E6000020CE11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_sass_control_expression_in_sass_while8590 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_sass_control_block_in_sass_while8592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_sass_control_block8613 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_sass_control_block8615 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_sass_control_block8618 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_sass_control_block8620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_FUNCTION_in_sass_function_declaration8656 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8658 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_sass_function_name_in_sass_function_declaration8660 = new BitSet(new long[]{0x0000000000880000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8662 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_sass_function_declaration8665 = new BitSet(new long[]{0x0000000000601000L,0x0000000003200000L});
    public static final BitSet FOLLOW_cp_args_list_in_sass_function_declaration8667 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_sass_function_declaration8670 = new BitSet(new long[]{0x0000000000802000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8672 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_sass_function_declaration8675 = new BitSet(new long[]{0xBFE0000000D45060L,0x00001B8740EC0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_declaration8677 = new BitSet(new long[]{0xBFE0000000545060L,0x00001B8740E00000L});
    public static final BitSet FOLLOW_declarations_in_sass_function_declaration8680 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_sass_function_declaration8682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_sass_function_name8707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_RETURN_in_sass_function_return8728 = new BitSet(new long[]{0x0000000000800000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_function_return8730 = new BitSet(new long[]{0x01E6000020CC11C0L,0x00000000002FFF00L});
    public static final BitSet FOLLOW_cp_expression_in_sass_function_return8732 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_function_return8734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_CONTENT_in_sass_content8759 = new BitSet(new long[]{0x0000000000800020L,0x00000000000C0000L});
    public static final BitSet FOLLOW_ws_in_sass_content8761 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_sass_content8764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css3476 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred1_Css3488 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css3490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQueryList_in_synpred2_Css3527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3613 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3625 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_set_in_synpred3_Css3627 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred3_Css3637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3641 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_synpred4_Css31227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32369 = new BitSet(new long[]{0xFFFFFFFFFFEFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred5_Css32377 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_synpred6_Css32491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_synpred7_Css32638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_synpred8_Css32719 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_synpred8_Css32721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred9_Css32786 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_COLON_in_synpred9_Css32798 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_set_in_synpred9_Css32800 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred9_Css32810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_nested_properties_in_synpred10_Css32827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_synpred11_Css32856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred12_Css33187 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred12_Css33191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred13_Css33266 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred13_Css33278 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred13_Css33280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred14_Css33378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred15_Css33399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred16_Css33508 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred16_Css33517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred17_Css34513 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x000000000003FFFFL});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred17_Css34525 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred17_Css34527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred18_Css34543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred19_Css35917 = new BitSet(new long[]{0x0000000000000002L});

}