// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-01 14:16:38

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "SEMI", "IDENT", "STRING", "URI", "CHARSET_SYM", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "LPAREN", "COLON", "RPAREN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH_SYMBOL", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_DEFAULT", "SASS_VAR", "SASS_MIXIN", "SASS_INCLUDE", "LESS_DOTS", "LESS_REST", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "LINE_COMMENT"
    };
    public static final int EOF=-1;
    public static final int NAMESPACE_SYM=4;
    public static final int SEMI=5;
    public static final int IDENT=6;
    public static final int STRING=7;
    public static final int URI=8;
    public static final int CHARSET_SYM=9;
    public static final int IMPORT_SYM=10;
    public static final int MEDIA_SYM=11;
    public static final int LBRACE=12;
    public static final int RBRACE=13;
    public static final int COMMA=14;
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
        "invalidRule", "moz_document", "cp_multiplyExp", "importItem", "moz_document_function", 
        "cp_variable_declaration", "pseudo", "imports", "scss_declaration_interpolation_expression", 
        "mediaQueryList", "body", "scss_nested_properties", "less_mixin_guarded", 
        "declaration", "synpred13_Css3", "synpred8_Css3", "namespacePrefix", 
        "counterStyle", "scss_interpolation_expression_var", "margin_sym", 
        "synpred4_Css3", "generic_at_rule", "synpred11_Css3", "term", "declarations", 
        "simpleSelectorSequence", "cp_mixin_call_args", "less_fn_name", 
        "cssClass", "esPred", "fnAttributeValue", "cp_variable", "propertyValue", 
        "less_args_list", "slAttributeValue", "elementName", "syncTo_RBRACE", 
        "function", "synpred3_Css3", "synpred7_Css3", "webkitKeyframes", 
        "cp_mixin_call", "elementSubsequent", "media", "styleSheet", "combinator", 
        "mediaFeature", "synpred10_Css3", "cp_mixin_declaration", "functionName", 
        "synpred9_Css3", "namespace", "atRuleId", "syncToDeclarationsRule", 
        "bodyItem", "namespaces", "namespacePrefixName", "prio", "slAttribute", 
        "less_arg", "mediaQuery", "ws", "scss_selector_interpolation_expression", 
        "mediaType", "synpred15_Css3", "synpred12_Css3", "property", "rule", 
        "fnAttributeName", "cp_additionExp", "synpred1_Css3", "mediaExpression", 
        "syncToFollow", "less_condition_operator", "cssId", "expressionPredicate", 
        "cp_expression", "charSet", "webkitKeyframeSelectors", "pseudoPage", 
        "slAttributeName", "expression", "mediaQueryOperator", "typeSelector", 
        "cp_term", "cp_mixin_name", "synpred6_Css3", "hexColor", "cp_atomExp", 
        "operator", "fontFace", "synpred14_Css3", "webkitKeyframesBlock", 
        "fnAttribute", "unaryOperator", "resourceIdentifier", "margin", 
        "page", "less_condition", "charSetValue", "vendorAtRule", "less_function_in_condition", 
        "synpred2_Css3", "synpred5_Css3", "selector", "selectorsGroup", 
        "syncTo_SEMI"
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
            false, false, false, true, false, false, false, false, true, 
            false, false, true, false, true, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
            false, false, false, true, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, true, false, false, false, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: importItem : IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI ;
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:9: IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI
            {
            dbg.location(358,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem347); if (state.failed) return ;
            dbg.location(358,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:20: ws
                    {
                    dbg.location(358,20);
                    pushFollow(FOLLOW_ws_in_importItem349);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(358,24);
            pushFollow(FOLLOW_resourceIdentifier_in_importItem352);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(358,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:43: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:43: ws
                    {
                    dbg.location(358,43);
                    pushFollow(FOLLOW_ws_in_importItem354);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(358,47);
            pushFollow(FOLLOW_mediaQueryList_in_importItem357);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(358,62);
            match(input,SEMI,FOLLOW_SEMI_in_importItem359); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(359, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(360, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE
            {
            dbg.location(361,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media375); if (state.failed) return ;
            dbg.location(361,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:17: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:17: ws
                    {
                    dbg.location(361,17);
                    pushFollow(FOLLOW_ws_in_media377);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(361,21);
            pushFollow(FOLLOW_mediaQueryList_in_media380);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(362,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media390); if (state.failed) return ;
            dbg.location(362,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:16: ws
                    {
                    dbg.location(362,16);
                    pushFollow(FOLLOW_ws_in_media392);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}

            dbg.location(363,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:13: ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( (LA23_0==IDENT||LA23_0==GEN||LA23_0==COLON||LA23_0==AT_IDENT||LA23_0==MOZ_DOCUMENT_SYM||LA23_0==WEBKIT_KEYFRAMES_SYM||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||(LA23_0>=MINUS && LA23_0<=PIPE)||LA23_0==LESS_AND||LA23_0==SASS_MIXIN) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:15: ( rule | page | fontFace | vendorAtRule ) ( ws )?
            	    {
            	    dbg.location(363,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:15: ( rule | page | fontFace | vendorAtRule )
            	    int alt21=4;
            	    try { dbg.enterSubRule(21);
            	    try { dbg.enterDecision(21, decisionCanBacktrack[21]);

            	    switch ( input.LA(1) ) {
            	    case IDENT:
            	    case GEN:
            	    case COLON:
            	    case MINUS:
            	    case HASH_SYMBOL:
            	    case HASH:
            	    case DOT:
            	    case LBRACKET:
            	    case DCOLON:
            	    case STAR:
            	    case PIPE:
            	    case LESS_AND:
            	    case SASS_MIXIN:
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:17: rule
            	            {
            	            dbg.location(363,17);
            	            pushFollow(FOLLOW_rule_in_media411);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:24: page
            	            {
            	            dbg.location(363,24);
            	            pushFollow(FOLLOW_page_in_media415);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:31: fontFace
            	            {
            	            dbg.location(363,31);
            	            pushFollow(FOLLOW_fontFace_in_media419);
            	            fontFace();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 4 :
            	            dbg.enterAlt(4);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:42: vendorAtRule
            	            {
            	            dbg.location(363,42);
            	            pushFollow(FOLLOW_vendorAtRule_in_media423);
            	            vendorAtRule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(363,57);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:57: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:57: ws
            	            {
            	            dbg.location(363,57);
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

            dbg.location(364,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media441); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(365, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(367, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(368,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            int LA26_0 = input.LA(1);

            if ( (LA26_0==IDENT||(LA26_0>=ONLY && LA26_0<=LPAREN)) ) {
                alt26=1;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(368,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList457);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(368,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:17: ( COMMA ( ws )? mediaQuery )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(368,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList461); if (state.failed) return ;
                    	    dbg.location(368,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:25: ws
                    	            {
                    	            dbg.location(368,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList463);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(368,29);
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
        dbg.location(369, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(372,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( mediaQueryOperator ( ws )? )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(372,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery485);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(372,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:24: ws
                                    {
                                    dbg.location(372,24);
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

                    dbg.location(372,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery494);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(372,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:42: ws
                            {
                            dbg.location(372,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery496);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(29);}

                    dbg.location(372,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:46: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(372,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery501); if (state.failed) return ;
                    	    dbg.location(372,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:52: ws
                    	            {
                    	            dbg.location(372,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery503);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(372,56);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(373,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery514);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(373,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:20: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(373,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery518); if (state.failed) return ;
                    	    dbg.location(373,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:26: ws
                    	            {
                    	            dbg.location(373,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery520);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(373,30);
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
        dbg.location(374, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(376, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(377,3);
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
        dbg.location(378, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(381,2);
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
        dbg.location(382, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(385,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression578); if (state.failed) return ;
            dbg.location(385,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:14: ws
                    {
                    dbg.location(385,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression580);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(385,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression583);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(385,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:31: ws
                    {
                    dbg.location(385,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression585);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(385,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:35: ( COLON ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:37: COLON ( ws )? expression
                    {
                    dbg.location(385,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression590); if (state.failed) return ;
                    dbg.location(385,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:43: ws
                            {
                            dbg.location(385,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression592);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(385,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression595);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(385,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression600); if (state.failed) return ;
            dbg.location(385,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:68: ws
                    {
                    dbg.location(385,68);
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
        dbg.location(386, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:4: IDENT
            {
            dbg.location(389,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature618); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( bodyItem ( ws )? )+
            {
            dbg.location(393,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( bodyItem ( ws )? )+
            int cnt41=0;
            try { dbg.enterSubRule(41);

            loop41:
            do {
                int alt41=2;
                try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                int LA41_0 = input.LA(1);

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||LA41_0==GEN||LA41_0==COLON||LA41_0==AT_IDENT||LA41_0==MOZ_DOCUMENT_SYM||LA41_0==WEBKIT_KEYFRAMES_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||(LA41_0>=MINUS && LA41_0<=PIPE)||LA41_0==LESS_AND||(LA41_0>=SASS_VAR && LA41_0<=SASS_INCLUDE)) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: bodyItem ( ws )?
            	    {
            	    dbg.location(393,4);
            	    pushFollow(FOLLOW_bodyItem_in_body634);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(393,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:13: ws
            	            {
            	            dbg.location(393,13);
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
        dbg.location(394, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call )
            int alt42=8;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:6: rule
                    {
                    dbg.location(398,6);
                    pushFollow(FOLLOW_rule_in_bodyItem661);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:11: media
                    {
                    dbg.location(399,11);
                    pushFollow(FOLLOW_media_in_bodyItem673);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:11: page
                    {
                    dbg.location(400,11);
                    pushFollow(FOLLOW_page_in_bodyItem685);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:11: counterStyle
                    {
                    dbg.location(401,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem697);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:11: fontFace
                    {
                    dbg.location(402,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem709);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:11: vendorAtRule
                    {
                    dbg.location(403,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem721);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:11: {...}? cp_variable_declaration
                    {
                    dbg.location(404,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(404,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem735);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:11: {...}? cp_mixin_call
                    {
                    dbg.location(405,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(405,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem749);
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
        dbg.location(406, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(414, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:1: ( moz_document | webkitKeyframes | generic_at_rule )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:3: moz_document
                    {
                    dbg.location(415,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule772);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:18: webkitKeyframes
                    {
                    dbg.location(415,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule776);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:36: generic_at_rule
                    {
                    dbg.location(415,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule780);
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
        dbg.location(415, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(418,2);
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
        dbg.location(420, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(422, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(423,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule816); if (state.failed) return ;
            dbg.location(423,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:16: WS
            	    {
            	    dbg.location(423,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule818); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(423,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:20: ( atRuleId ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:22: atRuleId ( WS )*
                    {
                    dbg.location(423,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule823);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(423,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:31: WS
                    	    {
                    	    dbg.location(423,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule825); if (state.failed) return ;

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

            dbg.location(424,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule840); if (state.failed) return ;
            dbg.location(425,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule852);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(426,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule862); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(427, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(430,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document878); if (state.failed) return ;
            dbg.location(430,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:19: ws
                    {
                    dbg.location(430,19);
                    pushFollow(FOLLOW_ws_in_moz_document880);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(430,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:25: moz_document_function ( ws )?
            {
            dbg.location(430,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document885);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(430,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:47: ws
                    {
                    dbg.location(430,47);
                    pushFollow(FOLLOW_ws_in_moz_document887);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}


            }

            dbg.location(430,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(430,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document893); if (state.failed) return ;
            	    dbg.location(430,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:60: ws
            	            {
            	            dbg.location(430,60);
            	            pushFollow(FOLLOW_ws_in_moz_document895);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(430,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document898);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(430,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:86: ws
            	            {
            	            dbg.location(430,86);
            	            pushFollow(FOLLOW_ws_in_moz_document900);
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

            dbg.location(431,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document907); if (state.failed) return ;
            dbg.location(431,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:9: ws
                    {
                    dbg.location(431,9);
                    pushFollow(FOLLOW_ws_in_moz_document909);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(432,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:3: ( body )?
            int alt53=2;
            try { dbg.enterSubRule(53);
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            int LA53_0 = input.LA(1);

            if ( (LA53_0==IDENT||LA53_0==MEDIA_SYM||LA53_0==GEN||LA53_0==COLON||LA53_0==AT_IDENT||LA53_0==MOZ_DOCUMENT_SYM||LA53_0==WEBKIT_KEYFRAMES_SYM||(LA53_0>=PAGE_SYM && LA53_0<=FONT_FACE_SYM)||(LA53_0>=MINUS && LA53_0<=PIPE)||LA53_0==LESS_AND||(LA53_0>=SASS_VAR && LA53_0<=SASS_INCLUDE)) ) {
                alt53=1;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:3: body
                    {
                    dbg.location(432,3);
                    pushFollow(FOLLOW_body_in_moz_document914);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(433,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document919); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "moz_document");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document"


    // $ANTLR start "moz_document_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(437,2);
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
        dbg.location(439, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(442, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(444,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes960); if (state.failed) return ;
            dbg.location(444,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:23: ws
                    {
                    dbg.location(444,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes962);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(444,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes965);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(444,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:36: ws
                    {
                    dbg.location(444,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes967);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(445,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes972); if (state.failed) return ;
            dbg.location(445,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:9: ws
                    {
                    dbg.location(445,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes974);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(446,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:3: ( webkitKeyframesBlock ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(446,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes981);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(446,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: ws
            	            {
            	            dbg.location(446,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes983);
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

            dbg.location(447,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes990); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(450, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(452,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1003);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(452,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:26: ws
                    {
                    dbg.location(452,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1005);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(454,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1010); if (state.failed) return ;
            dbg.location(454,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:10: ws
                    {
                    dbg.location(454,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1013);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(454,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1016);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(455,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1020);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(456,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1023); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(457, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(459, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(461,2);
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

            dbg.location(461,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(461,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:27: ws
            	            {
            	            dbg.location(461,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1050);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(461,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1053); if (state.failed) return ;
            	    dbg.location(461,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:37: ws
            	            {
            	            dbg.location(461,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1055);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(62);}

            	    dbg.location(461,41);
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
        dbg.location(462, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(464, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(465,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1087); if (state.failed) return ;
            dbg.location(465,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:16: ws
                    {
                    dbg.location(465,16);
                    pushFollow(FOLLOW_ws_in_page1089);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(465,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:20: ( IDENT ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:22: IDENT ( ws )?
                    {
                    dbg.location(465,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1094); if (state.failed) return ;
                    dbg.location(465,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:28: ws
                            {
                            dbg.location(465,28);
                            pushFollow(FOLLOW_ws_in_page1096);
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

            dbg.location(465,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:35: ( pseudoPage ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:36: pseudoPage ( ws )?
                    {
                    dbg.location(465,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1103);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(465,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:47: ws
                            {
                            dbg.location(465,47);
                            pushFollow(FOLLOW_ws_in_page1105);
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

            dbg.location(466,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1118); if (state.failed) return ;
            dbg.location(466,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:16: ws
                    {
                    dbg.location(466,16);
                    pushFollow(FOLLOW_ws_in_page1120);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(470,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:13: ( declaration | margin ( ws )? )?
            int alt71=3;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT||LA71_0==MEDIA_SYM||LA71_0==GEN||LA71_0==AT_IDENT||(LA71_0>=MINUS && LA71_0<=DOT)||LA71_0==STAR||LA71_0==SASS_VAR) ) {
                alt71=1;
            }
            else if ( ((LA71_0>=TOPLEFTCORNER_SYM && LA71_0<=RIGHTBOTTOM_SYM)) ) {
                alt71=2;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:14: declaration
                    {
                    dbg.location(470,14);
                    pushFollow(FOLLOW_declaration_in_page1175);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:26: margin ( ws )?
                    {
                    dbg.location(470,26);
                    pushFollow(FOLLOW_margin_in_page1177);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(470,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:33: ws
                            {
                            dbg.location(470,33);
                            pushFollow(FOLLOW_ws_in_page1179);
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

            dbg.location(470,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(470,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1185); if (state.failed) return ;
            	    dbg.location(470,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:45: ws
            	            {
            	            dbg.location(470,45);
            	            pushFollow(FOLLOW_ws_in_page1187);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(470,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:49: ( declaration | margin ( ws )? )?
            	    int alt74=3;
            	    try { dbg.enterSubRule(74);
            	    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            	    int LA74_0 = input.LA(1);

            	    if ( (LA74_0==IDENT||LA74_0==MEDIA_SYM||LA74_0==GEN||LA74_0==AT_IDENT||(LA74_0>=MINUS && LA74_0<=DOT)||LA74_0==STAR||LA74_0==SASS_VAR) ) {
            	        alt74=1;
            	    }
            	    else if ( ((LA74_0>=TOPLEFTCORNER_SYM && LA74_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt74=2;
            	    }
            	    } finally {dbg.exitDecision(74);}

            	    switch (alt74) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:50: declaration
            	            {
            	            dbg.location(470,50);
            	            pushFollow(FOLLOW_declaration_in_page1191);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:62: margin ( ws )?
            	            {
            	            dbg.location(470,62);
            	            pushFollow(FOLLOW_margin_in_page1193);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(470,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:69: ws
            	                    {
            	                    dbg.location(470,69);
            	                    pushFollow(FOLLOW_ws_in_page1195);
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

            dbg.location(471,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1210); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(474, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(475,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1231); if (state.failed) return ;
            dbg.location(475,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:25: ws
                    {
                    dbg.location(475,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1233);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(475,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1236); if (state.failed) return ;
            dbg.location(475,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:35: ws
                    {
                    dbg.location(475,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1238);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(476,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1249); if (state.failed) return ;
            dbg.location(476,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:16: ws
                    {
                    dbg.location(476,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1251);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(476,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1254);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(477,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1258);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(478,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1268); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(479, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(481, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(482,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1289); if (state.failed) return ;
            dbg.location(482,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:21: ws
                    {
                    dbg.location(482,21);
                    pushFollow(FOLLOW_ws_in_fontFace1291);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(483,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1302); if (state.failed) return ;
            dbg.location(483,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:16: ws
                    {
                    dbg.location(483,16);
                    pushFollow(FOLLOW_ws_in_fontFace1304);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(483,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1307);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(484,3);
            pushFollow(FOLLOW_declarations_in_fontFace1311);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(485,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1321); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(486, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(488, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(489,4);
            pushFollow(FOLLOW_margin_sym_in_margin1336);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(489,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:15: ws
                    {
                    dbg.location(489,15);
                    pushFollow(FOLLOW_ws_in_margin1338);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(489,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1341); if (state.failed) return ;
            dbg.location(489,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:26: ws
                    {
                    dbg.location(489,26);
                    pushFollow(FOLLOW_ws_in_margin1343);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(489,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1346);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(489,53);
            pushFollow(FOLLOW_declarations_in_margin1348);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(489,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1350); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(490, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(492, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(493,2);
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
        dbg.location(510, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(512, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:7: COLON IDENT
            {
            dbg.location(513,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1579); if (state.failed) return ;
            dbg.location(513,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1581); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(516, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(517,5);
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
        dbg.location(519, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(521, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
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
            case HASH_SYMBOL:
            case HASH:
            case DOT:
            case LBRACKET:
            case DCOLON:
            case STAR:
            case PIPE:
            case LESS_AND:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:7: PLUS ( ws )?
                    {
                    dbg.location(522,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1631); if (state.failed) return ;
                    dbg.location(522,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:12: ws
                            {
                            dbg.location(522,12);
                            pushFollow(FOLLOW_ws_in_combinator1633);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:7: GREATER ( ws )?
                    {
                    dbg.location(523,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1642); if (state.failed) return ;
                    dbg.location(523,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:15: ws
                            {
                            dbg.location(523,15);
                            pushFollow(FOLLOW_ws_in_combinator1644);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:7: TILDE ( ws )?
                    {
                    dbg.location(524,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1653); if (state.failed) return ;
                    dbg.location(524,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:13: ws
                            {
                            dbg.location(524,13);
                            pushFollow(FOLLOW_ws_in_combinator1655);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: 
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
        dbg.location(526, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(528, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(529,5);
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
        dbg.location(531, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(533, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(534,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: ( IDENT | GEN | {...}? cp_variable )
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
            case SASS_VAR:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:8: IDENT
                    {
                    dbg.location(534,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property1716); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:16: GEN
                    {
                    dbg.location(534,16);
                    match(input,GEN,FOLLOW_GEN_in_property1720); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:22: {...}? cp_variable
                    {
                    dbg.location(534,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(534,51);
                    pushFollow(FOLLOW_cp_variable_in_property1726);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(534,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:64: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:64: ws
                    {
                    dbg.location(534,64);
                    pushFollow(FOLLOW_ws_in_property1729);
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
        dbg.location(535, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(537, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(538,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(539,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(539,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(539,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule1773);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:13: ( selectorsGroup )
                    {
                    dbg.location(541,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:15: selectorsGroup
                    {
                    dbg.location(541,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule1806);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(544,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1829); if (state.failed) return ;
            dbg.location(544,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:16: ws
                    {
                    dbg.location(544,16);
                    pushFollow(FOLLOW_ws_in_rule1831);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(544,20);
            pushFollow(FOLLOW_syncToFollow_in_rule1834);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(545,13);
            pushFollow(FOLLOW_declarations_in_rule1848);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(546,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1858); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(547, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(555, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(557,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )*
            try { dbg.enterSubRule(95);

            loop95:
            do {
                int alt95=5;
                try { dbg.enterDecision(95, decisionCanBacktrack[95]);

                try {
                    isCyclicDecision = true;
                    alt95 = dfa95.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(95);}

                switch (alt95) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(561,120);
            	    pushFollow(FOLLOW_declaration_in_declarations1992);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(561,132);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1994); if (state.failed) return ;
            	    dbg.location(561,137);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:137: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:137: ws
            	            {
            	            dbg.location(561,137);
            	            pushFollow(FOLLOW_ws_in_declarations1996);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(91);}


            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(563,122);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2040);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(563,145);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:145: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:145: ws
            	            {
            	            dbg.location(563,145);
            	            pushFollow(FOLLOW_ws_in_declarations2042);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(92);}


            	    }
            	    break;
            	case 3 :
            	    dbg.enterAlt(3);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(565,50);
            	    pushFollow(FOLLOW_rule_in_declarations2079);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(565,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:55: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:55: ws
            	            {
            	            dbg.location(565,55);
            	            pushFollow(FOLLOW_ws_in_declarations2081);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(93);}


            	    }
            	    break;
            	case 4 :
            	    dbg.enterAlt(4);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(567,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(567,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2120);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(567,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:60: ws
            	            {
            	            dbg.location(567,60);
            	            pushFollow(FOLLOW_ws_in_declarations2122);
            	            ws();

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

            dbg.location(571,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt96=2;
            try { dbg.enterSubRule(96);
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==STAR) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==HASH_SYMBOL) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==IDENT) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==MINUS||(LA96_0>=HASH && LA96_0<=DOT)) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==GEN) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==MEDIA_SYM||LA96_0==AT_IDENT) && (synpred4_Css3())) {
                alt96=1;
            }
            else if ( (LA96_0==SASS_VAR) && (synpred4_Css3())) {
                alt96=1;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(571,36);
                    pushFollow(FOLLOW_declaration_in_declarations2166);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}


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
            dbg.exitRule(getGrammarFileName(), "declarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarations"


    // $ANTLR start "selectorsGroup"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(574, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt100=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(577,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2227);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(577,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:99: ws
                            {
                            dbg.location(577,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup2229);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(97);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(579,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup2244);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(579,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(99);

                    loop99:
                    do {
                        int alt99=2;
                        try { dbg.enterDecision(99, decisionCanBacktrack[99]);

                        int LA99_0 = input.LA(1);

                        if ( (LA99_0==COMMA) ) {
                            alt99=1;
                        }


                        } finally {dbg.exitDecision(99);}

                        switch (alt99) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(579,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2247); if (state.failed) return ;
                    	    dbg.location(579,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:25: ws
                    	            {
                    	            dbg.location(579,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup2249);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(98);}

                    	    dbg.location(579,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup2252);
                    	    selector();

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
                    break;

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
            dbg.exitRule(getGrammarFileName(), "selectorsGroup");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selectorsGroup"


    // $ANTLR start "selector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(582, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(583,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2279);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(583,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(101);

            loop101:
            do {
                int alt101=2;
                try { dbg.enterDecision(101, decisionCanBacktrack[101]);

                int LA101_0 = input.LA(1);

                if ( (LA101_0==IDENT||LA101_0==GEN||LA101_0==COLON||(LA101_0>=PLUS && LA101_0<=TILDE)||(LA101_0>=HASH_SYMBOL && LA101_0<=PIPE)||LA101_0==LESS_AND) ) {
                    alt101=1;
                }


                } finally {dbg.exitDecision(101);}

                switch (alt101) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(583,31);
            	    pushFollow(FOLLOW_combinator_in_selector2282);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(583,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2284);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop101;
                }
            } while (true);
            } finally {dbg.exitSubRule(101);}


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
            dbg.exitRule(getGrammarFileName(), "selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "simpleSelectorSequence"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(587, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt106=2;
            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

            int LA106_0 = input.LA(1);

            if ( (LA106_0==IDENT||LA106_0==GEN||(LA106_0>=STAR && LA106_0<=PIPE)||LA106_0==LESS_AND) ) {
                alt106=1;
            }
            else if ( (LA106_0==COLON||(LA106_0>=HASH_SYMBOL && LA106_0<=DCOLON)) ) {
                alt106=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(590,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(590,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2317);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(590,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(103);

                    loop103:
                    do {
                        int alt103=2;
                        try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                        try {
                            isCyclicDecision = true;
                            alt103 = dfa103.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(103);}

                        switch (alt103) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(590,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2324);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(590,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:46: ws
                    	            {
                    	            dbg.location(590,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2326);
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
                    	    break loop103;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(103);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(592,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(592,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt105=0;
                    try { dbg.enterSubRule(105);

                    loop105:
                    do {
                        int alt105=2;
                        try { dbg.enterDecision(105, decisionCanBacktrack[105]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA105_2 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA105_3 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA105_4 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA105_5 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA105_6 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(105);}

                        switch (alt105) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(592,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2345);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(592,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:33: ws
                    	            {
                    	            dbg.location(592,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2347);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(104);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt105 >= 1 ) break loop105;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(105, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt105++;
                    } while (true);
                    } finally {dbg.exitSubRule(105);}


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
        dbg.location(593, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(601,5);
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
        dbg.location(602, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(604, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(606,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt107=2;
            try { dbg.enterSubRule(107);
            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

            int LA107_0 = input.LA(1);

            if ( (LA107_0==IDENT) ) {
                int LA107_1 = input.LA(2);

                if ( (synpred8_Css3()) ) {
                    alt107=1;
                }
            }
            else if ( (LA107_0==STAR) ) {
                int LA107_2 = input.LA(2);

                if ( (synpred8_Css3()) ) {
                    alt107=1;
                }
            }
            else if ( (LA107_0==PIPE) && (synpred8_Css3())) {
                alt107=1;
            }
            } finally {dbg.exitDecision(107);}

            switch (alt107) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(606,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2459);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(107);}

            dbg.location(606,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:51: elementName ( ws )?
            {
            dbg.location(606,51);
            pushFollow(FOLLOW_elementName_in_typeSelector2465);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(606,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:63: ws
                    {
                    dbg.location(606,63);
                    pushFollow(FOLLOW_ws_in_typeSelector2467);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(108);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(607, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(609, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(610,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:5: ( namespacePrefixName | STAR )?
            int alt109=3;
            try { dbg.enterSubRule(109);
            try { dbg.enterDecision(109, decisionCanBacktrack[109]);

            int LA109_0 = input.LA(1);

            if ( (LA109_0==IDENT) ) {
                alt109=1;
            }
            else if ( (LA109_0==STAR) ) {
                alt109=2;
            }
            } finally {dbg.exitDecision(109);}

            switch (alt109) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:7: namespacePrefixName
                    {
                    dbg.location(610,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2485);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:29: STAR
                    {
                    dbg.location(610,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2489); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(109);}

            dbg.location(610,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2493); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(611, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(614, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(616,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt110=4;
            try { dbg.enterSubRule(110);
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            switch ( input.LA(1) ) {
            case HASH_SYMBOL:
            case HASH:
                {
                alt110=1;
                }
                break;
            case DOT:
                {
                alt110=2;
                }
                break;
            case LBRACKET:
                {
                alt110=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt110=4;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:6: cssId
                    {
                    dbg.location(617,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2527);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:8: cssClass
                    {
                    dbg.location(618,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2536);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:11: slAttribute
                    {
                    dbg.location(619,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2548);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:11: pseudo
                    {
                    dbg.location(620,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2560);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(110);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(622, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(625, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt111=2;
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( (LA111_0==HASH) ) {
                alt111=1;
            }
            else if ( (LA111_0==HASH_SYMBOL) ) {
                alt111=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 111, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:7: HASH
                    {
                    dbg.location(626,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2588); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(626,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:16: HASH_SYMBOL NAME
                    {
                    dbg.location(626,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId2594); if (state.failed) return ;
                    dbg.location(626,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId2596); if (state.failed) return ;

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
        dbg.location(627, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(633, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:7: DOT ( IDENT | GEN )
            {
            dbg.location(634,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2624); if (state.failed) return ;
            dbg.location(634,11);
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
        dbg.location(635, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(642, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(643,5);
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
        dbg.location(644, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(646, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(647,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2698); if (state.failed) return ;
            dbg.location(648,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:6: ( namespacePrefix )?
            int alt112=2;
            try { dbg.enterSubRule(112);
            try { dbg.enterDecision(112, decisionCanBacktrack[112]);

            int LA112_0 = input.LA(1);

            if ( (LA112_0==IDENT) ) {
                int LA112_1 = input.LA(2);

                if ( (LA112_1==PIPE) ) {
                    alt112=1;
                }
            }
            else if ( ((LA112_0>=STAR && LA112_0<=PIPE)) ) {
                alt112=1;
            }
            } finally {dbg.exitDecision(112);}

            switch (alt112) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:6: namespacePrefix
                    {
                    dbg.location(648,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2705);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}

            dbg.location(648,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:23: ws
                    {
                    dbg.location(648,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2708);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}

            dbg.location(649,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2719);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(649,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:25: ws
                    {
                    dbg.location(649,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2721);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(114);}

            dbg.location(651,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt117=2;
            try { dbg.enterSubRule(117);
            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

            int LA117_0 = input.LA(1);

            if ( ((LA117_0>=OPEQ && LA117_0<=CONTAINS)) ) {
                alt117=1;
            }
            } finally {dbg.exitDecision(117);}

            switch (alt117) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(652,17);
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

                    dbg.location(660,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:17: ws
                            {
                            dbg.location(660,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2943);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(115);}

                    dbg.location(661,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2962);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(662,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:17: ws
                            {
                            dbg.location(662,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2980);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(116);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(117);}

            dbg.location(665,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3009); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(666, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(673, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:4: IDENT
            {
            dbg.location(674,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3025); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(675, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(677, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:2: ( IDENT | STRING )
            {
            dbg.location(679,2);
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
        dbg.location(683, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(685, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(686,7);
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

            dbg.location(687,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            int LA125_0 = input.LA(1);

            if ( (LA125_0==IDENT||LA125_0==GEN) ) {
                alt125=1;
            }
            else if ( (LA125_0==NOT) ) {
                alt125=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(688,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(689,21);
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

                    dbg.location(690,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    int alt121=2;
                    try { dbg.enterSubRule(121);
                    try { dbg.enterDecision(121, decisionCanBacktrack[121]);

                    try {
                        isCyclicDecision = true;
                        alt121 = dfa121.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(121);}

                    switch (alt121) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(691,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:25: ws
                                    {
                                    dbg.location(691,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3220);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(118);}

                            dbg.location(691,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3223); if (state.failed) return ;
                            dbg.location(691,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:36: ws
                                    {
                                    dbg.location(691,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3225);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(119);}

                            dbg.location(691,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:40: ( expression | STAR )?
                            int alt120=3;
                            try { dbg.enterSubRule(120);
                            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                            int LA120_0 = input.LA(1);

                            if ( ((LA120_0>=IDENT && LA120_0<=URI)||LA120_0==MEDIA_SYM||LA120_0==GEN||LA120_0==AT_IDENT||LA120_0==PERCENTAGE||LA120_0==PLUS||LA120_0==MINUS||LA120_0==HASH||(LA120_0>=NUMBER && LA120_0<=DIMENSION)||LA120_0==SASS_VAR) ) {
                                alt120=1;
                            }
                            else if ( (LA120_0==STAR) ) {
                                alt120=2;
                            }
                            } finally {dbg.exitDecision(120);}

                            switch (alt120) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:42: expression
                                    {
                                    dbg.location(691,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3230);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:55: STAR
                                    {
                                    dbg.location(691,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3234); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(120);}

                            dbg.location(691,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3239); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(695,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(695,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3318); if (state.failed) return ;
                    dbg.location(695,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:23: ws
                            {
                            dbg.location(695,23);
                            pushFollow(FOLLOW_ws_in_pseudo3320);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(122);}

                    dbg.location(695,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3323); if (state.failed) return ;
                    dbg.location(695,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:34: ws
                            {
                            dbg.location(695,34);
                            pushFollow(FOLLOW_ws_in_pseudo3325);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(123);}

                    dbg.location(695,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:38: ( simpleSelectorSequence )?
                    int alt124=2;
                    try { dbg.enterSubRule(124);
                    try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                    int LA124_0 = input.LA(1);

                    if ( (LA124_0==IDENT||LA124_0==GEN||LA124_0==COLON||(LA124_0>=HASH_SYMBOL && LA124_0<=PIPE)||LA124_0==LESS_AND) ) {
                        alt124=1;
                    }
                    } finally {dbg.exitDecision(124);}

                    switch (alt124) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:38: simpleSelectorSequence
                            {
                            dbg.location(695,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3328);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(124);}

                    dbg.location(695,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3331); if (state.failed) return ;

                    }


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
        dbg.location(697, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:1: declaration : ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(699, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:5: ( ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:5: ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(702,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:5: ( STAR )?
            int alt126=2;
            try { dbg.enterSubRule(126);
            try { dbg.enterDecision(126, decisionCanBacktrack[126]);

            int LA126_0 = input.LA(1);

            if ( (LA126_0==STAR) ) {
                alt126=1;
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:5: STAR
                    {
                    dbg.location(702,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3375); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(703,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:5: ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property )
            int alt127=2;
            try { dbg.enterSubRule(127);
            try { dbg.enterDecision(127, decisionCanBacktrack[127]);

            int LA127_0 = input.LA(1);

            if ( (LA127_0==HASH_SYMBOL) && (synpred9_Css3())) {
                alt127=1;
            }
            else if ( (LA127_0==IDENT) ) {
                int LA127_2 = input.LA(2);

                if ( (synpred9_Css3()) ) {
                    alt127=1;
                }
                else if ( (true) ) {
                    alt127=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 127, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA127_0==MINUS||(LA127_0>=HASH && LA127_0<=DOT)) && (synpred9_Css3())) {
                alt127=1;
            }
            else if ( (LA127_0==MEDIA_SYM||LA127_0==GEN||LA127_0==AT_IDENT||LA127_0==SASS_VAR) ) {
                alt127=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 127, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(704,74);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_declaration3421);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:9: property
                    {
                    dbg.location(706,9);
                    pushFollow(FOLLOW_property_in_declaration3442);
                    property();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}

            dbg.location(708,5);
            match(input,COLON,FOLLOW_COLON_in_declaration3455); if (state.failed) return ;
            dbg.location(708,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:11: ws
                    {
                    dbg.location(708,11);
                    pushFollow(FOLLOW_ws_in_declaration3457);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(128);}

            dbg.location(708,15);
            pushFollow(FOLLOW_propertyValue_in_declaration3460);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(708,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:29: ( prio ( ws )? )?
            int alt130=2;
            try { dbg.enterSubRule(130);
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            int LA130_0 = input.LA(1);

            if ( (LA130_0==IMPORTANT_SYM) ) {
                alt130=1;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:30: prio ( ws )?
                    {
                    dbg.location(708,30);
                    pushFollow(FOLLOW_prio_in_declaration3463);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(708,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:35: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:35: ws
                            {
                            dbg.location(708,35);
                            pushFollow(FOLLOW_ws_in_declaration3465);
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
        dbg.location(709, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(717, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(719,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(719,34);
                    pushFollow(FOLLOW_expression_in_propertyValue3505);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:9: ({...}? cp_expression )
                    {
                    dbg.location(729,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:11: {...}? cp_expression
                    {
                    dbg.location(729,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(729,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue3548);
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
        dbg.location(730, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(733, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(736,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt132=0;
            try { dbg.enterSubRule(132);

            loop132:
            do {
                int alt132=2;
                try { dbg.enterDecision(132, decisionCanBacktrack[132]);

                int LA132_0 = input.LA(1);

                if ( (LA132_0==NAMESPACE_SYM||(LA132_0>=IDENT && LA132_0<=MEDIA_SYM)||(LA132_0>=COMMA && LA132_0<=RPAREN)||(LA132_0>=WS && LA132_0<=RIGHTBOTTOM_SYM)||(LA132_0>=PLUS && LA132_0<=DCOLON)||(LA132_0>=PIPE && LA132_0<=LINE_COMMENT)) ) {
                    alt132=1;
                }


                } finally {dbg.exitDecision(132);}

                switch (alt132) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(736,7);
            	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=RPAREN)||(input.LA(1)>=WS && input.LA(1)<=RIGHTBOTTOM_SYM)||(input.LA(1)>=PLUS && input.LA(1)<=DCOLON)||(input.LA(1)>=PIPE && input.LA(1)<=LINE_COMMENT) ) {
            	        input.consume();
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
            	    if ( cnt132 >= 1 ) break loop132;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(132, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt132++;
            } while (true);
            } finally {dbg.exitSubRule(132);}

            dbg.location(736,65);
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
        dbg.location(737, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(741, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:6: 
            {
            }

        }
        finally {
        }
        dbg.location(747, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(749, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:6: 
            {
            }

        }
        finally {
        }
        dbg.location(754, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(756, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:13: SEMI
            {
            dbg.location(761,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI3733); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(762, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(765, 1);

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
            dbg.exitRule(getGrammarFileName(), "syncToFollow");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToFollow"


    // $ANTLR start "prio"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:772:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(772, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:7: IMPORTANT_SYM
            {
            dbg.location(773,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3788); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(774, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(776, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(777,7);
            pushFollow(FOLLOW_term_in_expression3809);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(777,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(135);

            loop135:
            do {
                int alt135=2;
                try { dbg.enterDecision(135, decisionCanBacktrack[135]);

                try {
                    isCyclicDecision = true;
                    alt135 = dfa135.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(135);}

                switch (alt135) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(777,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:14: ( operator ( ws )? )?
            	    int alt134=2;
            	    try { dbg.enterSubRule(134);
            	    try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            	    int LA134_0 = input.LA(1);

            	    if ( (LA134_0==COMMA||LA134_0==SOLIDUS) ) {
            	        alt134=1;
            	    }
            	    } finally {dbg.exitDecision(134);}

            	    switch (alt134) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:15: operator ( ws )?
            	            {
            	            dbg.location(777,15);
            	            pushFollow(FOLLOW_operator_in_expression3814);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(777,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:24: ws
            	                    {
            	                    dbg.location(777,24);
            	                    pushFollow(FOLLOW_ws_in_expression3816);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(133);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(134);}

            	    dbg.location(777,30);
            	    pushFollow(FOLLOW_term_in_expression3821);
            	    term();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(778, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(780, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(781,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:7: ( unaryOperator ( ws )? )?
            int alt137=2;
            try { dbg.enterSubRule(137);
            try { dbg.enterDecision(137, decisionCanBacktrack[137]);

            int LA137_0 = input.LA(1);

            if ( (LA137_0==PLUS||LA137_0==MINUS) ) {
                alt137=1;
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:9: unaryOperator ( ws )?
                    {
                    dbg.location(781,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3846);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(781,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:23: ws
                            {
                            dbg.location(781,23);
                            pushFollow(FOLLOW_ws_in_term3848);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(136);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(782,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt138=8;
            try { dbg.enterSubRule(138);
            try { dbg.enterDecision(138, decisionCanBacktrack[138]);

            try {
                isCyclicDecision = true;
                alt138 = dfa138.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(138);}

            switch (alt138) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(783,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:7: STRING
                    {
                    dbg.location(796,7);
                    match(input,STRING,FOLLOW_STRING_in_term4072); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:7: IDENT
                    {
                    dbg.location(797,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term4080); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:7: GEN
                    {
                    dbg.location(798,7);
                    match(input,GEN,FOLLOW_GEN_in_term4088); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:7: URI
                    {
                    dbg.location(799,7);
                    match(input,URI,FOLLOW_URI_in_term4096); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:7: hexColor
                    {
                    dbg.location(800,7);
                    pushFollow(FOLLOW_hexColor_in_term4104);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:7: function
                    {
                    dbg.location(801,7);
                    pushFollow(FOLLOW_function_in_term4112);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:7: {...}? cp_variable
                    {
                    dbg.location(802,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(802,36);
                    pushFollow(FOLLOW_cp_variable_in_term4122);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(138);}

            dbg.location(804,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:5: ws
                    {
                    dbg.location(804,5);
                    pushFollow(FOLLOW_ws_in_term4134);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(139);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(807, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(808,5);
            pushFollow(FOLLOW_functionName_in_function4150);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(808,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:18: ws
                    {
                    dbg.location(808,18);
                    pushFollow(FOLLOW_ws_in_function4152);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(809,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4157); if (state.failed) return ;
            dbg.location(809,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:10: ws
                    {
                    dbg.location(809,10);
                    pushFollow(FOLLOW_ws_in_function4159);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(810,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:4: expression
                    {
                    dbg.location(811,4);
                    pushFollow(FOLLOW_expression_in_function4169);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(813,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(814,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4187);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(814,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(143);

                    loop143:
                    do {
                        int alt143=2;
                        try { dbg.enterDecision(143, decisionCanBacktrack[143]);

                        int LA143_0 = input.LA(1);

                        if ( (LA143_0==COMMA) ) {
                            alt143=1;
                        }


                        } finally {dbg.exitDecision(143);}

                        switch (alt143) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(814,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4190); if (state.failed) return ;
                    	    dbg.location(814,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:24: ws
                    	            {
                    	            dbg.location(814,24);
                    	            pushFollow(FOLLOW_ws_in_function4192);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(142);}

                    	    dbg.location(814,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4195);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop143;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(143);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(144);}

            dbg.location(817,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4216); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(818, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(824, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(828,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:4: ( IDENT COLON )?
            int alt145=2;
            try { dbg.enterSubRule(145);
            try { dbg.enterDecision(145, decisionCanBacktrack[145]);

            int LA145_0 = input.LA(1);

            if ( (LA145_0==IDENT) ) {
                int LA145_1 = input.LA(2);

                if ( (LA145_1==COLON) ) {
                    alt145=1;
                }
            }
            } finally {dbg.exitDecision(145);}

            switch (alt145) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:5: IDENT COLON
                    {
                    dbg.location(828,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4264); if (state.failed) return ;
                    dbg.location(828,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4266); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(145);}

            dbg.location(828,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4270); if (state.failed) return ;
            dbg.location(828,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:25: ( DOT IDENT )*
            try { dbg.enterSubRule(146);

            loop146:
            do {
                int alt146=2;
                try { dbg.enterDecision(146, decisionCanBacktrack[146]);

                int LA146_0 = input.LA(1);

                if ( (LA146_0==DOT) ) {
                    alt146=1;
                }


                } finally {dbg.exitDecision(146);}

                switch (alt146) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:26: DOT IDENT
            	    {
            	    dbg.location(828,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4273); if (state.failed) return ;
            	    dbg.location(828,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4275); if (state.failed) return ;

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
        dbg.location(830, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(832, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(833,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4298);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(833,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:20: ws
                    {
                    dbg.location(833,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4300);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(147);}

            dbg.location(833,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4303); if (state.failed) return ;
            dbg.location(833,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:29: ws
                    {
                    dbg.location(833,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4305);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(148);}

            dbg.location(833,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4308);
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
        dbg.location(834, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(836, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:4: IDENT ( DOT IDENT )*
            {
            dbg.location(837,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4323); if (state.failed) return ;
            dbg.location(837,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:10: ( DOT IDENT )*
            try { dbg.enterSubRule(149);

            loop149:
            do {
                int alt149=2;
                try { dbg.enterDecision(149, decisionCanBacktrack[149]);

                int LA149_0 = input.LA(1);

                if ( (LA149_0==DOT) ) {
                    alt149=1;
                }


                } finally {dbg.exitDecision(149);}

                switch (alt149) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:11: DOT IDENT
            	    {
            	    dbg.location(837,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4326); if (state.failed) return ;
            	    dbg.location(837,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4328); if (state.failed) return ;

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
        dbg.location(838, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(840, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:4: expression
            {
            dbg.location(841,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4342);
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
        dbg.location(842, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(844, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:7: HASH
            {
            dbg.location(845,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4360); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(846, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(848, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:7: ( WS | NL | COMMENT )+
            {
            dbg.location(849,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:7: ( WS | NL | COMMENT )+
            int cnt150=0;
            try { dbg.enterSubRule(150);

            loop150:
            do {
                int alt150=2;
                try { dbg.enterDecision(150, decisionCanBacktrack[150]);

                int LA150_0 = input.LA(1);

                if ( (LA150_0==WS||(LA150_0>=NL && LA150_0<=COMMENT)) ) {
                    alt150=1;
                }


                } finally {dbg.exitDecision(150);}

                switch (alt150) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(849,7);
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
            	    if ( cnt150 >= 1 ) break loop150;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(150, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt150++;
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
        dbg.location(850, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(855, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:856:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt157=2;
            try { dbg.enterDecision(157, decisionCanBacktrack[157]);

            int LA157_0 = input.LA(1);

            if ( (LA157_0==MEDIA_SYM||LA157_0==AT_IDENT) ) {
                int LA157_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt157=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt157=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 157, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA157_0==SASS_VAR) ) {
                int LA157_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt157=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt157=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 157, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 157, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(157);}

            switch (alt157) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
                    {
                    dbg.location(857,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(857,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4429);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(857,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:39: ws
                            {
                            dbg.location(857,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4431);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(151);}

                    dbg.location(857,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4434); if (state.failed) return ;
                    dbg.location(857,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:49: ws
                            {
                            dbg.location(857,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4436);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(152);}

                    dbg.location(857,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4439);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(857,67);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4441); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(859,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(859,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4468);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(859,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:39: ws
                            {
                            dbg.location(859,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4470);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(153);}

                    dbg.location(859,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4473); if (state.failed) return ;
                    dbg.location(859,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:49: ws
                            {
                            dbg.location(859,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4475);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(154);}

                    dbg.location(859,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4478);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(859,67);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:67: ( SASS_DEFAULT ( ws )? )?
                    int alt156=2;
                    try { dbg.enterSubRule(156);
                    try { dbg.enterDecision(156, decisionCanBacktrack[156]);

                    int LA156_0 = input.LA(1);

                    if ( (LA156_0==SASS_DEFAULT) ) {
                        alt156=1;
                    }
                    } finally {dbg.exitDecision(156);}

                    switch (alt156) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:68: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(859,68);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4481); if (state.failed) return ;
                            dbg.location(859,81);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:81: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:81: ws
                                    {
                                    dbg.location(859,81);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4483);
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

                    dbg.location(859,87);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4488); if (state.failed) return ;

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
        dbg.location(860, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(863, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt158=2;
            try { dbg.enterDecision(158, decisionCanBacktrack[158]);

            int LA158_0 = input.LA(1);

            if ( (LA158_0==MEDIA_SYM||LA158_0==AT_IDENT) ) {
                alt158=1;
            }
            else if ( (LA158_0==SASS_VAR) ) {
                alt158=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 158, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(158);}

            switch (alt158) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(865,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(865,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(867,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(867,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:29: SASS_VAR
                    {
                    dbg.location(867,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable4553); if (state.failed) return ;

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
        dbg.location(869, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(872, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:10: cp_additionExp
            {
            dbg.location(873,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression4577);
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
        dbg.location(874, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(876, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(877,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4597);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(878,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(161);

            loop161:
            do {
                int alt161=3;
                try { dbg.enterDecision(161, decisionCanBacktrack[161]);

                int LA161_0 = input.LA(1);

                if ( (LA161_0==PLUS) ) {
                    alt161=1;
                }
                else if ( (LA161_0==MINUS) ) {
                    alt161=2;
                }


                } finally {dbg.exitDecision(161);}

                switch (alt161) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(878,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp4611); if (state.failed) return ;
            	    dbg.location(878,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:17: ws
            	            {
            	            dbg.location(878,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4613);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(159);}

            	    dbg.location(878,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4616);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(879,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp4629); if (state.failed) return ;
            	    dbg.location(879,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:18: ws
            	            {
            	            dbg.location(879,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4631);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(160);}

            	    dbg.location(879,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4634);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(881, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(883, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(884,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4667);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(885,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:885:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(164);

            loop164:
            do {
                int alt164=3;
                try { dbg.enterDecision(164, decisionCanBacktrack[164]);

                int LA164_0 = input.LA(1);

                if ( (LA164_0==STAR) ) {
                    alt164=1;
                }
                else if ( (LA164_0==SOLIDUS) ) {
                    alt164=2;
                }


                } finally {dbg.exitDecision(164);}

                switch (alt164) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:885:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(885,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp4680); if (state.failed) return ;
            	    dbg.location(885,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:885:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:885:17: ws
            	            {
            	            dbg.location(885,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4682);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(162);}

            	    dbg.location(885,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4685);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(886,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp4699); if (state.failed) return ;
            	    dbg.location(886,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:20: ws
            	            {
            	            dbg.location(886,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4701);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(163);}

            	    dbg.location(886,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4704);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(888, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(890, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt168=2;
            try { dbg.enterDecision(168, decisionCanBacktrack[168]);

            int LA168_0 = input.LA(1);

            if ( ((LA168_0>=IDENT && LA168_0<=URI)||LA168_0==MEDIA_SYM||LA168_0==GEN||LA168_0==AT_IDENT||LA168_0==PERCENTAGE||LA168_0==PLUS||LA168_0==MINUS||LA168_0==HASH||(LA168_0>=NUMBER && LA168_0<=DIMENSION)||LA168_0==SASS_VAR) ) {
                alt168=1;
            }
            else if ( (LA168_0==LPAREN) ) {
                alt168=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:10: term ( ( term )=> term )*
                    {
                    dbg.location(891,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp4737);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(891,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(165);

                    loop165:
                    do {
                        int alt165=2;
                        try { dbg.enterDecision(165, decisionCanBacktrack[165]);

                        try {
                            isCyclicDecision = true;
                            alt165 = dfa165.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(165);}

                        switch (alt165) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:16: ( term )=> term
                    	    {
                    	    dbg.location(891,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp4744);
                    	    term();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(892,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp4758); if (state.failed) return ;
                    dbg.location(892,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:17: ws
                            {
                            dbg.location(892,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4760);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(166);}

                    dbg.location(892,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp4763);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(892,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp4765); if (state.failed) return ;
                    dbg.location(892,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:43: ws
                            {
                            dbg.location(892,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4767);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(167);}


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
        dbg.location(893, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(896, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(898,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt169=8;
            try { dbg.enterSubRule(169);
            try { dbg.enterDecision(169, decisionCanBacktrack[169]);

            try {
                isCyclicDecision = true;
                alt169 = dfa169.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(169);}

            switch (alt169) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(899,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:7: STRING
                    {
                    dbg.location(912,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term5005); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:913:7: IDENT
                    {
                    dbg.location(913,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term5013); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:7: GEN
                    {
                    dbg.location(914,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term5021); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:915:7: URI
                    {
                    dbg.location(915,7);
                    match(input,URI,FOLLOW_URI_in_cp_term5029); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:7: hexColor
                    {
                    dbg.location(916,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term5037);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:7: function
                    {
                    dbg.location(917,7);
                    pushFollow(FOLLOW_function_in_cp_term5045);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:7: cp_variable
                    {
                    dbg.location(918,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term5053);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(169);}

            dbg.location(920,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:5: ws
                    {
                    dbg.location(920,5);
                    pushFollow(FOLLOW_ws_in_cp_term5065);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(170);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(921, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(930, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:931:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? )
            int alt180=2;
            try { dbg.enterDecision(180, decisionCanBacktrack[180]);

            int LA180_0 = input.LA(1);

            if ( (LA180_0==DOT) ) {
                alt180=1;
            }
            else if ( (LA180_0==SASS_MIXIN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(932,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(932,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration5096); if (state.failed) return ;
                    dbg.location(932,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5098);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(932,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:41: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:41: ws
                            {
                            dbg.location(932,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5100);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(171);}

                    dbg.location(932,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5103); if (state.failed) return ;
                    dbg.location(932,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:52: ( less_args_list )?
                    int alt172=2;
                    try { dbg.enterSubRule(172);
                    try { dbg.enterDecision(172, decisionCanBacktrack[172]);

                    int LA172_0 = input.LA(1);

                    if ( (LA172_0==MEDIA_SYM||LA172_0==AT_IDENT||LA172_0==SASS_VAR||(LA172_0>=LESS_DOTS && LA172_0<=LESS_REST)) ) {
                        alt172=1;
                    }
                    } finally {dbg.exitDecision(172);}

                    switch (alt172) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:52: less_args_list
                            {
                            dbg.location(932,52);
                            pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5105);
                            less_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(172);}

                    dbg.location(932,68);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5108); if (state.failed) return ;
                    dbg.location(932,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:75: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:75: ws
                            {
                            dbg.location(932,75);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5110);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(173);}

                    dbg.location(932,79);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:79: ( less_mixin_guarded ( ws )? )?
                    int alt175=2;
                    try { dbg.enterSubRule(175);
                    try { dbg.enterDecision(175, decisionCanBacktrack[175]);

                    int LA175_0 = input.LA(1);

                    if ( (LA175_0==LESS_WHEN) ) {
                        alt175=1;
                    }
                    } finally {dbg.exitDecision(175);}

                    switch (alt175) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:80: less_mixin_guarded ( ws )?
                            {
                            dbg.location(932,80);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5114);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(932,99);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:99: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:99: ws
                                    {
                                    dbg.location(932,99);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5116);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(174);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(175);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(934,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(934,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5133); if (state.failed) return ;
                    dbg.location(934,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5135);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(934,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5137);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(934,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:51: ws
                            {
                            dbg.location(934,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5139);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(176);}

                    dbg.location(934,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:55: ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    int alt179=2;
                    try { dbg.enterSubRule(179);
                    try { dbg.enterDecision(179, decisionCanBacktrack[179]);

                    int LA179_0 = input.LA(1);

                    if ( (LA179_0==LPAREN) ) {
                        alt179=1;
                    }
                    } finally {dbg.exitDecision(179);}

                    switch (alt179) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:56: LPAREN ( less_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(934,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5143); if (state.failed) return ;
                            dbg.location(934,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:63: ( less_args_list )?
                            int alt177=2;
                            try { dbg.enterSubRule(177);
                            try { dbg.enterDecision(177, decisionCanBacktrack[177]);

                            int LA177_0 = input.LA(1);

                            if ( (LA177_0==MEDIA_SYM||LA177_0==AT_IDENT||LA177_0==SASS_VAR||(LA177_0>=LESS_DOTS && LA177_0<=LESS_REST)) ) {
                                alt177=1;
                            }
                            } finally {dbg.exitDecision(177);}

                            switch (alt177) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:63: less_args_list
                                    {
                                    dbg.location(934,63);
                                    pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5145);
                                    less_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(177);}

                            dbg.location(934,79);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5148); if (state.failed) return ;
                            dbg.location(934,86);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:86: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:86: ws
                                    {
                                    dbg.location(934,86);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5150);
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
        dbg.location(935, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:939:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(939, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(941,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:941:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt181=2;
            try { dbg.enterSubRule(181);
            try { dbg.enterDecision(181, decisionCanBacktrack[181]);

            int LA181_0 = input.LA(1);

            if ( (LA181_0==DOT) ) {
                alt181=1;
            }
            else if ( (LA181_0==SASS_INCLUDE) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:942:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(942,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(942,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call5192); if (state.failed) return ;
                    dbg.location(942,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5194);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(944,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(944,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call5216); if (state.failed) return ;
                    dbg.location(944,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5218);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(944,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5220);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(181);}

            dbg.location(946,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?
            int alt184=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:6: ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(946,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:6: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:6: ws
                            {
                            dbg.location(946,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call5233);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(182);}

                    dbg.location(946,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call5236); if (state.failed) return ;
                    dbg.location(946,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:17: ( cp_mixin_call_args )?
                    int alt183=2;
                    try { dbg.enterSubRule(183);
                    try { dbg.enterDecision(183, decisionCanBacktrack[183]);

                    int LA183_0 = input.LA(1);

                    if ( ((LA183_0>=IDENT && LA183_0<=URI)||LA183_0==MEDIA_SYM||LA183_0==GEN||LA183_0==AT_IDENT||LA183_0==PERCENTAGE||LA183_0==PLUS||LA183_0==MINUS||LA183_0==HASH||(LA183_0>=NUMBER && LA183_0<=DIMENSION)||LA183_0==SASS_VAR) ) {
                        alt183=1;
                    }
                    } finally {dbg.exitDecision(183);}

                    switch (alt183) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:17: cp_mixin_call_args
                            {
                            dbg.location(946,17);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call5238);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(183);}

                    dbg.location(946,37);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call5241); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(184);}

            dbg.location(946,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:46: ws
                    {
                    dbg.location(946,46);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5245);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(185);}

            dbg.location(946,50);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call5248); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(947, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:949:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(949, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:951:5: IDENT
            {
            dbg.location(951,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name5277); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_name"


    // $ANTLR start "cp_mixin_call_args"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:1: cp_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(954, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(958,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_args5313);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(958,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(187);

            loop187:
            do {
                int alt187=2;
                try { dbg.enterDecision(187, decisionCanBacktrack[187]);

                int LA187_0 = input.LA(1);

                if ( (LA187_0==SEMI||LA187_0==COMMA) ) {
                    alt187=1;
                }


                } finally {dbg.exitDecision(187);}

                switch (alt187) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(958,12);
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

            	    dbg.location(958,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:27: ws
            	            {
            	            dbg.location(958,27);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args5325);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(186);}

            	    dbg.location(958,31);
            	    pushFollow(FOLLOW_term_in_cp_mixin_call_args5328);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop187;
                }
            } while (true);
            } finally {dbg.exitSubRule(187);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(959, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(962, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt192=2;
            try { dbg.enterDecision(192, decisionCanBacktrack[192]);

            int LA192_0 = input.LA(1);

            if ( (LA192_0==MEDIA_SYM||LA192_0==AT_IDENT||LA192_0==SASS_VAR) ) {
                alt192=1;
            }
            else if ( ((LA192_0>=LESS_DOTS && LA192_0<=LESS_REST)) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(966,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(966,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5370);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(966,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(966,18);
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

                    	    dbg.location(966,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:35: ws
                    	            {
                    	            dbg.location(966,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5384);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(188);}

                    	    dbg.location(966,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5387);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop189;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(189);}

                    dbg.location(966,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:50: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt191=2;
                    try { dbg.enterSubRule(191);
                    try { dbg.enterDecision(191, decisionCanBacktrack[191]);

                    int LA191_0 = input.LA(1);

                    if ( (LA191_0==SEMI||LA191_0==COMMA) ) {
                        alt191=1;
                    }
                    } finally {dbg.exitDecision(191);}

                    switch (alt191) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:52: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(966,52);
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

                            dbg.location(966,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:69: ws
                                    {
                                    dbg.location(966,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5403);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(190);}

                            dbg.location(966,73);
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
                    } finally {dbg.exitSubRule(191);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(968,5);
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
        dbg.location(969, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(972, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(974,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg5460);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(974,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:17: ( COLON ( ws )? cp_expression )?
            int alt194=2;
            try { dbg.enterSubRule(194);
            try { dbg.enterDecision(194, decisionCanBacktrack[194]);

            int LA194_0 = input.LA(1);

            if ( (LA194_0==COLON) ) {
                alt194=1;
            }
            } finally {dbg.exitDecision(194);}

            switch (alt194) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(974,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5464); if (state.failed) return ;
                    dbg.location(974,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:25: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:25: ws
                            {
                            dbg.location(974,25);
                            pushFollow(FOLLOW_ws_in_less_arg5466);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(193);}

                    dbg.location(974,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg5469);
                    cp_expression();

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
        dbg.location(975, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:979:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(979, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:980:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(981,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded5495); if (state.failed) return ;
            dbg.location(981,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:15: ws
                    {
                    dbg.location(981,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded5497);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(195);}

            dbg.location(981,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5500);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(981,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(197);

            loop197:
            do {
                int alt197=2;
                try { dbg.enterDecision(197, decisionCanBacktrack[197]);

                int LA197_0 = input.LA(1);

                if ( ((LA197_0>=COMMA && LA197_0<=AND)) ) {
                    alt197=1;
                }


                } finally {dbg.exitDecision(197);}

                switch (alt197) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(981,36);
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

            	    dbg.location(981,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:50: ws
            	            {
            	            dbg.location(981,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded5512);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(196);}

            	    dbg.location(981,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5515);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop197;
                }
            } while (true);
            } finally {dbg.exitSubRule(197);}


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
            dbg.exitRule(getGrammarFileName(), "less_mixin_guarded");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_guarded"


    // $ANTLR start "less_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:986:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(986, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:987:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(988,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:5: ( NOT ( ws )? )?
            int alt199=2;
            try { dbg.enterSubRule(199);
            try { dbg.enterDecision(199, decisionCanBacktrack[199]);

            int LA199_0 = input.LA(1);

            if ( (LA199_0==NOT) ) {
                alt199=1;
            }
            } finally {dbg.exitDecision(199);}

            switch (alt199) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:6: NOT ( ws )?
                    {
                    dbg.location(988,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition5545); if (state.failed) return ;
                    dbg.location(988,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:10: ws
                            {
                            dbg.location(988,10);
                            pushFollow(FOLLOW_ws_in_less_condition5547);
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

            dbg.location(989,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition5556); if (state.failed) return ;
            dbg.location(989,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:12: ws
                    {
                    dbg.location(989,12);
                    pushFollow(FOLLOW_ws_in_less_condition5558);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(200);}

            dbg.location(990,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt205=2;
            try { dbg.enterSubRule(205);
            try { dbg.enterDecision(205, decisionCanBacktrack[205]);

            int LA205_0 = input.LA(1);

            if ( (LA205_0==IDENT) ) {
                alt205=1;
            }
            else if ( (LA205_0==MEDIA_SYM||LA205_0==AT_IDENT||LA205_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(991,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition5584);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(991,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:40: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:40: ws
                            {
                            dbg.location(991,40);
                            pushFollow(FOLLOW_ws_in_less_condition5586);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(201);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(993,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(993,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition5617);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(993,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt204=2;
                    try { dbg.enterSubRule(204);
                    try { dbg.enterDecision(204, decisionCanBacktrack[204]);

                    int LA204_0 = input.LA(1);

                    if ( (LA204_0==WS||LA204_0==GREATER||LA204_0==OPEQ||(LA204_0>=NL && LA204_0<=COMMENT)||(LA204_0>=GREATER_OR_EQ && LA204_0<=LESS_OR_EQ)) ) {
                        alt204=1;
                    }
                    } finally {dbg.exitDecision(204);}

                    switch (alt204) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(993,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:28: ws
                                    {
                                    dbg.location(993,28);
                                    pushFollow(FOLLOW_ws_in_less_condition5620);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(202);}

                            dbg.location(993,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition5623);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(993,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:56: ws
                                    {
                                    dbg.location(993,56);
                                    pushFollow(FOLLOW_ws_in_less_condition5625);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(203);}

                            dbg.location(993,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition5628);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(204);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(205);}

            dbg.location(995,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition5657); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition"


    // $ANTLR start "less_function_in_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(999, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1001,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition5683);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1001,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:18: ws
                    {
                    dbg.location(1001,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5685);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(206);}

            dbg.location(1001,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition5688); if (state.failed) return ;
            dbg.location(1001,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:29: ws
                    {
                    dbg.location(1001,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5690);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(207);}

            dbg.location(1001,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition5693);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1001,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:45: ws
                    {
                    dbg.location(1001,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5695);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(208);}

            dbg.location(1001,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition5698); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_function_in_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function_in_condition"


    // $ANTLR start "less_fn_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1005, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1006:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1007:5: IDENT
            {
            dbg.location(1007,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name5720); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_fn_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_fn_name"


    // $ANTLR start "less_condition_operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1010, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1011,5);
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
        dbg.location(1013, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1031:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1031, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1032:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1033:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            {
            dbg.location(1033,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1033:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1034,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5819);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
                    {
                    dbg.location(1036,13);
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
            } finally {dbg.exitSubRule(209);}

            dbg.location(1038,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            try { dbg.enterSubRule(212);

            loop212:
            do {
                int alt212=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1039:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    {
            	    dbg.location(1039,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1039:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1039:13: ws
            	            {
            	            dbg.location(1039,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression5904);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(210);}

            	    dbg.location(1040,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1040:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    int alt211=2;
            	    try { dbg.enterSubRule(211);
            	    try { dbg.enterDecision(211, decisionCanBacktrack[211]);

            	    try {
            	        isCyclicDecision = true;
            	        alt211 = dfa211.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(211);}

            	    switch (alt211) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1041,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5943);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1043:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
            	            {
            	            dbg.location(1043,17);
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
            	    } finally {dbg.exitSubRule(211);}


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
        dbg.location(1047, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1049:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1049, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1050:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1051:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1051,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1051:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt213=2;
            try { dbg.enterSubRule(213);
            try { dbg.enterDecision(213, decisionCanBacktrack[213]);

            int LA213_0 = input.LA(1);

            if ( (LA213_0==HASH_SYMBOL) ) {
                int LA213_1 = input.LA(2);

                if ( (LA213_1==LBRACE) && (synpred14_Css3())) {
                    alt213=1;
                }
                else if ( (LA213_1==IDENT||LA213_1==COLON||LA213_1==WS||(LA213_1>=MINUS && LA213_1<=DOT)||(LA213_1>=NL && LA213_1<=COMMENT)) ) {
                    alt213=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 213, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA213_0==IDENT||LA213_0==MINUS||(LA213_0>=HASH && LA213_0<=DOT)) ) {
                alt213=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 213, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(213);}

            switch (alt213) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1052,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6077);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1054,13);
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
            } finally {dbg.exitSubRule(213);}

            dbg.location(1056,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(216);

            loop216:
            do {
                int alt216=2;
                try { dbg.enterDecision(216, decisionCanBacktrack[216]);

                int LA216_0 = input.LA(1);

                if ( (LA216_0==IDENT||LA216_0==WS||(LA216_0>=MINUS && LA216_0<=DOT)||(LA216_0>=NL && LA216_0<=COMMENT)) ) {
                    alt216=1;
                }


                } finally {dbg.exitDecision(216);}

                switch (alt216) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1057,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1057:13: ws
            	            {
            	            dbg.location(1057,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression6158);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(214);}

            	    dbg.location(1058,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt215=2;
            	    try { dbg.enterSubRule(215);
            	    try { dbg.enterDecision(215, decisionCanBacktrack[215]);

            	    int LA215_0 = input.LA(1);

            	    if ( (LA215_0==HASH_SYMBOL) ) {
            	        int LA215_1 = input.LA(2);

            	        if ( (LA215_1==LBRACE) && (synpred15_Css3())) {
            	            alt215=1;
            	        }
            	        else if ( (LA215_1==IDENT||LA215_1==COLON||LA215_1==WS||(LA215_1>=MINUS && LA215_1<=DOT)||(LA215_1>=NL && LA215_1<=COMMENT)) ) {
            	            alt215=2;
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return ;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 215, 1, input);

            	            dbg.recognitionException(nvae);
            	            throw nvae;
            	        }
            	    }
            	    else if ( (LA215_0==IDENT||LA215_0==MINUS||(LA215_0>=HASH && LA215_0<=DOT)) ) {
            	        alt215=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 215, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(215);}

            	    switch (alt215) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1059,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6197);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1061,17);
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
            	    } finally {dbg.exitSubRule(215);}


            	    }
            	    break;

            	default :
            	    break loop216;
                }
            } while (true);
            } finally {dbg.exitSubRule(216);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1065, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_declaration_interpolation_expression"


    // $ANTLR start "scss_interpolation_expression_var"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1067:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1067, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1068:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1069,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var6306); if (state.failed) return ;
            dbg.location(1069,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var6308); if (state.failed) return ;
            dbg.location(1069,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:28: ws
                    {
                    dbg.location(1069,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var6310);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(217);}

            dbg.location(1069,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:32: ( cp_variable | less_function_in_condition )
            int alt218=2;
            try { dbg.enterSubRule(218);
            try { dbg.enterDecision(218, decisionCanBacktrack[218]);

            int LA218_0 = input.LA(1);

            if ( (LA218_0==MEDIA_SYM||LA218_0==AT_IDENT||LA218_0==SASS_VAR) ) {
                alt218=1;
            }
            else if ( (LA218_0==IDENT) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:34: cp_variable
                    {
                    dbg.location(1069,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var6315);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:48: less_function_in_condition
                    {
                    dbg.location(1069,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var6319);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(218);}

            dbg.location(1069,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:77: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1069:77: ws
                    {
                    dbg.location(1069,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var6323);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(219);}

            dbg.location(1069,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var6326); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "scss_interpolation_expression_var");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_interpolation_expression_var"


    // $ANTLR start "scss_nested_properties"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1090:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1090, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1091:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1092,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties6370);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1092,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties6372); if (state.failed) return ;
            dbg.location(1092,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:20: ws
                    {
                    dbg.location(1092,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties6374);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(220);}

            dbg.location(1092,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:24: ( propertyValue )?
            int alt221=2;
            try { dbg.enterSubRule(221);
            try { dbg.enterDecision(221, decisionCanBacktrack[221]);

            int LA221_0 = input.LA(1);

            if ( ((LA221_0>=IDENT && LA221_0<=URI)||LA221_0==MEDIA_SYM||(LA221_0>=GEN && LA221_0<=LPAREN)||LA221_0==AT_IDENT||LA221_0==PERCENTAGE||LA221_0==PLUS||LA221_0==MINUS||LA221_0==HASH||(LA221_0>=NUMBER && LA221_0<=DIMENSION)||LA221_0==SASS_VAR) ) {
                alt221=1;
            }
            } finally {dbg.exitDecision(221);}

            switch (alt221) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:24: propertyValue
                    {
                    dbg.location(1092,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties6377);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(221);}

            dbg.location(1092,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties6380); if (state.failed) return ;
            dbg.location(1092,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1092:46: ws
                    {
                    dbg.location(1092,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties6382);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(222);}

            dbg.location(1092,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties6385);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1092,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties6387);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1092,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties6389); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1093, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(561,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt223=0;
                try { dbg.enterSubRule(223);

                loop223:
                do {
                    int alt223=2;
                    try { dbg.enterDecision(223, decisionCanBacktrack[223]);

                    int LA223_0 = input.LA(1);

                    if ( (LA223_0==NAMESPACE_SYM||(LA223_0>=IDENT && LA223_0<=MEDIA_SYM)||(LA223_0>=COMMA && LA223_0<=LPAREN)||(LA223_0>=RPAREN && LA223_0<=LINE_COMMENT)) ) {
                        alt223=1;
                    }


                    } finally {dbg.exitDecision(223);}

                    switch (alt223) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(561,4);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=LINE_COMMENT) ) {
                	        input.consume();
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
                	    if ( cnt223 >= 1 ) break loop223;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(223, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt223++;
                } while (true);
                } finally {dbg.exitSubRule(223);}

                dbg.location(561,33);
                match(input,COLON,FOLLOW_COLON_in_synpred1_Css31970); if (state.failed) return ;
                dbg.location(561,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt224=0;
                try { dbg.enterSubRule(224);

                loop224:
                do {
                    int alt224=2;
                    try { dbg.enterDecision(224, decisionCanBacktrack[224]);

                    int LA224_0 = input.LA(1);

                    if ( (LA224_0==NAMESPACE_SYM||(LA224_0>=IDENT && LA224_0<=MEDIA_SYM)||(LA224_0>=COMMA && LA224_0<=LINE_COMMENT)) ) {
                        alt224=1;
                    }


                    } finally {dbg.exitDecision(224);}

                    switch (alt224) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(561,39);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=LINE_COMMENT) ) {
                	        input.consume();
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
                	    if ( cnt224 >= 1 ) break loop224;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(224, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt224++;
                } while (true);
                } finally {dbg.exitSubRule(224);}

                dbg.location(561,62);
                match(input,SEMI,FOLLOW_SEMI_in_synpred1_Css31982); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:69: scss_declaration_interpolation_expression COLON
                {
                dbg.location(561,69);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred1_Css31986);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(561,111);
                match(input,COLON,FOLLOW_COLON_in_synpred1_Css31988); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )
        int alt228=2;
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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE
                {
                dbg.location(563,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt226=0;
                try { dbg.enterSubRule(226);

                loop226:
                do {
                    int alt226=2;
                    try { dbg.enterDecision(226, decisionCanBacktrack[226]);

                    int LA226_0 = input.LA(1);

                    if ( (LA226_0==NAMESPACE_SYM||(LA226_0>=IDENT && LA226_0<=MEDIA_SYM)||(LA226_0>=COMMA && LA226_0<=LPAREN)||(LA226_0>=RPAREN && LA226_0<=LINE_COMMENT)) ) {
                        alt226=1;
                    }


                    } finally {dbg.exitDecision(226);}

                    switch (alt226) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(563,4);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=LINE_COMMENT) ) {
                	        input.consume();
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
                	    if ( cnt226 >= 1 ) break loop226;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(226, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt226++;
                } while (true);
                } finally {dbg.exitSubRule(226);}

                dbg.location(563,33);
                match(input,COLON,FOLLOW_COLON_in_synpred2_Css32018); if (state.failed) return ;
                dbg.location(563,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt227=0;
                try { dbg.enterSubRule(227);

                loop227:
                do {
                    int alt227=2;
                    try { dbg.enterDecision(227, decisionCanBacktrack[227]);

                    int LA227_0 = input.LA(1);

                    if ( (LA227_0==NAMESPACE_SYM||(LA227_0>=IDENT && LA227_0<=MEDIA_SYM)||(LA227_0>=COMMA && LA227_0<=LINE_COMMENT)) ) {
                        alt227=1;
                    }


                    } finally {dbg.exitDecision(227);}

                    switch (alt227) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(563,39);
                	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=LINE_COMMENT) ) {
                	        input.consume();
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
                	    if ( cnt227 >= 1 ) break loop227;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(227, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt227++;
                } while (true);
                } finally {dbg.exitSubRule(227);}

                dbg.location(563,62);
                match(input,LBRACE,FOLLOW_LBRACE_in_synpred2_Css32030); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:71: scss_declaration_interpolation_expression COLON
                {
                dbg.location(563,71);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred2_Css32034);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(563,113);
                match(input,COLON,FOLLOW_COLON_in_synpred2_Css32036); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(565,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:18: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt229=0;
        try { dbg.enterSubRule(229);

        loop229:
        do {
            int alt229=2;
            try { dbg.enterDecision(229, decisionCanBacktrack[229]);

            int LA229_0 = input.LA(1);

            if ( (LA229_0==NAMESPACE_SYM||(LA229_0>=IDENT && LA229_0<=MEDIA_SYM)||(LA229_0>=COMMA && LA229_0<=LINE_COMMENT)) ) {
                alt229=1;
            }


            } finally {dbg.exitDecision(229);}

            switch (alt229) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(565,18);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=LINE_COMMENT) ) {
        	        input.consume();
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
        	    if ( cnt229 >= 1 ) break loop229;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(229, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt229++;
        } while (true);
        } finally {dbg.exitSubRule(229);}

        dbg.location(565,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred3_Css32076); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(571,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:16: (~ ( RBRACE ) )+
        int cnt230=0;
        try { dbg.enterSubRule(230);

        loop230:
        do {
            int alt230=2;
            try { dbg.enterDecision(230, decisionCanBacktrack[230]);

            int LA230_0 = input.LA(1);

            if ( ((LA230_0>=NAMESPACE_SYM && LA230_0<=LBRACE)||(LA230_0>=COMMA && LA230_0<=LINE_COMMENT)) ) {
                alt230=1;
            }


            } finally {dbg.exitDecision(230);}

            switch (alt230) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:16: ~ ( RBRACE )
        	    {
        	    dbg.location(571,16);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=LBRACE)||(input.LA(1)>=COMMA && input.LA(1)<=LINE_COMMENT) ) {
        	        input.consume();
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
        	    if ( cnt230 >= 1 ) break loop230;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(230, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt230++;
        } while (true);
        } finally {dbg.exitSubRule(230);}

        dbg.location(571,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred4_Css32163); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(577,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(231);

        loop231:
        do {
            int alt231=2;
            try { dbg.enterDecision(231, decisionCanBacktrack[231]);

            int LA231_0 = input.LA(1);

            if ( ((LA231_0>=NAMESPACE_SYM && LA231_0<=MEDIA_SYM)||(LA231_0>=RBRACE && LA231_0<=MINUS)||(LA231_0>=HASH && LA231_0<=LINE_COMMENT)) ) {
                alt231=1;
            }


            } finally {dbg.exitDecision(231);}

            switch (alt231) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(577,11);
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
        	    break loop231;
            }
        } while (true);
        } finally {dbg.exitSubRule(231);}

        dbg.location(577,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred5_Css32221); if (state.failed) return ;
        dbg.location(577,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32223); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:19: esPred
        {
        dbg.location(590,19);
        pushFollow(FOLLOW_esPred_in_synpred6_Css32321);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:6: esPred
        {
        dbg.location(592,6);
        pushFollow(FOLLOW_esPred_in_synpred7_Css32342);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(606,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:8: ( IDENT | STAR )?
        int alt232=2;
        try { dbg.enterSubRule(232);
        try { dbg.enterDecision(232, decisionCanBacktrack[232]);

        int LA232_0 = input.LA(1);

        if ( (LA232_0==IDENT||LA232_0==STAR) ) {
            alt232=1;
        }
        } finally {dbg.exitDecision(232);}

        switch (alt232) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(606,8);
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
        } finally {dbg.exitSubRule(232);}

        dbg.location(606,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred8_Css32456); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(704,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )*
        try { dbg.enterSubRule(233);

        loop233:
        do {
            int alt233=2;
            try { dbg.enterDecision(233, decisionCanBacktrack[233]);

            int LA233_0 = input.LA(1);

            if ( (LA233_0==NAMESPACE_SYM||(LA233_0>=IDENT && LA233_0<=LBRACE)||(LA233_0>=COMMA && LA233_0<=LPAREN)||(LA233_0>=RPAREN && LA233_0<=MINUS)||(LA233_0>=HASH && LA233_0<=LINE_COMMENT)) ) {
                alt233=1;
            }


            } finally {dbg.exitDecision(233);}

            switch (alt233) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:11: ~ ( HASH_SYMBOL | COLON | SEMI | RBRACE )
        	    {
        	    dbg.location(704,11);
        	    if ( input.LA(1)==NAMESPACE_SYM||(input.LA(1)>=IDENT && input.LA(1)<=LBRACE)||(input.LA(1)>=COMMA && input.LA(1)<=LPAREN)||(input.LA(1)>=RPAREN && input.LA(1)<=MINUS)||(input.LA(1)>=HASH && input.LA(1)<=LINE_COMMENT) ) {
        	        input.consume();
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
        	    break loop233;
            }
        } while (true);
        } finally {dbg.exitSubRule(233);}

        dbg.location(704,51);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred9_Css33414); if (state.failed) return ;
        dbg.location(704,63);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred9_Css33416); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:12: expressionPredicate
        {
        dbg.location(719,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred10_Css33502);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:891:17: term
        {
        dbg.location(891,17);
        pushFollow(FOLLOW_term_in_synpred11_Css34741);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1034:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1034,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred12_Css35814); if (state.failed) return ;
        dbg.location(1034,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Css35816); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1041:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1041,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred13_Css35938); if (state.failed) return ;
        dbg.location(1041,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred13_Css35940); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1052:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1052,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred14_Css36072); if (state.failed) return ;
        dbg.location(1052,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred14_Css36074); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1059:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1059,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred15_Css36192); if (state.failed) return ;
        dbg.location(1059,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred15_Css36194); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Css3

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


    protected DFA42 dfa42 = new DFA42(this);
    protected DFA63 dfa63 = new DFA63(this);
    protected DFA89 dfa89 = new DFA89(this);
    protected DFA95 dfa95 = new DFA95(this);
    protected DFA100 dfa100 = new DFA100(this);
    protected DFA103 dfa103 = new DFA103(this);
    protected DFA121 dfa121 = new DFA121(this);
    protected DFA131 dfa131 = new DFA131(this);
    protected DFA135 dfa135 = new DFA135(this);
    protected DFA138 dfa138 = new DFA138(this);
    protected DFA144 dfa144 = new DFA144(this);
    protected DFA165 dfa165 = new DFA165(this);
    protected DFA169 dfa169 = new DFA169(this);
    protected DFA184 dfa184 = new DFA184(this);
    protected DFA189 dfa189 = new DFA189(this);
    protected DFA209 dfa209 = new DFA209(this);
    protected DFA212 dfa212 = new DFA212(this);
    protected DFA211 dfa211 = new DFA211(this);
    protected DFA225 dfa225 = new DFA225(this);
    protected DFA228 dfa228 = new DFA228(this);
    static final String DFA42_eotS =
        "\31\uffff";
    static final String DFA42_eofS =
        "\31\uffff";
    static final String DFA42_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\2\uffff\1\5\1\6\1\uffff\1\6\1\5\1\6"+
        "\3\5\1\6\1\5\1\6\2\5";
    static final String DFA42_maxS =
        "\1\126\1\122\1\uffff\1\122\4\uffff\1\122\2\uffff\2\122\1\uffff\2"+
        "\122\1\130\2\122\1\131\1\130\1\131\1\130\2\122";
    static final String DFA42_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\2\uffff\1"+
        "\2\13\uffff";
    static final String DFA42_specialS =
        "\31\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\2\4\uffff\1\3\6\uffff\1\2\1\uffff\1\2\1\uffff\1\10\1\uffff"+
            "\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3\2\1\1\4\2\1"+
            "\uffff\1\2\25\uffff\1\11\1\2\1\12",
            "\1\13\5\uffff\1\2\5\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\30\uffff\2\2",
            "",
            "\1\15\5\uffff\1\15\3\uffff\4\15\1\11\2\uffff\1\14\71\uffff"+
            "\2\14",
            "",
            "",
            "",
            "",
            "\2\7\4\uffff\1\7\7\uffff\1\11\2\uffff\1\16\71\uffff\2\11",
            "",
            "",
            "\1\12\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\20\1\2\2\uffff"+
            "\1\17\32\uffff\13\2\1\uffff\1\2\22\uffff\2\17",
            "\1\15\5\uffff\1\15\3\uffff\4\15\1\11\2\uffff\1\14\71\uffff"+
            "\2\14",
            "",
            "\2\7\4\uffff\1\7\7\uffff\1\11\2\uffff\1\16\71\uffff\2\11",
            "\1\12\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\20\1\2\2\uffff"+
            "\1\17\32\uffff\13\2\1\uffff\1\2\22\uffff\2\17",
            "\3\12\2\uffff\1\21\6\uffff\1\12\2\uffff\1\23\1\21\6\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\3"+
            "\uffff\1\22\2\uffff\2\2",
            "\1\24\10\uffff\1\24\5\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\24\10\uffff\1\24\5\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\12\6\uffff\1\2\12\uffff\1\25\71\uffff\2\25\6\uffff\1\2",
            "\3\12\2\uffff\1\27\6\uffff\1\12\3\uffff\1\27\1\26\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\26\1\uffff\1\30\2\uffff\2\2",
            "\1\12\6\uffff\1\2\12\uffff\1\25\71\uffff\2\25\6\uffff\1\2",
            "\3\12\2\uffff\1\27\6\uffff\1\12\3\uffff\1\27\1\26\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\26\1\uffff\1\30\2\uffff\2\2",
            "\1\24\10\uffff\1\24\5\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\24\10\uffff\1\24\5\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12"
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
            return "396:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );";
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
        "\2\122\2\uffff";
    static final String DFA63_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA63_specialS =
        "\4\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\2\1\uffff\1\3\10\uffff\1\1\71\uffff\2\1",
            "\1\2\1\uffff\1\3\10\uffff\1\1\71\uffff\2\1",
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
            return "()* loopback of 461:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
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
        "\2\6\2\uffff\2\6";
    static final String DFA89_maxS =
        "\1\125\1\122\2\uffff\2\122";
    static final String DFA89_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA89_specialS =
        "\6\uffff}>";
    static final String[] DFA89_transitionS = {
            "\1\3\13\uffff\1\3\1\uffff\1\3\40\uffff\3\3\1\1\4\3\1\uffff\1"+
            "\3\26\uffff\1\2",
            "\1\4\5\uffff\1\3\5\uffff\1\3\1\uffff\1\3\2\uffff\1\3\35\uffff"+
            "\4\3\30\uffff\2\3",
            "",
            "",
            "\1\3\5\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\13\3\1\uffff\1\3\22\uffff\2\5",
            "\1\3\5\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\2\1\3\2\uffff\1"+
            "\5\32\uffff\13\3\1\uffff\1\3\22\uffff\2\5"
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
            return "538:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA95_eotS =
        "\24\uffff";
    static final String DFA95_eofS =
        "\24\uffff";
    static final String DFA95_minS =
        "\1\6\7\0\1\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA95_maxS =
        "\1\126\7\0\1\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA95_acceptS =
        "\10\uffff\1\5\1\uffff\4\3\1\uffff\2\3\1\4\1\1\1\2";
    static final String DFA95_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\4\uffff\1\11\5\uffff}>";
    static final String[] DFA95_transitionS = {
            "\1\3\4\uffff\1\6\1\uffff\1\10\4\uffff\1\5\1\uffff\1\13\1\uffff"+
            "\1\6\36\uffff\1\16\1\2\1\11\1\4\1\17\1\20\1\1\1\14\1\uffff\1"+
            "\15\25\uffff\1\7\1\12\1\21",
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
            "",
            "",
            ""
    };

    static final short[] DFA95_eot = DFA.unpackEncodedString(DFA95_eotS);
    static final short[] DFA95_eof = DFA.unpackEncodedString(DFA95_eofS);
    static final char[] DFA95_min = DFA.unpackEncodedStringToUnsignedChars(DFA95_minS);
    static final char[] DFA95_max = DFA.unpackEncodedStringToUnsignedChars(DFA95_maxS);
    static final short[] DFA95_accept = DFA.unpackEncodedString(DFA95_acceptS);
    static final short[] DFA95_special = DFA.unpackEncodedString(DFA95_specialS);
    static final short[][] DFA95_transition;

    static {
        int numStates = DFA95_transitionS.length;
        DFA95_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA95_transition[i] = DFA.unpackEncodedString(DFA95_transitionS[i]);
        }
    }

    class DFA95 extends DFA {

        public DFA95(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 95;
            this.eot = DFA95_eot;
            this.eof = DFA95_eof;
            this.min = DFA95_min;
            this.max = DFA95_max;
            this.accept = DFA95_accept;
            this.special = DFA95_special;
            this.transition = DFA95_transition;
        }
        public String getDescription() {
            return "()* loopback of 557:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA95_0 = input.LA(1);

                         
                        int index95_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA95_0==STAR) ) {s = 1;}

                        else if ( (LA95_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA95_0==IDENT) ) {s = 3;}

                        else if ( (LA95_0==DOT) ) {s = 4;}

                        else if ( (LA95_0==GEN) ) {s = 5;}

                        else if ( (LA95_0==MEDIA_SYM||LA95_0==AT_IDENT) ) {s = 6;}

                        else if ( (LA95_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA95_0==RBRACE) ) {s = 8;}

                        else if ( (LA95_0==HASH) ) {s = 9;}

                        else if ( (LA95_0==SASS_MIXIN) && (synpred3_Css3())) {s = 10;}

                        else if ( (LA95_0==COLON) && (synpred3_Css3())) {s = 11;}

                        else if ( (LA95_0==PIPE) && (synpred3_Css3())) {s = 12;}

                        else if ( (LA95_0==LESS_AND) && (synpred3_Css3())) {s = 13;}

                        else if ( (LA95_0==MINUS) ) {s = 14;}

                        else if ( (LA95_0==LBRACKET) && (synpred3_Css3())) {s = 15;}

                        else if ( (LA95_0==DCOLON) && (synpred3_Css3())) {s = 16;}

                        else if ( (LA95_0==SASS_INCLUDE) ) {s = 17;}

                         
                        input.seek(index95_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA95_1 = input.LA(1);

                         
                        int index95_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA95_2 = input.LA(1);

                         
                        int index95_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA95_3 = input.LA(1);

                         
                        int index95_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred2_Css3()) ) {s = 19;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA95_4 = input.LA(1);

                         
                        int index95_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( ((synpred3_Css3()||((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 16;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA95_5 = input.LA(1);

                         
                        int index95_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred2_Css3()) ) {s = 19;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA95_6 = input.LA(1);

                         
                        int index95_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred1_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 18;}

                        else if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 19;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index95_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA95_7 = input.LA(1);

                         
                        int index95_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred1_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 18;}

                        else if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index95_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA95_9 = input.LA(1);

                         
                        int index95_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA95_14 = input.LA(1);

                         
                        int index95_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index95_14);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 95, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA100_eotS =
        "\17\uffff";
    static final String DFA100_eofS =
        "\17\uffff";
    static final String DFA100_minS =
        "\2\6\2\0\1\uffff\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA100_maxS =
        "\1\76\1\122\2\0\1\uffff\2\122\5\uffff\1\0\1\uffff\1\0";
    static final String DFA100_acceptS =
        "\4\uffff\1\2\2\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA100_specialS =
        "\1\4\1\7\1\6\1\5\1\uffff\1\0\1\2\5\uffff\1\3\1\uffff\1\1}>";
    static final String[] DFA100_transitionS = {
            "\1\2\13\uffff\1\4\1\uffff\1\6\40\uffff\1\7\1\1\1\3\1\5\4\4\1"+
            "\uffff\1\4",
            "\1\13\5\uffff\1\10\7\uffff\1\13\2\uffff\1\11\35\uffff\1\13"+
            "\1\12\2\13\4\uffff\1\4\23\uffff\2\11",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\14\5\uffff\1\15\5\uffff\1\4\1\uffff\1\13\2\uffff\1\11\35"+
            "\uffff\1\13\1\12\2\13\30\uffff\2\11",
            "\1\16\5\uffff\1\15\4\uffff\2\4\1\uffff\1\13\2\uffff\1\11\35"+
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
            return "574:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA100_5 = input.LA(1);

                         
                        int index100_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA100_5==IDENT) ) {s = 12;}

                        else if ( (LA100_5==WS||(LA100_5>=NL && LA100_5<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA100_5==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA100_5==GEN) ) {s = 4;}

                        else if ( (LA100_5==COLON||LA100_5==MINUS||(LA100_5>=HASH && LA100_5<=DOT)) && (synpred5_Css3())) {s = 11;}

                        else if ( (LA100_5==LBRACE) && (synpred5_Css3())) {s = 13;}

                         
                        input.seek(index100_5);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA100_14 = input.LA(1);

                         
                        int index100_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index100_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA100_6 = input.LA(1);

                         
                        int index100_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA100_6==WS||(LA100_6>=NL && LA100_6<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA100_6==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA100_6==IDENT) ) {s = 14;}

                        else if ( (LA100_6==LBRACE) && (synpred5_Css3())) {s = 13;}

                        else if ( (LA100_6==COLON||LA100_6==MINUS||(LA100_6>=HASH && LA100_6<=DOT)) && (synpred5_Css3())) {s = 11;}

                        else if ( ((LA100_6>=NOT && LA100_6<=GEN)) ) {s = 4;}

                         
                        input.seek(index100_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA100_12 = input.LA(1);

                         
                        int index100_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index100_12);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA100_0 = input.LA(1);

                         
                        int index100_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA100_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA100_0==IDENT) ) {s = 2;}

                        else if ( (LA100_0==HASH) ) {s = 3;}

                        else if ( (LA100_0==GEN||(LA100_0>=LBRACKET && LA100_0<=PIPE)||LA100_0==LESS_AND) ) {s = 4;}

                        else if ( (LA100_0==DOT) ) {s = 5;}

                        else if ( (LA100_0==COLON) ) {s = 6;}

                        else if ( (LA100_0==MINUS) && (synpred5_Css3())) {s = 7;}

                         
                        input.seek(index100_0);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA100_3 = input.LA(1);

                         
                        int index100_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index100_3);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA100_2 = input.LA(1);

                         
                        int index100_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index100_2);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA100_1 = input.LA(1);

                         
                        int index100_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA100_1==LBRACE) && (synpred5_Css3())) {s = 8;}

                        else if ( (LA100_1==NAME) ) {s = 4;}

                        else if ( (LA100_1==WS||(LA100_1>=NL && LA100_1<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA100_1==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA100_1==IDENT||LA100_1==COLON||LA100_1==MINUS||(LA100_1>=HASH && LA100_1<=DOT)) && (synpred5_Css3())) {s = 11;}

                         
                        input.seek(index100_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 100, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA103_eotS =
        "\21\uffff";
    static final String DFA103_eofS =
        "\21\uffff";
    static final String DFA103_minS =
        "\1\6\7\uffff\5\0\4\uffff";
    static final String DFA103_maxS =
        "\1\76\7\uffff\5\0\4\uffff";
    static final String DFA103_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA103_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA103_transitionS = {
            "\1\1\5\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\1\14\1\1\34"+
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

    static final short[] DFA103_eot = DFA.unpackEncodedString(DFA103_eotS);
    static final short[] DFA103_eof = DFA.unpackEncodedString(DFA103_eofS);
    static final char[] DFA103_min = DFA.unpackEncodedStringToUnsignedChars(DFA103_minS);
    static final char[] DFA103_max = DFA.unpackEncodedStringToUnsignedChars(DFA103_maxS);
    static final short[] DFA103_accept = DFA.unpackEncodedString(DFA103_acceptS);
    static final short[] DFA103_special = DFA.unpackEncodedString(DFA103_specialS);
    static final short[][] DFA103_transition;

    static {
        int numStates = DFA103_transitionS.length;
        DFA103_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA103_transition[i] = DFA.unpackEncodedString(DFA103_transitionS[i]);
        }
    }

    class DFA103 extends DFA {

        public DFA103(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 103;
            this.eot = DFA103_eot;
            this.eof = DFA103_eof;
            this.min = DFA103_min;
            this.max = DFA103_max;
            this.accept = DFA103_accept;
            this.special = DFA103_special;
            this.transition = DFA103_transition;
        }
        public String getDescription() {
            return "()* loopback of 590:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA103_8 = input.LA(1);

                         
                        int index103_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA103_9 = input.LA(1);

                         
                        int index103_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA103_10 = input.LA(1);

                         
                        int index103_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA103_11 = input.LA(1);

                         
                        int index103_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA103_12 = input.LA(1);

                         
                        int index103_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 103, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA121_eotS =
        "\4\uffff";
    static final String DFA121_eofS =
        "\4\uffff";
    static final String DFA121_minS =
        "\2\6\2\uffff";
    static final String DFA121_maxS =
        "\2\122\2\uffff";
    static final String DFA121_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA121_specialS =
        "\4\uffff}>";
    static final String[] DFA121_transitionS = {
            "\1\3\5\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "\1\3\5\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA121_eot = DFA.unpackEncodedString(DFA121_eotS);
    static final short[] DFA121_eof = DFA.unpackEncodedString(DFA121_eofS);
    static final char[] DFA121_min = DFA.unpackEncodedStringToUnsignedChars(DFA121_minS);
    static final char[] DFA121_max = DFA.unpackEncodedStringToUnsignedChars(DFA121_maxS);
    static final short[] DFA121_accept = DFA.unpackEncodedString(DFA121_acceptS);
    static final short[] DFA121_special = DFA.unpackEncodedString(DFA121_specialS);
    static final short[][] DFA121_transition;

    static {
        int numStates = DFA121_transitionS.length;
        DFA121_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA121_transition[i] = DFA.unpackEncodedString(DFA121_transitionS[i]);
        }
    }

    class DFA121 extends DFA {

        public DFA121(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 121;
            this.eot = DFA121_eot;
            this.eof = DFA121_eof;
            this.min = DFA121_min;
            this.max = DFA121_max;
            this.accept = DFA121_accept;
            this.special = DFA121_special;
            this.transition = DFA121_transition;
        }
        public String getDescription() {
            return "690:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA131_eotS =
        "\17\uffff";
    static final String DFA131_eofS =
        "\17\uffff";
    static final String DFA131_minS =
        "\2\6\10\0\1\uffff\1\6\2\0\1\uffff";
    static final String DFA131_maxS =
        "\2\124\10\0\1\uffff\1\124\2\0\1\uffff";
    static final String DFA131_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA131_specialS =
        "\2\uffff\1\11\1\6\1\10\1\3\1\0\1\7\1\1\1\5\2\uffff\1\2\1\4\1\uffff}>";
    static final String[] DFA131_transitionS = {
            "\1\4\1\3\1\6\2\uffff\1\10\6\uffff\1\5\1\12\2\uffff\1\10\6\uffff"+
            "\1\2\24\uffff\1\1\2\uffff\1\1\1\uffff\1\7\17\uffff\12\2\3\uffff"+
            "\1\11",
            "\1\4\1\3\1\6\2\uffff\1\14\6\uffff\1\5\3\uffff\1\14\1\13\5\uffff"+
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
            "\1\4\1\3\1\6\2\uffff\1\14\6\uffff\1\5\3\uffff\1\14\1\13\5\uffff"+
            "\1\2\31\uffff\1\7\17\uffff\12\2\2\13\1\uffff\1\15",
            "\1\uffff",
            "\1\uffff",
            ""
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
            return "717:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA131_6 = input.LA(1);

                         
                        int index131_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA131_8 = input.LA(1);

                         
                        int index131_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index131_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA131_12 = input.LA(1);

                         
                        int index131_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_12);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA131_5 = input.LA(1);

                         
                        int index131_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA131_13 = input.LA(1);

                         
                        int index131_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_13);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA131_9 = input.LA(1);

                         
                        int index131_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index131_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA131_3 = input.LA(1);

                         
                        int index131_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_3);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA131_7 = input.LA(1);

                         
                        int index131_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA131_4 = input.LA(1);

                         
                        int index131_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_4);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA131_2 = input.LA(1);

                         
                        int index131_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index131_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 131, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA135_eotS =
        "\12\uffff";
    static final String DFA135_eofS =
        "\12\uffff";
    static final String DFA135_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA135_maxS =
        "\1\124\1\uffff\1\124\1\uffff\2\124\1\6\1\124\2\122";
    static final String DFA135_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA135_specialS =
        "\12\uffff}>";
    static final String[] DFA135_transitionS = {
            "\1\1\3\3\2\uffff\1\3\2\1\1\2\3\uffff\1\3\2\uffff\1\1\1\3\6\uffff"+
            "\1\3\23\uffff\2\3\2\uffff\1\3\1\uffff\1\3\16\uffff\1\1\12\3"+
            "\3\uffff\1\3",
            "",
            "\1\5\2\3\2\uffff\1\3\6\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
            "\3\24\uffff\1\3\2\uffff\1\3\1\uffff\1\3\17\uffff\12\3\2\4\1"+
            "\uffff\1\3",
            "",
            "\1\5\2\3\2\uffff\1\3\6\uffff\1\3\3\uffff\1\3\1\4\5\uffff\1"+
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

    static final short[] DFA135_eot = DFA.unpackEncodedString(DFA135_eotS);
    static final short[] DFA135_eof = DFA.unpackEncodedString(DFA135_eofS);
    static final char[] DFA135_min = DFA.unpackEncodedStringToUnsignedChars(DFA135_minS);
    static final char[] DFA135_max = DFA.unpackEncodedStringToUnsignedChars(DFA135_maxS);
    static final short[] DFA135_accept = DFA.unpackEncodedString(DFA135_acceptS);
    static final short[] DFA135_special = DFA.unpackEncodedString(DFA135_specialS);
    static final short[][] DFA135_transition;

    static {
        int numStates = DFA135_transitionS.length;
        DFA135_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA135_transition[i] = DFA.unpackEncodedString(DFA135_transitionS[i]);
        }
    }

    class DFA135 extends DFA {

        public DFA135(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 135;
            this.eot = DFA135_eot;
            this.eof = DFA135_eof;
            this.min = DFA135_min;
            this.max = DFA135_max;
            this.accept = DFA135_accept;
            this.special = DFA135_special;
            this.transition = DFA135_transition;
        }
        public String getDescription() {
            return "()* loopback of 777:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA138_eotS =
        "\13\uffff";
    static final String DFA138_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA138_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA138_maxS =
        "\1\124\2\uffff\1\124\5\uffff\1\124\1\uffff";
    static final String DFA138_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA138_specialS =
        "\13\uffff}>";
    static final String[] DFA138_transitionS = {
            "\1\3\1\2\1\5\2\uffff\1\7\6\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
            "\31\uffff\1\6\17\uffff\12\1\3\uffff\1\7",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\2\10\2\12\1\11\5\uffff\1\12"+
            "\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\1\10\2\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12",
            "",
            "",
            "",
            "",
            "",
            "\4\12\2\uffff\4\12\3\uffff\1\12\1\10\1\uffff\2\12\1\11\5\uffff"+
            "\1\12\23\uffff\2\12\2\uffff\1\12\1\uffff\1\12\3\uffff\1\12\12"+
            "\uffff\13\12\2\11\2\12",
            ""
    };

    static final short[] DFA138_eot = DFA.unpackEncodedString(DFA138_eotS);
    static final short[] DFA138_eof = DFA.unpackEncodedString(DFA138_eofS);
    static final char[] DFA138_min = DFA.unpackEncodedStringToUnsignedChars(DFA138_minS);
    static final char[] DFA138_max = DFA.unpackEncodedStringToUnsignedChars(DFA138_maxS);
    static final short[] DFA138_accept = DFA.unpackEncodedString(DFA138_acceptS);
    static final short[] DFA138_special = DFA.unpackEncodedString(DFA138_specialS);
    static final short[][] DFA138_transition;

    static {
        int numStates = DFA138_transitionS.length;
        DFA138_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA138_transition[i] = DFA.unpackEncodedString(DFA138_transitionS[i]);
        }
    }

    class DFA138 extends DFA {

        public DFA138(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 138;
            this.eot = DFA138_eot;
            this.eof = DFA138_eof;
            this.min = DFA138_min;
            this.max = DFA138_max;
            this.accept = DFA138_accept;
            this.special = DFA138_special;
            this.transition = DFA138_transition;
        }
        public String getDescription() {
            return "782:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA144_eotS =
        "\10\uffff";
    static final String DFA144_eofS =
        "\10\uffff";
    static final String DFA144_minS =
        "\1\6\1\uffff\3\6\1\uffff\2\23";
    static final String DFA144_maxS =
        "\1\124\1\uffff\2\124\1\6\1\uffff\2\122";
    static final String DFA144_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA144_specialS =
        "\10\uffff}>";
    static final String[] DFA144_transitionS = {
            "\1\2\2\1\2\uffff\1\1\6\uffff\1\1\3\uffff\1\1\6\uffff\1\1\24"+
            "\uffff\1\1\2\uffff\1\1\1\uffff\1\1\17\uffff\12\1\3\uffff\1\1",
            "",
            "\3\1\2\uffff\1\1\2\uffff\1\1\3\uffff\5\1\1\3\5\uffff\1\1\23"+
            "\uffff\2\1\2\uffff\1\1\1\uffff\1\1\1\4\6\uffff\1\5\7\uffff\12"+
            "\1\2\3\1\uffff\1\1",
            "\3\1\2\uffff\1\1\2\uffff\1\1\3\uffff\2\1\1\uffff\2\1\1\3\5"+
            "\uffff\1\1\23\uffff\2\1\2\uffff\1\1\1\uffff\1\1\7\uffff\1\5"+
            "\7\uffff\12\1\2\3\1\uffff\1\1",
            "\1\6",
            "",
            "\1\1\3\uffff\1\7\40\uffff\1\4\6\uffff\1\5\21\uffff\2\7",
            "\1\1\3\uffff\1\7\47\uffff\1\5\21\uffff\2\7"
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
            return "810:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA165_eotS =
        "\36\uffff";
    static final String DFA165_eofS =
        "\36\uffff";
    static final String DFA165_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA165_maxS =
        "\1\124\1\uffff\2\124\10\uffff\1\124\10\0\1\124\10\0";
    static final String DFA165_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA165_specialS =
        "\1\0\14\uffff\1\15\1\14\1\13\1\17\1\6\1\10\1\12\1\11\1\uffff\1\20"+
        "\1\16\1\3\1\7\1\2\1\4\1\5\1\1}>";
    static final String[] DFA165_transitionS = {
            "\1\1\1\6\1\5\1\10\2\uffff\1\12\3\1\3\uffff\1\7\2\uffff\1\1\1"+
            "\12\6\uffff\1\4\23\uffff\1\1\1\2\2\uffff\1\3\1\uffff\1\11\3"+
            "\uffff\1\1\12\uffff\1\1\12\4\2\uffff\1\1\1\13",
            "",
            "\1\17\1\16\1\21\2\uffff\1\23\6\uffff\1\20\1\1\2\uffff\1\23"+
            "\1\14\5\uffff\1\15\24\uffff\1\1\2\uffff\1\1\1\uffff\1\22\17"+
            "\uffff\12\15\2\14\1\uffff\1\24",
            "\1\30\1\27\1\32\2\uffff\1\34\6\uffff\1\31\1\1\2\uffff\1\34"+
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
            "\1\17\1\16\1\21\2\uffff\1\23\6\uffff\1\20\1\1\2\uffff\1\23"+
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
            "\1\30\1\27\1\32\2\uffff\1\34\6\uffff\1\31\1\1\2\uffff\1\34"+
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

    static final short[] DFA165_eot = DFA.unpackEncodedString(DFA165_eotS);
    static final short[] DFA165_eof = DFA.unpackEncodedString(DFA165_eofS);
    static final char[] DFA165_min = DFA.unpackEncodedStringToUnsignedChars(DFA165_minS);
    static final char[] DFA165_max = DFA.unpackEncodedStringToUnsignedChars(DFA165_maxS);
    static final short[] DFA165_accept = DFA.unpackEncodedString(DFA165_acceptS);
    static final short[] DFA165_special = DFA.unpackEncodedString(DFA165_specialS);
    static final short[][] DFA165_transition;

    static {
        int numStates = DFA165_transitionS.length;
        DFA165_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA165_transition[i] = DFA.unpackEncodedString(DFA165_transitionS[i]);
        }
    }

    class DFA165 extends DFA {

        public DFA165(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 165;
            this.eot = DFA165_eot;
            this.eof = DFA165_eof;
            this.min = DFA165_min;
            this.max = DFA165_max;
            this.accept = DFA165_accept;
            this.special = DFA165_special;
            this.transition = DFA165_transition;
        }
        public String getDescription() {
            return "()* loopback of 891:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA165_0 = input.LA(1);

                         
                        int index165_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA165_0==SEMI||(LA165_0>=LBRACE && LA165_0<=COMMA)||LA165_0==RPAREN||LA165_0==SOLIDUS||LA165_0==STAR||LA165_0==IMPORTANT_SYM||LA165_0==SASS_DEFAULT) ) {s = 1;}

                        else if ( (LA165_0==PLUS) ) {s = 2;}

                        else if ( (LA165_0==MINUS) ) {s = 3;}

                        else if ( (LA165_0==PERCENTAGE||(LA165_0>=NUMBER && LA165_0<=DIMENSION)) && (synpred11_Css3())) {s = 4;}

                        else if ( (LA165_0==STRING) && (synpred11_Css3())) {s = 5;}

                        else if ( (LA165_0==IDENT) && (synpred11_Css3())) {s = 6;}

                        else if ( (LA165_0==GEN) && (synpred11_Css3())) {s = 7;}

                        else if ( (LA165_0==URI) && (synpred11_Css3())) {s = 8;}

                        else if ( (LA165_0==HASH) && (synpred11_Css3())) {s = 9;}

                        else if ( (LA165_0==MEDIA_SYM||LA165_0==AT_IDENT) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA165_0==SASS_VAR) && (synpred11_Css3())) {s = 11;}

                         
                        input.seek(index165_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA165_29 = input.LA(1);

                         
                        int index165_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_29);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA165_26 = input.LA(1);

                         
                        int index165_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_26);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA165_24 = input.LA(1);

                         
                        int index165_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_24);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA165_27 = input.LA(1);

                         
                        int index165_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_27);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA165_28 = input.LA(1);

                         
                        int index165_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_28);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA165_17 = input.LA(1);

                         
                        int index165_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_17);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA165_25 = input.LA(1);

                         
                        int index165_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_25);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA165_18 = input.LA(1);

                         
                        int index165_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_18);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA165_20 = input.LA(1);

                         
                        int index165_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_20);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA165_19 = input.LA(1);

                         
                        int index165_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_19);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA165_15 = input.LA(1);

                         
                        int index165_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_15);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA165_14 = input.LA(1);

                         
                        int index165_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA165_13 = input.LA(1);

                         
                        int index165_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA165_23 = input.LA(1);

                         
                        int index165_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_23);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA165_16 = input.LA(1);

                         
                        int index165_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA165_22 = input.LA(1);

                         
                        int index165_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index165_22);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 165, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA169_eotS =
        "\13\uffff";
    static final String DFA169_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA169_minS =
        "\1\6\2\uffff\1\23\5\uffff\1\23\1\uffff";
    static final String DFA169_maxS =
        "\1\124\2\uffff\1\122\5\uffff\1\122\1\uffff";
    static final String DFA169_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA169_specialS =
        "\13\uffff}>";
    static final String[] DFA169_transitionS = {
            "\1\3\1\2\1\5\2\uffff\1\7\6\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
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

    static final short[] DFA169_eot = DFA.unpackEncodedString(DFA169_eotS);
    static final short[] DFA169_eof = DFA.unpackEncodedString(DFA169_eofS);
    static final char[] DFA169_min = DFA.unpackEncodedStringToUnsignedChars(DFA169_minS);
    static final char[] DFA169_max = DFA.unpackEncodedStringToUnsignedChars(DFA169_maxS);
    static final short[] DFA169_accept = DFA.unpackEncodedString(DFA169_acceptS);
    static final short[] DFA169_special = DFA.unpackEncodedString(DFA169_specialS);
    static final short[][] DFA169_transition;

    static {
        int numStates = DFA169_transitionS.length;
        DFA169_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA169_transition[i] = DFA.unpackEncodedString(DFA169_transitionS[i]);
        }
    }

    class DFA169 extends DFA {

        public DFA169(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 169;
            this.eot = DFA169_eot;
            this.eof = DFA169_eof;
            this.min = DFA169_min;
            this.max = DFA169_max;
            this.accept = DFA169_accept;
            this.special = DFA169_special;
            this.transition = DFA169_transition;
        }
        public String getDescription() {
            return "898:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA184_eotS =
        "\4\uffff";
    static final String DFA184_eofS =
        "\4\uffff";
    static final String DFA184_minS =
        "\2\5\2\uffff";
    static final String DFA184_maxS =
        "\2\122\2\uffff";
    static final String DFA184_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA184_specialS =
        "\4\uffff}>";
    static final String[] DFA184_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "",
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
            return "946:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA189_eotS =
        "\5\uffff";
    static final String DFA189_eofS =
        "\5\uffff";
    static final String DFA189_minS =
        "\1\5\1\13\1\uffff\1\13\1\uffff";
    static final String DFA189_maxS =
        "\1\25\1\130\1\uffff\1\130\1\uffff";
    static final String DFA189_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA189_specialS =
        "\5\uffff}>";
    static final String[] DFA189_transitionS = {
            "\1\1\10\uffff\1\1\6\uffff\1\2",
            "\1\4\12\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\12\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            ""
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
            return "()* loopback of 966:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA209_eotS =
        "\14\uffff";
    static final String DFA209_eofS =
        "\14\uffff";
    static final String DFA209_minS =
        "\2\6\1\uffff\2\6\2\15\1\6\1\15\1\uffff\1\6\1\uffff";
    static final String DFA209_maxS =
        "\1\70\1\122\1\uffff\2\126\4\122\1\uffff\1\122\1\uffff";
    static final String DFA209_acceptS =
        "\2\uffff\1\2\6\uffff\1\1\1\uffff\1\1";
    static final String DFA209_specialS =
        "\5\uffff\1\0\1\4\1\3\1\1\1\uffff\1\2\1\uffff}>";
    static final String[] DFA209_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\5\uffff\1\3\7\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\4\uffff\1\5\1\uffff\1\2\4\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\4\uffff\1\5\1\uffff\1\2\4\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "",
            "\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
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
            return "1033:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA209_5 = input.LA(1);

                         
                        int index209_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA209_5==WS||(LA209_5>=NL && LA209_5<=COMMENT)) ) {s = 8;}

                        else if ( (LA209_5==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA209_5==COLON) ) {s = 2;}

                         
                        input.seek(index209_5);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA209_8 = input.LA(1);

                         
                        int index209_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA209_8==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA209_8==WS||(LA209_8>=NL && LA209_8<=COMMENT)) ) {s = 8;}

                        else if ( (LA209_8==COLON) ) {s = 2;}

                         
                        input.seek(index209_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA209_10 = input.LA(1);

                         
                        int index209_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA209_10==LPAREN) && (synpred12_Css3())) {s = 11;}

                        else if ( (LA209_10==WS||(LA209_10>=NL && LA209_10<=COMMENT)) ) {s = 10;}

                        else if ( (LA209_10==IDENT||LA209_10==LBRACE||LA209_10==COMMA||LA209_10==GEN||LA209_10==COLON||(LA209_10>=PLUS && LA209_10<=PIPE)||LA209_10==LESS_AND) ) {s = 2;}

                         
                        input.seek(index209_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA209_7 = input.LA(1);

                         
                        int index209_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA209_7==WS||(LA209_7>=NL && LA209_7<=COMMENT)) ) {s = 10;}

                        else if ( (LA209_7==LPAREN) && (synpred12_Css3())) {s = 11;}

                        else if ( (LA209_7==IDENT||LA209_7==LBRACE||LA209_7==COMMA||LA209_7==GEN||LA209_7==COLON||(LA209_7>=PLUS && LA209_7<=PIPE)||LA209_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index209_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA209_6 = input.LA(1);

                         
                        int index209_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA209_6==WS||(LA209_6>=NL && LA209_6<=COMMENT)) ) {s = 8;}

                        else if ( (LA209_6==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA209_6==COLON) ) {s = 2;}

                         
                        input.seek(index209_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 209, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA212_eotS =
        "\4\uffff";
    static final String DFA212_eofS =
        "\4\uffff";
    static final String DFA212_minS =
        "\2\6\2\uffff";
    static final String DFA212_maxS =
        "\2\122\2\uffff";
    static final String DFA212_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA212_specialS =
        "\4\uffff}>";
    static final String[] DFA212_transitionS = {
            "\1\3\5\uffff\1\2\7\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
            "\1\3\5\uffff\1\2\7\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
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
            return "()* loopback of 1038:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA211_eotS =
        "\14\uffff";
    static final String DFA211_eofS =
        "\14\uffff";
    static final String DFA211_minS =
        "\2\6\1\uffff\2\6\2\15\1\6\1\15\1\uffff\1\6\1\uffff";
    static final String DFA211_maxS =
        "\1\70\1\122\1\uffff\2\126\4\122\1\uffff\1\122\1\uffff";
    static final String DFA211_acceptS =
        "\2\uffff\1\2\6\uffff\1\1\1\uffff\1\1";
    static final String DFA211_specialS =
        "\5\uffff\1\0\1\4\1\3\1\1\1\uffff\1\2\1\uffff}>";
    static final String[] DFA211_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\5\uffff\1\3\7\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\4\uffff\1\5\1\uffff\1\2\4\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\4\uffff\1\5\1\uffff\1\2\4\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            "\1\11\6\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "",
            "\1\2\5\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            ""
    };

    static final short[] DFA211_eot = DFA.unpackEncodedString(DFA211_eotS);
    static final short[] DFA211_eof = DFA.unpackEncodedString(DFA211_eofS);
    static final char[] DFA211_min = DFA.unpackEncodedStringToUnsignedChars(DFA211_minS);
    static final char[] DFA211_max = DFA.unpackEncodedStringToUnsignedChars(DFA211_maxS);
    static final short[] DFA211_accept = DFA.unpackEncodedString(DFA211_acceptS);
    static final short[] DFA211_special = DFA.unpackEncodedString(DFA211_specialS);
    static final short[][] DFA211_transition;

    static {
        int numStates = DFA211_transitionS.length;
        DFA211_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA211_transition[i] = DFA.unpackEncodedString(DFA211_transitionS[i]);
        }
    }

    class DFA211 extends DFA {

        public DFA211(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 211;
            this.eot = DFA211_eot;
            this.eof = DFA211_eof;
            this.min = DFA211_min;
            this.max = DFA211_max;
            this.accept = DFA211_accept;
            this.special = DFA211_special;
            this.transition = DFA211_transition;
        }
        public String getDescription() {
            return "1040:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA211_5 = input.LA(1);

                         
                        int index211_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA211_5==WS||(LA211_5>=NL && LA211_5<=COMMENT)) ) {s = 8;}

                        else if ( (LA211_5==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA211_5==COLON) ) {s = 2;}

                         
                        input.seek(index211_5);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA211_8 = input.LA(1);

                         
                        int index211_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA211_8==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA211_8==WS||(LA211_8>=NL && LA211_8<=COMMENT)) ) {s = 8;}

                        else if ( (LA211_8==COLON) ) {s = 2;}

                         
                        input.seek(index211_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA211_10 = input.LA(1);

                         
                        int index211_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA211_10==LPAREN) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA211_10==WS||(LA211_10>=NL && LA211_10<=COMMENT)) ) {s = 10;}

                        else if ( (LA211_10==IDENT||LA211_10==LBRACE||LA211_10==COMMA||LA211_10==GEN||LA211_10==COLON||(LA211_10>=PLUS && LA211_10<=PIPE)||LA211_10==LESS_AND) ) {s = 2;}

                         
                        input.seek(index211_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA211_7 = input.LA(1);

                         
                        int index211_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA211_7==WS||(LA211_7>=NL && LA211_7<=COMMENT)) ) {s = 10;}

                        else if ( (LA211_7==LPAREN) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA211_7==IDENT||LA211_7==LBRACE||LA211_7==COMMA||LA211_7==GEN||LA211_7==COLON||(LA211_7>=PLUS && LA211_7<=PIPE)||LA211_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index211_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA211_6 = input.LA(1);

                         
                        int index211_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA211_6==WS||(LA211_6>=NL && LA211_6<=COMMENT)) ) {s = 8;}

                        else if ( (LA211_6==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA211_6==COLON) ) {s = 2;}

                         
                        input.seek(index211_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 211, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA225_eotS =
        "\11\uffff";
    static final String DFA225_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA225_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA225_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA225_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA225_specialS =
        "\11\uffff}>";
    static final String[] DFA225_transitionS = {
            "\1\3\1\uffff\1\2\5\3\2\uffff\6\3\1\uffff\40\3\1\2\1\1\2\2\111"+
            "\3",
            "\1\3\1\uffff\1\10\5\3\1\4\1\uffff\6\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\6\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3",
            "",
            "",
            "\1\3\1\uffff\6\3\2\uffff\164\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\11\3\1\6\35\3\1\10\1\7\2\10\30"+
            "\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\1\4\1\uffff\6\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\6\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3"
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
            return "561:3: synpred1_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA228_eotS =
        "\11\uffff";
    static final String DFA228_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA228_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA228_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA228_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA228_specialS =
        "\11\uffff}>";
    static final String[] DFA228_transitionS = {
            "\1\3\1\uffff\1\2\5\3\2\uffff\6\3\1\uffff\40\3\1\2\1\1\2\2\111"+
            "\3",
            "\1\3\1\uffff\1\10\5\3\1\4\1\uffff\6\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\6\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3",
            "",
            "",
            "\1\3\1\uffff\6\3\2\uffff\164\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\11\3\1\6\35\3\1\10\1\7\2\10\30"+
            "\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\1\4\1\uffff\6\3\1\5\2\3\1\6\35\3\1\10"+
            "\1\7\2\10\30\3\2\6\57\3",
            "\1\3\1\uffff\1\10\5\3\2\uffff\6\3\1\5\2\3\1\6\35\3\1\10\1\7"+
            "\2\10\30\3\2\6\57\3"
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
            return "563:3: synpred2_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x5FE00001D1540E50L,0x0000000000700000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x5FE00001D1D40C50L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x5FE00001D1540C50L,0x0000000000700000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x5FE00001D1540850L,0x0000000000700000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x5FE00001D1540840L,0x0000000000700000L});
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
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000008001C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x00000000008F0060L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x00000000000F0060L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x00000000008F1040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x00000000000F1040L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x5FE0000151D42040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x5FE0000151542040L,0x0000000000200000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x5FE0000151D42040L,0x0000000000260000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x5FE0000151D42040L,0x0000000000260000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x5FE0000151D42040L,0x0000000000260000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media423 = new BitSet(new long[]{0x5FE0000151D42040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_media427 = new BitSet(new long[]{0x5FE0000151542040L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACE_in_media441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList457 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList461 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList463 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList466 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery485 = new BitSet(new long[]{0x0000000000870040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery487 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery494 = new BitSet(new long[]{0x0000000000808002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery496 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery501 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery503 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery506 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery514 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery518 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery520 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery523 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression578 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression580 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression583 = new BitSet(new long[]{0x0000000000B00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression585 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression590 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_mediaExpression592 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_mediaExpression595 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression600 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body634 = new BitSet(new long[]{0x5FE00001D1D40842L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_body636 = new BitSet(new long[]{0x5FE00001D1540842L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_bodyItem661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule816 = new BitSet(new long[]{0x00000000008010C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule818 = new BitSet(new long[]{0x00000000008010C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule823 = new BitSet(new long[]{0x0000000000801000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule825 = new BitSet(new long[]{0x0000000000801000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule840 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule852 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document878 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document880 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document885 = new BitSet(new long[]{0x0000000000805000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document887 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document893 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document895 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document898 = new BitSet(new long[]{0x0000000000805000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document900 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document907 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_moz_document909 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_body_in_moz_document914 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document919 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes960 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes962 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes965 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes967 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes972 = new BitSet(new long[]{0x0000000020802040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes974 = new BitSet(new long[]{0x0000000020002040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes981 = new BitSet(new long[]{0x0000000020802040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes983 = new BitSet(new long[]{0x0000000020002040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1003 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1005 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1010 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1013 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1016 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1020 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1038 = new BitSet(new long[]{0x0000000000804002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1050 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1053 = new BitSet(new long[]{0x0000000020800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1055 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1058 = new BitSet(new long[]{0x0000000000804002L,0x0000000000060000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1087 = new BitSet(new long[]{0x0000000000901040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1089 = new BitSet(new long[]{0x0000000000101040L});
    public static final BitSet FOLLOW_IDENT_in_page1094 = new BitSet(new long[]{0x0000000000901000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1096 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1103 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1105 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page1118 = new BitSet(new long[]{0x09E1FFFE00C42860L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1120 = new BitSet(new long[]{0x09E1FFFE00442860L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1175 = new BitSet(new long[]{0x0000000000002020L});
    public static final BitSet FOLLOW_margin_in_page1177 = new BitSet(new long[]{0x0000000000802020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1179 = new BitSet(new long[]{0x0000000000002020L});
    public static final BitSet FOLLOW_SEMI_in_page1185 = new BitSet(new long[]{0x09E1FFFE00C42860L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1187 = new BitSet(new long[]{0x09E1FFFE00442860L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1191 = new BitSet(new long[]{0x0000000000002020L});
    public static final BitSet FOLLOW_margin_in_page1193 = new BitSet(new long[]{0x0000000000802020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1195 = new BitSet(new long[]{0x0000000000002020L});
    public static final BitSet FOLLOW_RBRACE_in_page1210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1231 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1233 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1236 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1238 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1249 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1251 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1254 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1258 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1289 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fontFace1291 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1302 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_fontFace1304 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1307 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1311 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1336 = new BitSet(new long[]{0x0000000000801000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_margin1338 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1341 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_margin1343 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1346 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_margin1348 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1579 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1631 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1642 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1653 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property1716 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_property1720 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_property1726 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_property1729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule1773 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1806 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1829 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_rule1831 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule1834 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_rule1848 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1992 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations1994 = new BitSet(new long[]{0x5FE00001D1D40842L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations1996 = new BitSet(new long[]{0x5FE00001D1540842L,0x0000000000700000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2040 = new BitSet(new long[]{0x5FE00001D1D40842L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2042 = new BitSet(new long[]{0x5FE00001D1540842L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_declarations2079 = new BitSet(new long[]{0x5FE00001D1D40842L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2081 = new BitSet(new long[]{0x5FE00001D1540842L,0x0000000000700000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2120 = new BitSet(new long[]{0x5FE00001D1D40842L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2122 = new BitSet(new long[]{0x5FE00001D1540842L,0x0000000000700000L});
    public static final BitSet FOLLOW_declaration_in_declarations2166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2227 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2244 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2247 = new BitSet(new long[]{0x5FE0000000940040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2249 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2252 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2279 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_combinator_in_selector2282 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2284 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2317 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2324 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2326 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2345 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2347 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2459 = new BitSet(new long[]{0x5800000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2465 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2485 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2489 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId2594 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2596 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2624 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass2626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2698 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2705 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2708 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2719 = new BitSet(new long[]{0x8000000000800000L,0x000000000006003FL});
    public static final BitSet FOLLOW_ws_in_slAttribute2721 = new BitSet(new long[]{0x8000000000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_set_in_slAttribute2763 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2943 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2962 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060020L});
    public static final BitSet FOLLOW_ws_in_slAttribute2980 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3099 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo3163 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3220 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3223 = new BitSet(new long[]{0x08A4000020E409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_pseudo3225 = new BitSet(new long[]{0x08A4000020E409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_pseudo3230 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo3234 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3318 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3320 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3323 = new BitSet(new long[]{0x5FE0000000B40040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_pseudo3325 = new BitSet(new long[]{0x5FE0000000340040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3328 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3375 = new BitSet(new long[]{0x09E0000000440840L,0x0000000000100000L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_declaration3421 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_property_in_declaration3442 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration3455 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_declaration3457 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3460 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_prio_in_declaration3463 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_declaration3465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue3548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3586 = new BitSet(new long[]{0xF7FDFFFFFFBFEFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI3733 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3809 = new BitSet(new long[]{0x00A6000020C449C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_operator_in_expression3814 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_expression3816 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_expression3821 = new BitSet(new long[]{0x00A6000020C449C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_unaryOperator_in_term3846 = new BitSet(new long[]{0x0080000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_term3848 = new BitSet(new long[]{0x00800000204409C0L,0x000000000011FF80L});
    public static final BitSet FOLLOW_set_in_term3872 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_term4072 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_term4080 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_term4088 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_term4096 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_term4104 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_term4112 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_term4122 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_term4134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4150 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_function4152 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function4157 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4159 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_function4169 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function4187 = new BitSet(new long[]{0x0000000000204000L});
    public static final BitSet FOLLOW_COMMA_in_function4190 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4192 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttribute_in_function4195 = new BitSet(new long[]{0x0000000000204000L});
    public static final BitSet FOLLOW_RPAREN_in_function4216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4264 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName4266 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4270 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4273 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4275 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4298 = new BitSet(new long[]{0x8000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4300 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4303 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4305 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4323 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4326 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4328 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4381 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4429 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4431 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4434 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4436 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4439 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4468 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4470 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4473 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4475 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4478 = new BitSet(new long[]{0x0000000000000020L,0x0000000000080000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4481 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4483 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable4521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable4553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression4577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4597 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp4611 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4613 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4616 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp4629 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4631 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4634 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4667 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp4680 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4682 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4685 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp4699 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4701 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4704 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4737 = new BitSet(new long[]{0x00A4000020C409C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4744 = new BitSet(new long[]{0x00A4000020C409C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp4758 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4760 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp4763 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp4765 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term4805 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_cp_term5005 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term5013 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_cp_term5021 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_cp_term5029 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term5037 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_cp_term5045 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term5053 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_term5065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration5096 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5098 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5100 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5103 = new BitSet(new long[]{0x0000000000600800L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5105 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5108 = new BitSet(new long[]{0x0000000000800002L,0x0000000002060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5110 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5114 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5133 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5135 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5137 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5139 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5143 = new BitSet(new long[]{0x0000000000600800L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5145 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5148 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call5192 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5194 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call5216 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5218 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5220 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5233 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call5236 = new BitSet(new long[]{0x00A4000020E409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call5238 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call5241 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5245 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call5248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name5277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5313 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args5317 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args5325 = new BitSet(new long[]{0x00A4000020C409C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5328 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5370 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_set_in_less_args_list5374 = new BitSet(new long[]{0x0000000000C00800L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5384 = new BitSet(new long[]{0x0000000000400800L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5387 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_set_in_less_args_list5393 = new BitSet(new long[]{0x0000000000800000L,0x0000000001860000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5403 = new BitSet(new long[]{0x0000000000000000L,0x0000000001800000L});
    public static final BitSet FOLLOW_set_in_less_args_list5406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg5460 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5464 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_arg5466 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg5469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded5495 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5497 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5500 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded5504 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5512 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5515 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_NOT_in_less_condition5545 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5547 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition5556 = new BitSet(new long[]{0x0000000000C00840L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_condition5558 = new BitSet(new long[]{0x0000000000400840L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition5584 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5586 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition5617 = new BitSet(new long[]{0x8008000000A00000L,0x000000001C060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5620 = new BitSet(new long[]{0x8008000000800000L,0x000000001C060000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition5623 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_condition5625 = new BitSet(new long[]{0x00A4000020CC09C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition5628 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition5657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition5683 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5685 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition5688 = new BitSet(new long[]{0x0000000000C00800L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5690 = new BitSet(new long[]{0x0000000000400800L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition5693 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5695 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition5698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name5720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5819 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression5847 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression5904 = new BitSet(new long[]{0x01E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5943 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression5979 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6077 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6105 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression6158 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6197 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6233 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var6306 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var6308 = new BitSet(new long[]{0x0000000000C00840L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var6310 = new BitSet(new long[]{0x0000000000400840L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var6315 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var6319 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var6323 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var6326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties6370 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties6372 = new BitSet(new long[]{0x00A4000020CC19C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties6374 = new BitSet(new long[]{0x00A4000020CC19C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties6377 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties6380 = new BitSet(new long[]{0x5FE00001D1D42840L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties6382 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties6385 = new BitSet(new long[]{0x5FE00001D1542840L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties6387 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties6389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css31958 = new BitSet(new long[]{0xFFFFFFFFFFFFCFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred1_Css31970 = new BitSet(new long[]{0xFFFFFFFFFFFFCFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred1_Css31972 = new BitSet(new long[]{0xFFFFFFFFFFFFCFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_SEMI_in_synpred1_Css31982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred1_Css31986 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred1_Css31988 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred2_Css32006 = new BitSet(new long[]{0xFFFFFFFFFFFFCFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Css32018 = new BitSet(new long[]{0xFFFFFFFFFFFFCFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred2_Css32020 = new BitSet(new long[]{0xFFFFFFFFFFFFDFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred2_Css32030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred2_Css32034 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Css32036 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css32066 = new BitSet(new long[]{0xFFFFFFFFFFFFDFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred3_Css32076 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32157 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_RBRACE_in_synpred4_Css32163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32209 = new BitSet(new long[]{0xFFFFFFFFFFFFEFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred5_Css32221 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred6_Css32321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred7_Css32342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_Css32447 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred8_Css32456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred9_Css33396 = new BitSet(new long[]{0xFFFFFFFFFFEFDFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred9_Css33414 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred9_Css33416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred10_Css33502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred11_Css34741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred12_Css35814 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Css35816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred13_Css35938 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred13_Css35940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred14_Css36072 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred14_Css36074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred15_Css36192 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred15_Css36194 = new BitSet(new long[]{0x0000000000000002L});

}