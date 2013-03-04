// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-04 13:10:18

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
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
    public static final int STAR=59;
    public static final int PIPE=60;
    public static final int NAME=61;
    public static final int LESS_AND=62;
    public static final int OPEQ=63;
    public static final int INCLUDES=64;
    public static final int DASHMATCH=65;
    public static final int BEGINS=66;
    public static final int ENDS=67;
    public static final int CONTAINS=68;
    public static final int RBRACKET=69;
    public static final int IMPORTANT_SYM=70;
    public static final int NUMBER=71;
    public static final int LENGTH=72;
    public static final int EMS=73;
    public static final int REM=74;
    public static final int EXS=75;
    public static final int ANGLE=76;
    public static final int TIME=77;
    public static final int FREQ=78;
    public static final int RESOLUTION=79;
    public static final int DIMENSION=80;
    public static final int NL=81;
    public static final int COMMENT=82;
    public static final int SASS_DEFAULT=83;
    public static final int SASS_VAR=84;
    public static final int SASS_MIXIN=85;
    public static final int SASS_INCLUDE=86;
    public static final int LESS_DOTS=87;
    public static final int LESS_REST=88;
    public static final int LESS_WHEN=89;
    public static final int GREATER_OR_EQ=90;
    public static final int LESS=91;
    public static final int LESS_OR_EQ=92;
    public static final int HEXCHAR=93;
    public static final int NONASCII=94;
    public static final int UNICODE=95;
    public static final int ESCAPE=96;
    public static final int NMSTART=97;
    public static final int NMCHAR=98;
    public static final int URL=99;
    public static final int A=100;
    public static final int B=101;
    public static final int C=102;
    public static final int D=103;
    public static final int E=104;
    public static final int F=105;
    public static final int G=106;
    public static final int H=107;
    public static final int I=108;
    public static final int J=109;
    public static final int K=110;
    public static final int L=111;
    public static final int M=112;
    public static final int N=113;
    public static final int O=114;
    public static final int P=115;
    public static final int Q=116;
    public static final int R=117;
    public static final int S=118;
    public static final int T=119;
    public static final int U=120;
    public static final int V=121;
    public static final int W=122;
    public static final int X=123;
    public static final int Y=124;
    public static final int Z=125;
    public static final int CDO=126;
    public static final int CDC=127;
    public static final int INVALID=128;
    public static final int LINE_COMMENT=129;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "synpred16_Css3", "mediaQueryOperator", "mediaQuery", 
        "cssId", "less_arg", "slAttributeValue", "pseudo", "charSet", "simpleSelectorSequence", 
        "mediaQueryList", "namespaces", "synpred4_Css3", "selector", "cp_additionExp", 
        "term", "webkitKeyframeSelectors", "charSetValue", "cp_mixin_call_args", 
        "cssClass", "mediaExpression", "synpred20_Css3", "importItem", "margin", 
        "synpred12_Css3", "page", "atRuleId", "synpred18_Css3", "body", 
        "scss_selector_interpolation_expression", "scss_interpolation_expression_var", 
        "synpred1_Css3", "combinator", "namespace", "unaryOperator", "less_condition_operator", 
        "scss_nested_properties", "slAttribute", "styleSheet", "synpred5_Css3", 
        "less_fn_name", "fnAttributeValue", "synpred8_Css3", "less_condition", 
        "fnAttribute", "synpred14_Css3", "moz_document_function", "cp_atomExp", 
        "cp_term", "cp_expression", "syncTo_RBRACE", "fnAttributeName", 
        "fontFace", "synpred17_Css3", "mediaType", "webkitKeyframesBlock", 
        "synpred13_Css3", "synpred15_Css3", "slAttributeName", "prio", "syncToDeclarationsRule", 
        "synpred7_Css3", "hexColor", "resourceIdentifier", "less_args_list", 
        "elementSubsequent", "scss_declaration_interpolation_expression", 
        "synpred2_Css3", "cp_multiplyExp", "esPred", "synpred19_Css3", "scss_mq_interpolation_expression", 
        "property", "syncTo_SEMI", "syncToFollow", "rule", "bodyItem", "synpred10_Css3", 
        "namespacePrefix", "cp_variable_declaration", "imports", "less_mixin_guarded", 
        "synpred6_Css3", "cp_mixin_name", "cp_mixin_declaration", "mediaFeature", 
        "operator", "webkitKeyframes", "propertyValue", "ws", "declarations", 
        "functionName", "counterStyle", "cp_variable", "selectorsGroup", 
        "typeSelector", "media", "expression", "less_function_in_condition", 
        "cp_mixin_call", "vendorAtRule", "synpred21_Css3", "margin_sym", 
        "moz_document", "synpred3_Css3", "generic_at_rule", "pseudoPage", 
        "function", "declaration", "expressionPredicate", "namespacePrefixName", 
        "synpred11_Css3", "synpred9_Css3", "elementName"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, false, false, false, true, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, true, false, false, false, 
            false, true, false, false, true, false, true, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, false, false, false, true, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, false, false, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_mq_interpolation_expression ( ws )? | ( mediaQueryList )=> mediaQueryList ) LBRACE ( ws )? ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )* RBRACE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*
            try { dbg.enterSubRule(33);

            loop33:
            do {
                int alt33=7;
                try { dbg.enterDecision(33, decisionCanBacktrack[33]);

                try {
                    isCyclicDecision = true;
                    alt33 = dfa33.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(33);}

                switch (alt33) {
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:19: rule ( ws )?
            	    {
            	    dbg.location(378,19);
            	    pushFollow(FOLLOW_rule_in_media672);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(378,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:25: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:25: ws
            	            {
            	            dbg.location(378,25);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:19: page ( ws )?
            	    {
            	    dbg.location(379,19);
            	    pushFollow(FOLLOW_page_in_media696);
            	    page();

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
            	            pushFollow(FOLLOW_ws_in_media699);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:19: fontFace ( ws )?
            	    {
            	    dbg.location(380,19);
            	    pushFollow(FOLLOW_fontFace_in_media720);
            	    fontFace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(380,29);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:29: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:29: ws
            	            {
            	            dbg.location(380,29);
            	            pushFollow(FOLLOW_ws_in_media723);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:19: vendorAtRule ( ws )?
            	    {
            	    dbg.location(381,19);
            	    pushFollow(FOLLOW_vendorAtRule_in_media744);
            	    vendorAtRule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(381,33);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:33: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:33: ws
            	            {
            	            dbg.location(381,33);
            	            pushFollow(FOLLOW_ws_in_media747);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:19: {...}? media ( ws )?
            	    {
            	    dbg.location(382,19);
            	    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "media", "isScssSource()");
            	    }
            	    dbg.location(382,37);
            	    pushFollow(FOLLOW_media_in_media770);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(382,43);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:43: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:43: ws
            	            {
            	            dbg.location(382,43);
            	            pushFollow(FOLLOW_ws_in_media772);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(32);}


            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);
            } finally {dbg.exitSubRule(33);}

            dbg.location(385,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media816); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(386, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(389,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt36=2;
            try { dbg.enterSubRule(36);
            try { dbg.enterDecision(36, decisionCanBacktrack[36]);

            int LA36_0 = input.LA(1);

            if ( (LA36_0==IDENT||(LA36_0>=ONLY && LA36_0<=LPAREN)) ) {
                alt36=1;
            }
            } finally {dbg.exitDecision(36);}

            switch (alt36) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(389,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList832);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(389,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(35);

                    loop35:
                    do {
                        int alt35=2;
                        try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==COMMA) ) {
                            alt35=1;
                        }


                        } finally {dbg.exitDecision(35);}

                        switch (alt35) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(389,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList836); if (state.failed) return ;
                    	    dbg.location(389,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:25: ws
                    	            {
                    	            dbg.location(389,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList838);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(34);}

                    	    dbg.location(389,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList841);
                    	    mediaQuery();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(35);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(390, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt44=2;
            try { dbg.enterDecision(44, decisionCanBacktrack[44]);

            int LA44_0 = input.LA(1);

            if ( (LA44_0==IDENT||(LA44_0>=ONLY && LA44_0<=GEN)) ) {
                alt44=1;
            }
            else if ( (LA44_0==LPAREN) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(44);}

            switch (alt44) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(393,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: ( mediaQueryOperator ( ws )? )?
                    int alt38=2;
                    try { dbg.enterSubRule(38);
                    try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                    int LA38_0 = input.LA(1);

                    if ( ((LA38_0>=ONLY && LA38_0<=NOT)) ) {
                        alt38=1;
                    }
                    } finally {dbg.exitDecision(38);}

                    switch (alt38) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(393,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery860);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(393,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:24: ws
                                    {
                                    dbg.location(393,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery862);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(37);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(38);}

                    dbg.location(393,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery869);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(393,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:42: ws
                            {
                            dbg.location(393,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery871);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(39);}

                    dbg.location(393,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(41);

                    loop41:
                    do {
                        int alt41=2;
                        try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                        int LA41_0 = input.LA(1);

                        if ( (LA41_0==AND) ) {
                            alt41=1;
                        }


                        } finally {dbg.exitDecision(41);}

                        switch (alt41) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(393,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery876); if (state.failed) return ;
                    	    dbg.location(393,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:52: ws
                    	            {
                    	            dbg.location(393,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery878);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(40);}

                    	    dbg.location(393,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery881);
                    	    mediaExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(41);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(394,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery889);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(394,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:20: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(394,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery893); if (state.failed) return ;
                    	    dbg.location(394,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:26: ws
                    	            {
                    	            dbg.location(394,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery895);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(42);}

                    	    dbg.location(394,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery898);
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

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(395, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(397, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(398,3);
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
        dbg.location(399, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(401, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(402,2);
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
        dbg.location(403, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(406,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression953); if (state.failed) return ;
            dbg.location(406,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:14: ws
                    {
                    dbg.location(406,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression955);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(45);}

            dbg.location(406,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression958);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(406,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:31: ws
                    {
                    dbg.location(406,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression960);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(46);}

            dbg.location(406,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:35: ( COLON ( ws )? expression )?
            int alt48=2;
            try { dbg.enterSubRule(48);
            try { dbg.enterDecision(48, decisionCanBacktrack[48]);

            int LA48_0 = input.LA(1);

            if ( (LA48_0==COLON) ) {
                alt48=1;
            }
            } finally {dbg.exitDecision(48);}

            switch (alt48) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:37: COLON ( ws )? expression
                    {
                    dbg.location(406,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression965); if (state.failed) return ;
                    dbg.location(406,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:43: ws
                            {
                            dbg.location(406,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression967);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(47);}

                    dbg.location(406,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression970);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}

            dbg.location(406,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression975); if (state.failed) return ;
            dbg.location(406,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:68: ws
                    {
                    dbg.location(406,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression977);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(49);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(407, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:4: IDENT
            {
            dbg.location(410,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature993); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:2: ( bodyItem ( ws )? )+
            {
            dbg.location(414,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:2: ( bodyItem ( ws )? )+
            int cnt51=0;
            try { dbg.enterSubRule(51);

            loop51:
            do {
                int alt51=2;
                try { dbg.enterDecision(51, decisionCanBacktrack[51]);

                int LA51_0 = input.LA(1);

                if ( (LA51_0==IDENT||LA51_0==MEDIA_SYM||LA51_0==GEN||LA51_0==COLON||LA51_0==AT_IDENT||LA51_0==MOZ_DOCUMENT_SYM||LA51_0==WEBKIT_KEYFRAMES_SYM||(LA51_0>=PAGE_SYM && LA51_0<=FONT_FACE_SYM)||(LA51_0>=MINUS && LA51_0<=PIPE)||LA51_0==LESS_AND||(LA51_0>=SASS_VAR && LA51_0<=SASS_INCLUDE)) ) {
                    alt51=1;
                }


                } finally {dbg.exitDecision(51);}

                switch (alt51) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:4: bodyItem ( ws )?
            	    {
            	    dbg.location(414,4);
            	    pushFollow(FOLLOW_bodyItem_in_body1009);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(414,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:13: ws
            	            {
            	            dbg.location(414,13);
            	            pushFollow(FOLLOW_ws_in_body1011);
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
            	    if ( cnt51 >= 1 ) break loop51;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(51, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt51++;
            } while (true);
            } finally {dbg.exitSubRule(51);}


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
            dbg.exitRule(getGrammarFileName(), "body");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "body"


    // $ANTLR start "bodyItem"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call )
            int alt52=8;
            try { dbg.enterDecision(52, decisionCanBacktrack[52]);

            try {
                isCyclicDecision = true;
                alt52 = dfa52.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(52);}

            switch (alt52) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:6: rule
                    {
                    dbg.location(419,6);
                    pushFollow(FOLLOW_rule_in_bodyItem1036);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:11: media
                    {
                    dbg.location(420,11);
                    pushFollow(FOLLOW_media_in_bodyItem1048);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:11: page
                    {
                    dbg.location(421,11);
                    pushFollow(FOLLOW_page_in_bodyItem1060);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:11: counterStyle
                    {
                    dbg.location(422,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem1072);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:11: fontFace
                    {
                    dbg.location(423,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem1084);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:11: vendorAtRule
                    {
                    dbg.location(424,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem1096);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:11: {...}? cp_variable_declaration
                    {
                    dbg.location(425,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(425,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem1110);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:11: {...}? cp_mixin_call
                    {
                    dbg.location(426,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(426,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem1124);
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
        dbg.location(427, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(435, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt53=3;
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt53=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt53=2;
                }
                break;
            case AT_IDENT:
                {
                alt53=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:3: moz_document
                    {
                    dbg.location(436,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule1147);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:18: webkitKeyframes
                    {
                    dbg.location(436,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule1151);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:36: generic_at_rule
                    {
                    dbg.location(436,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule1155);
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
        dbg.location(436, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(438, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(439,2);
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
        dbg.location(441, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(443, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(444,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule1191); if (state.failed) return ;
            dbg.location(444,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:16: WS
            	    {
            	    dbg.location(444,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule1193); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);
            } finally {dbg.exitSubRule(54);}

            dbg.location(444,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:20: ( atRuleId ( WS )* )?
            int alt56=2;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56, decisionCanBacktrack[56]);

            int LA56_0 = input.LA(1);

            if ( ((LA56_0>=IDENT && LA56_0<=STRING)) ) {
                alt56=1;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:22: atRuleId ( WS )*
                    {
                    dbg.location(444,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule1198);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(444,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:31: WS
                    	    {
                    	    dbg.location(444,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule1200); if (state.failed) return ;

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

            dbg.location(445,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule1215); if (state.failed) return ;
            dbg.location(446,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule1227);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(447,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule1237); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "generic_at_rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "generic_at_rule"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(451,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1253); if (state.failed) return ;
            dbg.location(451,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:19: ws
                    {
                    dbg.location(451,19);
                    pushFollow(FOLLOW_ws_in_moz_document1255);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(451,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:25: moz_document_function ( ws )?
            {
            dbg.location(451,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document1260);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(451,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:47: ws
                    {
                    dbg.location(451,47);
                    pushFollow(FOLLOW_ws_in_moz_document1262);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}


            }

            dbg.location(451,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(61);

            loop61:
            do {
                int alt61=2;
                try { dbg.enterDecision(61, decisionCanBacktrack[61]);

                int LA61_0 = input.LA(1);

                if ( (LA61_0==COMMA) ) {
                    alt61=1;
                }


                } finally {dbg.exitDecision(61);}

                switch (alt61) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(451,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document1268); if (state.failed) return ;
            	    dbg.location(451,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:60: ws
            	            {
            	            dbg.location(451,60);
            	            pushFollow(FOLLOW_ws_in_moz_document1270);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(59);}

            	    dbg.location(451,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document1273);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(451,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:86: ws
            	            {
            	            dbg.location(451,86);
            	            pushFollow(FOLLOW_ws_in_moz_document1275);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(60);}


            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);
            } finally {dbg.exitSubRule(61);}

            dbg.location(452,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document1282); if (state.failed) return ;
            dbg.location(452,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:9: ws
                    {
                    dbg.location(452,9);
                    pushFollow(FOLLOW_ws_in_moz_document1284);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(62);}

            dbg.location(453,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:3: ( body )?
            int alt63=2;
            try { dbg.enterSubRule(63);
            try { dbg.enterDecision(63, decisionCanBacktrack[63]);

            int LA63_0 = input.LA(1);

            if ( (LA63_0==IDENT||LA63_0==MEDIA_SYM||LA63_0==GEN||LA63_0==COLON||LA63_0==AT_IDENT||LA63_0==MOZ_DOCUMENT_SYM||LA63_0==WEBKIT_KEYFRAMES_SYM||(LA63_0>=PAGE_SYM && LA63_0<=FONT_FACE_SYM)||(LA63_0>=MINUS && LA63_0<=PIPE)||LA63_0==LESS_AND||(LA63_0>=SASS_VAR && LA63_0<=SASS_INCLUDE)) ) {
                alt63=1;
            }
            } finally {dbg.exitDecision(63);}

            switch (alt63) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:3: body
                    {
                    dbg.location(453,3);
                    pushFollow(FOLLOW_body_in_moz_document1289);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(454,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document1294); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(455, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(457, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(458,2);
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
        dbg.location(460, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(463, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(465,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1335); if (state.failed) return ;
            dbg.location(465,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:23: ws
                    {
                    dbg.location(465,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1337);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(465,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1340);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(465,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:36: ws
                    {
                    dbg.location(465,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1342);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(466,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1347); if (state.failed) return ;
            dbg.location(466,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:9: ws
                    {
                    dbg.location(466,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1349);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(66);}

            dbg.location(467,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(68);

            loop68:
            do {
                int alt68=2;
                try { dbg.enterDecision(68, decisionCanBacktrack[68]);

                int LA68_0 = input.LA(1);

                if ( (LA68_0==IDENT||LA68_0==PERCENTAGE) ) {
                    alt68=1;
                }


                } finally {dbg.exitDecision(68);}

                switch (alt68) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(467,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1356);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(467,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:26: ws
            	            {
            	            dbg.location(467,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1358);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(67);}


            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);
            } finally {dbg.exitSubRule(68);}

            dbg.location(468,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1365); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(469, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(471, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(473,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1378);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(473,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:26: ws
                    {
                    dbg.location(473,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1380);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(475,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1385); if (state.failed) return ;
            dbg.location(475,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:10: ws
                    {
                    dbg.location(475,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1388);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(475,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1391);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(476,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1395);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(477,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1398); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(478, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(480, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(482,2);
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

            dbg.location(482,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            try { dbg.enterSubRule(73);

            loop73:
            do {
                int alt73=2;
                try { dbg.enterDecision(73, decisionCanBacktrack[73]);

                try {
                    isCyclicDecision = true;
                    alt73 = dfa73.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(73);}

                switch (alt73) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(482,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:27: ws
            	            {
            	            dbg.location(482,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1425);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(71);}

            	    dbg.location(482,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1428); if (state.failed) return ;
            	    dbg.location(482,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:37: ws
            	            {
            	            dbg.location(482,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1430);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(482,41);
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
        dbg.location(483, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(485, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(486,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1462); if (state.failed) return ;
            dbg.location(486,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:16: ws
                    {
                    dbg.location(486,16);
                    pushFollow(FOLLOW_ws_in_page1464);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(74);}

            dbg.location(486,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:20: ( IDENT ( ws )? )?
            int alt76=2;
            try { dbg.enterSubRule(76);
            try { dbg.enterDecision(76, decisionCanBacktrack[76]);

            int LA76_0 = input.LA(1);

            if ( (LA76_0==IDENT) ) {
                alt76=1;
            }
            } finally {dbg.exitDecision(76);}

            switch (alt76) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:22: IDENT ( ws )?
                    {
                    dbg.location(486,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1469); if (state.failed) return ;
                    dbg.location(486,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:28: ws
                            {
                            dbg.location(486,28);
                            pushFollow(FOLLOW_ws_in_page1471);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(486,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:35: ( pseudoPage ( ws )? )?
            int alt78=2;
            try { dbg.enterSubRule(78);
            try { dbg.enterDecision(78, decisionCanBacktrack[78]);

            int LA78_0 = input.LA(1);

            if ( (LA78_0==COLON) ) {
                alt78=1;
            }
            } finally {dbg.exitDecision(78);}

            switch (alt78) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:36: pseudoPage ( ws )?
                    {
                    dbg.location(486,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1478);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(486,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:47: ws
                            {
                            dbg.location(486,47);
                            pushFollow(FOLLOW_ws_in_page1480);
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

            dbg.location(487,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1493); if (state.failed) return ;
            dbg.location(487,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ws
                    {
                    dbg.location(487,16);
                    pushFollow(FOLLOW_ws_in_page1495);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(491,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:13: ( declaration | margin ( ws )? )?
            int alt81=3;
            try { dbg.enterSubRule(81);
            try { dbg.enterDecision(81, decisionCanBacktrack[81]);

            int LA81_0 = input.LA(1);

            if ( (LA81_0==IDENT||LA81_0==MEDIA_SYM||LA81_0==GEN||LA81_0==AT_IDENT||(LA81_0>=MINUS && LA81_0<=DOT)||LA81_0==STAR||LA81_0==SASS_VAR) ) {
                alt81=1;
            }
            else if ( ((LA81_0>=TOPLEFTCORNER_SYM && LA81_0<=RIGHTBOTTOM_SYM)) ) {
                alt81=2;
            }
            } finally {dbg.exitDecision(81);}

            switch (alt81) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:14: declaration
                    {
                    dbg.location(491,14);
                    pushFollow(FOLLOW_declaration_in_page1550);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:26: margin ( ws )?
                    {
                    dbg.location(491,26);
                    pushFollow(FOLLOW_margin_in_page1552);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(491,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:33: ws
                            {
                            dbg.location(491,33);
                            pushFollow(FOLLOW_ws_in_page1554);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(80);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(491,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(85);

            loop85:
            do {
                int alt85=2;
                try { dbg.enterDecision(85, decisionCanBacktrack[85]);

                int LA85_0 = input.LA(1);

                if ( (LA85_0==SEMI) ) {
                    alt85=1;
                }


                } finally {dbg.exitDecision(85);}

                switch (alt85) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(491,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1560); if (state.failed) return ;
            	    dbg.location(491,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:45: ws
            	            {
            	            dbg.location(491,45);
            	            pushFollow(FOLLOW_ws_in_page1562);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(82);}

            	    dbg.location(491,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:49: ( declaration | margin ( ws )? )?
            	    int alt84=3;
            	    try { dbg.enterSubRule(84);
            	    try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            	    int LA84_0 = input.LA(1);

            	    if ( (LA84_0==IDENT||LA84_0==MEDIA_SYM||LA84_0==GEN||LA84_0==AT_IDENT||(LA84_0>=MINUS && LA84_0<=DOT)||LA84_0==STAR||LA84_0==SASS_VAR) ) {
            	        alt84=1;
            	    }
            	    else if ( ((LA84_0>=TOPLEFTCORNER_SYM && LA84_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt84=2;
            	    }
            	    } finally {dbg.exitDecision(84);}

            	    switch (alt84) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:50: declaration
            	            {
            	            dbg.location(491,50);
            	            pushFollow(FOLLOW_declaration_in_page1566);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:62: margin ( ws )?
            	            {
            	            dbg.location(491,62);
            	            pushFollow(FOLLOW_margin_in_page1568);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(491,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:69: ws
            	                    {
            	                    dbg.location(491,69);
            	                    pushFollow(FOLLOW_ws_in_page1570);
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


            	    }
            	    break;

            	default :
            	    break loop85;
                }
            } while (true);
            } finally {dbg.exitSubRule(85);}

            dbg.location(492,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1585); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(495, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(496,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1606); if (state.failed) return ;
            dbg.location(496,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:25: ws
                    {
                    dbg.location(496,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1608);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(86);}

            dbg.location(496,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1611); if (state.failed) return ;
            dbg.location(496,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:35: ws
                    {
                    dbg.location(496,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1613);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(497,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1624); if (state.failed) return ;
            dbg.location(497,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ws
                    {
                    dbg.location(497,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1626);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(88);}

            dbg.location(497,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1629);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1633);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(499,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1643); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "counterStyle");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "counterStyle"


    // $ANTLR start "fontFace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(502, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(503,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1664); if (state.failed) return ;
            dbg.location(503,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:21: ws
                    {
                    dbg.location(503,21);
                    pushFollow(FOLLOW_ws_in_fontFace1666);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(504,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1677); if (state.failed) return ;
            dbg.location(504,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:16: ws
                    {
                    dbg.location(504,16);
                    pushFollow(FOLLOW_ws_in_fontFace1679);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(504,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1682);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(505,3);
            pushFollow(FOLLOW_declarations_in_fontFace1686);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(506,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1696); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(507, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(509, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(510,4);
            pushFollow(FOLLOW_margin_sym_in_margin1711);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(510,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:15: ws
                    {
                    dbg.location(510,15);
                    pushFollow(FOLLOW_ws_in_margin1713);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(510,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1716); if (state.failed) return ;
            dbg.location(510,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:26: ws
                    {
                    dbg.location(510,26);
                    pushFollow(FOLLOW_ws_in_margin1718);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(510,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1721);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(510,53);
            pushFollow(FOLLOW_declarations_in_margin1723);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(510,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1725); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(511, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(513, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(514,2);
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
        dbg.location(531, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(533, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: COLON IDENT
            {
            dbg.location(534,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1954); if (state.failed) return ;
            dbg.location(534,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1956); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(535, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(537, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(538,5);
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
        dbg.location(540, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(542, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt96=4;
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt96=1;
                }
                break;
            case GREATER:
                {
                alt96=2;
                }
                break;
            case TILDE:
                {
                alt96=3;
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
            case STAR:
            case PIPE:
            case LESS_AND:
                {
                alt96=4;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:7: PLUS ( ws )?
                    {
                    dbg.location(543,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator2006); if (state.failed) return ;
                    dbg.location(543,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:12: ws
                            {
                            dbg.location(543,12);
                            pushFollow(FOLLOW_ws_in_combinator2008);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(93);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:7: GREATER ( ws )?
                    {
                    dbg.location(544,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator2017); if (state.failed) return ;
                    dbg.location(544,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:15: ws
                            {
                            dbg.location(544,15);
                            pushFollow(FOLLOW_ws_in_combinator2019);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(94);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:7: TILDE ( ws )?
                    {
                    dbg.location(545,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator2028); if (state.failed) return ;
                    dbg.location(545,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ws
                            {
                            dbg.location(545,13);
                            pushFollow(FOLLOW_ws_in_combinator2030);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(95);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:5: 
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
        dbg.location(547, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(549, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(550,5);
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
        dbg.location(552, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(554, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(555,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:7: ( IDENT | GEN | {...}? cp_variable )
            int alt97=3;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt97=1;
                }
                break;
            case GEN:
                {
                alt97=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
            case SASS_VAR:
                {
                alt97=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:8: IDENT
                    {
                    dbg.location(555,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property2091); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:16: GEN
                    {
                    dbg.location(555,16);
                    match(input,GEN,FOLLOW_GEN_in_property2095); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:22: {...}? cp_variable
                    {
                    dbg.location(555,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(555,51);
                    pushFollow(FOLLOW_cp_variable_in_property2101);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(555,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:64: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:64: ws
                    {
                    dbg.location(555,64);
                    pushFollow(FOLLOW_ws_in_property2104);
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
        dbg.location(556, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(558, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(559,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
            int alt99=2;
            try { dbg.enterSubRule(99);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(560,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(560,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(560,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule2148);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:13: ( selectorsGroup )
                    {
                    dbg.location(562,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:15: selectorsGroup
                    {
                    dbg.location(562,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule2181);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(99);}

            dbg.location(565,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule2204); if (state.failed) return ;
            dbg.location(565,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:16: ws
                    {
                    dbg.location(565,16);
                    pushFollow(FOLLOW_ws_in_rule2206);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}

            dbg.location(565,20);
            pushFollow(FOLLOW_syncToFollow_in_rule2209);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(566,13);
            pushFollow(FOLLOW_declarations_in_rule2223);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(567,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule2233); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(568, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(576, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(578,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*
            try { dbg.enterSubRule(106);

            loop106:
            do {
                int alt106=6;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(582,120);
            	    pushFollow(FOLLOW_declaration_in_declarations2367);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(582,132);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2369); if (state.failed) return ;
            	    dbg.location(582,137);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:137: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:137: ws
            	            {
            	            dbg.location(582,137);
            	            pushFollow(FOLLOW_ws_in_declarations2371);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(101);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(584,122);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2415);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(584,145);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:145: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:145: ws
            	            {
            	            dbg.location(584,145);
            	            pushFollow(FOLLOW_ws_in_declarations2417);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(102);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(586,50);
            	    pushFollow(FOLLOW_rule_in_declarations2454);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(586,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:55: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:55: ws
            	            {
            	            dbg.location(586,55);
            	            pushFollow(FOLLOW_ws_in_declarations2456);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(103);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:17: {...}? media ( ws )?
            	    {
            	    dbg.location(588,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(588,46);
            	    pushFollow(FOLLOW_media_in_declarations2495);
            	    media();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(588,52);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:52: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:52: ws
            	            {
            	            dbg.location(588,52);
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
            	case 5 :
            	    dbg.enterAlt(5);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(590,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(590,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2536);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(590,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:60: ws
            	            {
            	            dbg.location(590,60);
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

            	default :
            	    break loop106;
                }
            } while (true);
            } finally {dbg.exitSubRule(106);}

            dbg.location(594,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt107=2;
            try { dbg.enterSubRule(107);
            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

            int LA107_0 = input.LA(1);

            if ( (LA107_0==STAR) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==HASH_SYMBOL) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==IDENT) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==MINUS||(LA107_0>=HASH && LA107_0<=DOT)) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==GEN) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==MEDIA_SYM||LA107_0==AT_IDENT) && (synpred7_Css3())) {
                alt107=1;
            }
            else if ( (LA107_0==SASS_VAR) && (synpred7_Css3())) {
                alt107=1;
            }
            } finally {dbg.exitDecision(107);}

            switch (alt107) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(594,36);
                    pushFollow(FOLLOW_declaration_in_declarations2582);
                    declaration();

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
        dbg.location(595, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(597, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt111=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(600,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2643);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(600,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:99: ws
                            {
                            dbg.location(600,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup2645);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(108);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(602,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup2660);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(602,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(110);

                    loop110:
                    do {
                        int alt110=2;
                        try { dbg.enterDecision(110, decisionCanBacktrack[110]);

                        int LA110_0 = input.LA(1);

                        if ( (LA110_0==COMMA) ) {
                            alt110=1;
                        }


                        } finally {dbg.exitDecision(110);}

                        switch (alt110) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(602,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2663); if (state.failed) return ;
                    	    dbg.location(602,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:25: ws
                    	            {
                    	            dbg.location(602,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup2665);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(109);}

                    	    dbg.location(602,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup2668);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop110;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(110);}


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
        dbg.location(603, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(605, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(606,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2695);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(606,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(112);

            loop112:
            do {
                int alt112=2;
                try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                int LA112_0 = input.LA(1);

                if ( (LA112_0==IDENT||LA112_0==GEN||LA112_0==COLON||(LA112_0>=PLUS && LA112_0<=TILDE)||(LA112_0>=HASH_SYMBOL && LA112_0<=PIPE)||LA112_0==LESS_AND) ) {
                    alt112=1;
                }


                } finally {dbg.exitDecision(112);}

                switch (alt112) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(606,31);
            	    pushFollow(FOLLOW_combinator_in_selector2698);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(606,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2700);
            	    simpleSelectorSequence();

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

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(607, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(610, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt117=2;
            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

            int LA117_0 = input.LA(1);

            if ( (LA117_0==IDENT||LA117_0==GEN||(LA117_0>=STAR && LA117_0<=PIPE)||LA117_0==LESS_AND) ) {
                alt117=1;
            }
            else if ( (LA117_0==COLON||(LA117_0>=HASH_SYMBOL && LA117_0<=DCOLON)) ) {
                alt117=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 117, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(117);}

            switch (alt117) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(613,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(613,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2733);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(613,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(114);

                    loop114:
                    do {
                        int alt114=2;
                        try { dbg.enterDecision(114, decisionCanBacktrack[114]);

                        try {
                            isCyclicDecision = true;
                            alt114 = dfa114.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(114);}

                        switch (alt114) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(613,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2740);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(613,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:46: ws
                    	            {
                    	            dbg.location(613,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2742);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(113);}


                    	    }
                    	    break;

                    	default :
                    	    break loop114;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(114);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(615,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(615,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt116=0;
                    try { dbg.enterSubRule(116);

                    loop116:
                    do {
                        int alt116=2;
                        try { dbg.enterDecision(116, decisionCanBacktrack[116]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA116_2 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt116=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA116_3 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt116=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA116_4 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt116=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA116_5 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt116=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA116_6 = input.LA(2);

                            if ( (synpred10_Css3()) ) {
                                alt116=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(116);}

                        switch (alt116) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(615,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2761);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(615,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:33: ws
                    	            {
                    	            dbg.location(615,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2763);
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
                    	    if ( cnt116 >= 1 ) break loop116;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(116, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt116++;
                    } while (true);
                    } finally {dbg.exitSubRule(116);}


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
        dbg.location(616, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(623, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(624,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH_SYMBOL && input.LA(1)<=DCOLON) ) {
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
        dbg.location(625, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(627, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(629,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt118=2;
            try { dbg.enterSubRule(118);
            try { dbg.enterDecision(118, decisionCanBacktrack[118]);

            int LA118_0 = input.LA(1);

            if ( (LA118_0==IDENT) ) {
                int LA118_1 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt118=1;
                }
            }
            else if ( (LA118_0==STAR) ) {
                int LA118_2 = input.LA(2);

                if ( (synpred11_Css3()) ) {
                    alt118=1;
                }
            }
            else if ( (LA118_0==PIPE) && (synpred11_Css3())) {
                alt118=1;
            }
            } finally {dbg.exitDecision(118);}

            switch (alt118) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(629,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2875);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(629,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:51: elementName ( ws )?
            {
            dbg.location(629,51);
            pushFollow(FOLLOW_elementName_in_typeSelector2881);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(629,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:63: ws
                    {
                    dbg.location(629,63);
                    pushFollow(FOLLOW_ws_in_typeSelector2883);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(630, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(632, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(633,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:5: ( namespacePrefixName | STAR )?
            int alt120=3;
            try { dbg.enterSubRule(120);
            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

            int LA120_0 = input.LA(1);

            if ( (LA120_0==IDENT) ) {
                alt120=1;
            }
            else if ( (LA120_0==STAR) ) {
                alt120=2;
            }
            } finally {dbg.exitDecision(120);}

            switch (alt120) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:7: namespacePrefixName
                    {
                    dbg.location(633,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2901);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:29: STAR
                    {
                    dbg.location(633,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2905); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(120);}

            dbg.location(633,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2909); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(634, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(637, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(639,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt121=4;
            try { dbg.enterSubRule(121);
            try { dbg.enterDecision(121, decisionCanBacktrack[121]);

            switch ( input.LA(1) ) {
            case HASH_SYMBOL:
            case HASH:
                {
                alt121=1;
                }
                break;
            case DOT:
                {
                alt121=2;
                }
                break;
            case LBRACKET:
                {
                alt121=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt121=4;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:6: cssId
                    {
                    dbg.location(640,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2943);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:8: cssClass
                    {
                    dbg.location(641,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2952);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:11: slAttribute
                    {
                    dbg.location(642,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2964);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:11: pseudo
                    {
                    dbg.location(643,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2976);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(121);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(645, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(648, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt122=2;
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            int LA122_0 = input.LA(1);

            if ( (LA122_0==HASH) ) {
                alt122=1;
            }
            else if ( (LA122_0==HASH_SYMBOL) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:7: HASH
                    {
                    dbg.location(649,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId3004); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(649,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:16: HASH_SYMBOL NAME
                    {
                    dbg.location(649,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId3010); if (state.failed) return ;
                    dbg.location(649,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId3012); if (state.failed) return ;

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
        dbg.location(650, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(656, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:7: DOT ( IDENT | GEN )
            {
            dbg.location(657,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass3040); if (state.failed) return ;
            dbg.location(657,11);
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
        dbg.location(658, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(665, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(666,5);
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
        dbg.location(667, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(669, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(670,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute3114); if (state.failed) return ;
            dbg.location(671,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:6: ( namespacePrefix )?
            int alt123=2;
            try { dbg.enterSubRule(123);
            try { dbg.enterDecision(123, decisionCanBacktrack[123]);

            int LA123_0 = input.LA(1);

            if ( (LA123_0==IDENT) ) {
                int LA123_1 = input.LA(2);

                if ( (LA123_1==PIPE) ) {
                    alt123=1;
                }
            }
            else if ( ((LA123_0>=STAR && LA123_0<=PIPE)) ) {
                alt123=1;
            }
            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:6: namespacePrefix
                    {
                    dbg.location(671,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute3121);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}

            dbg.location(671,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:23: ws
                    {
                    dbg.location(671,23);
                    pushFollow(FOLLOW_ws_in_slAttribute3124);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(124);}

            dbg.location(672,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute3135);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(672,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:25: ws
                    {
                    dbg.location(672,25);
                    pushFollow(FOLLOW_ws_in_slAttribute3137);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(674,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt128=2;
            try { dbg.enterSubRule(128);
            try { dbg.enterDecision(128, decisionCanBacktrack[128]);

            int LA128_0 = input.LA(1);

            if ( ((LA128_0>=OPEQ && LA128_0<=CONTAINS)) ) {
                alt128=1;
            }
            } finally {dbg.exitDecision(128);}

            switch (alt128) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(675,17);
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

                    dbg.location(683,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:17: ws
                            {
                            dbg.location(683,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3359);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(126);}

                    dbg.location(684,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute3378);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(685,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:17: ws
                            {
                            dbg.location(685,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3396);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(127);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(128);}

            dbg.location(688,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3425); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(689, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(696, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:697:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:697:4: IDENT
            {
            dbg.location(697,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3441); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(698, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(700, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:2: ( IDENT | STRING )
            {
            dbg.location(702,2);
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
        dbg.location(706, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(708, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(709,7);
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

            dbg.location(710,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt136=2;
            try { dbg.enterSubRule(136);
            try { dbg.enterDecision(136, decisionCanBacktrack[136]);

            int LA136_0 = input.LA(1);

            if ( (LA136_0==IDENT||LA136_0==GEN) ) {
                alt136=1;
            }
            else if ( (LA136_0==NOT) ) {
                alt136=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 136, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(136);}

            switch (alt136) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(711,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(712,21);
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

                    dbg.location(713,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    int alt132=2;
                    try { dbg.enterSubRule(132);
                    try { dbg.enterDecision(132, decisionCanBacktrack[132]);

                    try {
                        isCyclicDecision = true;
                        alt132 = dfa132.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(132);}

                    switch (alt132) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(714,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:25: ws
                                    {
                                    dbg.location(714,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3636);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(129);}

                            dbg.location(714,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3639); if (state.failed) return ;
                            dbg.location(714,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:36: ws
                                    {
                                    dbg.location(714,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3641);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(130);}

                            dbg.location(714,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:40: ( expression | STAR )?
                            int alt131=3;
                            try { dbg.enterSubRule(131);
                            try { dbg.enterDecision(131, decisionCanBacktrack[131]);

                            int LA131_0 = input.LA(1);

                            if ( ((LA131_0>=IDENT && LA131_0<=URI)||LA131_0==MEDIA_SYM||LA131_0==GEN||LA131_0==AT_IDENT||LA131_0==PERCENTAGE||LA131_0==PLUS||LA131_0==MINUS||LA131_0==HASH||(LA131_0>=NUMBER && LA131_0<=DIMENSION)||LA131_0==SASS_VAR) ) {
                                alt131=1;
                            }
                            else if ( (LA131_0==STAR) ) {
                                alt131=2;
                            }
                            } finally {dbg.exitDecision(131);}

                            switch (alt131) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:42: expression
                                    {
                                    dbg.location(714,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3646);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:55: STAR
                                    {
                                    dbg.location(714,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3650); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(131);}

                            dbg.location(714,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3655); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(132);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(718,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(718,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3734); if (state.failed) return ;
                    dbg.location(718,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:23: ws
                            {
                            dbg.location(718,23);
                            pushFollow(FOLLOW_ws_in_pseudo3736);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(133);}

                    dbg.location(718,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3739); if (state.failed) return ;
                    dbg.location(718,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:34: ws
                            {
                            dbg.location(718,34);
                            pushFollow(FOLLOW_ws_in_pseudo3741);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(134);}

                    dbg.location(718,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:38: ( simpleSelectorSequence )?
                    int alt135=2;
                    try { dbg.enterSubRule(135);
                    try { dbg.enterDecision(135, decisionCanBacktrack[135]);

                    int LA135_0 = input.LA(1);

                    if ( (LA135_0==IDENT||LA135_0==GEN||LA135_0==COLON||(LA135_0>=HASH_SYMBOL && LA135_0<=PIPE)||LA135_0==LESS_AND) ) {
                        alt135=1;
                    }
                    } finally {dbg.exitDecision(135);}

                    switch (alt135) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:38: simpleSelectorSequence
                            {
                            dbg.location(718,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3744);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(135);}

                    dbg.location(718,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3747); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(720, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:1: declaration : ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(722, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:5: ( ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:5: ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(725,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:5: ( STAR )?
            int alt137=2;
            try { dbg.enterSubRule(137);
            try { dbg.enterDecision(137, decisionCanBacktrack[137]);

            int LA137_0 = input.LA(1);

            if ( (LA137_0==STAR) ) {
                alt137=1;
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:5: STAR
                    {
                    dbg.location(725,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3791); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(726,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:5: ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property )
            int alt138=2;
            try { dbg.enterSubRule(138);
            try { dbg.enterDecision(138, decisionCanBacktrack[138]);

            int LA138_0 = input.LA(1);

            if ( (LA138_0==HASH_SYMBOL) && (synpred12_Css3())) {
                alt138=1;
            }
            else if ( (LA138_0==IDENT) ) {
                int LA138_2 = input.LA(2);

                if ( (synpred12_Css3()) ) {
                    alt138=1;
                }
                else if ( (true) ) {
                    alt138=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 138, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA138_0==MINUS||(LA138_0>=HASH && LA138_0<=DOT)) && (synpred12_Css3())) {
                alt138=1;
            }
            else if ( (LA138_0==MEDIA_SYM||LA138_0==GEN||LA138_0==AT_IDENT||LA138_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(727,74);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_declaration3837);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:9: property
                    {
                    dbg.location(729,9);
                    pushFollow(FOLLOW_property_in_declaration3858);
                    property();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(138);}

            dbg.location(731,5);
            match(input,COLON,FOLLOW_COLON_in_declaration3871); if (state.failed) return ;
            dbg.location(731,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:11: ws
                    {
                    dbg.location(731,11);
                    pushFollow(FOLLOW_ws_in_declaration3873);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(139);}

            dbg.location(731,15);
            pushFollow(FOLLOW_propertyValue_in_declaration3876);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(731,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:29: ( prio ( ws )? )?
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            int LA141_0 = input.LA(1);

            if ( (LA141_0==IMPORTANT_SYM) ) {
                alt141=1;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:30: prio ( ws )?
                    {
                    dbg.location(731,30);
                    pushFollow(FOLLOW_prio_in_declaration3879);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(731,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:35: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:35: ws
                            {
                            dbg.location(731,35);
                            pushFollow(FOLLOW_ws_in_declaration3881);
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
        dbg.location(732, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(740, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
            int alt142=2;
            try { dbg.enterDecision(142, decisionCanBacktrack[142]);

            try {
                isCyclicDecision = true;
                alt142 = dfa142.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(142);}

            switch (alt142) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(742,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(742,34);
                    pushFollow(FOLLOW_expression_in_propertyValue3921);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:9: ({...}? cp_expression )
                    {
                    dbg.location(752,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:11: {...}? cp_expression
                    {
                    dbg.location(752,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(752,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue3964);
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
        dbg.location(753, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(756, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(759,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt143=0;
            try { dbg.enterSubRule(143);

            loop143:
            do {
                int alt143=2;
                try { dbg.enterDecision(143, decisionCanBacktrack[143]);

                int LA143_0 = input.LA(1);

                if ( (LA143_0==NAMESPACE_SYM||(LA143_0>=IDENT && LA143_0<=MEDIA_SYM)||(LA143_0>=AND && LA143_0<=RPAREN)||(LA143_0>=WS && LA143_0<=RIGHTBOTTOM_SYM)||(LA143_0>=PLUS && LA143_0<=DCOLON)||(LA143_0>=PIPE && LA143_0<=LINE_COMMENT)) ) {
                    alt143=1;
                }


                } finally {dbg.exitDecision(143);}

                switch (alt143) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(759,7);
            	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=AND && input.LA(1)<=RPAREN)||(input.LA(1)>=WS && input.LA(1)<=RIGHTBOTTOM_SYM)||(input.LA(1)>=PLUS && input.LA(1)<=DCOLON)||(input.LA(1)>=PIPE && input.LA(1)<=LINE_COMMENT) ) {
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
            	    if ( cnt143 >= 1 ) break loop143;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(143, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt143++;
            } while (true);
            } finally {dbg.exitSubRule(143);}

            dbg.location(759,65);
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
        dbg.location(760, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(764, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:6: 
            {
            }

        }
        finally {
        }
        dbg.location(770, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(772, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:6: 
            {
            }

        }
        finally {
        }
        dbg.location(777, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(779, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:13: SEMI
            {
            dbg.location(784,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI4149); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(785, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(788, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:793:6: 
            {
            }

        }
        finally {
        }
        dbg.location(793, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(795, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:7: IMPORTANT_SYM
            {
            dbg.location(796,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio4204); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(797, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(799, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(800,7);
            pushFollow(FOLLOW_term_in_expression4225);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(800,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(146);

            loop146:
            do {
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(800,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:14: ( operator ( ws )? )?
            	    int alt145=2;
            	    try { dbg.enterSubRule(145);
            	    try { dbg.enterDecision(145, decisionCanBacktrack[145]);

            	    int LA145_0 = input.LA(1);

            	    if ( (LA145_0==COMMA||LA145_0==SOLIDUS) ) {
            	        alt145=1;
            	    }
            	    } finally {dbg.exitDecision(145);}

            	    switch (alt145) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:15: operator ( ws )?
            	            {
            	            dbg.location(800,15);
            	            pushFollow(FOLLOW_operator_in_expression4230);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(800,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:24: ws
            	                    {
            	                    dbg.location(800,24);
            	                    pushFollow(FOLLOW_ws_in_expression4232);
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

            	    dbg.location(800,30);
            	    pushFollow(FOLLOW_term_in_expression4237);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop146;
                }
            } while (true);
            } finally {dbg.exitSubRule(146);}


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
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | ( function )=> function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(803, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | ( function )=> function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | ( function )=> function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(804,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:7: ( unaryOperator ( ws )? )?
            int alt148=2;
            try { dbg.enterSubRule(148);
            try { dbg.enterDecision(148, decisionCanBacktrack[148]);

            int LA148_0 = input.LA(1);

            if ( (LA148_0==PLUS||LA148_0==MINUS) ) {
                alt148=1;
            }
            } finally {dbg.exitDecision(148);}

            switch (alt148) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:9: unaryOperator ( ws )?
                    {
                    dbg.location(804,9);
                    pushFollow(FOLLOW_unaryOperator_in_term4262);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(804,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:23: ws
                            {
                            dbg.location(804,23);
                            pushFollow(FOLLOW_ws_in_term4264);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(147);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(148);}

            dbg.location(805,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:805:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | ( function )=> function | {...}? cp_variable )
            int alt149=8;
            try { dbg.enterSubRule(149);
            try { dbg.enterDecision(149, decisionCanBacktrack[149]);

            try {
                isCyclicDecision = true;
                alt149 = dfa149.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(149);}

            switch (alt149) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(806,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:7: STRING
                    {
                    dbg.location(819,7);
                    match(input,STRING,FOLLOW_STRING_in_term4488); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:820:7: IDENT
                    {
                    dbg.location(820,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term4496); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:7: GEN
                    {
                    dbg.location(821,7);
                    match(input,GEN,FOLLOW_GEN_in_term4504); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:7: URI
                    {
                    dbg.location(822,7);
                    match(input,URI,FOLLOW_URI_in_term4512); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:7: hexColor
                    {
                    dbg.location(823,7);
                    pushFollow(FOLLOW_hexColor_in_term4520);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:7: ( function )=> function
                    {
                    dbg.location(824,19);
                    pushFollow(FOLLOW_function_in_term4532);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:7: {...}? cp_variable
                    {
                    dbg.location(825,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(825,36);
                    pushFollow(FOLLOW_cp_variable_in_term4542);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(149);}

            dbg.location(827,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:5: ws
                    {
                    dbg.location(827,5);
                    pushFollow(FOLLOW_ws_in_term4554);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(830, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(831,5);
            pushFollow(FOLLOW_functionName_in_function4570);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(831,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:18: ws
                    {
                    dbg.location(831,18);
                    pushFollow(FOLLOW_ws_in_function4572);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(832,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4577); if (state.failed) return ;
            dbg.location(832,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:10: ws
                    {
                    dbg.location(832,10);
                    pushFollow(FOLLOW_ws_in_function4579);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(152);}

            dbg.location(833,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt155=2;
            try { dbg.enterSubRule(155);
            try { dbg.enterDecision(155, decisionCanBacktrack[155]);

            try {
                isCyclicDecision = true;
                alt155 = dfa155.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(155);}

            switch (alt155) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:4: expression
                    {
                    dbg.location(834,4);
                    pushFollow(FOLLOW_expression_in_function4589);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(836,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(837,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4607);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(837,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(154);

                    loop154:
                    do {
                        int alt154=2;
                        try { dbg.enterDecision(154, decisionCanBacktrack[154]);

                        int LA154_0 = input.LA(1);

                        if ( (LA154_0==COMMA) ) {
                            alt154=1;
                        }


                        } finally {dbg.exitDecision(154);}

                        switch (alt154) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(837,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4610); if (state.failed) return ;
                    	    dbg.location(837,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:24: ws
                    	            {
                    	            dbg.location(837,24);
                    	            pushFollow(FOLLOW_ws_in_function4612);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(153);}

                    	    dbg.location(837,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4615);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop154;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(154);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(155);}

            dbg.location(840,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4636); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(841, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(847, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(851,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:4: ( IDENT COLON )?
            int alt156=2;
            try { dbg.enterSubRule(156);
            try { dbg.enterDecision(156, decisionCanBacktrack[156]);

            int LA156_0 = input.LA(1);

            if ( (LA156_0==IDENT) ) {
                int LA156_1 = input.LA(2);

                if ( (LA156_1==COLON) ) {
                    alt156=1;
                }
            }
            } finally {dbg.exitDecision(156);}

            switch (alt156) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:5: IDENT COLON
                    {
                    dbg.location(851,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4684); if (state.failed) return ;
                    dbg.location(851,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4686); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(156);}

            dbg.location(851,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4690); if (state.failed) return ;
            dbg.location(851,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:25: ( DOT IDENT )*
            try { dbg.enterSubRule(157);

            loop157:
            do {
                int alt157=2;
                try { dbg.enterDecision(157, decisionCanBacktrack[157]);

                int LA157_0 = input.LA(1);

                if ( (LA157_0==DOT) ) {
                    alt157=1;
                }


                } finally {dbg.exitDecision(157);}

                switch (alt157) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:26: DOT IDENT
            	    {
            	    dbg.location(851,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4693); if (state.failed) return ;
            	    dbg.location(851,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4695); if (state.failed) return ;

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
        dbg.location(853, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(855, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(856,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4718);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(856,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:20: ws
                    {
                    dbg.location(856,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4720);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(158);}

            dbg.location(856,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4723); if (state.failed) return ;
            dbg.location(856,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:29: ws
                    {
                    dbg.location(856,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4725);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(159);}

            dbg.location(856,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4728);
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
        dbg.location(857, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(859, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:4: IDENT ( DOT IDENT )*
            {
            dbg.location(860,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4743); if (state.failed) return ;
            dbg.location(860,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:10: ( DOT IDENT )*
            try { dbg.enterSubRule(160);

            loop160:
            do {
                int alt160=2;
                try { dbg.enterDecision(160, decisionCanBacktrack[160]);

                int LA160_0 = input.LA(1);

                if ( (LA160_0==DOT) ) {
                    alt160=1;
                }


                } finally {dbg.exitDecision(160);}

                switch (alt160) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:11: DOT IDENT
            	    {
            	    dbg.location(860,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4746); if (state.failed) return ;
            	    dbg.location(860,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4748); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop160;
                }
            } while (true);
            } finally {dbg.exitSubRule(160);}


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
            dbg.exitRule(getGrammarFileName(), "fnAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttributeName"


    // $ANTLR start "fnAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(863, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:4: expression
            {
            dbg.location(864,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4762);
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
        dbg.location(865, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(867, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:7: HASH
            {
            dbg.location(868,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4780); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(869, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(871, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:7: ( WS | NL | COMMENT )+
            {
            dbg.location(872,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:7: ( WS | NL | COMMENT )+
            int cnt161=0;
            try { dbg.enterSubRule(161);

            loop161:
            do {
                int alt161=2;
                try { dbg.enterDecision(161, decisionCanBacktrack[161]);

                int LA161_0 = input.LA(1);

                if ( (LA161_0==WS||(LA161_0>=NL && LA161_0<=COMMENT)) ) {
                    alt161=1;
                }


                } finally {dbg.exitDecision(161);}

                switch (alt161) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(872,7);
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
            	    if ( cnt161 >= 1 ) break loop161;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(161, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt161++;
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
        dbg.location(873, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(878, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt168=2;
            try { dbg.enterDecision(168, decisionCanBacktrack[168]);

            int LA168_0 = input.LA(1);

            if ( (LA168_0==MEDIA_SYM||LA168_0==AT_IDENT) ) {
                int LA168_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt168=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt168=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 168, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA168_0==SASS_VAR) ) {
                int LA168_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt168=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt168=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 168, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 168, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(168);}

            switch (alt168) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
                    {
                    dbg.location(880,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(880,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4849);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(880,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:39: ws
                            {
                            dbg.location(880,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4851);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(162);}

                    dbg.location(880,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4854); if (state.failed) return ;
                    dbg.location(880,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:49: ws
                            {
                            dbg.location(880,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4856);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(163);}

                    dbg.location(880,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4859);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(880,67);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4861); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(882,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(882,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4888);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(882,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:39: ws
                            {
                            dbg.location(882,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4890);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(164);}

                    dbg.location(882,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4893); if (state.failed) return ;
                    dbg.location(882,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:49: ws
                            {
                            dbg.location(882,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4895);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(165);}

                    dbg.location(882,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4898);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(882,67);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:67: ( SASS_DEFAULT ( ws )? )?
                    int alt167=2;
                    try { dbg.enterSubRule(167);
                    try { dbg.enterDecision(167, decisionCanBacktrack[167]);

                    int LA167_0 = input.LA(1);

                    if ( (LA167_0==SASS_DEFAULT) ) {
                        alt167=1;
                    }
                    } finally {dbg.exitDecision(167);}

                    switch (alt167) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:68: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(882,68);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4901); if (state.failed) return ;
                            dbg.location(882,81);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:81: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:81: ws
                                    {
                                    dbg.location(882,81);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4903);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(166);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(167);}

                    dbg.location(882,87);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4908); if (state.failed) return ;

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
        dbg.location(883, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(886, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt169=2;
            try { dbg.enterDecision(169, decisionCanBacktrack[169]);

            int LA169_0 = input.LA(1);

            if ( (LA169_0==MEDIA_SYM||LA169_0==AT_IDENT) ) {
                alt169=1;
            }
            else if ( (LA169_0==SASS_VAR) ) {
                alt169=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 169, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(169);}

            switch (alt169) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(888,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(888,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(890,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(890,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:29: SASS_VAR
                    {
                    dbg.location(890,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable4973); if (state.failed) return ;

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
        dbg.location(892, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(895, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:10: cp_additionExp
            {
            dbg.location(896,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression4997);
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
        dbg.location(897, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(899, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(900,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5017);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(901,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(172);

            loop172:
            do {
                int alt172=3;
                try { dbg.enterDecision(172, decisionCanBacktrack[172]);

                int LA172_0 = input.LA(1);

                if ( (LA172_0==PLUS) ) {
                    alt172=1;
                }
                else if ( (LA172_0==MINUS) ) {
                    alt172=2;
                }


                } finally {dbg.exitDecision(172);}

                switch (alt172) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(901,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp5031); if (state.failed) return ;
            	    dbg.location(901,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:17: ws
            	            {
            	            dbg.location(901,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5033);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(170);}

            	    dbg.location(901,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5036);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(902,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp5049); if (state.failed) return ;
            	    dbg.location(902,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:18: ws
            	            {
            	            dbg.location(902,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp5051);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(171);}

            	    dbg.location(902,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp5054);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(904, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(906, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(907,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5087);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(908,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(175);

            loop175:
            do {
                int alt175=3;
                try { dbg.enterDecision(175, decisionCanBacktrack[175]);

                int LA175_0 = input.LA(1);

                if ( (LA175_0==STAR) ) {
                    alt175=1;
                }
                else if ( (LA175_0==SOLIDUS) ) {
                    alt175=2;
                }


                } finally {dbg.exitDecision(175);}

                switch (alt175) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(908,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp5100); if (state.failed) return ;
            	    dbg.location(908,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:17: ws
            	            {
            	            dbg.location(908,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5102);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(173);}

            	    dbg.location(908,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5105);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(909,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp5119); if (state.failed) return ;
            	    dbg.location(909,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:20: ws
            	            {
            	            dbg.location(909,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp5121);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(174);}

            	    dbg.location(909,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp5124);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop175;
                }
            } while (true);
            } finally {dbg.exitSubRule(175);}


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
            dbg.exitRule(getGrammarFileName(), "cp_multiplyExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_multiplyExp"


    // $ANTLR start "cp_atomExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(913, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt179=2;
            try { dbg.enterDecision(179, decisionCanBacktrack[179]);

            int LA179_0 = input.LA(1);

            if ( ((LA179_0>=IDENT && LA179_0<=URI)||LA179_0==MEDIA_SYM||LA179_0==GEN||LA179_0==AT_IDENT||LA179_0==PERCENTAGE||LA179_0==PLUS||LA179_0==MINUS||LA179_0==HASH||(LA179_0>=NUMBER && LA179_0<=DIMENSION)||LA179_0==SASS_VAR) ) {
                alt179=1;
            }
            else if ( (LA179_0==LPAREN) ) {
                alt179=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:10: term ( ( term )=> term )*
                    {
                    dbg.location(914,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp5157);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(914,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(176);

                    loop176:
                    do {
                        int alt176=2;
                        try { dbg.enterDecision(176, decisionCanBacktrack[176]);

                        try {
                            isCyclicDecision = true;
                            alt176 = dfa176.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(176);}

                        switch (alt176) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:16: ( term )=> term
                    	    {
                    	    dbg.location(914,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp5164);
                    	    term();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(915,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp5178); if (state.failed) return ;
                    dbg.location(915,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:17: ws
                            {
                            dbg.location(915,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5180);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(177);}

                    dbg.location(915,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp5183);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(915,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp5185); if (state.failed) return ;
                    dbg.location(915,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:43: ws
                            {
                            dbg.location(915,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp5187);
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
            dbg.exitRule(getGrammarFileName(), "cp_atomExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_atomExp"


    // $ANTLR start "cp_term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(919, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(921,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt180=8;
            try { dbg.enterSubRule(180);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(922,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:935:7: STRING
                    {
                    dbg.location(935,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term5425); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:7: IDENT
                    {
                    dbg.location(936,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term5433); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:7: GEN
                    {
                    dbg.location(937,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term5441); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:7: URI
                    {
                    dbg.location(938,7);
                    match(input,URI,FOLLOW_URI_in_cp_term5449); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:7: hexColor
                    {
                    dbg.location(939,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term5457);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:7: function
                    {
                    dbg.location(940,7);
                    pushFollow(FOLLOW_function_in_cp_term5465);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:7: cp_variable
                    {
                    dbg.location(941,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term5473);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(180);}

            dbg.location(943,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:5: ws
                    {
                    dbg.location(943,5);
                    pushFollow(FOLLOW_ws_in_cp_term5485);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(181);}


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
            dbg.exitRule(getGrammarFileName(), "cp_term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_term"


    // $ANTLR start "cp_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(953, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? )
            int alt191=2;
            try { dbg.enterDecision(191, decisionCanBacktrack[191]);

            int LA191_0 = input.LA(1);

            if ( (LA191_0==DOT) ) {
                alt191=1;
            }
            else if ( (LA191_0==SASS_MIXIN) ) {
                alt191=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 191, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(191);}

            switch (alt191) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(955,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(955,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration5516); if (state.failed) return ;
                    dbg.location(955,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5518);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(955,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:41: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:41: ws
                            {
                            dbg.location(955,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5520);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(182);}

                    dbg.location(955,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5523); if (state.failed) return ;
                    dbg.location(955,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:52: ( less_args_list )?
                    int alt183=2;
                    try { dbg.enterSubRule(183);
                    try { dbg.enterDecision(183, decisionCanBacktrack[183]);

                    int LA183_0 = input.LA(1);

                    if ( (LA183_0==MEDIA_SYM||LA183_0==AT_IDENT||LA183_0==SASS_VAR||(LA183_0>=LESS_DOTS && LA183_0<=LESS_REST)) ) {
                        alt183=1;
                    }
                    } finally {dbg.exitDecision(183);}

                    switch (alt183) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:52: less_args_list
                            {
                            dbg.location(955,52);
                            pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5525);
                            less_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(183);}

                    dbg.location(955,68);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5528); if (state.failed) return ;
                    dbg.location(955,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:75: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:75: ws
                            {
                            dbg.location(955,75);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5530);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(184);}

                    dbg.location(955,79);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:79: ( less_mixin_guarded ( ws )? )?
                    int alt186=2;
                    try { dbg.enterSubRule(186);
                    try { dbg.enterDecision(186, decisionCanBacktrack[186]);

                    int LA186_0 = input.LA(1);

                    if ( (LA186_0==LESS_WHEN) ) {
                        alt186=1;
                    }
                    } finally {dbg.exitDecision(186);}

                    switch (alt186) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:80: less_mixin_guarded ( ws )?
                            {
                            dbg.location(955,80);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5534);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(955,99);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:99: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:99: ws
                                    {
                                    dbg.location(955,99);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5536);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(185);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(186);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(957,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(957,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5553); if (state.failed) return ;
                    dbg.location(957,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5555);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(957,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5557);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(957,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:51: ws
                            {
                            dbg.location(957,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5559);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(187);}

                    dbg.location(957,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:55: ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    int alt190=2;
                    try { dbg.enterSubRule(190);
                    try { dbg.enterDecision(190, decisionCanBacktrack[190]);

                    int LA190_0 = input.LA(1);

                    if ( (LA190_0==LPAREN) ) {
                        alt190=1;
                    }
                    } finally {dbg.exitDecision(190);}

                    switch (alt190) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:56: LPAREN ( less_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(957,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5563); if (state.failed) return ;
                            dbg.location(957,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:63: ( less_args_list )?
                            int alt188=2;
                            try { dbg.enterSubRule(188);
                            try { dbg.enterDecision(188, decisionCanBacktrack[188]);

                            int LA188_0 = input.LA(1);

                            if ( (LA188_0==MEDIA_SYM||LA188_0==AT_IDENT||LA188_0==SASS_VAR||(LA188_0>=LESS_DOTS && LA188_0<=LESS_REST)) ) {
                                alt188=1;
                            }
                            } finally {dbg.exitDecision(188);}

                            switch (alt188) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:63: less_args_list
                                    {
                                    dbg.location(957,63);
                                    pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5565);
                                    less_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(188);}

                            dbg.location(957,79);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5568); if (state.failed) return ;
                            dbg.location(957,86);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:86: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:957:86: ws
                                    {
                                    dbg.location(957,86);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5570);
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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_declaration"


    // $ANTLR start "cp_mixin_call"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(962, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:964:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(964,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:964:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt192=2;
            try { dbg.enterSubRule(192);
            try { dbg.enterDecision(192, decisionCanBacktrack[192]);

            int LA192_0 = input.LA(1);

            if ( (LA192_0==DOT) ) {
                alt192=1;
            }
            else if ( (LA192_0==SASS_INCLUDE) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(965,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(965,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call5612); if (state.failed) return ;
                    dbg.location(965,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5614);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:967:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(967,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(967,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call5636); if (state.failed) return ;
                    dbg.location(967,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5638);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(967,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5640);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(192);}

            dbg.location(969,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?
            int alt195=2;
            try { dbg.enterSubRule(195);
            try { dbg.enterDecision(195, decisionCanBacktrack[195]);

            try {
                isCyclicDecision = true;
                alt195 = dfa195.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(195);}

            switch (alt195) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:6: ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(969,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:6: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:6: ws
                            {
                            dbg.location(969,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call5653);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(193);}

                    dbg.location(969,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call5656); if (state.failed) return ;
                    dbg.location(969,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:17: ( cp_mixin_call_args )?
                    int alt194=2;
                    try { dbg.enterSubRule(194);
                    try { dbg.enterDecision(194, decisionCanBacktrack[194]);

                    int LA194_0 = input.LA(1);

                    if ( ((LA194_0>=IDENT && LA194_0<=URI)||LA194_0==MEDIA_SYM||LA194_0==GEN||LA194_0==AT_IDENT||LA194_0==PERCENTAGE||LA194_0==PLUS||LA194_0==MINUS||LA194_0==HASH||(LA194_0>=NUMBER && LA194_0<=DIMENSION)||LA194_0==SASS_VAR) ) {
                        alt194=1;
                    }
                    } finally {dbg.exitDecision(194);}

                    switch (alt194) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:17: cp_mixin_call_args
                            {
                            dbg.location(969,17);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call5658);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(194);}

                    dbg.location(969,37);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call5661); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(195);}

            dbg.location(969,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:46: ws
                    {
                    dbg.location(969,46);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5665);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(196);}

            dbg.location(969,50);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call5668); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(970, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(972, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:5: IDENT
            {
            dbg.location(974,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name5697); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(975, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:1: cp_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(977, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(981,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_args5733);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(981,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(198);

            loop198:
            do {
                int alt198=2;
                try { dbg.enterDecision(198, decisionCanBacktrack[198]);

                int LA198_0 = input.LA(1);

                if ( (LA198_0==SEMI||LA198_0==COMMA) ) {
                    alt198=1;
                }


                } finally {dbg.exitDecision(198);}

                switch (alt198) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(981,12);
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

            	    dbg.location(981,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:27: ws
            	            {
            	            dbg.location(981,27);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args5745);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(197);}

            	    dbg.location(981,31);
            	    pushFollow(FOLLOW_term_in_cp_mixin_call_args5748);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop198;
                }
            } while (true);
            } finally {dbg.exitSubRule(198);}


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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_args"


    // $ANTLR start "less_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(985, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt203=2;
            try { dbg.enterDecision(203, decisionCanBacktrack[203]);

            int LA203_0 = input.LA(1);

            if ( (LA203_0==MEDIA_SYM||LA203_0==AT_IDENT||LA203_0==SASS_VAR) ) {
                alt203=1;
            }
            else if ( ((LA203_0>=LESS_DOTS && LA203_0<=LESS_REST)) ) {
                alt203=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 203, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(203);}

            switch (alt203) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(989,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(989,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5790);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(989,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
                    try { dbg.enterSubRule(200);

                    loop200:
                    do {
                        int alt200=2;
                        try { dbg.enterDecision(200, decisionCanBacktrack[200]);

                        try {
                            isCyclicDecision = true;
                            alt200 = dfa200.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(200);}

                        switch (alt200) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(989,18);
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

                    	    dbg.location(989,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:35: ws
                    	            {
                    	            dbg.location(989,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5804);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(199);}

                    	    dbg.location(989,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5807);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop200;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(200);}

                    dbg.location(989,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:50: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt202=2;
                    try { dbg.enterSubRule(202);
                    try { dbg.enterDecision(202, decisionCanBacktrack[202]);

                    int LA202_0 = input.LA(1);

                    if ( (LA202_0==SEMI||LA202_0==COMMA) ) {
                        alt202=1;
                    }
                    } finally {dbg.exitDecision(202);}

                    switch (alt202) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:52: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(989,52);
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

                            dbg.location(989,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:69: ws
                                    {
                                    dbg.location(989,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5823);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(201);}

                            dbg.location(989,73);
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
                    } finally {dbg.exitSubRule(202);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(991,5);
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
        dbg.location(992, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(995, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:996:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(997,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg5880);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(997,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:17: ( COLON ( ws )? cp_expression )?
            int alt205=2;
            try { dbg.enterSubRule(205);
            try { dbg.enterDecision(205, decisionCanBacktrack[205]);

            int LA205_0 = input.LA(1);

            if ( (LA205_0==COLON) ) {
                alt205=1;
            }
            } finally {dbg.exitDecision(205);}

            switch (alt205) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(997,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5884); if (state.failed) return ;
                    dbg.location(997,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:25: ( ws )?
                    int alt204=2;
                    try { dbg.enterSubRule(204);
                    try { dbg.enterDecision(204, decisionCanBacktrack[204]);

                    int LA204_0 = input.LA(1);

                    if ( (LA204_0==WS||(LA204_0>=NL && LA204_0<=COMMENT)) ) {
                        alt204=1;
                    }
                    } finally {dbg.exitDecision(204);}

                    switch (alt204) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:25: ws
                            {
                            dbg.location(997,25);
                            pushFollow(FOLLOW_ws_in_less_arg5886);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(204);}

                    dbg.location(997,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg5889);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(205);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(998, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1002, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1003:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(1004,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded5915); if (state.failed) return ;
            dbg.location(1004,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:15: ws
                    {
                    dbg.location(1004,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded5917);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(206);}

            dbg.location(1004,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5920);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1004,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(208);

            loop208:
            do {
                int alt208=2;
                try { dbg.enterDecision(208, decisionCanBacktrack[208]);

                int LA208_0 = input.LA(1);

                if ( (LA208_0==COMMA||LA208_0==AND) ) {
                    alt208=1;
                }


                } finally {dbg.exitDecision(208);}

                switch (alt208) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(1004,36);
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

            	    dbg.location(1004,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:50: ws
            	            {
            	            dbg.location(1004,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded5932);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(207);}

            	    dbg.location(1004,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5935);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop208;
                }
            } while (true);
            } finally {dbg.exitSubRule(208);}


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
            dbg.exitRule(getGrammarFileName(), "less_mixin_guarded");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_guarded"


    // $ANTLR start "less_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1009:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1009, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(1011,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( NOT ( ws )? )?
            int alt210=2;
            try { dbg.enterSubRule(210);
            try { dbg.enterDecision(210, decisionCanBacktrack[210]);

            int LA210_0 = input.LA(1);

            if ( (LA210_0==NOT) ) {
                alt210=1;
            }
            } finally {dbg.exitDecision(210);}

            switch (alt210) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:6: NOT ( ws )?
                    {
                    dbg.location(1011,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition5965); if (state.failed) return ;
                    dbg.location(1011,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:10: ws
                            {
                            dbg.location(1011,10);
                            pushFollow(FOLLOW_ws_in_less_condition5967);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(209);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(210);}

            dbg.location(1012,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition5976); if (state.failed) return ;
            dbg.location(1012,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:12: ws
                    {
                    dbg.location(1012,12);
                    pushFollow(FOLLOW_ws_in_less_condition5978);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(211);}

            dbg.location(1013,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1013:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt216=2;
            try { dbg.enterSubRule(216);
            try { dbg.enterDecision(216, decisionCanBacktrack[216]);

            int LA216_0 = input.LA(1);

            if ( (LA216_0==IDENT) ) {
                alt216=1;
            }
            else if ( (LA216_0==MEDIA_SYM||LA216_0==AT_IDENT||LA216_0==SASS_VAR) ) {
                alt216=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1014,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition6004);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1014,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:40: ( ws )?
                    int alt212=2;
                    try { dbg.enterSubRule(212);
                    try { dbg.enterDecision(212, decisionCanBacktrack[212]);

                    int LA212_0 = input.LA(1);

                    if ( (LA212_0==WS||(LA212_0>=NL && LA212_0<=COMMENT)) ) {
                        alt212=1;
                    }
                    } finally {dbg.exitDecision(212);}

                    switch (alt212) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:40: ws
                            {
                            dbg.location(1014,40);
                            pushFollow(FOLLOW_ws_in_less_condition6006);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(212);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1016,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1016,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition6037);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1016,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt215=2;
                    try { dbg.enterSubRule(215);
                    try { dbg.enterDecision(215, decisionCanBacktrack[215]);

                    int LA215_0 = input.LA(1);

                    if ( (LA215_0==WS||LA215_0==GREATER||LA215_0==OPEQ||(LA215_0>=NL && LA215_0<=COMMENT)||(LA215_0>=GREATER_OR_EQ && LA215_0<=LESS_OR_EQ)) ) {
                        alt215=1;
                    }
                    } finally {dbg.exitDecision(215);}

                    switch (alt215) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1016,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:28: ws
                                    {
                                    dbg.location(1016,28);
                                    pushFollow(FOLLOW_ws_in_less_condition6040);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(213);}

                            dbg.location(1016,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition6043);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1016,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:56: ws
                                    {
                                    dbg.location(1016,56);
                                    pushFollow(FOLLOW_ws_in_less_condition6045);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(214);}

                            dbg.location(1016,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition6048);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(215);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(216);}

            dbg.location(1018,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition6077); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1019, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1022, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1023:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1024,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition6103);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1024,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:18: ws
                    {
                    dbg.location(1024,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6105);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(217);}

            dbg.location(1024,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition6108); if (state.failed) return ;
            dbg.location(1024,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:29: ws
                    {
                    dbg.location(1024,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6110);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(218);}

            dbg.location(1024,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition6113);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1024,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1024:45: ws
                    {
                    dbg.location(1024,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition6115);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(219);}

            dbg.location(1024,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition6118); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1025, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1028:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1028, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1029:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1030:5: IDENT
            {
            dbg.location(1030,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name6140); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1031, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1033:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1033, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1034,5);
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
        dbg.location(1036, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1054, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            {
            dbg.location(1056,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            int alt220=2;
            try { dbg.enterSubRule(220);
            try { dbg.enterDecision(220, decisionCanBacktrack[220]);

            try {
                isCyclicDecision = true;
                alt220 = dfa220.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(220);}

            switch (alt220) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1057,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6239);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
                    {
                    dbg.location(1059,13);
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
            } finally {dbg.exitSubRule(220);}

            dbg.location(1061,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            try { dbg.enterSubRule(223);

            loop223:
            do {
                int alt223=2;
                try { dbg.enterDecision(223, decisionCanBacktrack[223]);

                try {
                    isCyclicDecision = true;
                    alt223 = dfa223.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(223);}

                switch (alt223) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    {
            	    dbg.location(1062,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:13: ws
            	            {
            	            dbg.location(1062,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression6324);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(221);}

            	    dbg.location(1063,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1064,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6363);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1066:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
            	            {
            	            dbg.location(1066,17);
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


            	    }
            	    break;

            	default :
            	    break loop223;
                }
            } while (true);
            } finally {dbg.exitSubRule(223);}


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
            dbg.exitRule(getGrammarFileName(), "scss_selector_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_selector_interpolation_expression"


    // $ANTLR start "scss_declaration_interpolation_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1072, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1074:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1074,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1074:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt224=2;
            try { dbg.enterSubRule(224);
            try { dbg.enterDecision(224, decisionCanBacktrack[224]);

            int LA224_0 = input.LA(1);

            if ( (LA224_0==HASH_SYMBOL) ) {
                int LA224_1 = input.LA(2);

                if ( (LA224_1==LBRACE) && (synpred18_Css3())) {
                    alt224=1;
                }
                else if ( (LA224_1==IDENT||LA224_1==COLON||LA224_1==WS||(LA224_1>=MINUS && LA224_1<=DOT)||(LA224_1>=NL && LA224_1<=COMMENT)) ) {
                    alt224=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 224, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA224_0==IDENT||LA224_0==MINUS||(LA224_0>=HASH && LA224_0<=DOT)) ) {
                alt224=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 224, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(224);}

            switch (alt224) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1075:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1075,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6497);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1077:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1077,13);
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
            } finally {dbg.exitSubRule(224);}

            dbg.location(1079,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1079:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(227);

            loop227:
            do {
                int alt227=2;
                try { dbg.enterDecision(227, decisionCanBacktrack[227]);

                int LA227_0 = input.LA(1);

                if ( (LA227_0==IDENT||LA227_0==WS||(LA227_0>=MINUS && LA227_0<=DOT)||(LA227_0>=NL && LA227_0<=COMMENT)) ) {
                    alt227=1;
                }


                } finally {dbg.exitDecision(227);}

                switch (alt227) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1080,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1080:13: ws
            	            {
            	            dbg.location(1080,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression6578);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(225);}

            	    dbg.location(1081,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1081:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt226=2;
            	    try { dbg.enterSubRule(226);
            	    try { dbg.enterDecision(226, decisionCanBacktrack[226]);

            	    int LA226_0 = input.LA(1);

            	    if ( (LA226_0==HASH_SYMBOL) ) {
            	        int LA226_1 = input.LA(2);

            	        if ( (LA226_1==LBRACE) && (synpred19_Css3())) {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1082:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1082,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6617);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1084:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1084,17);
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
        dbg.location(1088, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:1: scss_mq_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* ;
    public final void scss_mq_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_mq_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1090, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1091:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            {
            dbg.location(1092,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            int alt228=2;
            try { dbg.enterSubRule(228);
            try { dbg.enterDecision(228, decisionCanBacktrack[228]);

            try {
                isCyclicDecision = true;
                alt228 = dfa228.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(228);}

            switch (alt228) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1093:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1093,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6747);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1095:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
                    {
                    dbg.location(1095,13);
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
            } finally {dbg.exitSubRule(228);}

            dbg.location(1097,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1097:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*
            try { dbg.enterSubRule(231);

            loop231:
            do {
                int alt231=2;
                try { dbg.enterDecision(231, decisionCanBacktrack[231]);

                try {
                    isCyclicDecision = true;
                    alt231 = dfa231.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(231);}

                switch (alt231) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1098:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
            	    {
            	    dbg.location(1098,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1098:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1098:13: ws
            	            {
            	            dbg.location(1098,13);
            	            pushFollow(FOLLOW_ws_in_scss_mq_interpolation_expression6841);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(229);}

            	    dbg.location(1099,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1099:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1100,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6880);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1102:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT )
            	            {
            	            dbg.location(1102,17);
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
        dbg.location(1106, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1108:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1108, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1109:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1110,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7001); if (state.failed) return ;
            dbg.location(1110,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var7003); if (state.failed) return ;
            dbg.location(1110,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:28: ws
                    {
                    dbg.location(1110,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7005);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(232);}

            dbg.location(1110,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:32: ( cp_variable | less_function_in_condition )
            int alt233=2;
            try { dbg.enterSubRule(233);
            try { dbg.enterDecision(233, decisionCanBacktrack[233]);

            int LA233_0 = input.LA(1);

            if ( (LA233_0==MEDIA_SYM||LA233_0==AT_IDENT||LA233_0==SASS_VAR) ) {
                alt233=1;
            }
            else if ( (LA233_0==IDENT) ) {
                alt233=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 233, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(233);}

            switch (alt233) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:34: cp_variable
                    {
                    dbg.location(1110,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var7010);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:48: less_function_in_condition
                    {
                    dbg.location(1110,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7014);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(233);}

            dbg.location(1110,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:77: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1110:77: ws
                    {
                    dbg.location(1110,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var7018);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(234);}

            dbg.location(1110,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var7021); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1111, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1131:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1131, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1132:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1133,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties7065);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1133,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties7067); if (state.failed) return ;
            dbg.location(1133,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:20: ws
                    {
                    dbg.location(1133,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7069);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(235);}

            dbg.location(1133,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:24: ( propertyValue )?
            int alt236=2;
            try { dbg.enterSubRule(236);
            try { dbg.enterDecision(236, decisionCanBacktrack[236]);

            int LA236_0 = input.LA(1);

            if ( ((LA236_0>=IDENT && LA236_0<=URI)||LA236_0==MEDIA_SYM||(LA236_0>=GEN && LA236_0<=LPAREN)||LA236_0==AT_IDENT||LA236_0==PERCENTAGE||LA236_0==PLUS||LA236_0==MINUS||LA236_0==HASH||(LA236_0>=NUMBER && LA236_0<=DIMENSION)||LA236_0==SASS_VAR) ) {
                alt236=1;
            }
            } finally {dbg.exitDecision(236);}

            switch (alt236) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:24: propertyValue
                    {
                    dbg.location(1133,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties7072);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(236);}

            dbg.location(1133,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties7075); if (state.failed) return ;
            dbg.location(1133,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1133:46: ws
                    {
                    dbg.location(1133,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties7077);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(237);}

            dbg.location(1133,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties7080);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1133,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties7082);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1133,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties7084); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1134, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_nested_properties");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_nested_properties"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:13: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(368,15);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:15: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(238);

        loop238:
        do {
            int alt238=2;
            try { dbg.enterDecision(238, decisionCanBacktrack[238]);

            int LA238_0 = input.LA(1);

            if ( ((LA238_0>=NAMESPACE_SYM && LA238_0<=MEDIA_SYM)||(LA238_0>=RBRACE && LA238_0<=MINUS)||(LA238_0>=HASH && LA238_0<=LINE_COMMENT)) ) {
                alt238=1;
            }


            } finally {dbg.exitDecision(238);}

            switch (alt238) {
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
        	    break loop238;
            }
        } while (true);
        } finally {dbg.exitSubRule(238);}

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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(376,18);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:18: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt239=0;
                try { dbg.enterSubRule(239);

                loop239:
                do {
                    int alt239=2;
                    try { dbg.enterDecision(239, decisionCanBacktrack[239]);

                    int LA239_0 = input.LA(1);

                    if ( (LA239_0==NAMESPACE_SYM||(LA239_0>=IDENT && LA239_0<=MEDIA_SYM)||(LA239_0>=AND && LA239_0<=LPAREN)||(LA239_0>=RPAREN && LA239_0<=LINE_COMMENT)) ) {
                        alt239=1;
                    }


                    } finally {dbg.exitDecision(239);}

                    switch (alt239) {
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
                	    if ( cnt239 >= 1 ) break loop239;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(239, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt239++;
                } while (true);
                } finally {dbg.exitSubRule(239);}

                dbg.location(376,47);
                match(input,COLON,FOLLOW_COLON_in_synpred3_Css3624); if (state.failed) return ;
                dbg.location(376,53);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:53: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt240=0;
                try { dbg.enterSubRule(240);

                loop240:
                do {
                    int alt240=2;
                    try { dbg.enterDecision(240, decisionCanBacktrack[240]);

                    int LA240_0 = input.LA(1);

                    if ( (LA240_0==NAMESPACE_SYM||(LA240_0>=IDENT && LA240_0<=MEDIA_SYM)||(LA240_0>=AND && LA240_0<=LINE_COMMENT)) ) {
                        alt240=1;
                    }


                    } finally {dbg.exitDecision(240);}

                    switch (alt240) {
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
                	    if ( cnt240 >= 1 ) break loop240;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(240, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt240++;
                } while (true);
                } finally {dbg.exitSubRule(240);}

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt244=2;
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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(582,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt242=0;
                try { dbg.enterSubRule(242);

                loop242:
                do {
                    int alt242=2;
                    try { dbg.enterDecision(242, decisionCanBacktrack[242]);

                    int LA242_0 = input.LA(1);

                    if ( (LA242_0==NAMESPACE_SYM||(LA242_0>=IDENT && LA242_0<=MEDIA_SYM)||(LA242_0>=AND && LA242_0<=LPAREN)||(LA242_0>=RPAREN && LA242_0<=LINE_COMMENT)) ) {
                        alt242=1;
                    }


                    } finally {dbg.exitDecision(242);}

                    switch (alt242) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(582,4);
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
                	    if ( cnt242 >= 1 ) break loop242;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(242, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt242++;
                } while (true);
                } finally {dbg.exitSubRule(242);}

                dbg.location(582,33);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32345); if (state.failed) return ;
                dbg.location(582,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt243=0;
                try { dbg.enterSubRule(243);

                loop243:
                do {
                    int alt243=2;
                    try { dbg.enterDecision(243, decisionCanBacktrack[243]);

                    int LA243_0 = input.LA(1);

                    if ( (LA243_0==NAMESPACE_SYM||(LA243_0>=IDENT && LA243_0<=MEDIA_SYM)||(LA243_0>=AND && LA243_0<=LINE_COMMENT)) ) {
                        alt243=1;
                    }


                    } finally {dbg.exitDecision(243);}

                    switch (alt243) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(582,39);
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

                dbg.location(582,62);
                match(input,SEMI,FOLLOW_SEMI_in_synpred4_Css32357); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:69: scss_declaration_interpolation_expression COLON
                {
                dbg.location(582,69);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32361);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(582,111);
                match(input,COLON,FOLLOW_COLON_in_synpred4_Css32363); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )
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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE
                {
                dbg.location(584,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
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

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(584,4);
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

                dbg.location(584,33);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32393); if (state.failed) return ;
                dbg.location(584,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:39: (~ ( SEMI | LBRACE | RBRACE ) )+
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

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(584,39);
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

                dbg.location(584,62);
                match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32405); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:71: scss_declaration_interpolation_expression COLON
                {
                dbg.location(584,71);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32409);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(584,113);
                match(input,COLON,FOLLOW_COLON_in_synpred5_Css32411); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(586,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:18: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt248=0;
        try { dbg.enterSubRule(248);

        loop248:
        do {
            int alt248=2;
            try { dbg.enterDecision(248, decisionCanBacktrack[248]);

            int LA248_0 = input.LA(1);

            if ( (LA248_0==NAMESPACE_SYM||(LA248_0>=IDENT && LA248_0<=MEDIA_SYM)||(LA248_0>=AND && LA248_0<=LINE_COMMENT)) ) {
                alt248=1;
            }


            } finally {dbg.exitDecision(248);}

            switch (alt248) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(586,18);
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

        dbg.location(586,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred6_Css32451); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(594,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:16: (~ ( RBRACE ) )+
        int cnt249=0;
        try { dbg.enterSubRule(249);

        loop249:
        do {
            int alt249=2;
            try { dbg.enterDecision(249, decisionCanBacktrack[249]);

            int LA249_0 = input.LA(1);

            if ( ((LA249_0>=NAMESPACE_SYM && LA249_0<=LBRACE)||(LA249_0>=AND && LA249_0<=LINE_COMMENT)) ) {
                alt249=1;
            }


            } finally {dbg.exitDecision(249);}

            switch (alt249) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:16: ~ ( RBRACE )
        	    {
        	    dbg.location(594,16);
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

        dbg.location(594,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred7_Css32579); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(600,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(250);

        loop250:
        do {
            int alt250=2;
            try { dbg.enterDecision(250, decisionCanBacktrack[250]);

            int LA250_0 = input.LA(1);

            if ( ((LA250_0>=NAMESPACE_SYM && LA250_0<=MEDIA_SYM)||(LA250_0>=RBRACE && LA250_0<=MINUS)||(LA250_0>=HASH && LA250_0<=LINE_COMMENT)) ) {
                alt250=1;
            }


            } finally {dbg.exitDecision(250);}

            switch (alt250) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(600,11);
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
        	    break loop250;
            }
        } while (true);
        } finally {dbg.exitSubRule(250);}

        dbg.location(600,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred8_Css32637); if (state.failed) return ;
        dbg.location(600,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred8_Css32639); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:19: esPred
        {
        dbg.location(613,19);
        pushFollow(FOLLOW_esPred_in_synpred9_Css32737);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:6: esPred
        {
        dbg.location(615,6);
        pushFollow(FOLLOW_esPred_in_synpred10_Css32758);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(629,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:8: ( IDENT | STAR )?
        int alt251=2;
        try { dbg.enterSubRule(251);
        try { dbg.enterDecision(251, decisionCanBacktrack[251]);

        int LA251_0 = input.LA(1);

        if ( (LA251_0==IDENT||LA251_0==STAR) ) {
            alt251=1;
        }
        } finally {dbg.exitDecision(251);}

        switch (alt251) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(629,8);
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
        } finally {dbg.exitSubRule(251);}

        dbg.location(629,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred11_Css32872); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(727,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )*
        try { dbg.enterSubRule(252);

        loop252:
        do {
            int alt252=2;
            try { dbg.enterDecision(252, decisionCanBacktrack[252]);

            int LA252_0 = input.LA(1);

            if ( (LA252_0==NAMESPACE_SYM||(LA252_0>=IDENT && LA252_0<=LBRACE)||(LA252_0>=AND && LA252_0<=LPAREN)||(LA252_0>=RPAREN && LA252_0<=MINUS)||(LA252_0>=HASH && LA252_0<=LINE_COMMENT)) ) {
                alt252=1;
            }


            } finally {dbg.exitDecision(252);}

            switch (alt252) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:11: ~ ( HASH_SYMBOL | COLON | SEMI | RBRACE )
        	    {
        	    dbg.location(727,11);
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
        	    break loop252;
            }
        } while (true);
        } finally {dbg.exitSubRule(252);}

        dbg.location(727,51);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred12_Css33830); if (state.failed) return ;
        dbg.location(727,63);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Css33832); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:12: expressionPredicate
        {
        dbg.location(742,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred13_Css33918);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:7: ( function )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:8: function
        {
        dbg.location(824,8);
        pushFollow(FOLLOW_function_in_synpred14_Css34529);
        function();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:17: term
        {
        dbg.location(914,17);
        pushFollow(FOLLOW_term_in_synpred15_Css35161);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

    // $ANTLR start synpred16_Css3
    public final void synpred16_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1057,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred16_Css36234); if (state.failed) return ;
        dbg.location(1057,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred16_Css36236); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Css3

    // $ANTLR start synpred17_Css3
    public final void synpred17_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1064:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1064,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred17_Css36358); if (state.failed) return ;
        dbg.location(1064,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred17_Css36360); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Css3

    // $ANTLR start synpred18_Css3
    public final void synpred18_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1075:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1075:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1075,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred18_Css36492); if (state.failed) return ;
        dbg.location(1075,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred18_Css36494); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Css3

    // $ANTLR start synpred19_Css3
    public final void synpred19_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1082:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1082:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1082,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred19_Css36612); if (state.failed) return ;
        dbg.location(1082,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred19_Css36614); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Css3

    // $ANTLR start synpred20_Css3
    public final void synpred20_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1093:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1093:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1093,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred20_Css36742); if (state.failed) return ;
        dbg.location(1093,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred20_Css36744); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Css3

    // $ANTLR start synpred21_Css3
    public final void synpred21_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1100:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1100,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred21_Css36875); if (state.failed) return ;
        dbg.location(1100,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred21_Css36877); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_Css3

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
    protected DFA33 dfa33 = new DFA33(this);
    protected DFA52 dfa52 = new DFA52(this);
    protected DFA73 dfa73 = new DFA73(this);
    protected DFA99 dfa99 = new DFA99(this);
    protected DFA106 dfa106 = new DFA106(this);
    protected DFA111 dfa111 = new DFA111(this);
    protected DFA114 dfa114 = new DFA114(this);
    protected DFA132 dfa132 = new DFA132(this);
    protected DFA142 dfa142 = new DFA142(this);
    protected DFA146 dfa146 = new DFA146(this);
    protected DFA149 dfa149 = new DFA149(this);
    protected DFA155 dfa155 = new DFA155(this);
    protected DFA176 dfa176 = new DFA176(this);
    protected DFA180 dfa180 = new DFA180(this);
    protected DFA195 dfa195 = new DFA195(this);
    protected DFA200 dfa200 = new DFA200(this);
    protected DFA220 dfa220 = new DFA220(this);
    protected DFA223 dfa223 = new DFA223(this);
    protected DFA222 dfa222 = new DFA222(this);
    protected DFA228 dfa228 = new DFA228(this);
    protected DFA231 dfa231 = new DFA231(this);
    protected DFA230 dfa230 = new DFA230(this);
    protected DFA241 dfa241 = new DFA241(this);
    protected DFA244 dfa244 = new DFA244(this);
    protected DFA247 dfa247 = new DFA247(this);
    static final String DFA22_eotS =
        "\7\uffff";
    static final String DFA22_eofS =
        "\7\uffff";
    static final String DFA22_minS =
        "\1\12\2\7\2\5\2\uffff";
    static final String DFA22_maxS =
        "\1\12\4\122\2\uffff";
    static final String DFA22_acceptS =
        "\5\uffff\1\1\1\2";
    static final String DFA22_specialS =
        "\7\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\1",
            "\2\3\16\uffff\1\2\71\uffff\2\2",
            "\2\3\16\uffff\1\2\71\uffff\2\2",
            "\2\5\4\uffff\1\6\4\uffff\4\5\3\uffff\1\4\71\uffff\2\4",
            "\2\5\4\uffff\1\6\4\uffff\4\5\3\uffff\1\4\71\uffff\2\4",
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
        "\1\70\1\uffff\1\122\1\0\5\uffff\1\122\1\uffff\1\0\2\uffff";
    static final String DFA25_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\1\3\2\1\uffff\1\1\1\uffff\2\1";
    static final String DFA25_specialS =
        "\1\2\1\uffff\1\0\1\4\5\uffff\1\1\1\uffff\1\3\2\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\3\6\uffff\1\10\1\uffff\1\5\1\4\1\2\1\6\1\7\1\5\40\uffff\1"+
            "\5\1\1\2\5",
            "",
            "\1\13\6\uffff\1\14\1\uffff\1\15\1\uffff\1\15\1\6\1\uffff\1"+
            "\15\2\uffff\1\11\35\uffff\1\15\1\12\2\15\30\uffff\2\11",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\13\6\uffff\1\14\1\uffff\1\15\1\uffff\1\15\1\6\1\uffff\1"+
            "\15\2\uffff\1\11\35\uffff\1\15\1\12\2\15\30\uffff\2\11",
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
                    case 1 : 
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
                    case 2 : 
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
                    case 3 : 
                        int LA25_11 = input.LA(1);

                         
                        int index25_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA25_3 = input.LA(1);

                         
                        int index25_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 8;}

                         
                        input.seek(index25_3);
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
    static final String DFA33_eotS =
        "\27\uffff";
    static final String DFA33_eofS =
        "\27\uffff";
    static final String DFA33_minS =
        "\1\6\1\uffff\6\0\1\uffff\1\0\4\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA33_maxS =
        "\1\125\1\uffff\6\0\1\uffff\1\0\4\uffff\1\0\6\uffff\1\0\1\uffff";
    static final String DFA33_acceptS =
        "\1\uffff\1\7\6\uffff\1\1\1\uffff\1\2\6\uffff\1\3\1\4\1\5\2\uffff"+
        "\1\6";
    static final String DFA33_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\uffff\1\7\4\uffff\1\10\6"+
        "\uffff\1\11\1\uffff}>";
    static final String[] DFA33_transitionS = {
            "\1\4\5\uffff\1\25\1\uffff\1\1\3\uffff\1\6\1\uffff\1\12\1\uffff"+
            "\1\7\1\uffff\1\23\3\uffff\1\23\1\uffff\1\21\1\uffff\1\22\24"+
            "\uffff\1\16\1\3\1\11\1\5\2\12\1\2\1\12\1\uffff\1\12\25\uffff"+
            "\1\10\1\12",
            "",
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

    static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
    static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
    static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
    static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
    static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
    static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
    static final short[][] DFA33_transition;

    static {
        int numStates = DFA33_transitionS.length;
        DFA33_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
        }
    }

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = DFA33_eot;
            this.eof = DFA33_eof;
            this.min = DFA33_min;
            this.max = DFA33_max;
            this.accept = DFA33_accept;
            this.special = DFA33_special;
            this.transition = DFA33_transition;
        }
        public String getDescription() {
            return "()* loopback of 374:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | rule ( ws )? | page ( ws )? | fontFace ( ws )? | vendorAtRule ( ws )? | {...}? media ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA33_0 = input.LA(1);

                         
                        int index33_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA33_0==RBRACE) ) {s = 1;}

                        else if ( (LA33_0==STAR) ) {s = 2;}

                        else if ( (LA33_0==HASH_SYMBOL) ) {s = 3;}

                        else if ( (LA33_0==IDENT) ) {s = 4;}

                        else if ( (LA33_0==DOT) ) {s = 5;}

                        else if ( (LA33_0==GEN) ) {s = 6;}

                        else if ( (LA33_0==AT_IDENT) ) {s = 7;}

                        else if ( (LA33_0==SASS_VAR) && (synpred3_Css3())) {s = 8;}

                        else if ( (LA33_0==HASH) ) {s = 9;}

                        else if ( (LA33_0==COLON||(LA33_0>=LBRACKET && LA33_0<=DCOLON)||LA33_0==PIPE||LA33_0==LESS_AND||LA33_0==SASS_MIXIN) ) {s = 10;}

                        else if ( (LA33_0==MINUS) ) {s = 14;}

                        else if ( (LA33_0==PAGE_SYM) ) {s = 17;}

                        else if ( (LA33_0==FONT_FACE_SYM) ) {s = 18;}

                        else if ( (LA33_0==MOZ_DOCUMENT_SYM||LA33_0==WEBKIT_KEYFRAMES_SYM) ) {s = 19;}

                        else if ( (LA33_0==MEDIA_SYM) ) {s = 21;}

                         
                        input.seek(index33_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA33_2 = input.LA(1);

                         
                        int index33_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA33_3 = input.LA(1);

                         
                        int index33_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA33_4 = input.LA(1);

                         
                        int index33_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA33_5 = input.LA(1);

                         
                        int index33_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA33_6 = input.LA(1);

                         
                        int index33_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA33_7 = input.LA(1);

                         
                        int index33_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA33_9 = input.LA(1);

                         
                        int index33_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA33_14 = input.LA(1);

                         
                        int index33_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 8;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index33_14);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA33_21 = input.LA(1);

                         
                        int index33_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                        else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {s = 22;}

                         
                        input.seek(index33_21);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 33, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA52_eotS =
        "\155\uffff";
    static final String DFA52_eofS =
        "\155\uffff";
    static final String DFA52_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\2\uffff\1\5\1\6\1\uffff\2\6\1\5\3\6"+
        "\5\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\1\5\2\6\2\5\2\6\1\5\1\6\1\5\2\6"+
        "\2\5\2\6\1\5\1\6\2\5\1\6\1\5\1\6\2\5\4\6\2\5\1\6\1\5\1\6\1\5\2\6"+
        "\1\5\1\6\2\5\1\6\1\5\3\6\1\5\1\6\1\5\4\6\2\5\1\6\1\5\1\6\1\5\6\6"+
        "\1\5\1\6\1\5\7\6";
    static final String DFA52_maxS =
        "\1\126\1\122\1\uffff\1\122\4\uffff\1\122\2\uffff\2\122\1\uffff\1"+
        "\124\2\122\1\130\4\124\2\122\1\131\1\124\1\122\4\124\1\122\1\124"+
        "\1\130\1\131\1\122\4\124\1\122\1\124\1\122\1\124\1\122\1\130\4\122"+
        "\4\124\1\122\4\124\3\122\3\124\1\122\1\124\1\122\1\124\2\122\4\124"+
        "\1\122\1\124\3\122\1\124\1\122\1\124\3\122\3\124\1\122\1\124\1\122"+
        "\1\124\6\122\1\124\1\122\1\124\7\122";
    static final String DFA52_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\2\uffff\1"+
        "\2\137\uffff";
    static final String DFA52_specialS =
        "\155\uffff}>";
    static final String[] DFA52_transitionS = {
            "\1\2\5\uffff\1\3\5\uffff\1\2\1\uffff\1\2\1\uffff\1\10\1\uffff"+
            "\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3\2\1\1\4\2\1"+
            "\uffff\1\2\25\uffff\1\11\1\2\1\12",
            "\1\13\6\uffff\1\2\4\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\30\uffff\2\2",
            "",
            "\1\15\6\uffff\1\15\1\uffff\5\15\1\16\2\uffff\1\14\35\uffff"+
            "\4\15\30\uffff\2\14",
            "",
            "",
            "",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\17\71\uffff\2\11",
            "",
            "",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\21\1\2\2\uffff"+
            "\1\20\32\uffff\13\2\1\uffff\1\2\22\uffff\2\20",
            "\1\15\6\uffff\1\15\1\uffff\5\15\1\16\2\uffff\1\14\35\uffff"+
            "\4\15\30\uffff\2\14",
            "",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\22\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\23\1\15\1\25\1\15\16\uffff\12\11\2\22\1\uffff\1\11",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\17\71\uffff\2\11",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\21\1\2\2\uffff"+
            "\1\20\32\uffff\13\2\1\uffff\1\2\22\uffff\2\20",
            "\3\12\3\uffff\1\26\5\uffff\1\12\2\uffff\1\30\1\26\6\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\3"+
            "\uffff\1\27\2\uffff\2\2",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\22\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\23\1\15\1\25\1\15\16\uffff\12\11\2\22\1\uffff\1\11",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\31\5\uffff\1\11\27\uffff\2\15\1"+
            "\25\1\15\16\uffff\12\11\2\31\1\uffff\1\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\32\1\uffff\1\11\1\33\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\37\2\uffff\1\11\13\uffff\12\11\2\33\2\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\40\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\40\2\11",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\12\7\uffff\1\2\11\uffff\1\42\71\uffff\2\42\6\uffff\1\2",
            "\1\24\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\31\5\uffff\1\11\27\uffff\2\15\1"+
            "\25\1\15\16\uffff\12\11\2\31\1\uffff\1\11",
            "\1\43\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\33\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11\2\33\2\11",
            "\1\45\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\44\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\46\1\15\16\uffff\12\11\2\44\1\uffff\1\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\50\1\uffff\1\11\1\51\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\52\2\uffff\1\11\13\uffff\12\11\2\51\2\11",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\53\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\53\2\11",
            "\1\54\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\40\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\40\2\11",
            "\3\12\3\uffff\1\56\5\uffff\1\12\3\uffff\1\56\1\55\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\55\1\uffff\1\57\2\uffff\2\2",
            "\1\12\7\uffff\1\2\11\uffff\1\42\71\uffff\2\42\6\uffff\1\2",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\3\15\1\37\30\uffff\2\60",
            "\1\45\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\44\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\46\1\15\16\uffff\12\11\2\44\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\61\1\uffff\1\11\1\62\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\66\2\uffff\1\11\13\uffff\12\11\2\62\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\67\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\67\2\11",
            "\1\71\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\70\5\uffff\1\11\27\uffff\2\15\1"+
            "\72\1\15\16\uffff\12\11\2\70\1\uffff\1\11",
            "\1\73\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\51\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11\2\51\2\11",
            "\1\74\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\35\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\53\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\34\1\15\1\36\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\53\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\3\15\1\37\30\uffff\2\60",
            "\3\12\3\uffff\1\56\5\uffff\1\12\3\uffff\1\56\1\55\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\55\1\uffff\1\57\2\uffff\2\2",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\41\5\uffff\1\41\10\uffff\1\2\1\30\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\60\35\uffff\4\15\30\uffff\2\60",
            "\1\75\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15\2"+
            "\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\62\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11\2\62\2\11",
            "\1\77\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\76\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\100\1\15\16\uffff\12\11\2\76\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\101\1\uffff\1\11\1\102\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\63\1\15\1\65\1\103\2\uffff\1\11\13\uffff\12\11\2\102"+
            "\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\104\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\104\2\11",
            "\1\105\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\67\5\uffff\1\11\23\uffff\2"+
            "\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\67\2\11",
            "\1\71\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\1\11"+
            "\1\uffff\1\15\1\uffff\1\11\1\70\5\uffff\1\11\27\uffff\2\15\1"+
            "\72\1\15\16\uffff\12\11\2\70\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\106\1\uffff\1\11\1\107\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\113\2\uffff\1\11\13\uffff\12\11\2"+
            "\107\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\114\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\114\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\3\15\1\52\30\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\3\15\1\52\30\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\3\15\1\66\30\uffff\2\116",
            "\1\77\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\76\5\uffff\1\11\24\uffff\1\11\2\uffff\1"+
            "\47\1\15\1\100\1\15\16\uffff\12\11\2\76\1\uffff\1\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\117\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\63\1\15\1\65\1\121\2\uffff\1\11\13\uffff\12\11\2\120"+
            "\2\11",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\122\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\122\2\11",
            "\1\123\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\102\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11\2\102\2\11",
            "\1\124\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\104\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\104\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\3\15\1\66\30\uffff\2\116",
            "\1\125\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\107\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12\11\2\107\2\11",
            "\1\127\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\126\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\47\1\15\1\130\1\15\16\uffff\12\11\2\126\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\131\1\uffff\1\11\1\132\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\133\2\uffff\1\11\13\uffff\12\11\2"+
            "\132\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\134\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\134\2\11",
            "\1\135\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\114\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\114\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\115\35\uffff\4\15\30\uffff\2\115",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\116\35\uffff\4\15\30\uffff\2\116",
            "\1\136\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\120\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11\2\120\2\11",
            "\1\137\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\64\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\122\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\63\1\15\1\65\1\15\2\uffff\1\11\13\uffff\12\11"+
            "\2\122\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\3\15\1\103\30\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\3\15\1\103\30\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\3\15\1\113\30\uffff\2\141",
            "\1\127\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15\2\11"+
            "\1\15\1\uffff\1\11\1\126\5\uffff\1\11\24\uffff\1\11\2\uffff"+
            "\1\47\1\15\1\130\1\15\16\uffff\12\11\2\126\1\uffff\1\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\142\1\uffff\1\11\1\143\5\uffff\1\11\23\uffff\2\11\2"+
            "\uffff\1\110\1\15\1\112\1\144\2\uffff\1\11\13\uffff\12\11\2"+
            "\143\2\11",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\145\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\145\2\11",
            "\1\146\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\132\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12\11\2\132\2\11",
            "\1\147\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\134\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\134\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\3\15\1\113\30\uffff\2\141",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\3\15\1\121\30\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\3\15\1\121\30\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\140\35\uffff\4\15\30\uffff\2\140",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\141\35\uffff\4\15\30\uffff\2\141",
            "\1\151\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\2\11\1\15\1\uffff\1\11\1\143\5\uffff\1\11\23\uffff\2\11\2\uffff"+
            "\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12\11\2\143\2\11",
            "\1\152\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\2\uffff\1\15"+
            "\2\uffff\1\15\35\uffff\4\15\30\uffff\2\15",
            "\1\11\1\111\2\11\3\uffff\1\11\1\15\1\uffff\1\15\1\uffff\1\15"+
            "\1\11\1\uffff\1\15\1\uffff\1\11\1\145\5\uffff\1\11\23\uffff"+
            "\2\11\2\uffff\1\110\1\15\1\112\1\15\2\uffff\1\11\13\uffff\12"+
            "\11\2\145\2\11",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\3\15\1\133\30\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\3\15\1\133\30\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\150\35\uffff\4\15\30\uffff\2\150",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\3\15\1\144\30\uffff\2\154",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\3\15\1\144\30\uffff\2\154",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\153\35\uffff\4\15\30\uffff\2\153",
            "\1\15\6\uffff\1\15\1\uffff\1\15\1\uffff\1\15\1\uffff\1\11\1"+
            "\15\2\uffff\1\154\35\uffff\4\15\30\uffff\2\154"
    };

    static final short[] DFA52_eot = DFA.unpackEncodedString(DFA52_eotS);
    static final short[] DFA52_eof = DFA.unpackEncodedString(DFA52_eofS);
    static final char[] DFA52_min = DFA.unpackEncodedStringToUnsignedChars(DFA52_minS);
    static final char[] DFA52_max = DFA.unpackEncodedStringToUnsignedChars(DFA52_maxS);
    static final short[] DFA52_accept = DFA.unpackEncodedString(DFA52_acceptS);
    static final short[] DFA52_special = DFA.unpackEncodedString(DFA52_specialS);
    static final short[][] DFA52_transition;

    static {
        int numStates = DFA52_transitionS.length;
        DFA52_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA52_transition[i] = DFA.unpackEncodedString(DFA52_transitionS[i]);
        }
    }

    class DFA52 extends DFA {

        public DFA52(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 52;
            this.eot = DFA52_eot;
            this.eof = DFA52_eof;
            this.min = DFA52_min;
            this.max = DFA52_max;
            this.accept = DFA52_accept;
            this.special = DFA52_special;
            this.transition = DFA52_transition;
        }
        public String getDescription() {
            return "417:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA73_eotS =
        "\4\uffff";
    static final String DFA73_eofS =
        "\4\uffff";
    static final String DFA73_minS =
        "\2\13\2\uffff";
    static final String DFA73_maxS =
        "\2\122\2\uffff";
    static final String DFA73_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA73_specialS =
        "\4\uffff}>";
    static final String[] DFA73_transitionS = {
            "\1\3\1\uffff\1\2\11\uffff\1\1\71\uffff\2\1",
            "\1\3\1\uffff\1\2\11\uffff\1\1\71\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA73_eot = DFA.unpackEncodedString(DFA73_eotS);
    static final short[] DFA73_eof = DFA.unpackEncodedString(DFA73_eofS);
    static final char[] DFA73_min = DFA.unpackEncodedStringToUnsignedChars(DFA73_minS);
    static final char[] DFA73_max = DFA.unpackEncodedStringToUnsignedChars(DFA73_maxS);
    static final short[] DFA73_accept = DFA.unpackEncodedString(DFA73_acceptS);
    static final short[] DFA73_special = DFA.unpackEncodedString(DFA73_specialS);
    static final short[][] DFA73_transition;

    static {
        int numStates = DFA73_transitionS.length;
        DFA73_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA73_transition[i] = DFA.unpackEncodedString(DFA73_transitionS[i]);
        }
    }

    class DFA73 extends DFA {

        public DFA73(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 73;
            this.eot = DFA73_eot;
            this.eof = DFA73_eof;
            this.min = DFA73_min;
            this.max = DFA73_max;
            this.accept = DFA73_accept;
            this.special = DFA73_special;
            this.transition = DFA73_transition;
        }
        public String getDescription() {
            return "()* loopback of 482:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA99_eotS =
        "\6\uffff";
    static final String DFA99_eofS =
        "\6\uffff";
    static final String DFA99_minS =
        "\2\6\2\uffff\2\6";
    static final String DFA99_maxS =
        "\1\125\1\122\2\uffff\2\122";
    static final String DFA99_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA99_specialS =
        "\6\uffff}>";
    static final String[] DFA99_transitionS = {
            "\1\3\13\uffff\1\3\1\uffff\1\3\40\uffff\3\3\1\1\4\3\1\uffff\1"+
            "\3\26\uffff\1\2",
            "\1\4\6\uffff\1\3\4\uffff\1\3\1\uffff\1\3\2\uffff\1\3\35\uffff"+
            "\4\3\30\uffff\2\3",
            "",
            "",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\13\3\1\uffff\1\3\22\uffff\2\5",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\13\3\1\uffff\1\3\22\uffff\2\5"
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
            return "559:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA106_eotS =
        "\26\uffff";
    static final String DFA106_eofS =
        "\26\uffff";
    static final String DFA106_minS =
        "\1\6\7\0\1\uffff\1\0\4\uffff\1\0\2\uffff\1\0\4\uffff";
    static final String DFA106_maxS =
        "\1\126\7\0\1\uffff\1\0\4\uffff\1\0\2\uffff\1\0\4\uffff";
    static final String DFA106_acceptS =
        "\10\uffff\1\6\1\uffff\4\3\1\uffff\2\3\1\uffff\1\5\1\1\1\2\1\4";
    static final String DFA106_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\4\uffff\1\11\2\uffff"+
        "\1\12\4\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\13\1\uffff"+
            "\1\21\36\uffff\1\16\1\2\1\11\1\4\1\17\1\20\1\1\1\14\1\uffff"+
            "\1\15\25\uffff\1\7\1\12\1\22",
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
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "",
            "",
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
            return "()* loopback of 578:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? media ( ws )? | {...}? cp_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA106_0 = input.LA(1);

                         
                        int index106_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA106_0==STAR) ) {s = 1;}

                        else if ( (LA106_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA106_0==IDENT) ) {s = 3;}

                        else if ( (LA106_0==DOT) ) {s = 4;}

                        else if ( (LA106_0==GEN) ) {s = 5;}

                        else if ( (LA106_0==MEDIA_SYM) ) {s = 6;}

                        else if ( (LA106_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA106_0==RBRACE) ) {s = 8;}

                        else if ( (LA106_0==HASH) ) {s = 9;}

                        else if ( (LA106_0==SASS_MIXIN) && (synpred6_Css3())) {s = 10;}

                        else if ( (LA106_0==COLON) && (synpred6_Css3())) {s = 11;}

                        else if ( (LA106_0==PIPE) && (synpred6_Css3())) {s = 12;}

                        else if ( (LA106_0==LESS_AND) && (synpred6_Css3())) {s = 13;}

                        else if ( (LA106_0==MINUS) ) {s = 14;}

                        else if ( (LA106_0==LBRACKET) && (synpred6_Css3())) {s = 15;}

                        else if ( (LA106_0==DCOLON) && (synpred6_Css3())) {s = 16;}

                        else if ( (LA106_0==AT_IDENT) ) {s = 17;}

                        else if ( (LA106_0==SASS_INCLUDE) ) {s = 18;}

                         
                        input.seek(index106_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA106_1 = input.LA(1);

                         
                        int index106_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA106_2 = input.LA(1);

                         
                        int index106_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA106_3 = input.LA(1);

                         
                        int index106_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred5_Css3()) ) {s = 20;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA106_4 = input.LA(1);

                         
                        int index106_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( ((((synpred6_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))||synpred6_Css3())) ) {s = 16;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 18;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA106_5 = input.LA(1);

                         
                        int index106_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred5_Css3()) ) {s = 20;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA106_6 = input.LA(1);

                         
                        int index106_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 19;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 20;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 21;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index106_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA106_7 = input.LA(1);

                         
                        int index106_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 20;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index106_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA106_9 = input.LA(1);

                         
                        int index106_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA106_14 = input.LA(1);

                         
                        int index106_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 19;}

                        else if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index106_14);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA106_17 = input.LA(1);

                         
                        int index106_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 19;}

                        else if ( (((synpred5_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 20;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index106_17);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 106, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA111_eotS =
        "\17\uffff";
    static final String DFA111_eofS =
        "\17\uffff";
    static final String DFA111_minS =
        "\2\6\2\0\1\uffff\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA111_maxS =
        "\1\76\1\122\2\0\1\uffff\2\122\5\uffff\1\0\1\uffff\1\0";
    static final String DFA111_acceptS =
        "\4\uffff\1\2\2\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA111_specialS =
        "\1\4\1\5\1\3\1\6\1\uffff\1\2\1\0\5\uffff\1\7\1\uffff\1\1}>";
    static final String[] DFA111_transitionS = {
            "\1\2\13\uffff\1\4\1\uffff\1\6\40\uffff\1\7\1\1\1\3\1\5\4\4\1"+
            "\uffff\1\4",
            "\1\13\6\uffff\1\10\6\uffff\1\13\2\uffff\1\11\35\uffff\1\13"+
            "\1\12\2\13\4\uffff\1\4\23\uffff\2\11",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\14\6\uffff\1\15\4\uffff\1\4\1\uffff\1\13\2\uffff\1\11\35"+
            "\uffff\1\13\1\12\2\13\30\uffff\2\11",
            "\1\16\6\uffff\1\15\3\uffff\2\4\1\uffff\1\13\2\uffff\1\11\35"+
            "\uffff\1\13\1\12\2\13\30\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff"
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
            return "597:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA111_6 = input.LA(1);

                         
                        int index111_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA111_6==WS||(LA111_6>=NL && LA111_6<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA111_6==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA111_6==IDENT) ) {s = 14;}

                        else if ( (LA111_6==LBRACE) && (synpred8_Css3())) {s = 13;}

                        else if ( (LA111_6==COLON||LA111_6==MINUS||(LA111_6>=HASH && LA111_6<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( ((LA111_6>=NOT && LA111_6<=GEN)) ) {s = 4;}

                         
                        input.seek(index111_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA111_14 = input.LA(1);

                         
                        int index111_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index111_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA111_5 = input.LA(1);

                         
                        int index111_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA111_5==IDENT) ) {s = 12;}

                        else if ( (LA111_5==WS||(LA111_5>=NL && LA111_5<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA111_5==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA111_5==GEN) ) {s = 4;}

                        else if ( (LA111_5==COLON||LA111_5==MINUS||(LA111_5>=HASH && LA111_5<=DOT)) && (synpred8_Css3())) {s = 11;}

                        else if ( (LA111_5==LBRACE) && (synpred8_Css3())) {s = 13;}

                         
                        input.seek(index111_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA111_2 = input.LA(1);

                         
                        int index111_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index111_2);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA111_0 = input.LA(1);

                         
                        int index111_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA111_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA111_0==IDENT) ) {s = 2;}

                        else if ( (LA111_0==HASH) ) {s = 3;}

                        else if ( (LA111_0==GEN||(LA111_0>=LBRACKET && LA111_0<=PIPE)||LA111_0==LESS_AND) ) {s = 4;}

                        else if ( (LA111_0==DOT) ) {s = 5;}

                        else if ( (LA111_0==COLON) ) {s = 6;}

                        else if ( (LA111_0==MINUS) && (synpred8_Css3())) {s = 7;}

                         
                        input.seek(index111_0);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA111_1 = input.LA(1);

                         
                        int index111_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA111_1==LBRACE) && (synpred8_Css3())) {s = 8;}

                        else if ( (LA111_1==NAME) ) {s = 4;}

                        else if ( (LA111_1==WS||(LA111_1>=NL && LA111_1<=COMMENT)) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA111_1==HASH_SYMBOL) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA111_1==IDENT||LA111_1==COLON||LA111_1==MINUS||(LA111_1>=HASH && LA111_1<=DOT)) && (synpred8_Css3())) {s = 11;}

                         
                        input.seek(index111_1);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA111_3 = input.LA(1);

                         
                        int index111_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index111_3);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA111_12 = input.LA(1);

                         
                        int index111_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index111_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 111, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA114_eotS =
        "\21\uffff";
    static final String DFA114_eofS =
        "\21\uffff";
    static final String DFA114_minS =
        "\1\6\7\uffff\5\0\4\uffff";
    static final String DFA114_maxS =
        "\1\76\7\uffff\5\0\4\uffff";
    static final String DFA114_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA114_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA114_transitionS = {
            "\1\1\4\uffff\1\1\1\uffff\1\1\4\uffff\1\1\1\uffff\1\14\1\1\34"+
            "\uffff\3\1\1\uffff\1\11\1\10\1\12\1\13\1\14\2\1\1\uffff\1\1",
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

    static final short[] DFA114_eot = DFA.unpackEncodedString(DFA114_eotS);
    static final short[] DFA114_eof = DFA.unpackEncodedString(DFA114_eofS);
    static final char[] DFA114_min = DFA.unpackEncodedStringToUnsignedChars(DFA114_minS);
    static final char[] DFA114_max = DFA.unpackEncodedStringToUnsignedChars(DFA114_maxS);
    static final short[] DFA114_accept = DFA.unpackEncodedString(DFA114_acceptS);
    static final short[] DFA114_special = DFA.unpackEncodedString(DFA114_specialS);
    static final short[][] DFA114_transition;

    static {
        int numStates = DFA114_transitionS.length;
        DFA114_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA114_transition[i] = DFA.unpackEncodedString(DFA114_transitionS[i]);
        }
    }

    class DFA114 extends DFA {

        public DFA114(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 114;
            this.eot = DFA114_eot;
            this.eof = DFA114_eof;
            this.min = DFA114_min;
            this.max = DFA114_max;
            this.accept = DFA114_accept;
            this.special = DFA114_special;
            this.transition = DFA114_transition;
        }
        public String getDescription() {
            return "()* loopback of 613:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA114_8 = input.LA(1);

                         
                        int index114_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index114_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA114_9 = input.LA(1);

                         
                        int index114_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index114_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA114_10 = input.LA(1);

                         
                        int index114_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index114_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA114_11 = input.LA(1);

                         
                        int index114_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index114_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA114_12 = input.LA(1);

                         
                        int index114_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index114_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 114, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA132_eotS =
        "\4\uffff";
    static final String DFA132_eofS =
        "\4\uffff";
    static final String DFA132_minS =
        "\2\6\2\uffff";
    static final String DFA132_maxS =
        "\2\122\2\uffff";
    static final String DFA132_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA132_specialS =
        "\4\uffff}>";
    static final String[] DFA132_transitionS = {
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA132_eot = DFA.unpackEncodedString(DFA132_eotS);
    static final short[] DFA132_eof = DFA.unpackEncodedString(DFA132_eofS);
    static final char[] DFA132_min = DFA.unpackEncodedStringToUnsignedChars(DFA132_minS);
    static final char[] DFA132_max = DFA.unpackEncodedStringToUnsignedChars(DFA132_maxS);
    static final short[] DFA132_accept = DFA.unpackEncodedString(DFA132_acceptS);
    static final short[] DFA132_special = DFA.unpackEncodedString(DFA132_specialS);
    static final short[][] DFA132_transition;

    static {
        int numStates = DFA132_transitionS.length;
        DFA132_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA132_transition[i] = DFA.unpackEncodedString(DFA132_transitionS[i]);
        }
    }

    class DFA132 extends DFA {

        public DFA132(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 132;
            this.eot = DFA132_eot;
            this.eof = DFA132_eof;
            this.min = DFA132_min;
            this.max = DFA132_max;
            this.accept = DFA132_accept;
            this.special = DFA132_special;
            this.transition = DFA132_transition;
        }
        public String getDescription() {
            return "713:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA142_eotS =
        "\17\uffff";
    static final String DFA142_eofS =
        "\17\uffff";
    static final String DFA142_minS =
        "\2\6\10\0\1\uffff\1\6\2\0\1\uffff";
    static final String DFA142_maxS =
        "\2\124\10\0\1\uffff\1\124\2\0\1\uffff";
    static final String DFA142_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA142_specialS =
        "\2\uffff\1\0\1\2\1\11\1\6\1\5\1\1\1\3\1\10\2\uffff\1\4\1\7\1\uffff}>";
    static final String[] DFA142_transitionS = {
            "\1\4\1\3\1\6\3\uffff\1\10\5\uffff\1\5\1\12\2\uffff\1\10\6\uffff"+
            "\1\2\24\uffff\1\1\2\uffff\1\1\1\uffff\1\7\17\uffff\12\2\3\uffff"+
            "\1\11",
            "\1\4\1\3\1\6\3\uffff\1\14\5\uffff\1\5\3\uffff\1\14\1\13\5\uffff"+
            "\1\2\31\uffff\1\7\17\uffff\12\2\2\13\1\uffff\1\15",
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
            "\1\2\31\uffff\1\7\17\uffff\12\2\2\13\1\uffff\1\15",
            "\1\uffff",
            "\1\uffff",
            ""
    };

    static final short[] DFA142_eot = DFA.unpackEncodedString(DFA142_eotS);
    static final short[] DFA142_eof = DFA.unpackEncodedString(DFA142_eofS);
    static final char[] DFA142_min = DFA.unpackEncodedStringToUnsignedChars(DFA142_minS);
    static final char[] DFA142_max = DFA.unpackEncodedStringToUnsignedChars(DFA142_maxS);
    static final short[] DFA142_accept = DFA.unpackEncodedString(DFA142_acceptS);
    static final short[] DFA142_special = DFA.unpackEncodedString(DFA142_specialS);
    static final short[][] DFA142_transition;

    static {
        int numStates = DFA142_transitionS.length;
        DFA142_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA142_transition[i] = DFA.unpackEncodedString(DFA142_transitionS[i]);
        }
    }

    class DFA142 extends DFA {

        public DFA142(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 142;
            this.eot = DFA142_eot;
            this.eof = DFA142_eof;
            this.min = DFA142_min;
            this.max = DFA142_max;
            this.accept = DFA142_accept;
            this.special = DFA142_special;
            this.transition = DFA142_transition;
        }
        public String getDescription() {
            return "740:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA142_2 = input.LA(1);

                         
                        int index142_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA142_7 = input.LA(1);

                         
                        int index142_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA142_3 = input.LA(1);

                         
                        int index142_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA142_8 = input.LA(1);

                         
                        int index142_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index142_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA142_12 = input.LA(1);

                         
                        int index142_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA142_6 = input.LA(1);

                         
                        int index142_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA142_5 = input.LA(1);

                         
                        int index142_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_5);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA142_13 = input.LA(1);

                         
                        int index142_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_13);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA142_9 = input.LA(1);

                         
                        int index142_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred13_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index142_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA142_4 = input.LA(1);

                         
                        int index142_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index142_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 142, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA146_eotS =
        "\12\uffff";
    static final String DFA146_eofS =
        "\12\uffff";
    static final String DFA146_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA146_maxS =
        "\1\124\1\uffff\1\124\1\uffff\2\124\1\6\1\124\2\122";
    static final String DFA146_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA146_specialS =
        "\12\uffff}>";
    static final String[] DFA146_transitionS = {
            "\1\1\3\3\2\uffff\1\2\1\3\2\1\3\uffff\1\3\2\uffff\1\1\1\3\6\uffff"+
            "\1\3\23\uffff\2\3\2\uffff\1\3\1\uffff\1\3\16\uffff\1\1\12\3"+
            "\3\uffff\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\5\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
            "\3\24\uffff\1\3\2\uffff\1\3\1\uffff\1\3\17\uffff\12\3\2\4\1"+
            "\uffff\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\5\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
            "\3\24\uffff\1\3\2\uffff\1\3\1\uffff\1\3\17\uffff\12\3\2\4\1"+
            "\uffff\1\3",
            "\4\3\2\uffff\4\3\3\uffff\5\3\1\7\5\uffff\1\3\23\uffff\2\3\2"+
            "\uffff\1\3\1\uffff\1\3\1\6\6\uffff\1\1\6\uffff\13\3\2\7\1\uffff"+
            "\1\3",
            "\1\10",
            "\4\3\2\uffff\4\3\3\uffff\2\3\1\uffff\2\3\1\7\5\uffff\1\3\23"+
            "\uffff\2\3\2\uffff\1\3\1\uffff\1\3\7\uffff\1\1\6\uffff\13\3"+
            "\2\7\1\uffff\1\3",
            "\1\3\3\uffff\1\11\40\uffff\1\6\6\uffff\1\1\21\uffff\2\11",
            "\1\3\3\uffff\1\11\47\uffff\1\1\21\uffff\2\11"
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
            return "()* loopback of 800:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA149_eotS =
        "\15\uffff";
    static final String DFA149_eofS =
        "\3\uffff\1\12\5\uffff\1\12\3\uffff";
    static final String DFA149_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\3\uffff";
    static final String DFA149_maxS =
        "\1\124\2\uffff\1\124\5\uffff\1\124\3\uffff";
    static final String DFA149_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3\2\7";
    static final String DFA149_specialS =
        "\3\uffff\1\1\5\uffff\1\0\3\uffff}>";
    static final String[] DFA149_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\17\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\1\14\1\10\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\1\13\2\uffff\1"+
            "\12\12\uffff\13\12\2\11\2\12",
            "",
            "",
            "",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\1\14\1\uffff\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\3\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12",
            "",
            "",
            ""
    };

    static final short[] DFA149_eot = DFA.unpackEncodedString(DFA149_eotS);
    static final short[] DFA149_eof = DFA.unpackEncodedString(DFA149_eofS);
    static final char[] DFA149_min = DFA.unpackEncodedStringToUnsignedChars(DFA149_minS);
    static final char[] DFA149_max = DFA.unpackEncodedStringToUnsignedChars(DFA149_maxS);
    static final short[] DFA149_accept = DFA.unpackEncodedString(DFA149_acceptS);
    static final short[] DFA149_special = DFA.unpackEncodedString(DFA149_specialS);
    static final short[][] DFA149_transition;

    static {
        int numStates = DFA149_transitionS.length;
        DFA149_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA149_transition[i] = DFA.unpackEncodedString(DFA149_transitionS[i]);
        }
    }

    class DFA149 extends DFA {

        public DFA149(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 149;
            this.eot = DFA149_eot;
            this.eof = DFA149_eof;
            this.min = DFA149_min;
            this.max = DFA149_max;
            this.accept = DFA149_accept;
            this.special = DFA149_special;
            this.transition = DFA149_transition;
        }
        public String getDescription() {
            return "805:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | ( function )=> function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA149_9 = input.LA(1);

                         
                        int index149_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA149_9==EOF||(LA149_9>=SEMI && LA149_9<=URI)||(LA149_9>=COMMA && LA149_9<=RBRACE)||LA149_9==GEN||(LA149_9>=RPAREN && LA149_9<=AT_IDENT)||LA149_9==PERCENTAGE||(LA149_9>=SOLIDUS && LA149_9<=PLUS)||LA149_9==MINUS||LA149_9==HASH||LA149_9==STAR||(LA149_9>=IMPORTANT_SYM && LA149_9<=DIMENSION)||(LA149_9>=SASS_DEFAULT && LA149_9<=SASS_VAR)) ) {s = 10;}

                        else if ( (LA149_9==WS||(LA149_9>=NL && LA149_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA149_9==LPAREN) && (synpred14_Css3())) {s = 12;}

                         
                        input.seek(index149_9);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA149_3 = input.LA(1);

                         
                        int index149_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA149_3==COLON) && (synpred14_Css3())) {s = 8;}

                        else if ( (LA149_3==WS||(LA149_3>=NL && LA149_3<=COMMENT)) ) {s = 9;}

                        else if ( (LA149_3==EOF||(LA149_3>=SEMI && LA149_3<=URI)||(LA149_3>=COMMA && LA149_3<=RBRACE)||LA149_3==GEN||(LA149_3>=RPAREN && LA149_3<=AT_IDENT)||LA149_3==PERCENTAGE||(LA149_3>=SOLIDUS && LA149_3<=PLUS)||LA149_3==MINUS||LA149_3==HASH||LA149_3==STAR||(LA149_3>=IMPORTANT_SYM && LA149_3<=DIMENSION)||(LA149_3>=SASS_DEFAULT && LA149_3<=SASS_VAR)) ) {s = 10;}

                        else if ( (LA149_3==DOT) && (synpred14_Css3())) {s = 11;}

                        else if ( (LA149_3==LPAREN) && (synpred14_Css3())) {s = 12;}

                         
                        input.seek(index149_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 149, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA155_eotS =
        "\10\uffff";
    static final String DFA155_eofS =
        "\10\uffff";
    static final String DFA155_minS =
        "\1\6\1\uffff\3\6\1\uffff\2\23";
    static final String DFA155_maxS =
        "\1\124\1\uffff\2\124\1\6\1\uffff\2\122";
    static final String DFA155_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA155_specialS =
        "\10\uffff}>";
    static final String[] DFA155_transitionS = {
            "\1\2\2\1\3\uffff\1\1\5\uffff\1\1\3\uffff\1\1\6\uffff\1\1\24"+
            "\uffff\1\1\2\uffff\1\1\1\uffff\1\1\17\uffff\12\1\3\uffff\1\1",
            "",
            "\3\1\2\uffff\2\1\5\uffff\5\1\1\3\5\uffff\1\1\23\uffff\2\1\2"+
            "\uffff\1\1\1\uffff\1\1\1\4\6\uffff\1\5\7\uffff\12\1\2\3\1\uffff"+
            "\1\1",
            "\3\1\2\uffff\2\1\5\uffff\2\1\1\uffff\2\1\1\3\5\uffff\1\1\23"+
            "\uffff\2\1\2\uffff\1\1\1\uffff\1\1\7\uffff\1\5\7\uffff\12\1"+
            "\2\3\1\uffff\1\1",
            "\1\6",
            "",
            "\1\1\3\uffff\1\7\40\uffff\1\4\6\uffff\1\5\21\uffff\2\7",
            "\1\1\3\uffff\1\7\47\uffff\1\5\21\uffff\2\7"
    };

    static final short[] DFA155_eot = DFA.unpackEncodedString(DFA155_eotS);
    static final short[] DFA155_eof = DFA.unpackEncodedString(DFA155_eofS);
    static final char[] DFA155_min = DFA.unpackEncodedStringToUnsignedChars(DFA155_minS);
    static final char[] DFA155_max = DFA.unpackEncodedStringToUnsignedChars(DFA155_maxS);
    static final short[] DFA155_accept = DFA.unpackEncodedString(DFA155_acceptS);
    static final short[] DFA155_special = DFA.unpackEncodedString(DFA155_specialS);
    static final short[][] DFA155_transition;

    static {
        int numStates = DFA155_transitionS.length;
        DFA155_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA155_transition[i] = DFA.unpackEncodedString(DFA155_transitionS[i]);
        }
    }

    class DFA155 extends DFA {

        public DFA155(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 155;
            this.eot = DFA155_eot;
            this.eof = DFA155_eof;
            this.min = DFA155_min;
            this.max = DFA155_max;
            this.accept = DFA155_accept;
            this.special = DFA155_special;
            this.transition = DFA155_transition;
        }
        public String getDescription() {
            return "833:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA176_eotS =
        "\36\uffff";
    static final String DFA176_eofS =
        "\36\uffff";
    static final String DFA176_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA176_maxS =
        "\1\124\1\uffff\2\124\10\uffff\1\124\10\0\1\124\10\0";
    static final String DFA176_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA176_specialS =
        "\1\2\14\uffff\1\0\1\1\1\11\1\5\1\20\1\14\1\7\1\16\1\uffff\1\3\1"+
        "\6\1\4\1\15\1\12\1\10\1\17\1\13}>";
    static final String[] DFA176_transitionS = {
            "\1\1\1\6\1\5\1\10\2\uffff\1\1\1\12\2\1\3\uffff\1\7\2\uffff\1"+
            "\1\1\12\6\uffff\1\4\23\uffff\1\1\1\2\2\uffff\1\3\1\uffff\1\11"+
            "\3\uffff\1\1\12\uffff\1\1\12\4\2\uffff\1\1\1\13",
            "",
            "\1\17\1\16\1\21\3\uffff\1\23\5\uffff\1\20\1\1\2\uffff\1\23"+
            "\1\14\5\uffff\1\15\24\uffff\1\1\2\uffff\1\1\1\uffff\1\22\17"+
            "\uffff\12\15\2\14\1\uffff\1\24",
            "\1\30\1\27\1\32\3\uffff\1\34\5\uffff\1\31\1\1\2\uffff\1\34"+
            "\1\25\5\uffff\1\26\24\uffff\1\1\2\uffff\1\1\1\uffff\1\33\17"+
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
            "\1\14\5\uffff\1\15\24\uffff\1\1\2\uffff\1\1\1\uffff\1\22\17"+
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
            "\1\25\5\uffff\1\26\24\uffff\1\1\2\uffff\1\1\1\uffff\1\33\17"+
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

    static final short[] DFA176_eot = DFA.unpackEncodedString(DFA176_eotS);
    static final short[] DFA176_eof = DFA.unpackEncodedString(DFA176_eofS);
    static final char[] DFA176_min = DFA.unpackEncodedStringToUnsignedChars(DFA176_minS);
    static final char[] DFA176_max = DFA.unpackEncodedStringToUnsignedChars(DFA176_maxS);
    static final short[] DFA176_accept = DFA.unpackEncodedString(DFA176_acceptS);
    static final short[] DFA176_special = DFA.unpackEncodedString(DFA176_specialS);
    static final short[][] DFA176_transition;

    static {
        int numStates = DFA176_transitionS.length;
        DFA176_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA176_transition[i] = DFA.unpackEncodedString(DFA176_transitionS[i]);
        }
    }

    class DFA176 extends DFA {

        public DFA176(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 176;
            this.eot = DFA176_eot;
            this.eof = DFA176_eof;
            this.min = DFA176_min;
            this.max = DFA176_max;
            this.accept = DFA176_accept;
            this.special = DFA176_special;
            this.transition = DFA176_transition;
        }
        public String getDescription() {
            return "()* loopback of 914:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA176_13 = input.LA(1);

                         
                        int index176_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA176_14 = input.LA(1);

                         
                        int index176_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA176_0 = input.LA(1);

                         
                        int index176_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA176_0==SEMI||LA176_0==COMMA||(LA176_0>=LBRACE && LA176_0<=RBRACE)||LA176_0==RPAREN||LA176_0==SOLIDUS||LA176_0==STAR||LA176_0==IMPORTANT_SYM||LA176_0==SASS_DEFAULT) ) {s = 1;}

                        else if ( (LA176_0==PLUS) ) {s = 2;}

                        else if ( (LA176_0==MINUS) ) {s = 3;}

                        else if ( (LA176_0==PERCENTAGE||(LA176_0>=NUMBER && LA176_0<=DIMENSION)) && (synpred15_Css3())) {s = 4;}

                        else if ( (LA176_0==STRING) && (synpred15_Css3())) {s = 5;}

                        else if ( (LA176_0==IDENT) && (synpred15_Css3())) {s = 6;}

                        else if ( (LA176_0==GEN) && (synpred15_Css3())) {s = 7;}

                        else if ( (LA176_0==URI) && (synpred15_Css3())) {s = 8;}

                        else if ( (LA176_0==HASH) && (synpred15_Css3())) {s = 9;}

                        else if ( (LA176_0==MEDIA_SYM||LA176_0==AT_IDENT) && (synpred15_Css3())) {s = 10;}

                        else if ( (LA176_0==SASS_VAR) && (synpred15_Css3())) {s = 11;}

                         
                        input.seek(index176_0);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA176_22 = input.LA(1);

                         
                        int index176_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_22);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA176_24 = input.LA(1);

                         
                        int index176_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_24);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA176_16 = input.LA(1);

                         
                        int index176_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_16);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA176_23 = input.LA(1);

                         
                        int index176_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_23);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA176_19 = input.LA(1);

                         
                        int index176_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_19);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA176_27 = input.LA(1);

                         
                        int index176_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_27);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA176_15 = input.LA(1);

                         
                        int index176_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA176_26 = input.LA(1);

                         
                        int index176_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_26);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA176_29 = input.LA(1);

                         
                        int index176_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_29);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA176_18 = input.LA(1);

                         
                        int index176_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_18);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA176_25 = input.LA(1);

                         
                        int index176_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_25);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA176_20 = input.LA(1);

                         
                        int index176_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_20);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA176_28 = input.LA(1);

                         
                        int index176_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_28);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA176_17 = input.LA(1);

                         
                        int index176_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index176_17);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 176, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA180_eotS =
        "\13\uffff";
    static final String DFA180_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA180_minS =
        "\1\6\2\uffff\1\23\5\uffff\1\23\1\uffff";
    static final String DFA180_maxS =
        "\1\124\2\uffff\1\122\5\uffff\1\122\1\uffff";
    static final String DFA180_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA180_specialS =
        "\13\uffff}>";
    static final String[] DFA180_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\17\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\2\10\2\uffff\1\11\40\uffff\1\10\30\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\10\3\uffff\1\11\71\uffff\2\11",
            ""
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
            return "921:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA195_eotS =
        "\4\uffff";
    static final String DFA195_eofS =
        "\4\uffff";
    static final String DFA195_minS =
        "\2\5\2\uffff";
    static final String DFA195_maxS =
        "\2\122\2\uffff";
    static final String DFA195_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA195_specialS =
        "\4\uffff}>";
    static final String[] DFA195_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA195_eot = DFA.unpackEncodedString(DFA195_eotS);
    static final short[] DFA195_eof = DFA.unpackEncodedString(DFA195_eofS);
    static final char[] DFA195_min = DFA.unpackEncodedStringToUnsignedChars(DFA195_minS);
    static final char[] DFA195_max = DFA.unpackEncodedStringToUnsignedChars(DFA195_maxS);
    static final short[] DFA195_accept = DFA.unpackEncodedString(DFA195_acceptS);
    static final short[] DFA195_special = DFA.unpackEncodedString(DFA195_specialS);
    static final short[][] DFA195_transition;

    static {
        int numStates = DFA195_transitionS.length;
        DFA195_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA195_transition[i] = DFA.unpackEncodedString(DFA195_transitionS[i]);
        }
    }

    class DFA195 extends DFA {

        public DFA195(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 195;
            this.eot = DFA195_eot;
            this.eof = DFA195_eof;
            this.min = DFA195_min;
            this.max = DFA195_max;
            this.accept = DFA195_accept;
            this.special = DFA195_special;
            this.transition = DFA195_transition;
        }
        public String getDescription() {
            return "969:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA200_eotS =
        "\5\uffff";
    static final String DFA200_eofS =
        "\5\uffff";
    static final String DFA200_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA200_maxS =
        "\1\25\1\130\1\uffff\1\130\1\uffff";
    static final String DFA200_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA200_specialS =
        "\5\uffff}>";
    static final String[] DFA200_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
    };

    static final short[] DFA200_eot = DFA.unpackEncodedString(DFA200_eotS);
    static final short[] DFA200_eof = DFA.unpackEncodedString(DFA200_eofS);
    static final char[] DFA200_min = DFA.unpackEncodedStringToUnsignedChars(DFA200_minS);
    static final char[] DFA200_max = DFA.unpackEncodedStringToUnsignedChars(DFA200_maxS);
    static final short[] DFA200_accept = DFA.unpackEncodedString(DFA200_acceptS);
    static final short[] DFA200_special = DFA.unpackEncodedString(DFA200_specialS);
    static final short[][] DFA200_transition;

    static {
        int numStates = DFA200_transitionS.length;
        DFA200_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA200_transition[i] = DFA.unpackEncodedString(DFA200_transitionS[i]);
        }
    }

    class DFA200 extends DFA {

        public DFA200(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 200;
            this.eot = DFA200_eot;
            this.eof = DFA200_eof;
            this.min = DFA200_min;
            this.max = DFA200_max;
            this.accept = DFA200_accept;
            this.special = DFA200_special;
            this.transition = DFA200_transition;
        }
        public String getDescription() {
            return "()* loopback of 989:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA220_eotS =
        "\16\uffff";
    static final String DFA220_eofS =
        "\16\uffff";
    static final String DFA220_minS =
        "\2\6\1\uffff\3\6\1\16\1\6\1\16\1\6\1\uffff\1\16\1\6\1\uffff";
    static final String DFA220_maxS =
        "\1\70\1\122\1\uffff\2\126\5\122\1\uffff\2\122\1\uffff";
    static final String DFA220_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA220_specialS =
        "\5\uffff\1\2\1\0\1\3\1\4\1\1\1\uffff\1\5\1\6\1\uffff}>";
    static final String[] DFA220_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\30\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\30\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            ""
    };

    static final short[] DFA220_eot = DFA.unpackEncodedString(DFA220_eotS);
    static final short[] DFA220_eof = DFA.unpackEncodedString(DFA220_eofS);
    static final char[] DFA220_min = DFA.unpackEncodedStringToUnsignedChars(DFA220_minS);
    static final char[] DFA220_max = DFA.unpackEncodedStringToUnsignedChars(DFA220_maxS);
    static final short[] DFA220_accept = DFA.unpackEncodedString(DFA220_acceptS);
    static final short[] DFA220_special = DFA.unpackEncodedString(DFA220_specialS);
    static final short[][] DFA220_transition;

    static {
        int numStates = DFA220_transitionS.length;
        DFA220_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA220_transition[i] = DFA.unpackEncodedString(DFA220_transitionS[i]);
        }
    }

    class DFA220 extends DFA {

        public DFA220(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 220;
            this.eot = DFA220_eot;
            this.eof = DFA220_eof;
            this.min = DFA220_min;
            this.max = DFA220_max;
            this.accept = DFA220_accept;
            this.special = DFA220_special;
            this.transition = DFA220_transition;
        }
        public String getDescription() {
            return "1056:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA220_6 = input.LA(1);

                         
                        int index220_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_6==WS||(LA220_6>=NL && LA220_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA220_6==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA220_6==COLON) ) {s = 2;}

                         
                        input.seek(index220_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA220_9 = input.LA(1);

                         
                        int index220_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_9==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA220_9==WS||(LA220_9>=NL && LA220_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA220_9==IDENT||LA220_9==LBRACE||(LA220_9>=AND && LA220_9<=COLON)||(LA220_9>=MINUS && LA220_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index220_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA220_5 = input.LA(1);

                         
                        int index220_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_5==WS||(LA220_5>=NL && LA220_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA220_5==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA220_5==IDENT||LA220_5==LBRACE||(LA220_5>=AND && LA220_5<=COLON)||(LA220_5>=MINUS && LA220_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index220_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA220_7 = input.LA(1);

                         
                        int index220_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_7==WS||(LA220_7>=NL && LA220_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA220_7==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA220_7==IDENT||LA220_7==COMMA||LA220_7==LBRACE||LA220_7==GEN||LA220_7==COLON||(LA220_7>=PLUS && LA220_7<=PIPE)||LA220_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index220_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA220_8 = input.LA(1);

                         
                        int index220_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_8==WS||(LA220_8>=NL && LA220_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA220_8==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA220_8==COLON) ) {s = 2;}

                         
                        input.seek(index220_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA220_11 = input.LA(1);

                         
                        int index220_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_11==RBRACE) && (synpred16_Css3())) {s = 10;}

                        else if ( (LA220_11==WS||(LA220_11>=NL && LA220_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA220_11==COLON) ) {s = 2;}

                         
                        input.seek(index220_11);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA220_12 = input.LA(1);

                         
                        int index220_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA220_12==LPAREN) && (synpred16_Css3())) {s = 13;}

                        else if ( (LA220_12==WS||(LA220_12>=NL && LA220_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA220_12==IDENT||LA220_12==COMMA||LA220_12==LBRACE||LA220_12==GEN||LA220_12==COLON||(LA220_12>=PLUS && LA220_12<=PIPE)||LA220_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index220_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 220, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA223_eotS =
        "\4\uffff";
    static final String DFA223_eofS =
        "\4\uffff";
    static final String DFA223_minS =
        "\2\6\2\uffff";
    static final String DFA223_maxS =
        "\2\122\2\uffff";
    static final String DFA223_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA223_specialS =
        "\4\uffff}>";
    static final String[] DFA223_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
            "",
            ""
    };

    static final short[] DFA223_eot = DFA.unpackEncodedString(DFA223_eotS);
    static final short[] DFA223_eof = DFA.unpackEncodedString(DFA223_eofS);
    static final char[] DFA223_min = DFA.unpackEncodedStringToUnsignedChars(DFA223_minS);
    static final char[] DFA223_max = DFA.unpackEncodedStringToUnsignedChars(DFA223_maxS);
    static final short[] DFA223_accept = DFA.unpackEncodedString(DFA223_acceptS);
    static final short[] DFA223_special = DFA.unpackEncodedString(DFA223_specialS);
    static final short[][] DFA223_transition;

    static {
        int numStates = DFA223_transitionS.length;
        DFA223_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA223_transition[i] = DFA.unpackEncodedString(DFA223_transitionS[i]);
        }
    }

    class DFA223 extends DFA {

        public DFA223(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 223;
            this.eot = DFA223_eot;
            this.eof = DFA223_eof;
            this.min = DFA223_min;
            this.max = DFA223_max;
            this.accept = DFA223_accept;
            this.special = DFA223_special;
            this.transition = DFA223_transition;
        }
        public String getDescription() {
            return "()* loopback of 1061:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*";
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
        "\1\70\1\122\1\uffff\2\126\5\122\1\uffff\2\122\1\uffff";
    static final String DFA222_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1";
    static final String DFA222_specialS =
        "\5\uffff\1\2\1\0\1\3\1\4\1\1\1\uffff\1\5\1\6\1\uffff}>";
    static final String[] DFA222_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\10\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\30\uffff"+
            "\2\11",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\11\35\uffff\4\2\30\uffff"+
            "\2\11",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
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
            return "1063:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA222_6 = input.LA(1);

                         
                        int index222_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_6==WS||(LA222_6>=NL && LA222_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_6==RBRACE) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA222_6==COLON) ) {s = 2;}

                         
                        input.seek(index222_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA222_9 = input.LA(1);

                         
                        int index222_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_9==RBRACE) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA222_9==WS||(LA222_9>=NL && LA222_9<=COMMENT)) ) {s = 9;}

                        else if ( (LA222_9==IDENT||LA222_9==LBRACE||(LA222_9>=AND && LA222_9<=COLON)||(LA222_9>=MINUS && LA222_9<=DOT)) ) {s = 2;}

                         
                        input.seek(index222_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA222_5 = input.LA(1);

                         
                        int index222_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_5==WS||(LA222_5>=NL && LA222_5<=COMMENT)) ) {s = 9;}

                        else if ( (LA222_5==RBRACE) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA222_5==IDENT||LA222_5==LBRACE||(LA222_5>=AND && LA222_5<=COLON)||(LA222_5>=MINUS && LA222_5<=DOT)) ) {s = 2;}

                         
                        input.seek(index222_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA222_7 = input.LA(1);

                         
                        int index222_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_7==WS||(LA222_7>=NL && LA222_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA222_7==LPAREN) && (synpred17_Css3())) {s = 13;}

                        else if ( (LA222_7==IDENT||LA222_7==COMMA||LA222_7==LBRACE||LA222_7==GEN||LA222_7==COLON||(LA222_7>=PLUS && LA222_7<=PIPE)||LA222_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index222_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA222_8 = input.LA(1);

                         
                        int index222_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_8==WS||(LA222_8>=NL && LA222_8<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_8==RBRACE) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA222_8==COLON) ) {s = 2;}

                         
                        input.seek(index222_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA222_11 = input.LA(1);

                         
                        int index222_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_11==RBRACE) && (synpred17_Css3())) {s = 10;}

                        else if ( (LA222_11==WS||(LA222_11>=NL && LA222_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA222_11==COLON) ) {s = 2;}

                         
                        input.seek(index222_11);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA222_12 = input.LA(1);

                         
                        int index222_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA222_12==LPAREN) && (synpred17_Css3())) {s = 13;}

                        else if ( (LA222_12==WS||(LA222_12>=NL && LA222_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA222_12==IDENT||LA222_12==COMMA||LA222_12==LBRACE||LA222_12==GEN||LA222_12==COLON||(LA222_12>=PLUS && LA222_12<=PIPE)||LA222_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index222_12);
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
    static final String DFA228_eotS =
        "\17\uffff";
    static final String DFA228_eofS =
        "\17\uffff";
    static final String DFA228_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA228_maxS =
        "\1\70\1\122\1\uffff\2\125\5\122\1\uffff\2\122\1\uffff\1\122";
    static final String DFA228_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA228_specialS =
        "\5\uffff\1\7\1\0\1\3\1\4\1\1\1\uffff\1\2\1\5\1\uffff\1\6}>";
    static final String[] DFA228_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\30\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\10"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\10"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\71\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\30\uffff"+
            "\2\16",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\71\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\30\uffff"+
            "\2\16"
    };

    static final short[] DFA228_eot = DFA.unpackEncodedString(DFA228_eotS);
    static final short[] DFA228_eof = DFA.unpackEncodedString(DFA228_eofS);
    static final char[] DFA228_min = DFA.unpackEncodedStringToUnsignedChars(DFA228_minS);
    static final char[] DFA228_max = DFA.unpackEncodedStringToUnsignedChars(DFA228_maxS);
    static final short[] DFA228_accept = DFA.unpackEncodedString(DFA228_acceptS);
    static final short[] DFA228_special = DFA.unpackEncodedString(DFA228_specialS);
    static final short[][] DFA228_transition;

    static {
        int numStates = DFA228_transitionS.length;
        DFA228_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA228_transition[i] = DFA.unpackEncodedString(DFA228_transitionS[i]);
        }
    }

    class DFA228 extends DFA {

        public DFA228(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 228;
            this.eot = DFA228_eot;
            this.eof = DFA228_eof;
            this.min = DFA228_min;
            this.max = DFA228_max;
            this.accept = DFA228_accept;
            this.special = DFA228_special;
            this.transition = DFA228_transition;
        }
        public String getDescription() {
            return "1092:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA228_6 = input.LA(1);

                         
                        int index228_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_6==WS||(LA228_6>=NL && LA228_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA228_6==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA228_6==COLON) ) {s = 2;}

                         
                        input.seek(index228_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA228_9 = input.LA(1);

                         
                        int index228_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_9==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA228_9==WS) ) {s = 9;}

                        else if ( ((LA228_9>=IDENT && LA228_9<=STRING)||LA228_9==LBRACE||LA228_9==COLON) ) {s = 2;}

                        else if ( ((LA228_9>=NL && LA228_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index228_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA228_11 = input.LA(1);

                         
                        int index228_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_11==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA228_11==WS||(LA228_11>=NL && LA228_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA228_11==COLON) ) {s = 2;}

                         
                        input.seek(index228_11);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA228_7 = input.LA(1);

                         
                        int index228_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_7==WS||(LA228_7>=NL && LA228_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA228_7==LPAREN) && (synpred20_Css3())) {s = 13;}

                        else if ( (LA228_7==IDENT||LA228_7==COMMA||LA228_7==LBRACE||LA228_7==GEN||LA228_7==COLON||(LA228_7>=PLUS && LA228_7<=PIPE)||LA228_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index228_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA228_8 = input.LA(1);

                         
                        int index228_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_8==WS||(LA228_8>=NL && LA228_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA228_8==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA228_8==IDENT||LA228_8==LBRACE||(LA228_8>=AND && LA228_8<=COLON)||(LA228_8>=MINUS && LA228_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index228_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA228_12 = input.LA(1);

                         
                        int index228_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_12==LPAREN) && (synpred20_Css3())) {s = 13;}

                        else if ( (LA228_12==WS||(LA228_12>=NL && LA228_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA228_12==IDENT||LA228_12==COMMA||LA228_12==LBRACE||LA228_12==GEN||LA228_12==COLON||(LA228_12>=PLUS && LA228_12<=PIPE)||LA228_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index228_12);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA228_14 = input.LA(1);

                         
                        int index228_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_14==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( (LA228_14==WS||(LA228_14>=NL && LA228_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA228_14==IDENT||LA228_14==LBRACE||(LA228_14>=AND && LA228_14<=COLON)||(LA228_14>=MINUS && LA228_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index228_14);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA228_5 = input.LA(1);

                         
                        int index228_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA228_5==WS) ) {s = 9;}

                        else if ( (LA228_5==RBRACE) && (synpred20_Css3())) {s = 10;}

                        else if ( ((LA228_5>=IDENT && LA228_5<=STRING)||LA228_5==LBRACE||LA228_5==COLON) ) {s = 2;}

                        else if ( ((LA228_5>=NL && LA228_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index228_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 228, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA231_eotS =
        "\4\uffff";
    static final String DFA231_eofS =
        "\4\uffff";
    static final String DFA231_minS =
        "\2\6\2\uffff";
    static final String DFA231_maxS =
        "\2\122\2\uffff";
    static final String DFA231_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA231_specialS =
        "\4\uffff}>";
    static final String[] DFA231_transitionS = {
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\30\uffff\2\1",
            "\1\3\6\uffff\1\2\1\uffff\1\3\1\uffff\1\3\2\uffff\1\3\2\uffff"+
            "\1\1\35\uffff\4\3\30\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA231_eot = DFA.unpackEncodedString(DFA231_eotS);
    static final short[] DFA231_eof = DFA.unpackEncodedString(DFA231_eofS);
    static final char[] DFA231_min = DFA.unpackEncodedStringToUnsignedChars(DFA231_minS);
    static final char[] DFA231_max = DFA.unpackEncodedStringToUnsignedChars(DFA231_maxS);
    static final short[] DFA231_accept = DFA.unpackEncodedString(DFA231_acceptS);
    static final short[] DFA231_special = DFA.unpackEncodedString(DFA231_specialS);
    static final short[][] DFA231_transition;

    static {
        int numStates = DFA231_transitionS.length;
        DFA231_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA231_transition[i] = DFA.unpackEncodedString(DFA231_transitionS[i]);
        }
    }

    class DFA231 extends DFA {

        public DFA231(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 231;
            this.eot = DFA231_eot;
            this.eof = DFA231_eof;
            this.min = DFA231_min;
            this.max = DFA231_max;
            this.accept = DFA231_accept;
            this.special = DFA231_special;
            this.transition = DFA231_transition;
        }
        public String getDescription() {
            return "()* loopback of 1097:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA230_eotS =
        "\17\uffff";
    static final String DFA230_eofS =
        "\17\uffff";
    static final String DFA230_minS =
        "\2\6\1\uffff\3\6\1\16\3\6\1\uffff\1\16\1\6\1\uffff\1\6";
    static final String DFA230_maxS =
        "\1\70\1\122\1\uffff\2\125\5\122\1\uffff\2\122\1\uffff\1\122";
    static final String DFA230_acceptS =
        "\2\uffff\1\2\7\uffff\1\1\2\uffff\1\1\1\uffff";
    static final String DFA230_specialS =
        "\5\uffff\1\7\1\0\1\3\1\4\1\1\1\uffff\1\2\1\5\1\uffff\1\6}>";
    static final String[] DFA230_transitionS = {
            "\1\2\10\uffff\1\2\1\uffff\1\2\2\uffff\1\2\40\uffff\1\2\1\1\2"+
            "\2",
            "\1\2\6\uffff\1\3\1\uffff\1\2\1\uffff\1\2\2\uffff\1\2\2\uffff"+
            "\1\2\35\uffff\4\2\30\uffff\2\2",
            "",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\10"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2",
            "\1\7\5\uffff\1\10\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff\1\2\24\uffff\10"+
            "\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6\1\2",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\71\uffff\2\13",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\30\uffff"+
            "\2\16",
            "\2\2\5\uffff\1\2\1\12\5\uffff\1\2\2\uffff\1\11\71\uffff\2\13",
            "",
            "\1\12\5\uffff\1\2\2\uffff\1\13\71\uffff\2\13",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\15\1\2\2\uffff\1"+
            "\14\32\uffff\13\2\1\uffff\1\2\22\uffff\2\14",
            "",
            "\1\2\6\uffff\1\2\1\12\6\2\2\uffff\1\16\35\uffff\4\2\30\uffff"+
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
            return "1099:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | AND | NOT ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA230_6 = input.LA(1);

                         
                        int index230_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_6==WS||(LA230_6>=NL && LA230_6<=COMMENT)) ) {s = 11;}

                        else if ( (LA230_6==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( (LA230_6==COLON) ) {s = 2;}

                         
                        input.seek(index230_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA230_9 = input.LA(1);

                         
                        int index230_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_9==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( (LA230_9==WS) ) {s = 9;}

                        else if ( ((LA230_9>=IDENT && LA230_9<=STRING)||LA230_9==LBRACE||LA230_9==COLON) ) {s = 2;}

                        else if ( ((LA230_9>=NL && LA230_9<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index230_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA230_11 = input.LA(1);

                         
                        int index230_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_11==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( (LA230_11==WS||(LA230_11>=NL && LA230_11<=COMMENT)) ) {s = 11;}

                        else if ( (LA230_11==COLON) ) {s = 2;}

                         
                        input.seek(index230_11);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA230_7 = input.LA(1);

                         
                        int index230_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_7==WS||(LA230_7>=NL && LA230_7<=COMMENT)) ) {s = 12;}

                        else if ( (LA230_7==LPAREN) && (synpred21_Css3())) {s = 13;}

                        else if ( (LA230_7==IDENT||LA230_7==COMMA||LA230_7==LBRACE||LA230_7==GEN||LA230_7==COLON||(LA230_7>=PLUS && LA230_7<=PIPE)||LA230_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index230_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA230_8 = input.LA(1);

                         
                        int index230_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_8==WS||(LA230_8>=NL && LA230_8<=COMMENT)) ) {s = 14;}

                        else if ( (LA230_8==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( (LA230_8==IDENT||LA230_8==LBRACE||(LA230_8>=AND && LA230_8<=COLON)||(LA230_8>=MINUS && LA230_8<=DOT)) ) {s = 2;}

                         
                        input.seek(index230_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA230_12 = input.LA(1);

                         
                        int index230_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_12==LPAREN) && (synpred21_Css3())) {s = 13;}

                        else if ( (LA230_12==WS||(LA230_12>=NL && LA230_12<=COMMENT)) ) {s = 12;}

                        else if ( (LA230_12==IDENT||LA230_12==COMMA||LA230_12==LBRACE||LA230_12==GEN||LA230_12==COLON||(LA230_12>=PLUS && LA230_12<=PIPE)||LA230_12==LESS_AND) ) {s = 2;}

                         
                        input.seek(index230_12);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA230_14 = input.LA(1);

                         
                        int index230_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_14==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( (LA230_14==WS||(LA230_14>=NL && LA230_14<=COMMENT)) ) {s = 14;}

                        else if ( (LA230_14==IDENT||LA230_14==LBRACE||(LA230_14>=AND && LA230_14<=COLON)||(LA230_14>=MINUS && LA230_14<=DOT)) ) {s = 2;}

                         
                        input.seek(index230_14);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA230_5 = input.LA(1);

                         
                        int index230_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA230_5==WS) ) {s = 9;}

                        else if ( (LA230_5==RBRACE) && (synpred21_Css3())) {s = 10;}

                        else if ( ((LA230_5>=IDENT && LA230_5<=STRING)||LA230_5==LBRACE||LA230_5==COLON) ) {s = 2;}

                        else if ( ((LA230_5>=NL && LA230_5<=COMMENT)) ) {s = 11;}

                         
                        input.seek(index230_5);
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
    static final String DFA241_eotS =
        "\11\uffff";
    static final String DFA241_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA241_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA241_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA241_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA241_specialS =
        "\11\uffff}>";
    static final String[] DFA241_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\111"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\163\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\30"+
            "\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3"
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
            return "376:17: synpred3_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA244_eotS =
        "\11\uffff";
    static final String DFA244_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA244_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA244_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA244_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA244_specialS =
        "\11\uffff}>";
    static final String[] DFA244_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\111"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\163\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\30"+
            "\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3"
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
            return "582:3: synpred4_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA247_eotS =
        "\11\uffff";
    static final String DFA247_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA247_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA247_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA247_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA247_specialS =
        "\11\uffff}>";
    static final String[] DFA247_transitionS = {
            "\1\3\1\uffff\1\2\6\3\2\uffff\5\3\1\uffff\40\3\1\2\1\1\2\2\111"+
            "\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3",
            "",
            "",
            "\1\3\1\uffff\7\3\2\uffff\163\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\10\3\1\6\35\3\1\10\1\7\2\10\30"+
            "\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\1\4\1\uffff\5\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\6\3\2\uffff\5\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3"
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
            return "584:3: synpred5_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x5FE00001D1541650L,0x0000000000700000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x5FE00001D1D41450L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x5FE00001D1541450L,0x0000000000700000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x5FE00001D1541050L,0x0000000000700000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x5FE00001D1541040L,0x0000000000700000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000800012L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000800080L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000800080L,0x0000000000060000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000800402L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem356 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem358 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem361 = new BitSet(new long[]{0x00000000008F0060L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem363 = new BitSet(new long[]{0x00000000000F0060L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem366 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem399 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem401 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem404 = new BitSet(new long[]{0x0000000000800800L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem406 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_importItem410 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem412 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem415 = new BitSet(new long[]{0x00000000000F0060L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem418 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media436 = new BitSet(new long[]{0x01E00000009FA040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_media438 = new BitSet(new long[]{0x01E00000001FA040L});
    public static final BitSet FOLLOW_scss_mq_interpolation_expression_in_media493 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_media495 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_mediaQueryList_in_media529 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media558 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media560 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_declaration_in_media646 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_media648 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media650 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_rule_in_media672 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media675 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_page_in_media696 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media699 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_fontFace_in_media720 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media723 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media744 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media747 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_media_in_media770 = new BitSet(new long[]{0x5FE0000151D45040L,0x0000000000360000L});
    public static final BitSet FOLLOW_ws_in_media772 = new BitSet(new long[]{0x5FE0000151545040L,0x0000000000300000L});
    public static final BitSet FOLLOW_RBRACE_in_media816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList832 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList836 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList838 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList841 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery860 = new BitSet(new long[]{0x0000000000870040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery862 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery869 = new BitSet(new long[]{0x0000000000808002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery871 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery876 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery878 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery881 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery889 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery893 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery895 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery898 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression953 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression955 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression958 = new BitSet(new long[]{0x0000000000B00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression960 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression965 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_mediaExpression967 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_mediaExpression970 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression975 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression977 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body1009 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_body1011 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_bodyItem1036 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem1060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem1072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem1096 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem1110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule1147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule1191 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1193 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule1198 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule1200 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule1215 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule1227 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document1253 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document1255 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1260 = new BitSet(new long[]{0x0000000000802800L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document1262 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document1268 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document1270 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document1273 = new BitSet(new long[]{0x0000000000802800L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document1275 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document1282 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_moz_document1284 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_body_in_moz_document1289 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document1294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1335 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1337 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1340 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1342 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1347 = new BitSet(new long[]{0x0000000020804040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1349 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1356 = new BitSet(new long[]{0x0000000020804040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1358 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1378 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1380 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1385 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1388 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1391 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1395 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1413 = new BitSet(new long[]{0x0000000000800802L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1425 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1428 = new BitSet(new long[]{0x0000000020800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1430 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1433 = new BitSet(new long[]{0x0000000000800802L,0x0000000000060000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1462 = new BitSet(new long[]{0x0000000000902040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1464 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1469 = new BitSet(new long[]{0x0000000000902000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1471 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1478 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1480 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1493 = new BitSet(new long[]{0x09E1FFFE00C45060L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1495 = new BitSet(new long[]{0x09E1FFFE00445060L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1550 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1552 = new BitSet(new long[]{0x0000000000804020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1554 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1560 = new BitSet(new long[]{0x09E1FFFE00C45060L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1562 = new BitSet(new long[]{0x09E1FFFE00445060L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1566 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1568 = new BitSet(new long[]{0x0000000000804020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1570 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1606 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1608 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1611 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1613 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1624 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1626 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1629 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1633 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1664 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fontFace1666 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1677 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_fontFace1679 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1682 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1686 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1711 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_margin1713 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1716 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_margin1718 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1721 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_margin1723 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1954 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator2006 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator2008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator2017 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator2019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator2028 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator2030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property2091 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_property2095 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_property2101 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_property2104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule2148 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule2181 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule2204 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_rule2206 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule2209 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_rule2223 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule2233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations2367 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2369 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2371 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2415 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2417 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_declarations2454 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2456 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_media_in_declarations2495 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2497 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2536 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2538 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_declaration_in_declarations2582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2643 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2660 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2663 = new BitSet(new long[]{0x5FE0000000940040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2665 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2668 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2695 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_combinator_in_selector2698 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2700 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2733 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2740 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2742 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2761 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2763 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2875 = new BitSet(new long[]{0x5800000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2881 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2901 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2905 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId3004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId3010 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId3012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass3040 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass3042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute3114 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute3121 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3124 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute3135 = new BitSet(new long[]{0x8000000000800000L,0x000000000006003FL});
    public static final BitSet FOLLOW_ws_in_slAttribute3137 = new BitSet(new long[]{0x8000000000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_set_in_slAttribute3179 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3359 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute3378 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060020L});
    public static final BitSet FOLLOW_ws_in_slAttribute3396 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3515 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo3579 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3636 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3639 = new BitSet(new long[]{0x08A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_pseudo3641 = new BitSet(new long[]{0x08A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_pseudo3646 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo3650 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3734 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3736 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3739 = new BitSet(new long[]{0x5FE0000000B40040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_pseudo3741 = new BitSet(new long[]{0x5FE0000000340040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3744 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3791 = new BitSet(new long[]{0x09E0000000441040L,0x0000000000100000L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_declaration3837 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_property_in_declaration3858 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration3871 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_declaration3873 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3876 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_prio_in_declaration3879 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_declaration3881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue3964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4002 = new BitSet(new long[]{0xF7FDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_expressionPredicate4031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI4149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio4204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression4225 = new BitSet(new long[]{0x00A6000020C419C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_operator_in_expression4230 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_expression4232 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_expression4237 = new BitSet(new long[]{0x00A6000020C419C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_unaryOperator_in_term4262 = new BitSet(new long[]{0x0080000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_term4264 = new BitSet(new long[]{0x00800000204411C0L,0x000000000011FF80L});
    public static final BitSet FOLLOW_set_in_term4288 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_term4488 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_term4496 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_term4504 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_term4512 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_term4520 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_term4532 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_term4542 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_term4554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4570 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_function4572 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function4577 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4579 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_function4589 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function4607 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function4610 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4612 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttribute_in_function4615 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function4636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4684 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName4686 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4690 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4693 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4695 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4718 = new BitSet(new long[]{0x8000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4720 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4723 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4725 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4743 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4746 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4748 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4801 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4849 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4851 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4854 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4856 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4859 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4888 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4890 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4893 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4895 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4898 = new BitSet(new long[]{0x0000000000000020L,0x0000000000080000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4901 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4903 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable4941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable4973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression4997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5017 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp5031 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5033 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5036 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp5049 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp5051 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp5054 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5087 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp5100 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5102 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5105 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp5119 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp5121 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp5124 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5157 = new BitSet(new long[]{0x00A4000020C411C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_atomExp5164 = new BitSet(new long[]{0x00A4000020C411C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp5178 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5180 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp5183 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp5185 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp5187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term5225 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_cp_term5425 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term5433 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_cp_term5441 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_cp_term5449 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term5457 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_cp_term5465 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term5473 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_term5485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration5516 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5518 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5520 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5523 = new BitSet(new long[]{0x0000000000601000L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5525 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5528 = new BitSet(new long[]{0x0000000000800002L,0x0000000002060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5530 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5534 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5553 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5555 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5557 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5559 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5563 = new BitSet(new long[]{0x0000000000601000L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5565 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5568 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call5612 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5614 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call5636 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5638 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5640 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5653 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call5656 = new BitSet(new long[]{0x00A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call5658 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call5661 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5665 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call5668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name5697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5733 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args5737 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args5745 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5748 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5790 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5794 = new BitSet(new long[]{0x0000000000C01000L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5804 = new BitSet(new long[]{0x0000000000401000L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5807 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5813 = new BitSet(new long[]{0x0000000000800000L,0x0000000001860000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5823 = new BitSet(new long[]{0x0000000000000000L,0x0000000001800000L});
    public static final BitSet FOLLOW_set_in_less_args_list5826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg5880 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5884 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_arg5886 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg5889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded5915 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5917 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5920 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded5924 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5932 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5935 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition5965 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5967 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition5976 = new BitSet(new long[]{0x0000000000C01040L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_condition5978 = new BitSet(new long[]{0x0000000000401040L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition6004 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition6006 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition6037 = new BitSet(new long[]{0x8008000000A00000L,0x000000001C060000L});
    public static final BitSet FOLLOW_ws_in_less_condition6040 = new BitSet(new long[]{0x8008000000800000L,0x000000001C060000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition6043 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_condition6045 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition6048 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition6077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition6103 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6105 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition6108 = new BitSet(new long[]{0x0000000000C01000L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6110 = new BitSet(new long[]{0x0000000000401000L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition6113 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition6115 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition6118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name6140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6239 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6267 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression6324 = new BitSet(new long[]{0x01E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6363 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6399 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6497 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6525 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression6578 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6617 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6653 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6747 = new BitSet(new long[]{0x01E0000000928042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression6775 = new BitSet(new long[]{0x01E0000000928042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_mq_interpolation_expression6841 = new BitSet(new long[]{0x01E0000000128040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_mq_interpolation_expression6880 = new BitSet(new long[]{0x01E0000000928042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_mq_interpolation_expression6916 = new BitSet(new long[]{0x01E0000000928042L,0x0000000000060000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var7001 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var7003 = new BitSet(new long[]{0x0000000000C01040L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7005 = new BitSet(new long[]{0x0000000000401040L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var7010 = new BitSet(new long[]{0x0000000000804000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var7014 = new BitSet(new long[]{0x0000000000804000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var7018 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var7021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties7065 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties7067 = new BitSet(new long[]{0x00A4000020CC31C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7069 = new BitSet(new long[]{0x00A4000020CC31C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties7072 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties7075 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties7077 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties7080 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties7082 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties7084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css3475 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred1_Css3487 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css3489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQueryList_in_synpred2_Css3526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3612 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3624 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred3_Css3626 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_SEMI_in_synpred3_Css3636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred3_Css3640 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred3_Css3642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32333 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32345 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32347 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_SEMI_in_synpred4_Css32357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred4_Css32361 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred4_Css32363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32381 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32393 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32395 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred5_Css32409 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred5_Css32411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred6_Css32441 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred6_Css32451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred7_Css32573 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_RBRACE_in_synpred7_Css32579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_Css32625 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred8_Css32637 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred8_Css32639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred9_Css32737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred10_Css32758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred11_Css32863 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred11_Css32872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred12_Css33812 = new BitSet(new long[]{0xFFFFFFFFFFEFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred12_Css33830 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Css33832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred13_Css33918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_synpred14_Css34529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred15_Css35161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred16_Css36234 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred16_Css36236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred17_Css36358 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred17_Css36360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred18_Css36492 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred18_Css36494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred19_Css36612 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred19_Css36614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred20_Css36742 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred20_Css36744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred21_Css36875 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred21_Css36877 = new BitSet(new long[]{0x0000000000000002L});

}