// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-03-04 09:33:12

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
        "invalidRule", "expression", "mediaQuery", "vendorAtRule", "charSet", 
        "mediaType", "fnAttributeValue", "combinator", "margin", "webkitKeyframes", 
        "syncToFollow", "moz_document", "fontFace", "synpred12_Css3", "namespacePrefixName", 
        "scss_interpolation_expression_var", "webkitKeyframesBlock", "syncTo_SEMI", 
        "operator", "synpred2_Css3", "functionName", "cp_mixin_declaration", 
        "pseudo", "synpred1_Css3", "cssClass", "elementSubsequent", "scss_nested_properties", 
        "margin_sym", "synpred5_Css3", "synpred14_Css3", "fnAttribute", 
        "charSetValue", "synpred9_Css3", "generic_at_rule", "slAttributeName", 
        "namespacePrefix", "ws", "less_condition_operator", "media", "elementName", 
        "propertyValue", "esPred", "mediaFeature", "declaration", "less_fn_name", 
        "selectorsGroup", "synpred3_Css3", "hexColor", "styleSheet", "prio", 
        "mediaQueryList", "pseudoPage", "slAttribute", "page", "expressionPredicate", 
        "scss_selector_interpolation_expression", "cp_mixin_call", "unaryOperator", 
        "syncTo_RBRACE", "rule", "synpred11_Css3", "cp_mixin_name", "syncToDeclarationsRule", 
        "synpred7_Css3", "property", "body", "less_arg", "selector", "declarations", 
        "cp_atomExp", "less_args_list", "less_mixin_guarded", "imports", 
        "term", "synpred15_Css3", "cp_additionExp", "importItem", "namespace", 
        "less_function_in_condition", "mediaExpression", "resourceIdentifier", 
        "bodyItem", "mediaQueryOperator", "less_condition", "atRuleId", 
        "slAttributeValue", "namespaces", "function", "synpred8_Css3", "cssId", 
        "typeSelector", "moz_document_function", "fnAttributeName", "cp_multiplyExp", 
        "synpred4_Css3", "cp_expression", "synpred13_Css3", "synpred10_Css3", 
        "simpleSelectorSequence", "counterStyle", "cp_variable", "cp_variable_declaration", 
        "scss_declaration_interpolation_expression", "webkitKeyframeSelectors", 
        "synpred6_Css3", "cp_mixin_call_args", "cp_term"
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
            false, false
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE
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

            dbg.location(365,21);
            pushFollow(FOLLOW_mediaQueryList_in_media441);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(366,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media451); if (state.failed) return ;
            dbg.location(366,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:16: ws
                    {
                    dbg.location(366,16);
                    pushFollow(FOLLOW_ws_in_media453);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(24);}

            dbg.location(367,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:13: ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )*
            try { dbg.enterSubRule(27);

            loop27:
            do {
                int alt27=2;
                try { dbg.enterDecision(27, decisionCanBacktrack[27]);

                int LA27_0 = input.LA(1);

                if ( (LA27_0==IDENT||LA27_0==GEN||LA27_0==COLON||LA27_0==AT_IDENT||LA27_0==MOZ_DOCUMENT_SYM||LA27_0==WEBKIT_KEYFRAMES_SYM||LA27_0==PAGE_SYM||LA27_0==FONT_FACE_SYM||(LA27_0>=MINUS && LA27_0<=PIPE)||LA27_0==LESS_AND||LA27_0==SASS_MIXIN) ) {
                    alt27=1;
                }


                } finally {dbg.exitDecision(27);}

                switch (alt27) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:15: ( rule | page | fontFace | vendorAtRule ) ( ws )?
            	    {
            	    dbg.location(367,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:15: ( rule | page | fontFace | vendorAtRule )
            	    int alt25=4;
            	    try { dbg.enterSubRule(25);
            	    try { dbg.enterDecision(25, decisionCanBacktrack[25]);

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
            	        alt25=1;
            	        }
            	        break;
            	    case PAGE_SYM:
            	        {
            	        alt25=2;
            	        }
            	        break;
            	    case FONT_FACE_SYM:
            	        {
            	        alt25=3;
            	        }
            	        break;
            	    case AT_IDENT:
            	    case MOZ_DOCUMENT_SYM:
            	    case WEBKIT_KEYFRAMES_SYM:
            	        {
            	        alt25=4;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 25, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }

            	    } finally {dbg.exitDecision(25);}

            	    switch (alt25) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:17: rule
            	            {
            	            dbg.location(367,17);
            	            pushFollow(FOLLOW_rule_in_media472);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:24: page
            	            {
            	            dbg.location(367,24);
            	            pushFollow(FOLLOW_page_in_media476);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:31: fontFace
            	            {
            	            dbg.location(367,31);
            	            pushFollow(FOLLOW_fontFace_in_media480);
            	            fontFace();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 4 :
            	            dbg.enterAlt(4);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:42: vendorAtRule
            	            {
            	            dbg.location(367,42);
            	            pushFollow(FOLLOW_vendorAtRule_in_media484);
            	            vendorAtRule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(25);}

            	    dbg.location(367,57);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:57: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:57: ws
            	            {
            	            dbg.location(367,57);
            	            pushFollow(FOLLOW_ws_in_media488);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(26);}


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);
            } finally {dbg.exitSubRule(27);}

            dbg.location(368,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media502); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(369, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(372,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt30=2;
            try { dbg.enterSubRule(30);
            try { dbg.enterDecision(30, decisionCanBacktrack[30]);

            int LA30_0 = input.LA(1);

            if ( (LA30_0==IDENT||(LA30_0>=ONLY && LA30_0<=LPAREN)) ) {
                alt30=1;
            }
            } finally {dbg.exitDecision(30);}

            switch (alt30) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(372,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList518);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(372,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(29);

                    loop29:
                    do {
                        int alt29=2;
                        try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                        int LA29_0 = input.LA(1);

                        if ( (LA29_0==COMMA) ) {
                            alt29=1;
                        }


                        } finally {dbg.exitDecision(29);}

                        switch (alt29) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(372,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList522); if (state.failed) return ;
                    	    dbg.location(372,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ws
                    	            {
                    	            dbg.location(372,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList524);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(28);}

                    	    dbg.location(372,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList527);
                    	    mediaQuery();

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
            } finally {dbg.exitSubRule(30);}


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
            dbg.exitRule(getGrammarFileName(), "mediaQueryList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaQueryList"


    // $ANTLR start "mediaQuery"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(375, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt38=2;
            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

            int LA38_0 = input.LA(1);

            if ( (LA38_0==IDENT||(LA38_0>=ONLY && LA38_0<=GEN)) ) {
                alt38=1;
            }
            else if ( (LA38_0==LPAREN) ) {
                alt38=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(376,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:4: ( mediaQueryOperator ( ws )? )?
                    int alt32=2;
                    try { dbg.enterSubRule(32);
                    try { dbg.enterDecision(32, decisionCanBacktrack[32]);

                    int LA32_0 = input.LA(1);

                    if ( ((LA32_0>=ONLY && LA32_0<=NOT)) ) {
                        alt32=1;
                    }
                    } finally {dbg.exitDecision(32);}

                    switch (alt32) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(376,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery546);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(376,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:24: ws
                                    {
                                    dbg.location(376,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery548);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(31);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(32);}

                    dbg.location(376,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery555);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(376,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:42: ws
                            {
                            dbg.location(376,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery557);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(33);}

                    dbg.location(376,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(35);

                    loop35:
                    do {
                        int alt35=2;
                        try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==AND) ) {
                            alt35=1;
                        }


                        } finally {dbg.exitDecision(35);}

                        switch (alt35) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(376,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery562); if (state.failed) return ;
                    	    dbg.location(376,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:52: ws
                    	            {
                    	            dbg.location(376,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery564);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(34);}

                    	    dbg.location(376,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery567);
                    	    mediaExpression();

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
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(377,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery575);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(377,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:20: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(37);

                    loop37:
                    do {
                        int alt37=2;
                        try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                        int LA37_0 = input.LA(1);

                        if ( (LA37_0==AND) ) {
                            alt37=1;
                        }


                        } finally {dbg.exitDecision(37);}

                        switch (alt37) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(377,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery579); if (state.failed) return ;
                    	    dbg.location(377,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:26: ws
                    	            {
                    	            dbg.location(377,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery581);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(36);}

                    	    dbg.location(377,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery584);
                    	    mediaExpression();

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
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(378, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(381,3);
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
        dbg.location(382, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(385,2);
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
        dbg.location(386, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:1: mediaExpression : LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:5: ( LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:7: LPAREN ( ws )? mediaFeature ( ws )? ( COLON ( ws )? expression )? RPAREN ( ws )?
            {
            dbg.location(389,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression639); if (state.failed) return ;
            dbg.location(389,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:14: ws
                    {
                    dbg.location(389,14);
                    pushFollow(FOLLOW_ws_in_mediaExpression641);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(39);}

            dbg.location(389,18);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression644);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:31: ws
                    {
                    dbg.location(389,31);
                    pushFollow(FOLLOW_ws_in_mediaExpression646);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(40);}

            dbg.location(389,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:35: ( COLON ( ws )? expression )?
            int alt42=2;
            try { dbg.enterSubRule(42);
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

            int LA42_0 = input.LA(1);

            if ( (LA42_0==COLON) ) {
                alt42=1;
            }
            } finally {dbg.exitDecision(42);}

            switch (alt42) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:37: COLON ( ws )? expression
                    {
                    dbg.location(389,37);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression651); if (state.failed) return ;
                    dbg.location(389,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:43: ws
                            {
                            dbg.location(389,43);
                            pushFollow(FOLLOW_ws_in_mediaExpression653);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(41);}

                    dbg.location(389,47);
                    pushFollow(FOLLOW_expression_in_mediaExpression656);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(42);}

            dbg.location(389,61);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression661); if (state.failed) return ;
            dbg.location(389,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:68: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:68: ws
                    {
                    dbg.location(389,68);
                    pushFollow(FOLLOW_ws_in_mediaExpression663);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(43);}


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
            dbg.exitRule(getGrammarFileName(), "mediaExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaExpression"


    // $ANTLR start "mediaFeature"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:4: IDENT
            {
            dbg.location(393,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature679); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:2: ( bodyItem ( ws )? )+
            {
            dbg.location(397,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:2: ( bodyItem ( ws )? )+
            int cnt45=0;
            try { dbg.enterSubRule(45);

            loop45:
            do {
                int alt45=2;
                try { dbg.enterDecision(45, decisionCanBacktrack[45]);

                int LA45_0 = input.LA(1);

                if ( (LA45_0==IDENT||LA45_0==MEDIA_SYM||LA45_0==GEN||LA45_0==COLON||LA45_0==AT_IDENT||LA45_0==MOZ_DOCUMENT_SYM||LA45_0==WEBKIT_KEYFRAMES_SYM||(LA45_0>=PAGE_SYM && LA45_0<=FONT_FACE_SYM)||(LA45_0>=MINUS && LA45_0<=PIPE)||LA45_0==LESS_AND||(LA45_0>=SASS_VAR && LA45_0<=SASS_INCLUDE)) ) {
                    alt45=1;
                }


                } finally {dbg.exitDecision(45);}

                switch (alt45) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:4: bodyItem ( ws )?
            	    {
            	    dbg.location(397,4);
            	    pushFollow(FOLLOW_bodyItem_in_body695);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(397,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:13: ws
            	            {
            	            dbg.location(397,13);
            	            pushFollow(FOLLOW_ws_in_body697);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(44);}


            	    }
            	    break;

            	default :
            	    if ( cnt45 >= 1 ) break loop45;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(45, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt45++;
            } while (true);
            } finally {dbg.exitSubRule(45);}


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
            dbg.exitRule(getGrammarFileName(), "body");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "body"


    // $ANTLR start "bodyItem"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(400, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call )
            int alt46=8;
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            try {
                isCyclicDecision = true;
                alt46 = dfa46.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:6: rule
                    {
                    dbg.location(402,6);
                    pushFollow(FOLLOW_rule_in_bodyItem722);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:11: media
                    {
                    dbg.location(403,11);
                    pushFollow(FOLLOW_media_in_bodyItem734);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:11: page
                    {
                    dbg.location(404,11);
                    pushFollow(FOLLOW_page_in_bodyItem746);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:11: counterStyle
                    {
                    dbg.location(405,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem758);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:11: fontFace
                    {
                    dbg.location(406,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem770);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:11: vendorAtRule
                    {
                    dbg.location(407,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem782);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:11: {...}? cp_variable_declaration
                    {
                    dbg.location(408,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(408,40);
                    pushFollow(FOLLOW_cp_variable_declaration_in_bodyItem796);
                    cp_variable_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:11: {...}? cp_mixin_call
                    {
                    dbg.location(409,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isCssPreprocessorSource()");
                    }
                    dbg.location(409,40);
                    pushFollow(FOLLOW_cp_mixin_call_in_bodyItem810);
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
        dbg.location(410, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(418, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt47=3;
            try { dbg.enterDecision(47, decisionCanBacktrack[47]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt47=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt47=2;
                }
                break;
            case AT_IDENT:
                {
                alt47=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(47);}

            switch (alt47) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:3: moz_document
                    {
                    dbg.location(419,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule833);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:18: webkitKeyframes
                    {
                    dbg.location(419,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule837);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:36: generic_at_rule
                    {
                    dbg.location(419,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule841);
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
        dbg.location(419, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(422,2);
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
        dbg.location(424, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(426, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(427,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule877); if (state.failed) return ;
            dbg.location(427,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:16: WS
            	    {
            	    dbg.location(427,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule879); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);
            } finally {dbg.exitSubRule(48);}

            dbg.location(427,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:20: ( atRuleId ( WS )* )?
            int alt50=2;
            try { dbg.enterSubRule(50);
            try { dbg.enterDecision(50, decisionCanBacktrack[50]);

            int LA50_0 = input.LA(1);

            if ( ((LA50_0>=IDENT && LA50_0<=STRING)) ) {
                alt50=1;
            }
            } finally {dbg.exitDecision(50);}

            switch (alt50) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:22: atRuleId ( WS )*
                    {
                    dbg.location(427,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule884);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(427,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:31: ( WS )*
                    try { dbg.enterSubRule(49);

                    loop49:
                    do {
                        int alt49=2;
                        try { dbg.enterDecision(49, decisionCanBacktrack[49]);

                        int LA49_0 = input.LA(1);

                        if ( (LA49_0==WS) ) {
                            alt49=1;
                        }


                        } finally {dbg.exitDecision(49);}

                        switch (alt49) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:31: WS
                    	    {
                    	    dbg.location(427,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule886); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop49;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(49);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}

            dbg.location(428,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule901); if (state.failed) return ;
            dbg.location(429,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule913);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(430,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule923); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(431, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(432, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(434,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document939); if (state.failed) return ;
            dbg.location(434,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:19: ws
                    {
                    dbg.location(434,19);
                    pushFollow(FOLLOW_ws_in_moz_document941);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}

            dbg.location(434,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:25: moz_document_function ( ws )?
            {
            dbg.location(434,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document946);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(434,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:47: ws
                    {
                    dbg.location(434,47);
                    pushFollow(FOLLOW_ws_in_moz_document948);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}


            }

            dbg.location(434,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(55);

            loop55:
            do {
                int alt55=2;
                try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                int LA55_0 = input.LA(1);

                if ( (LA55_0==COMMA) ) {
                    alt55=1;
                }


                } finally {dbg.exitDecision(55);}

                switch (alt55) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(434,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document954); if (state.failed) return ;
            	    dbg.location(434,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:60: ws
            	            {
            	            dbg.location(434,60);
            	            pushFollow(FOLLOW_ws_in_moz_document956);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(53);}

            	    dbg.location(434,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document959);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(434,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:86: ws
            	            {
            	            dbg.location(434,86);
            	            pushFollow(FOLLOW_ws_in_moz_document961);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(54);}


            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);
            } finally {dbg.exitSubRule(55);}

            dbg.location(435,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document968); if (state.failed) return ;
            dbg.location(435,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:9: ws
                    {
                    dbg.location(435,9);
                    pushFollow(FOLLOW_ws_in_moz_document970);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(436,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:3: ( body )?
            int alt57=2;
            try { dbg.enterSubRule(57);
            try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            int LA57_0 = input.LA(1);

            if ( (LA57_0==IDENT||LA57_0==MEDIA_SYM||LA57_0==GEN||LA57_0==COLON||LA57_0==AT_IDENT||LA57_0==MOZ_DOCUMENT_SYM||LA57_0==WEBKIT_KEYFRAMES_SYM||(LA57_0>=PAGE_SYM && LA57_0<=FONT_FACE_SYM)||(LA57_0>=MINUS && LA57_0<=PIPE)||LA57_0==LESS_AND||(LA57_0>=SASS_VAR && LA57_0<=SASS_INCLUDE)) ) {
                alt57=1;
            }
            } finally {dbg.exitDecision(57);}

            switch (alt57) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:3: body
                    {
                    dbg.location(436,3);
                    pushFollow(FOLLOW_body_in_moz_document975);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(437,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document980); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(438, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(440, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(441,2);
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
        dbg.location(443, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(446, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(448,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1021); if (state.failed) return ;
            dbg.location(448,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:23: ws
                    {
                    dbg.location(448,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1023);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}

            dbg.location(448,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes1026);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(448,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:36: ws
                    {
                    dbg.location(448,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1028);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(449,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes1033); if (state.failed) return ;
            dbg.location(449,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:9: ws
                    {
                    dbg.location(449,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes1035);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(450,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(62);

            loop62:
            do {
                int alt62=2;
                try { dbg.enterDecision(62, decisionCanBacktrack[62]);

                int LA62_0 = input.LA(1);

                if ( (LA62_0==IDENT||LA62_0==PERCENTAGE) ) {
                    alt62=1;
                }


                } finally {dbg.exitDecision(62);}

                switch (alt62) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(450,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1042);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(450,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:26: ws
            	            {
            	            dbg.location(450,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes1044);
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

            dbg.location(451,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes1051); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(454, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(456,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1064);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(456,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:26: ws
                    {
                    dbg.location(456,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1066);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(458,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock1071); if (state.failed) return ;
            dbg.location(458,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:10: ws
                    {
                    dbg.location(458,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock1074);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(458,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1077);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(459,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1081);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(460,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1084); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframesBlock");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframesBlock"


    // $ANTLR start "webkitKeyframeSelectors"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(463, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(465,2);
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

            dbg.location(465,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            try { dbg.enterSubRule(67);

            loop67:
            do {
                int alt67=2;
                try { dbg.enterDecision(67, decisionCanBacktrack[67]);

                try {
                    isCyclicDecision = true;
                    alt67 = dfa67.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(67);}

                switch (alt67) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(465,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:27: ws
            	            {
            	            dbg.location(465,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1111);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(65);}

            	    dbg.location(465,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1114); if (state.failed) return ;
            	    dbg.location(465,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:37: ws
            	            {
            	            dbg.location(465,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1116);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(66);}

            	    dbg.location(465,41);
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
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}


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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframeSelectors");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframeSelectors"


    // $ANTLR start "page"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(468, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(469,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1148); if (state.failed) return ;
            dbg.location(469,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:16: ws
                    {
                    dbg.location(469,16);
                    pushFollow(FOLLOW_ws_in_page1150);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(469,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:20: ( IDENT ( ws )? )?
            int alt70=2;
            try { dbg.enterSubRule(70);
            try { dbg.enterDecision(70, decisionCanBacktrack[70]);

            int LA70_0 = input.LA(1);

            if ( (LA70_0==IDENT) ) {
                alt70=1;
            }
            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:22: IDENT ( ws )?
                    {
                    dbg.location(469,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1155); if (state.failed) return ;
                    dbg.location(469,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:28: ws
                            {
                            dbg.location(469,28);
                            pushFollow(FOLLOW_ws_in_page1157);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(69);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(469,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:35: ( pseudoPage ( ws )? )?
            int alt72=2;
            try { dbg.enterSubRule(72);
            try { dbg.enterDecision(72, decisionCanBacktrack[72]);

            int LA72_0 = input.LA(1);

            if ( (LA72_0==COLON) ) {
                alt72=1;
            }
            } finally {dbg.exitDecision(72);}

            switch (alt72) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:36: pseudoPage ( ws )?
                    {
                    dbg.location(469,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1164);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(469,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:47: ws
                            {
                            dbg.location(469,47);
                            pushFollow(FOLLOW_ws_in_page1166);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(71);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(470,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1179); if (state.failed) return ;
            dbg.location(470,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:16: ws
                    {
                    dbg.location(470,16);
                    pushFollow(FOLLOW_ws_in_page1181);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(73);}

            dbg.location(474,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:13: ( declaration | margin ( ws )? )?
            int alt75=3;
            try { dbg.enterSubRule(75);
            try { dbg.enterDecision(75, decisionCanBacktrack[75]);

            int LA75_0 = input.LA(1);

            if ( (LA75_0==IDENT||LA75_0==MEDIA_SYM||LA75_0==GEN||LA75_0==AT_IDENT||(LA75_0>=MINUS && LA75_0<=DOT)||LA75_0==STAR||LA75_0==SASS_VAR) ) {
                alt75=1;
            }
            else if ( ((LA75_0>=TOPLEFTCORNER_SYM && LA75_0<=RIGHTBOTTOM_SYM)) ) {
                alt75=2;
            }
            } finally {dbg.exitDecision(75);}

            switch (alt75) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:14: declaration
                    {
                    dbg.location(474,14);
                    pushFollow(FOLLOW_declaration_in_page1236);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:26: margin ( ws )?
                    {
                    dbg.location(474,26);
                    pushFollow(FOLLOW_margin_in_page1238);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(474,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:33: ws
                            {
                            dbg.location(474,33);
                            pushFollow(FOLLOW_ws_in_page1240);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(74);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(75);}

            dbg.location(474,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(79);

            loop79:
            do {
                int alt79=2;
                try { dbg.enterDecision(79, decisionCanBacktrack[79]);

                int LA79_0 = input.LA(1);

                if ( (LA79_0==SEMI) ) {
                    alt79=1;
                }


                } finally {dbg.exitDecision(79);}

                switch (alt79) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(474,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1246); if (state.failed) return ;
            	    dbg.location(474,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:45: ws
            	            {
            	            dbg.location(474,45);
            	            pushFollow(FOLLOW_ws_in_page1248);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(76);}

            	    dbg.location(474,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:49: ( declaration | margin ( ws )? )?
            	    int alt78=3;
            	    try { dbg.enterSubRule(78);
            	    try { dbg.enterDecision(78, decisionCanBacktrack[78]);

            	    int LA78_0 = input.LA(1);

            	    if ( (LA78_0==IDENT||LA78_0==MEDIA_SYM||LA78_0==GEN||LA78_0==AT_IDENT||(LA78_0>=MINUS && LA78_0<=DOT)||LA78_0==STAR||LA78_0==SASS_VAR) ) {
            	        alt78=1;
            	    }
            	    else if ( ((LA78_0>=TOPLEFTCORNER_SYM && LA78_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt78=2;
            	    }
            	    } finally {dbg.exitDecision(78);}

            	    switch (alt78) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:50: declaration
            	            {
            	            dbg.location(474,50);
            	            pushFollow(FOLLOW_declaration_in_page1252);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:62: margin ( ws )?
            	            {
            	            dbg.location(474,62);
            	            pushFollow(FOLLOW_margin_in_page1254);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(474,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:69: ws
            	                    {
            	                    dbg.location(474,69);
            	                    pushFollow(FOLLOW_ws_in_page1256);
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


            	    }
            	    break;

            	default :
            	    break loop79;
                }
            } while (true);
            } finally {dbg.exitSubRule(79);}

            dbg.location(475,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1271); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(478, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(479,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1292); if (state.failed) return ;
            dbg.location(479,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:25: ws
                    {
                    dbg.location(479,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1294);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(479,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1297); if (state.failed) return ;
            dbg.location(479,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:35: ws
                    {
                    dbg.location(479,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1299);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(480,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1310); if (state.failed) return ;
            dbg.location(480,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:16: ws
                    {
                    dbg.location(480,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1312);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(480,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1315);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(481,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1319);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(482,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1329); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "counterStyle");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "counterStyle"


    // $ANTLR start "fontFace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(485, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(486,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1350); if (state.failed) return ;
            dbg.location(486,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:21: ws
                    {
                    dbg.location(486,21);
                    pushFollow(FOLLOW_ws_in_fontFace1352);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(83);}

            dbg.location(487,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1363); if (state.failed) return ;
            dbg.location(487,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:16: ws
                    {
                    dbg.location(487,16);
                    pushFollow(FOLLOW_ws_in_fontFace1365);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(84);}

            dbg.location(487,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1368);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(488,3);
            pushFollow(FOLLOW_declarations_in_fontFace1372);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(489,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1382); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(490, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(492, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(493,4);
            pushFollow(FOLLOW_margin_sym_in_margin1397);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(493,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:15: ws
                    {
                    dbg.location(493,15);
                    pushFollow(FOLLOW_ws_in_margin1399);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(85);}

            dbg.location(493,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1402); if (state.failed) return ;
            dbg.location(493,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:26: ws
                    {
                    dbg.location(493,26);
                    pushFollow(FOLLOW_ws_in_margin1404);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(86);}

            dbg.location(493,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1407);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(493,53);
            pushFollow(FOLLOW_declarations_in_margin1409);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(493,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1411); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(494, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(496, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(497,2);
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
        dbg.location(514, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(516, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:7: COLON IDENT
            {
            dbg.location(517,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1640); if (state.failed) return ;
            dbg.location(517,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1642); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(520, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(521,5);
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
        dbg.location(523, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(525, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt90=4;
            try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt90=1;
                }
                break;
            case GREATER:
                {
                alt90=2;
                }
                break;
            case TILDE:
                {
                alt90=3;
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
                alt90=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:7: PLUS ( ws )?
                    {
                    dbg.location(526,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1692); if (state.failed) return ;
                    dbg.location(526,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:12: ws
                            {
                            dbg.location(526,12);
                            pushFollow(FOLLOW_ws_in_combinator1694);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(87);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:7: GREATER ( ws )?
                    {
                    dbg.location(527,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1703); if (state.failed) return ;
                    dbg.location(527,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:15: ws
                            {
                            dbg.location(527,15);
                            pushFollow(FOLLOW_ws_in_combinator1705);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(88);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:7: TILDE ( ws )?
                    {
                    dbg.location(528,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1714); if (state.failed) return ;
                    dbg.location(528,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:13: ws
                            {
                            dbg.location(528,13);
                            pushFollow(FOLLOW_ws_in_combinator1716);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(89);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:5: 
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
        dbg.location(530, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:532:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(532, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(533,5);
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
        dbg.location(535, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(537, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(538,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:7: ( IDENT | GEN | {...}? cp_variable )
            int alt91=3;
            try { dbg.enterSubRule(91);
            try { dbg.enterDecision(91, decisionCanBacktrack[91]);

            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt91=1;
                }
                break;
            case GEN:
                {
                alt91=2;
                }
                break;
            case MEDIA_SYM:
            case AT_IDENT:
            case SASS_VAR:
                {
                alt91=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 91, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(91);}

            switch (alt91) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:8: IDENT
                    {
                    dbg.location(538,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property1777); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:16: GEN
                    {
                    dbg.location(538,16);
                    match(input,GEN,FOLLOW_GEN_in_property1781); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:22: {...}? cp_variable
                    {
                    dbg.location(538,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(538,51);
                    pushFollow(FOLLOW_cp_variable_in_property1787);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(538,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:64: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:64: ws
                    {
                    dbg.location(538,64);
                    pushFollow(FOLLOW_ws_in_property1790);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(539, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:1: rule : ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(541, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:5: ( ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(542,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )
            int alt93=2;
            try { dbg.enterSubRule(93);
            try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            try {
                isCyclicDecision = true;
                alt93 = dfa93.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:13: ({...}? cp_mixin_declaration )
                    {
                    dbg.location(543,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:13: ({...}? cp_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:15: {...}? cp_mixin_declaration
                    {
                    dbg.location(543,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(543,44);
                    pushFollow(FOLLOW_cp_mixin_declaration_in_rule1834);
                    cp_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ( selectorsGroup )
                    {
                    dbg.location(545,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:15: selectorsGroup
                    {
                    dbg.location(545,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule1867);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(548,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1890); if (state.failed) return ;
            dbg.location(548,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:16: ws
                    {
                    dbg.location(548,16);
                    pushFollow(FOLLOW_ws_in_rule1892);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(548,20);
            pushFollow(FOLLOW_syncToFollow_in_rule1895);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(549,13);
            pushFollow(FOLLOW_declarations_in_rule1909);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(550,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1919); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(551, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(559, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(561,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )*
            try { dbg.enterSubRule(99);

            loop99:
            do {
                int alt99=5;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(565,120);
            	    pushFollow(FOLLOW_declaration_in_declarations2053);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(565,132);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations2055); if (state.failed) return ;
            	    dbg.location(565,137);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:137: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:137: ws
            	            {
            	            dbg.location(565,137);
            	            pushFollow(FOLLOW_ws_in_declarations2057);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )?
            	    {
            	    dbg.location(567,122);
            	    pushFollow(FOLLOW_scss_nested_properties_in_declarations2101);
            	    scss_nested_properties();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(567,145);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:145: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:145: ws
            	            {
            	            dbg.location(567,145);
            	            pushFollow(FOLLOW_ws_in_declarations2103);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(569,50);
            	    pushFollow(FOLLOW_rule_in_declarations2140);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(569,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:55: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:55: ws
            	            {
            	            dbg.location(569,55);
            	            pushFollow(FOLLOW_ws_in_declarations2142);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:17: {...}? cp_mixin_call ( ws )?
            	    {
            	    dbg.location(571,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(571,46);
            	    pushFollow(FOLLOW_cp_mixin_call_in_declarations2181);
            	    cp_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(571,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:60: ws
            	            {
            	            dbg.location(571,60);
            	            pushFollow(FOLLOW_ws_in_declarations2183);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(98);}


            	    }
            	    break;

            	default :
            	    break loop99;
                }
            } while (true);
            } finally {dbg.exitSubRule(99);}

            dbg.location(575,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt100=2;
            try { dbg.enterSubRule(100);
            try { dbg.enterDecision(100, decisionCanBacktrack[100]);

            int LA100_0 = input.LA(1);

            if ( (LA100_0==STAR) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==HASH_SYMBOL) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==IDENT) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==MINUS||(LA100_0>=HASH && LA100_0<=DOT)) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==GEN) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==MEDIA_SYM||LA100_0==AT_IDENT) && (synpred4_Css3())) {
                alt100=1;
            }
            else if ( (LA100_0==SASS_VAR) && (synpred4_Css3())) {
                alt100=1;
            }
            } finally {dbg.exitDecision(100);}

            switch (alt100) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(575,36);
                    pushFollow(FOLLOW_declaration_in_declarations2227);
                    declaration();

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
        dbg.location(576, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(578, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:5: ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* )
            int alt104=2;
            try { dbg.enterDecision(104, decisionCanBacktrack[104]);

            try {
                isCyclicDecision = true;
                alt104 = dfa104.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(104);}

            switch (alt104) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )?
                    {
                    dbg.location(581,60);
                    pushFollow(FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2288);
                    scss_selector_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(581,99);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:99: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:99: ws
                            {
                            dbg.location(581,99);
                            pushFollow(FOLLOW_ws_in_selectorsGroup2290);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:9: selector ( COMMA ( ws )? selector )*
                    {
                    dbg.location(583,9);
                    pushFollow(FOLLOW_selector_in_selectorsGroup2305);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(583,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:18: ( COMMA ( ws )? selector )*
                    try { dbg.enterSubRule(103);

                    loop103:
                    do {
                        int alt103=2;
                        try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                        int LA103_0 = input.LA(1);

                        if ( (LA103_0==COMMA) ) {
                            alt103=1;
                        }


                        } finally {dbg.exitDecision(103);}

                        switch (alt103) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:19: COMMA ( ws )? selector
                    	    {
                    	    dbg.location(583,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2308); if (state.failed) return ;
                    	    dbg.location(583,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:25: ws
                    	            {
                    	            dbg.location(583,25);
                    	            pushFollow(FOLLOW_ws_in_selectorsGroup2310);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(102);}

                    	    dbg.location(583,29);
                    	    pushFollow(FOLLOW_selector_in_selectorsGroup2313);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop103;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(103);}


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
        dbg.location(584, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(586, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(587,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2340);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(587,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(105);

            loop105:
            do {
                int alt105=2;
                try { dbg.enterDecision(105, decisionCanBacktrack[105]);

                int LA105_0 = input.LA(1);

                if ( (LA105_0==IDENT||LA105_0==GEN||LA105_0==COLON||(LA105_0>=PLUS && LA105_0<=TILDE)||(LA105_0>=HASH_SYMBOL && LA105_0<=PIPE)||LA105_0==LESS_AND) ) {
                    alt105=1;
                }


                } finally {dbg.exitDecision(105);}

                switch (alt105) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(587,31);
            	    pushFollow(FOLLOW_combinator_in_selector2343);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(587,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2345);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop105;
                }
            } while (true);
            } finally {dbg.exitSubRule(105);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(588, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(591, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt110=2;
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            int LA110_0 = input.LA(1);

            if ( (LA110_0==IDENT||LA110_0==GEN||(LA110_0>=STAR && LA110_0<=PIPE)||LA110_0==LESS_AND) ) {
                alt110=1;
            }
            else if ( (LA110_0==COLON||(LA110_0>=HASH_SYMBOL && LA110_0<=DCOLON)) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(594,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(594,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2378);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(594,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:17: ( ( esPred )=> elementSubsequent ( ws )? )*
                    try { dbg.enterSubRule(107);

                    loop107:
                    do {
                        int alt107=2;
                        try { dbg.enterDecision(107, decisionCanBacktrack[107]);

                        try {
                            isCyclicDecision = true;
                            alt107 = dfa107.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(107);}

                        switch (alt107) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(594,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2385);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(594,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:46: ws
                    	            {
                    	            dbg.location(594,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2387);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(106);}


                    	    }
                    	    break;

                    	default :
                    	    break loop107;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(107);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(596,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(596,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    int cnt109=0;
                    try { dbg.enterSubRule(109);

                    loop109:
                    do {
                        int alt109=2;
                        try { dbg.enterDecision(109, decisionCanBacktrack[109]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA109_2 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt109=1;
                            }


                            }
                            break;
                        case HASH_SYMBOL:
                            {
                            int LA109_3 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt109=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA109_4 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt109=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA109_5 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt109=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA109_6 = input.LA(2);

                            if ( (synpred7_Css3()) ) {
                                alt109=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(109);}

                        switch (alt109) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(596,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2406);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(596,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:33: ws
                    	            {
                    	            dbg.location(596,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2408);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(108);}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt109 >= 1 ) break loop109;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(109, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt109++;
                    } while (true);
                    } finally {dbg.exitSubRule(109);}


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
        dbg.location(597, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:1: esPred : ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(604, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:5: ( HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(605,5);
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
        dbg.location(606, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(608, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(610,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt111=2;
            try { dbg.enterSubRule(111);
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( (LA111_0==IDENT) ) {
                int LA111_1 = input.LA(2);

                if ( (synpred8_Css3()) ) {
                    alt111=1;
                }
            }
            else if ( (LA111_0==STAR) ) {
                int LA111_2 = input.LA(2);

                if ( (synpred8_Css3()) ) {
                    alt111=1;
                }
            }
            else if ( (LA111_0==PIPE) && (synpred8_Css3())) {
                alt111=1;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(610,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2520);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(610,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:51: elementName ( ws )?
            {
            dbg.location(610,51);
            pushFollow(FOLLOW_elementName_in_typeSelector2526);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(610,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:63: ws
                    {
                    dbg.location(610,63);
                    pushFollow(FOLLOW_ws_in_typeSelector2528);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}


            }


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
            dbg.exitRule(getGrammarFileName(), "typeSelector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeSelector"


    // $ANTLR start "namespacePrefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(613, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(614,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:5: ( namespacePrefixName | STAR )?
            int alt113=3;
            try { dbg.enterSubRule(113);
            try { dbg.enterDecision(113, decisionCanBacktrack[113]);

            int LA113_0 = input.LA(1);

            if ( (LA113_0==IDENT) ) {
                alt113=1;
            }
            else if ( (LA113_0==STAR) ) {
                alt113=2;
            }
            } finally {dbg.exitDecision(113);}

            switch (alt113) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:7: namespacePrefixName
                    {
                    dbg.location(614,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2546);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:29: STAR
                    {
                    dbg.location(614,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2550); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}

            dbg.location(614,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2554); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(615, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(618, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(620,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt114=4;
            try { dbg.enterSubRule(114);
            try { dbg.enterDecision(114, decisionCanBacktrack[114]);

            switch ( input.LA(1) ) {
            case HASH_SYMBOL:
            case HASH:
                {
                alt114=1;
                }
                break;
            case DOT:
                {
                alt114=2;
                }
                break;
            case LBRACKET:
                {
                alt114=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt114=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 114, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(114);}

            switch (alt114) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:6: cssId
                    {
                    dbg.location(621,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2588);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:8: cssClass
                    {
                    dbg.location(622,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2597);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:11: slAttribute
                    {
                    dbg.location(623,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2609);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:11: pseudo
                    {
                    dbg.location(624,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2621);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(114);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(626, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:1: cssId : ( HASH | ( HASH_SYMBOL NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(629, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:5: ( HASH | ( HASH_SYMBOL NAME ) )
            int alt115=2;
            try { dbg.enterDecision(115, decisionCanBacktrack[115]);

            int LA115_0 = input.LA(1);

            if ( (LA115_0==HASH) ) {
                alt115=1;
            }
            else if ( (LA115_0==HASH_SYMBOL) ) {
                alt115=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 115, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(115);}

            switch (alt115) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:7: HASH
                    {
                    dbg.location(630,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2649); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:14: ( HASH_SYMBOL NAME )
                    {
                    dbg.location(630,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:14: ( HASH_SYMBOL NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:16: HASH_SYMBOL NAME
                    {
                    dbg.location(630,16);
                    match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_cssId2655); if (state.failed) return ;
                    dbg.location(630,28);
                    match(input,NAME,FOLLOW_NAME_in_cssId2657); if (state.failed) return ;

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
        dbg.location(631, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(637, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:7: DOT ( IDENT | GEN )
            {
            dbg.location(638,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2685); if (state.failed) return ;
            dbg.location(638,11);
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
        dbg.location(639, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:1: elementName : ( ( IDENT | GEN | LESS_AND ) | STAR );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(646, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:5: ( ( IDENT | GEN | LESS_AND ) | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(647,5);
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
        dbg.location(648, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(650, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(651,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2759); if (state.failed) return ;
            dbg.location(652,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:6: ( namespacePrefix )?
            int alt116=2;
            try { dbg.enterSubRule(116);
            try { dbg.enterDecision(116, decisionCanBacktrack[116]);

            int LA116_0 = input.LA(1);

            if ( (LA116_0==IDENT) ) {
                int LA116_1 = input.LA(2);

                if ( (LA116_1==PIPE) ) {
                    alt116=1;
                }
            }
            else if ( ((LA116_0>=STAR && LA116_0<=PIPE)) ) {
                alt116=1;
            }
            } finally {dbg.exitDecision(116);}

            switch (alt116) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:6: namespacePrefix
                    {
                    dbg.location(652,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2766);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(116);}

            dbg.location(652,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:23: ws
                    {
                    dbg.location(652,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2769);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(117);}

            dbg.location(653,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2780);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(653,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:25: ws
                    {
                    dbg.location(653,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2782);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(655,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt121=2;
            try { dbg.enterSubRule(121);
            try { dbg.enterDecision(121, decisionCanBacktrack[121]);

            int LA121_0 = input.LA(1);

            if ( ((LA121_0>=OPEQ && LA121_0<=CONTAINS)) ) {
                alt121=1;
            }
            } finally {dbg.exitDecision(121);}

            switch (alt121) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(656,17);
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

                    dbg.location(664,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:17: ws
                            {
                            dbg.location(664,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3004);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(119);}

                    dbg.location(665,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute3023);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(666,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:17: ws
                            {
                            dbg.location(666,17);
                            pushFollow(FOLLOW_ws_in_slAttribute3041);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(121);}

            dbg.location(669,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3070); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(670, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(677, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:4: IDENT
            {
            dbg.location(678,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3086); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(679, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(681, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:2: ( IDENT | STRING )
            {
            dbg.location(683,2);
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
        dbg.location(687, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(689, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(690,7);
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

            dbg.location(691,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt129=2;
            try { dbg.enterSubRule(129);
            try { dbg.enterDecision(129, decisionCanBacktrack[129]);

            int LA129_0 = input.LA(1);

            if ( (LA129_0==IDENT||LA129_0==GEN) ) {
                alt129=1;
            }
            else if ( (LA129_0==NOT) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    {
                    dbg.location(692,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
                    {
                    dbg.location(693,21);
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

                    dbg.location(694,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:25: ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN
                            {
                            dbg.location(695,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:25: ws
                                    {
                                    dbg.location(695,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3281);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(122);}

                            dbg.location(695,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3284); if (state.failed) return ;
                            dbg.location(695,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:36: ws
                                    {
                                    dbg.location(695,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3286);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(123);}

                            dbg.location(695,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:40: ( expression | STAR )?
                            int alt124=3;
                            try { dbg.enterSubRule(124);
                            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                            int LA124_0 = input.LA(1);

                            if ( ((LA124_0>=IDENT && LA124_0<=URI)||LA124_0==MEDIA_SYM||LA124_0==GEN||LA124_0==AT_IDENT||LA124_0==PERCENTAGE||LA124_0==PLUS||LA124_0==MINUS||LA124_0==HASH||(LA124_0>=NUMBER && LA124_0<=DIMENSION)||LA124_0==SASS_VAR) ) {
                                alt124=1;
                            }
                            else if ( (LA124_0==STAR) ) {
                                alt124=2;
                            }
                            } finally {dbg.exitDecision(124);}

                            switch (alt124) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:42: expression
                                    {
                                    dbg.location(695,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3291);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:55: STAR
                                    {
                                    dbg.location(695,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3295); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(124);}

                            dbg.location(695,63);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3300); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(125);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(699,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(699,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3379); if (state.failed) return ;
                    dbg.location(699,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:23: ws
                            {
                            dbg.location(699,23);
                            pushFollow(FOLLOW_ws_in_pseudo3381);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(126);}

                    dbg.location(699,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3384); if (state.failed) return ;
                    dbg.location(699,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:34: ws
                            {
                            dbg.location(699,34);
                            pushFollow(FOLLOW_ws_in_pseudo3386);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(127);}

                    dbg.location(699,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:38: ( simpleSelectorSequence )?
                    int alt128=2;
                    try { dbg.enterSubRule(128);
                    try { dbg.enterDecision(128, decisionCanBacktrack[128]);

                    int LA128_0 = input.LA(1);

                    if ( (LA128_0==IDENT||LA128_0==GEN||LA128_0==COLON||(LA128_0>=HASH_SYMBOL && LA128_0<=PIPE)||LA128_0==LESS_AND) ) {
                        alt128=1;
                    }
                    } finally {dbg.exitDecision(128);}

                    switch (alt128) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:38: simpleSelectorSequence
                            {
                            dbg.location(699,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3389);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(128);}

                    dbg.location(699,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3392); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(129);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(701, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:1: declaration : ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(703, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:5: ( ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:5: ( STAR )? ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property ) COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(706,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:5: ( STAR )?
            int alt130=2;
            try { dbg.enterSubRule(130);
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            int LA130_0 = input.LA(1);

            if ( (LA130_0==STAR) ) {
                alt130=1;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:5: STAR
                    {
                    dbg.location(706,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3436); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(130);}

            dbg.location(707,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:5: ( ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression | property )
            int alt131=2;
            try { dbg.enterSubRule(131);
            try { dbg.enterDecision(131, decisionCanBacktrack[131]);

            int LA131_0 = input.LA(1);

            if ( (LA131_0==HASH_SYMBOL) && (synpred9_Css3())) {
                alt131=1;
            }
            else if ( (LA131_0==IDENT) ) {
                int LA131_2 = input.LA(2);

                if ( (synpred9_Css3()) ) {
                    alt131=1;
                }
                else if ( (true) ) {
                    alt131=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 131, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA131_0==MINUS||(LA131_0>=HASH && LA131_0<=DOT)) && (synpred9_Css3())) {
                alt131=1;
            }
            else if ( (LA131_0==MEDIA_SYM||LA131_0==GEN||LA131_0==AT_IDENT||LA131_0==SASS_VAR) ) {
                alt131=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 131, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(131);}

            switch (alt131) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )=> scss_declaration_interpolation_expression
                    {
                    dbg.location(708,74);
                    pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_declaration3482);
                    scss_declaration_interpolation_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:9: property
                    {
                    dbg.location(710,9);
                    pushFollow(FOLLOW_property_in_declaration3503);
                    property();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(131);}

            dbg.location(712,5);
            match(input,COLON,FOLLOW_COLON_in_declaration3516); if (state.failed) return ;
            dbg.location(712,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:11: ws
                    {
                    dbg.location(712,11);
                    pushFollow(FOLLOW_ws_in_declaration3518);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(132);}

            dbg.location(712,15);
            pushFollow(FOLLOW_propertyValue_in_declaration3521);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(712,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:29: ( prio ( ws )? )?
            int alt134=2;
            try { dbg.enterSubRule(134);
            try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            int LA134_0 = input.LA(1);

            if ( (LA134_0==IMPORTANT_SYM) ) {
                alt134=1;
            }
            } finally {dbg.exitDecision(134);}

            switch (alt134) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:30: prio ( ws )?
                    {
                    dbg.location(712,30);
                    pushFollow(FOLLOW_prio_in_declaration3524);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(712,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:35: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:35: ws
                            {
                            dbg.location(712,35);
                            pushFollow(FOLLOW_ws_in_declaration3526);
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
        dbg.location(713, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(721, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(723,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(723,34);
                    pushFollow(FOLLOW_expression_in_propertyValue3566);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:9: ({...}? cp_expression )
                    {
                    dbg.location(733,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:11: {...}? cp_expression
                    {
                    dbg.location(733,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(733,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue3609);
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
        dbg.location(734, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(737, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(740,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt136=0;
            try { dbg.enterSubRule(136);

            loop136:
            do {
                int alt136=2;
                try { dbg.enterDecision(136, decisionCanBacktrack[136]);

                int LA136_0 = input.LA(1);

                if ( (LA136_0==NAMESPACE_SYM||(LA136_0>=IDENT && LA136_0<=MEDIA_SYM)||(LA136_0>=AND && LA136_0<=RPAREN)||(LA136_0>=WS && LA136_0<=RIGHTBOTTOM_SYM)||(LA136_0>=PLUS && LA136_0<=DCOLON)||(LA136_0>=PIPE && LA136_0<=LINE_COMMENT)) ) {
                    alt136=1;
                }


                } finally {dbg.exitDecision(136);}

                switch (alt136) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(740,7);
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
            	    if ( cnt136 >= 1 ) break loop136;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(136, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt136++;
            } while (true);
            } finally {dbg.exitSubRule(136);}

            dbg.location(740,65);
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
        dbg.location(741, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(745, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:6: 
            {
            }

        }
        finally {
        }
        dbg.location(751, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(753, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:757:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:6: 
            {
            }

        }
        finally {
        }
        dbg.location(758, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:1: syncTo_SEMI : SEMI ;
    public final void syncTo_SEMI() throws RecognitionException {

                syncToSet(BitSet.of(SEMI)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_SEMI");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(760, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:6: ( SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:13: SEMI
            {
            dbg.location(765,13);
            match(input,SEMI,FOLLOW_SEMI_in_syncTo_SEMI3794); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(766, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(769, 1);

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
            dbg.exitRule(getGrammarFileName(), "syncToFollow");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToFollow"


    // $ANTLR start "prio"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(776, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:7: IMPORTANT_SYM
            {
            dbg.location(777,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3849); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:780:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(780, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(781,7);
            pushFollow(FOLLOW_term_in_expression3870);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(781,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(139);

            loop139:
            do {
                int alt139=2;
                try { dbg.enterDecision(139, decisionCanBacktrack[139]);

                try {
                    isCyclicDecision = true;
                    alt139 = dfa139.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(139);}

                switch (alt139) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(781,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:14: ( operator ( ws )? )?
            	    int alt138=2;
            	    try { dbg.enterSubRule(138);
            	    try { dbg.enterDecision(138, decisionCanBacktrack[138]);

            	    int LA138_0 = input.LA(1);

            	    if ( (LA138_0==COMMA||LA138_0==SOLIDUS) ) {
            	        alt138=1;
            	    }
            	    } finally {dbg.exitDecision(138);}

            	    switch (alt138) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:15: operator ( ws )?
            	            {
            	            dbg.location(781,15);
            	            pushFollow(FOLLOW_operator_in_expression3875);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(781,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:24: ws
            	                    {
            	                    dbg.location(781,24);
            	                    pushFollow(FOLLOW_ws_in_expression3877);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(137);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(138);}

            	    dbg.location(781,30);
            	    pushFollow(FOLLOW_term_in_expression3882);
            	    term();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(782, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(784, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(785,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:7: ( unaryOperator ( ws )? )?
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            int LA141_0 = input.LA(1);

            if ( (LA141_0==PLUS||LA141_0==MINUS) ) {
                alt141=1;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:9: unaryOperator ( ws )?
                    {
                    dbg.location(785,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3907);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(785,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:23: ws
                            {
                            dbg.location(785,23);
                            pushFollow(FOLLOW_ws_in_term3909);
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

            dbg.location(786,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt142=8;
            try { dbg.enterSubRule(142);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(787,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:7: STRING
                    {
                    dbg.location(800,7);
                    match(input,STRING,FOLLOW_STRING_in_term4133); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:7: IDENT
                    {
                    dbg.location(801,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term4141); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:7: GEN
                    {
                    dbg.location(802,7);
                    match(input,GEN,FOLLOW_GEN_in_term4149); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:7: URI
                    {
                    dbg.location(803,7);
                    match(input,URI,FOLLOW_URI_in_term4157); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:7: hexColor
                    {
                    dbg.location(804,7);
                    pushFollow(FOLLOW_hexColor_in_term4165);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:805:7: function
                    {
                    dbg.location(805,7);
                    pushFollow(FOLLOW_function_in_term4173);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:7: {...}? cp_variable
                    {
                    dbg.location(806,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(806,36);
                    pushFollow(FOLLOW_cp_variable_in_term4183);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(142);}

            dbg.location(808,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:5: ws
                    {
                    dbg.location(808,5);
                    pushFollow(FOLLOW_ws_in_term4195);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}


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
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(811, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(812,5);
            pushFollow(FOLLOW_functionName_in_function4211);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(812,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:18: ws
                    {
                    dbg.location(812,18);
                    pushFollow(FOLLOW_ws_in_function4213);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(144);}

            dbg.location(813,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4218); if (state.failed) return ;
            dbg.location(813,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:10: ws
                    {
                    dbg.location(813,10);
                    pushFollow(FOLLOW_ws_in_function4220);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(145);}

            dbg.location(814,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt148=2;
            try { dbg.enterSubRule(148);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:4: expression
                    {
                    dbg.location(815,4);
                    pushFollow(FOLLOW_expression_in_function4230);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(817,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(818,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4248);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(818,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(147);

                    loop147:
                    do {
                        int alt147=2;
                        try { dbg.enterDecision(147, decisionCanBacktrack[147]);

                        int LA147_0 = input.LA(1);

                        if ( (LA147_0==COMMA) ) {
                            alt147=1;
                        }


                        } finally {dbg.exitDecision(147);}

                        switch (alt147) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(818,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4251); if (state.failed) return ;
                    	    dbg.location(818,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:24: ws
                    	            {
                    	            dbg.location(818,24);
                    	            pushFollow(FOLLOW_ws_in_function4253);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(146);}

                    	    dbg.location(818,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4256);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop147;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(147);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(148);}

            dbg.location(821,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4277); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(822, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(828, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(832,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:4: ( IDENT COLON )?
            int alt149=2;
            try { dbg.enterSubRule(149);
            try { dbg.enterDecision(149, decisionCanBacktrack[149]);

            int LA149_0 = input.LA(1);

            if ( (LA149_0==IDENT) ) {
                int LA149_1 = input.LA(2);

                if ( (LA149_1==COLON) ) {
                    alt149=1;
                }
            }
            } finally {dbg.exitDecision(149);}

            switch (alt149) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:5: IDENT COLON
                    {
                    dbg.location(832,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4325); if (state.failed) return ;
                    dbg.location(832,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4327); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(149);}

            dbg.location(832,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4331); if (state.failed) return ;
            dbg.location(832,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:25: ( DOT IDENT )*
            try { dbg.enterSubRule(150);

            loop150:
            do {
                int alt150=2;
                try { dbg.enterDecision(150, decisionCanBacktrack[150]);

                int LA150_0 = input.LA(1);

                if ( (LA150_0==DOT) ) {
                    alt150=1;
                }


                } finally {dbg.exitDecision(150);}

                switch (alt150) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:26: DOT IDENT
            	    {
            	    dbg.location(832,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4334); if (state.failed) return ;
            	    dbg.location(832,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4336); if (state.failed) return ;

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
        dbg.location(834, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(836, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(837,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4359);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(837,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:20: ws
                    {
                    dbg.location(837,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4361);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(837,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4364); if (state.failed) return ;
            dbg.location(837,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:29: ws
                    {
                    dbg.location(837,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4366);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(152);}

            dbg.location(837,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4369);
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
        dbg.location(838, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:840:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(840, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:4: IDENT ( DOT IDENT )*
            {
            dbg.location(841,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4384); if (state.failed) return ;
            dbg.location(841,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:10: ( DOT IDENT )*
            try { dbg.enterSubRule(153);

            loop153:
            do {
                int alt153=2;
                try { dbg.enterDecision(153, decisionCanBacktrack[153]);

                int LA153_0 = input.LA(1);

                if ( (LA153_0==DOT) ) {
                    alt153=1;
                }


                } finally {dbg.exitDecision(153);}

                switch (alt153) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:11: DOT IDENT
            	    {
            	    dbg.location(841,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4387); if (state.failed) return ;
            	    dbg.location(841,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4389); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop153;
                }
            } while (true);
            } finally {dbg.exitSubRule(153);}


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
            dbg.exitRule(getGrammarFileName(), "fnAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttributeName"


    // $ANTLR start "fnAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(844, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:4: expression
            {
            dbg.location(845,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4403);
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
        dbg.location(846, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(848, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:7: HASH
            {
            dbg.location(849,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4421); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "hexColor");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "hexColor"


    // $ANTLR start "ws"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:852:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(852, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:853:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:853:7: ( WS | NL | COMMENT )+
            {
            dbg.location(853,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:853:7: ( WS | NL | COMMENT )+
            int cnt154=0;
            try { dbg.enterSubRule(154);

            loop154:
            do {
                int alt154=2;
                try { dbg.enterDecision(154, decisionCanBacktrack[154]);

                int LA154_0 = input.LA(1);

                if ( (LA154_0==WS||(LA154_0>=NL && LA154_0<=COMMENT)) ) {
                    alt154=1;
                }


                } finally {dbg.exitDecision(154);}

                switch (alt154) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(853,7);
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(854, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:1: cp_variable_declaration : ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI );
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(859, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:5: ({...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI | {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI )
            int alt161=2;
            try { dbg.enterDecision(161, decisionCanBacktrack[161]);

            int LA161_0 = input.LA(1);

            if ( (LA161_0==MEDIA_SYM||LA161_0==AT_IDENT) ) {
                int LA161_1 = input.LA(2);

                if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {
                    alt161=1;
                }
                else if ( ((evalPredicate(isScssSource(),"isScssSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {
                    alt161=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 161, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA161_0==SASS_VAR) ) {
                int LA161_2 = input.LA(2);

                if ( ((evalPredicate(isLessSource(),"isLessSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {
                    alt161=1;
                }
                else if ( (evalPredicate(isScssSource(),"isScssSource()")) ) {
                    alt161=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 161, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 161, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(161);}

            switch (alt161) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
                    {
                    dbg.location(861,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isLessSource()");
                    }
                    dbg.location(861,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4490);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(861,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:39: ws
                            {
                            dbg.location(861,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4492);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(155);}

                    dbg.location(861,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4495); if (state.failed) return ;
                    dbg.location(861,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:861:49: ws
                            {
                            dbg.location(861,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4497);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(156);}

                    dbg.location(861,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4500);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(861,67);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4502); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:9: {...}? cp_variable ( ws )? COLON ( ws )? cp_expression ( SASS_DEFAULT ( ws )? )? SEMI
                    {
                    dbg.location(863,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable_declaration", "isScssSource()");
                    }
                    dbg.location(863,27);
                    pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4529);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(863,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:39: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:39: ws
                            {
                            dbg.location(863,39);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4531);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(157);}

                    dbg.location(863,43);
                    match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4534); if (state.failed) return ;
                    dbg.location(863,49);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:49: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:49: ws
                            {
                            dbg.location(863,49);
                            pushFollow(FOLLOW_ws_in_cp_variable_declaration4536);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(158);}

                    dbg.location(863,53);
                    pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4539);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(863,67);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:67: ( SASS_DEFAULT ( ws )? )?
                    int alt160=2;
                    try { dbg.enterSubRule(160);
                    try { dbg.enterDecision(160, decisionCanBacktrack[160]);

                    int LA160_0 = input.LA(1);

                    if ( (LA160_0==SASS_DEFAULT) ) {
                        alt160=1;
                    }
                    } finally {dbg.exitDecision(160);}

                    switch (alt160) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:68: SASS_DEFAULT ( ws )?
                            {
                            dbg.location(863,68);
                            match(input,SASS_DEFAULT,FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4542); if (state.failed) return ;
                            dbg.location(863,81);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:81: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:81: ws
                                    {
                                    dbg.location(863,81);
                                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4544);
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

                    dbg.location(863,87);
                    match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4549); if (state.failed) return ;

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
        dbg.location(864, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(867, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt162=2;
            try { dbg.enterDecision(162, decisionCanBacktrack[162]);

            int LA162_0 = input.LA(1);

            if ( (LA162_0==MEDIA_SYM||LA162_0==AT_IDENT) ) {
                alt162=1;
            }
            else if ( (LA162_0==SASS_VAR) ) {
                alt162=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 162, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(162);}

            switch (alt162) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(869,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(869,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(871,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(871,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:29: SASS_VAR
                    {
                    dbg.location(871,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable4614); if (state.failed) return ;

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
        dbg.location(873, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(876, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:10: cp_additionExp
            {
            dbg.location(877,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression4638);
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
        dbg.location(878, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(880, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:881:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:881:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(881,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4658);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(882,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(165);

            loop165:
            do {
                int alt165=3;
                try { dbg.enterDecision(165, decisionCanBacktrack[165]);

                int LA165_0 = input.LA(1);

                if ( (LA165_0==PLUS) ) {
                    alt165=1;
                }
                else if ( (LA165_0==MINUS) ) {
                    alt165=2;
                }


                } finally {dbg.exitDecision(165);}

                switch (alt165) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(882,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp4672); if (state.failed) return ;
            	    dbg.location(882,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:17: ws
            	            {
            	            dbg.location(882,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4674);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(163);}

            	    dbg.location(882,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4677);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(883,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp4690); if (state.failed) return ;
            	    dbg.location(883,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:18: ws
            	            {
            	            dbg.location(883,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4692);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(164);}

            	    dbg.location(883,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4695);
            	    cp_multiplyExp();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(885, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(887, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(888,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4728);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(889,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(168);

            loop168:
            do {
                int alt168=3;
                try { dbg.enterDecision(168, decisionCanBacktrack[168]);

                int LA168_0 = input.LA(1);

                if ( (LA168_0==STAR) ) {
                    alt168=1;
                }
                else if ( (LA168_0==SOLIDUS) ) {
                    alt168=2;
                }


                } finally {dbg.exitDecision(168);}

                switch (alt168) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(889,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp4741); if (state.failed) return ;
            	    dbg.location(889,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:17: ws
            	            {
            	            dbg.location(889,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4743);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(166);}

            	    dbg.location(889,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4746);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(890,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp4760); if (state.failed) return ;
            	    dbg.location(890,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:890:20: ws
            	            {
            	            dbg.location(890,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4762);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(167);}

            	    dbg.location(890,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4765);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(892, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(894, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt172=2;
            try { dbg.enterDecision(172, decisionCanBacktrack[172]);

            int LA172_0 = input.LA(1);

            if ( ((LA172_0>=IDENT && LA172_0<=URI)||LA172_0==MEDIA_SYM||LA172_0==GEN||LA172_0==AT_IDENT||LA172_0==PERCENTAGE||LA172_0==PLUS||LA172_0==MINUS||LA172_0==HASH||(LA172_0>=NUMBER && LA172_0<=DIMENSION)||LA172_0==SASS_VAR) ) {
                alt172=1;
            }
            else if ( (LA172_0==LPAREN) ) {
                alt172=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:10: term ( ( term )=> term )*
                    {
                    dbg.location(895,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp4798);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(895,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(169);

                    loop169:
                    do {
                        int alt169=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:16: ( term )=> term
                    	    {
                    	    dbg.location(895,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp4805);
                    	    term();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(896,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp4819); if (state.failed) return ;
                    dbg.location(896,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:17: ws
                            {
                            dbg.location(896,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4821);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(170);}

                    dbg.location(896,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp4824);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(896,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp4826); if (state.failed) return ;
                    dbg.location(896,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:43: ws
                            {
                            dbg.location(896,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4828);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(171);}


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
        dbg.location(897, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(900, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(902,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt173=8;
            try { dbg.enterSubRule(173);
            try { dbg.enterDecision(173, decisionCanBacktrack[173]);

            try {
                isCyclicDecision = true;
                alt173 = dfa173.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(173);}

            switch (alt173) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:903:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(903,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:7: STRING
                    {
                    dbg.location(916,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term5066); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:917:7: IDENT
                    {
                    dbg.location(917,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term5074); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:918:7: GEN
                    {
                    dbg.location(918,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term5082); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:7: URI
                    {
                    dbg.location(919,7);
                    match(input,URI,FOLLOW_URI_in_cp_term5090); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:920:7: hexColor
                    {
                    dbg.location(920,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term5098);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:921:7: function
                    {
                    dbg.location(921,7);
                    pushFollow(FOLLOW_function_in_cp_term5106);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:7: cp_variable
                    {
                    dbg.location(922,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term5114);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(173);}

            dbg.location(924,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:5: ws
                    {
                    dbg.location(924,5);
                    pushFollow(FOLLOW_ws_in_cp_term5126);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(174);}


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
            dbg.exitRule(getGrammarFileName(), "cp_term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_term"


    // $ANTLR start "cp_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:934:1: cp_mixin_declaration : ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? );
    public final void cp_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(934, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:935:5: ({...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? | {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )? )
            int alt184=2;
            try { dbg.enterDecision(184, decisionCanBacktrack[184]);

            int LA184_0 = input.LA(1);

            if ( (LA184_0==DOT) ) {
                alt184=1;
            }
            else if ( (LA184_0==SASS_MIXIN) ) {
                alt184=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 184, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(184);}

            switch (alt184) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:5: {...}? DOT cp_mixin_name ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
                    {
                    dbg.location(936,5);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isLessSource()");
                    }
                    dbg.location(936,23);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_declaration5157); if (state.failed) return ;
                    dbg.location(936,27);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5159);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(936,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:41: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:41: ws
                            {
                            dbg.location(936,41);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5161);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(175);}

                    dbg.location(936,45);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5164); if (state.failed) return ;
                    dbg.location(936,52);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:52: ( less_args_list )?
                    int alt176=2;
                    try { dbg.enterSubRule(176);
                    try { dbg.enterDecision(176, decisionCanBacktrack[176]);

                    int LA176_0 = input.LA(1);

                    if ( (LA176_0==MEDIA_SYM||LA176_0==AT_IDENT||LA176_0==SASS_VAR||(LA176_0>=LESS_DOTS && LA176_0<=LESS_REST)) ) {
                        alt176=1;
                    }
                    } finally {dbg.exitDecision(176);}

                    switch (alt176) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:52: less_args_list
                            {
                            dbg.location(936,52);
                            pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5166);
                            less_args_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(176);}

                    dbg.location(936,68);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5169); if (state.failed) return ;
                    dbg.location(936,75);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:75: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:75: ws
                            {
                            dbg.location(936,75);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5171);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(177);}

                    dbg.location(936,79);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:79: ( less_mixin_guarded ( ws )? )?
                    int alt179=2;
                    try { dbg.enterSubRule(179);
                    try { dbg.enterDecision(179, decisionCanBacktrack[179]);

                    int LA179_0 = input.LA(1);

                    if ( (LA179_0==LESS_WHEN) ) {
                        alt179=1;
                    }
                    } finally {dbg.exitDecision(179);}

                    switch (alt179) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:80: less_mixin_guarded ( ws )?
                            {
                            dbg.location(936,80);
                            pushFollow(FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5175);
                            less_mixin_guarded();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(936,99);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:99: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:99: ws
                                    {
                                    dbg.location(936,99);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5177);
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
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:5: {...}? SASS_MIXIN ws cp_mixin_name ( ws )? ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    {
                    dbg.location(938,5);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_declaration", "isScssSource()");
                    }
                    dbg.location(938,23);
                    match(input,SASS_MIXIN,FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5194); if (state.failed) return ;
                    dbg.location(938,34);
                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5196);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(938,37);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_declaration5198);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(938,51);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:51: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:51: ws
                            {
                            dbg.location(938,51);
                            pushFollow(FOLLOW_ws_in_cp_mixin_declaration5200);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(180);}

                    dbg.location(938,55);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:55: ( LPAREN ( less_args_list )? RPAREN ( ws )? )?
                    int alt183=2;
                    try { dbg.enterSubRule(183);
                    try { dbg.enterDecision(183, decisionCanBacktrack[183]);

                    int LA183_0 = input.LA(1);

                    if ( (LA183_0==LPAREN) ) {
                        alt183=1;
                    }
                    } finally {dbg.exitDecision(183);}

                    switch (alt183) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:56: LPAREN ( less_args_list )? RPAREN ( ws )?
                            {
                            dbg.location(938,56);
                            match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_declaration5204); if (state.failed) return ;
                            dbg.location(938,63);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:63: ( less_args_list )?
                            int alt181=2;
                            try { dbg.enterSubRule(181);
                            try { dbg.enterDecision(181, decisionCanBacktrack[181]);

                            int LA181_0 = input.LA(1);

                            if ( (LA181_0==MEDIA_SYM||LA181_0==AT_IDENT||LA181_0==SASS_VAR||(LA181_0>=LESS_DOTS && LA181_0<=LESS_REST)) ) {
                                alt181=1;
                            }
                            } finally {dbg.exitDecision(181);}

                            switch (alt181) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:63: less_args_list
                                    {
                                    dbg.location(938,63);
                                    pushFollow(FOLLOW_less_args_list_in_cp_mixin_declaration5206);
                                    less_args_list();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(181);}

                            dbg.location(938,79);
                            match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_declaration5209); if (state.failed) return ;
                            dbg.location(938,86);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:86: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:86: ws
                                    {
                                    dbg.location(938,86);
                                    pushFollow(FOLLOW_ws_in_cp_mixin_declaration5211);
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
                    } finally {dbg.exitSubRule(183);}


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
        dbg.location(939, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:943:1: cp_mixin_call : ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI ;
    public final void cp_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(943, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:5: ( ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name ) ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )? ( ws )? SEMI
            {
            dbg.location(945,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:5: ({...}? DOT cp_mixin_name | {...}? SASS_INCLUDE ws cp_mixin_name )
            int alt185=2;
            try { dbg.enterSubRule(185);
            try { dbg.enterDecision(185, decisionCanBacktrack[185]);

            int LA185_0 = input.LA(1);

            if ( (LA185_0==DOT) ) {
                alt185=1;
            }
            else if ( (LA185_0==SASS_INCLUDE) ) {
                alt185=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 185, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(185);}

            switch (alt185) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:946:9: {...}? DOT cp_mixin_name
                    {
                    dbg.location(946,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isLessSource()");
                    }
                    dbg.location(946,27);
                    match(input,DOT,FOLLOW_DOT_in_cp_mixin_call5253); if (state.failed) return ;
                    dbg.location(946,31);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5255);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:9: {...}? SASS_INCLUDE ws cp_mixin_name
                    {
                    dbg.location(948,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_mixin_call", "isScssSource()");
                    }
                    dbg.location(948,27);
                    match(input,SASS_INCLUDE,FOLLOW_SASS_INCLUDE_in_cp_mixin_call5277); if (state.failed) return ;
                    dbg.location(948,40);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5279);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(948,43);
                    pushFollow(FOLLOW_cp_mixin_name_in_cp_mixin_call5281);
                    cp_mixin_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(185);}

            dbg.location(950,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?
            int alt188=2;
            try { dbg.enterSubRule(188);
            try { dbg.enterDecision(188, decisionCanBacktrack[188]);

            try {
                isCyclicDecision = true;
                alt188 = dfa188.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(188);}

            switch (alt188) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:6: ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN
                    {
                    dbg.location(950,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:6: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:6: ws
                            {
                            dbg.location(950,6);
                            pushFollow(FOLLOW_ws_in_cp_mixin_call5294);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(186);}

                    dbg.location(950,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_mixin_call5297); if (state.failed) return ;
                    dbg.location(950,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:17: ( cp_mixin_call_args )?
                    int alt187=2;
                    try { dbg.enterSubRule(187);
                    try { dbg.enterDecision(187, decisionCanBacktrack[187]);

                    int LA187_0 = input.LA(1);

                    if ( ((LA187_0>=IDENT && LA187_0<=URI)||LA187_0==MEDIA_SYM||LA187_0==GEN||LA187_0==AT_IDENT||LA187_0==PERCENTAGE||LA187_0==PLUS||LA187_0==MINUS||LA187_0==HASH||(LA187_0>=NUMBER && LA187_0<=DIMENSION)||LA187_0==SASS_VAR) ) {
                        alt187=1;
                    }
                    } finally {dbg.exitDecision(187);}

                    switch (alt187) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:17: cp_mixin_call_args
                            {
                            dbg.location(950,17);
                            pushFollow(FOLLOW_cp_mixin_call_args_in_cp_mixin_call5299);
                            cp_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(187);}

                    dbg.location(950,37);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_mixin_call5302); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(188);}

            dbg.location(950,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:46: ws
                    {
                    dbg.location(950,46);
                    pushFollow(FOLLOW_ws_in_cp_mixin_call5306);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(189);}

            dbg.location(950,50);
            match(input,SEMI,FOLLOW_SEMI_in_cp_mixin_call5309); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call"


    // $ANTLR start "cp_mixin_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:1: cp_mixin_name : IDENT ;
    public final void cp_mixin_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(953, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:5: IDENT
            {
            dbg.location(955,5);
            match(input,IDENT,FOLLOW_IDENT_in_cp_mixin_name5338); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(956, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:958:1: cp_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void cp_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(958, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:959:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(962,5);
            pushFollow(FOLLOW_term_in_cp_mixin_call_args5374);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(962,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(191);

            loop191:
            do {
                int alt191=2;
                try { dbg.enterDecision(191, decisionCanBacktrack[191]);

                int LA191_0 = input.LA(1);

                if ( (LA191_0==SEMI||LA191_0==COMMA) ) {
                    alt191=1;
                }


                } finally {dbg.exitDecision(191);}

                switch (alt191) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(962,12);
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

            	    dbg.location(962,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:27: ws
            	            {
            	            dbg.location(962,27);
            	            pushFollow(FOLLOW_ws_in_cp_mixin_call_args5386);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(190);}

            	    dbg.location(962,31);
            	    pushFollow(FOLLOW_term_in_cp_mixin_call_args5389);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop191;
                }
            } while (true);
            } finally {dbg.exitSubRule(191);}


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
            dbg.exitRule(getGrammarFileName(), "cp_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_mixin_call_args"


    // $ANTLR start "less_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(966, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:967:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? ) | ( LESS_DOTS | LESS_REST ) )
            int alt196=2;
            try { dbg.enterDecision(196, decisionCanBacktrack[196]);

            int LA196_0 = input.LA(1);

            if ( (LA196_0==MEDIA_SYM||LA196_0==AT_IDENT||LA196_0==SASS_VAR) ) {
                alt196=1;
            }
            else if ( ((LA196_0>=LESS_DOTS && LA196_0<=LESS_REST)) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    {
                    dbg.location(970,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    {
                    dbg.location(970,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5431);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(970,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
                    try { dbg.enterSubRule(193);

                    loop193:
                    do {
                        int alt193=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(970,18);
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

                    	    dbg.location(970,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:35: ws
                    	            {
                    	            dbg.location(970,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5445);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(192);}

                    	    dbg.location(970,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5448);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop193;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(193);}

                    dbg.location(970,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:50: ( ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST ) )?
                    int alt195=2;
                    try { dbg.enterSubRule(195);
                    try { dbg.enterDecision(195, decisionCanBacktrack[195]);

                    int LA195_0 = input.LA(1);

                    if ( (LA195_0==SEMI||LA195_0==COMMA) ) {
                        alt195=1;
                    }
                    } finally {dbg.exitDecision(195);}

                    switch (alt195) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:52: ( COMMA | SEMI ) ( ws )? ( LESS_DOTS | LESS_REST )
                            {
                            dbg.location(970,52);
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

                            dbg.location(970,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:69: ws
                                    {
                                    dbg.location(970,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5464);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(194);}

                            dbg.location(970,73);
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
                    } finally {dbg.exitSubRule(195);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:5: ( LESS_DOTS | LESS_REST )
                    {
                    dbg.location(972,5);
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
        dbg.location(973, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:976:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(976, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(978,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg5521);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(978,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:17: ( COLON ( ws )? cp_expression )?
            int alt198=2;
            try { dbg.enterSubRule(198);
            try { dbg.enterDecision(198, decisionCanBacktrack[198]);

            int LA198_0 = input.LA(1);

            if ( (LA198_0==COLON) ) {
                alt198=1;
            }
            } finally {dbg.exitDecision(198);}

            switch (alt198) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(978,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5525); if (state.failed) return ;
                    dbg.location(978,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:25: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:978:25: ws
                            {
                            dbg.location(978,25);
                            pushFollow(FOLLOW_ws_in_less_arg5527);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(197);}

                    dbg.location(978,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg5530);
                    cp_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(198);}


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
            dbg.exitRule(getGrammarFileName(), "less_arg");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_arg"


    // $ANTLR start "less_mixin_guarded"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(983, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:984:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(985,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded5556); if (state.failed) return ;
            dbg.location(985,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:15: ws
                    {
                    dbg.location(985,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded5558);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(199);}

            dbg.location(985,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5561);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(985,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(201);

            loop201:
            do {
                int alt201=2;
                try { dbg.enterDecision(201, decisionCanBacktrack[201]);

                int LA201_0 = input.LA(1);

                if ( (LA201_0==COMMA||LA201_0==AND) ) {
                    alt201=1;
                }


                } finally {dbg.exitDecision(201);}

                switch (alt201) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(985,36);
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

            	    dbg.location(985,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:50: ws
            	            {
            	            dbg.location(985,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded5573);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(200);}

            	    dbg.location(985,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5576);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop201;
                }
            } while (true);
            } finally {dbg.exitSubRule(201);}


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
            dbg.exitRule(getGrammarFileName(), "less_mixin_guarded");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_guarded"


    // $ANTLR start "less_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(990, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(992,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:5: ( NOT ( ws )? )?
            int alt203=2;
            try { dbg.enterSubRule(203);
            try { dbg.enterDecision(203, decisionCanBacktrack[203]);

            int LA203_0 = input.LA(1);

            if ( (LA203_0==NOT) ) {
                alt203=1;
            }
            } finally {dbg.exitDecision(203);}

            switch (alt203) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:6: NOT ( ws )?
                    {
                    dbg.location(992,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition5606); if (state.failed) return ;
                    dbg.location(992,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:10: ws
                            {
                            dbg.location(992,10);
                            pushFollow(FOLLOW_ws_in_less_condition5608);
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

            dbg.location(993,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition5617); if (state.failed) return ;
            dbg.location(993,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:12: ws
                    {
                    dbg.location(993,12);
                    pushFollow(FOLLOW_ws_in_less_condition5619);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(204);}

            dbg.location(994,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:994:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt209=2;
            try { dbg.enterSubRule(209);
            try { dbg.enterDecision(209, decisionCanBacktrack[209]);

            int LA209_0 = input.LA(1);

            if ( (LA209_0==IDENT) ) {
                alt209=1;
            }
            else if ( (LA209_0==MEDIA_SYM||LA209_0==AT_IDENT||LA209_0==SASS_VAR) ) {
                alt209=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 209, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(209);}

            switch (alt209) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(995,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition5645);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(995,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:40: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:995:40: ws
                            {
                            dbg.location(995,40);
                            pushFollow(FOLLOW_ws_in_less_condition5647);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(205);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(997,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(997,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition5678);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(997,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt208=2;
                    try { dbg.enterSubRule(208);
                    try { dbg.enterDecision(208, decisionCanBacktrack[208]);

                    int LA208_0 = input.LA(1);

                    if ( (LA208_0==WS||LA208_0==GREATER||LA208_0==OPEQ||(LA208_0>=NL && LA208_0<=COMMENT)||(LA208_0>=GREATER_OR_EQ && LA208_0<=LESS_OR_EQ)) ) {
                        alt208=1;
                    }
                    } finally {dbg.exitDecision(208);}

                    switch (alt208) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(997,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:28: ws
                                    {
                                    dbg.location(997,28);
                                    pushFollow(FOLLOW_ws_in_less_condition5681);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(206);}

                            dbg.location(997,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition5684);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(997,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:56: ws
                                    {
                                    dbg.location(997,56);
                                    pushFollow(FOLLOW_ws_in_less_condition5686);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(207);}

                            dbg.location(997,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition5689);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(208);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(209);}

            dbg.location(999,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition5718); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition"


    // $ANTLR start "less_function_in_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1003:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1003, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1005,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition5744);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1005,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:18: ws
                    {
                    dbg.location(1005,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5746);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(210);}

            dbg.location(1005,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition5749); if (state.failed) return ;
            dbg.location(1005,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:29: ws
                    {
                    dbg.location(1005,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5751);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(211);}

            dbg.location(1005,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition5754);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1005,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1005:45: ws
                    {
                    dbg.location(1005,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5756);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(212);}

            dbg.location(1005,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition5759); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_function_in_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function_in_condition"


    // $ANTLR start "less_fn_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1009:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1009, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: IDENT
            {
            dbg.location(1011,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name5781); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1012, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1014:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1014, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1015:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1015,5);
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
        dbg.location(1017, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1035:1: scss_selector_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* ;
    public final void scss_selector_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_selector_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1035, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1036:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            {
            dbg.location(1037,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1037:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            int alt213=2;
            try { dbg.enterSubRule(213);
            try { dbg.enterDecision(213, decisionCanBacktrack[213]);

            try {
                isCyclicDecision = true;
                alt213 = dfa213.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(213);}

            switch (alt213) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1038,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5880);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1040:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
                    {
                    dbg.location(1040,13);
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
            } finally {dbg.exitSubRule(213);}

            dbg.location(1042,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1042:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*
            try { dbg.enterSubRule(216);

            loop216:
            do {
                int alt216=2;
                try { dbg.enterDecision(216, decisionCanBacktrack[216]);

                try {
                    isCyclicDecision = true;
                    alt216 = dfa216.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(216);}

                switch (alt216) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1043:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    {
            	    dbg.location(1043,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1043:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1043:13: ws
            	            {
            	            dbg.location(1043,13);
            	            pushFollow(FOLLOW_ws_in_scss_selector_interpolation_expression5965);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(214);}

            	    dbg.location(1044,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1044:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )
            	    int alt215=2;
            	    try { dbg.enterSubRule(215);
            	    try { dbg.enterDecision(215, decisionCanBacktrack[215]);

            	    try {
            	        isCyclicDecision = true;
            	        alt215 = dfa215.predict(input);
            	    }
            	    catch (NoViableAltException nvae) {
            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(215);}

            	    switch (alt215) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1045,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6004);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1047:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON )
            	            {
            	            dbg.location(1047,17);
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
        dbg.location(1051, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1053:1: scss_declaration_interpolation_expression : ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* ;
    public final void scss_declaration_interpolation_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_declaration_interpolation_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1053, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1054:5: ( ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            {
            dbg.location(1055,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1055:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            int alt217=2;
            try { dbg.enterSubRule(217);
            try { dbg.enterDecision(217, decisionCanBacktrack[217]);

            int LA217_0 = input.LA(1);

            if ( (LA217_0==HASH_SYMBOL) ) {
                int LA217_1 = input.LA(2);

                if ( (LA217_1==LBRACE) && (synpred14_Css3())) {
                    alt217=1;
                }
                else if ( (LA217_1==IDENT||LA217_1==COLON||LA217_1==WS||(LA217_1>=MINUS && LA217_1<=DOT)||(LA217_1>=NL && LA217_1<=COMMENT)) ) {
                    alt217=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 217, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA217_0==IDENT||LA217_0==MINUS||(LA217_0>=HASH && LA217_0<=DOT)) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:13: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
                    {
                    dbg.location(1056,35);
                    pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6138);
                    scss_interpolation_expression_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1058:13: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
                    {
                    dbg.location(1058,13);
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
            } finally {dbg.exitSubRule(217);}

            dbg.location(1060,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1060:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) ) )*
            try { dbg.enterSubRule(220);

            loop220:
            do {
                int alt220=2;
                try { dbg.enterDecision(220, decisionCanBacktrack[220]);

                int LA220_0 = input.LA(1);

                if ( (LA220_0==IDENT||LA220_0==WS||(LA220_0>=MINUS && LA220_0<=DOT)||(LA220_0>=NL && LA220_0<=COMMENT)) ) {
                    alt220=1;
                }


                } finally {dbg.exitDecision(220);}

                switch (alt220) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    {
            	    dbg.location(1061,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1061:13: ws
            	            {
            	            dbg.location(1061,13);
            	            pushFollow(FOLLOW_ws_in_scss_declaration_interpolation_expression6219);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(218);}

            	    dbg.location(1062,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1062:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH ) )
            	    int alt219=2;
            	    try { dbg.enterSubRule(219);
            	    try { dbg.enterDecision(219, decisionCanBacktrack[219]);

            	    int LA219_0 = input.LA(1);

            	    if ( (LA219_0==HASH_SYMBOL) ) {
            	        int LA219_1 = input.LA(2);

            	        if ( (LA219_1==LBRACE) && (synpred15_Css3())) {
            	            alt219=1;
            	        }
            	        else if ( (LA219_1==IDENT||LA219_1==COLON||LA219_1==WS||(LA219_1>=MINUS && LA219_1<=DOT)||(LA219_1>=NL && LA219_1<=COMMENT)) ) {
            	            alt219=2;
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return ;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 219, 1, input);

            	            dbg.recognitionException(nvae);
            	            throw nvae;
            	        }
            	    }
            	    else if ( (LA219_0==IDENT||LA219_0==MINUS||(LA219_0>=HASH && LA219_0<=DOT)) ) {
            	        alt219=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 219, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(219);}

            	    switch (alt219) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:17: ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var
            	            {
            	            dbg.location(1063,39);
            	            pushFollow(FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6258);
            	            scss_interpolation_expression_var();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1065:17: ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH )
            	            {
            	            dbg.location(1065,17);
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
            	    } finally {dbg.exitSubRule(219);}


            	    }
            	    break;

            	default :
            	    break loop220;
                }
            } while (true);
            } finally {dbg.exitSubRule(220);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1069, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1071:1: scss_interpolation_expression_var : HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE ;
    public final void scss_interpolation_expression_var() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_interpolation_expression_var");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1071, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1072:5: ( HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:9: HASH_SYMBOL LBRACE ( ws )? ( cp_variable | less_function_in_condition ) ( ws )? RBRACE
            {
            dbg.location(1073,9);
            match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var6367); if (state.failed) return ;
            dbg.location(1073,21);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_interpolation_expression_var6369); if (state.failed) return ;
            dbg.location(1073,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:28: ws
                    {
                    dbg.location(1073,28);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var6371);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(221);}

            dbg.location(1073,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:32: ( cp_variable | less_function_in_condition )
            int alt222=2;
            try { dbg.enterSubRule(222);
            try { dbg.enterDecision(222, decisionCanBacktrack[222]);

            int LA222_0 = input.LA(1);

            if ( (LA222_0==MEDIA_SYM||LA222_0==AT_IDENT||LA222_0==SASS_VAR) ) {
                alt222=1;
            }
            else if ( (LA222_0==IDENT) ) {
                alt222=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 222, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(222);}

            switch (alt222) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:34: cp_variable
                    {
                    dbg.location(1073,34);
                    pushFollow(FOLLOW_cp_variable_in_scss_interpolation_expression_var6376);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:48: less_function_in_condition
                    {
                    dbg.location(1073,48);
                    pushFollow(FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var6380);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(222);}

            dbg.location(1073,77);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:77: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1073:77: ws
                    {
                    dbg.location(1073,77);
                    pushFollow(FOLLOW_ws_in_scss_interpolation_expression_var6384);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(223);}

            dbg.location(1073,81);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_interpolation_expression_var6387); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "scss_interpolation_expression_var");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "scss_interpolation_expression_var"


    // $ANTLR start "scss_nested_properties"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1094:1: scss_nested_properties : property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void scss_nested_properties() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "scss_nested_properties");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1094, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1095:5: ( property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:5: property COLON ( ws )? ( propertyValue )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(1096,5);
            pushFollow(FOLLOW_property_in_scss_nested_properties6431);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1096,14);
            match(input,COLON,FOLLOW_COLON_in_scss_nested_properties6433); if (state.failed) return ;
            dbg.location(1096,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:20: ( ws )?
            int alt224=2;
            try { dbg.enterSubRule(224);
            try { dbg.enterDecision(224, decisionCanBacktrack[224]);

            int LA224_0 = input.LA(1);

            if ( (LA224_0==WS||(LA224_0>=NL && LA224_0<=COMMENT)) ) {
                alt224=1;
            }
            } finally {dbg.exitDecision(224);}

            switch (alt224) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:20: ws
                    {
                    dbg.location(1096,20);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties6435);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(224);}

            dbg.location(1096,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:24: ( propertyValue )?
            int alt225=2;
            try { dbg.enterSubRule(225);
            try { dbg.enterDecision(225, decisionCanBacktrack[225]);

            int LA225_0 = input.LA(1);

            if ( ((LA225_0>=IDENT && LA225_0<=URI)||LA225_0==MEDIA_SYM||(LA225_0>=GEN && LA225_0<=LPAREN)||LA225_0==AT_IDENT||LA225_0==PERCENTAGE||LA225_0==PLUS||LA225_0==MINUS||LA225_0==HASH||(LA225_0>=NUMBER && LA225_0<=DIMENSION)||LA225_0==SASS_VAR) ) {
                alt225=1;
            }
            } finally {dbg.exitDecision(225);}

            switch (alt225) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:24: propertyValue
                    {
                    dbg.location(1096,24);
                    pushFollow(FOLLOW_propertyValue_in_scss_nested_properties6438);
                    propertyValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(225);}

            dbg.location(1096,39);
            match(input,LBRACE,FOLLOW_LBRACE_in_scss_nested_properties6441); if (state.failed) return ;
            dbg.location(1096,46);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:46: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1096:46: ws
                    {
                    dbg.location(1096,46);
                    pushFollow(FOLLOW_ws_in_scss_nested_properties6443);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(226);}

            dbg.location(1096,50);
            pushFollow(FOLLOW_syncToFollow_in_scss_nested_properties6446);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1096,63);
            pushFollow(FOLLOW_declarations_in_scss_nested_properties6448);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1096,76);
            match(input,RBRACE,FOLLOW_RBRACE_in_scss_nested_properties6450); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1097, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )
        int alt229=2;
        try { dbg.enterDecision(229, decisionCanBacktrack[229]);

        try {
            isCyclicDecision = true;
            alt229 = dfa229.predict(input);
        }
        catch (NoViableAltException nvae) {
            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(229);}

        switch (alt229) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI
                {
                dbg.location(565,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt227=0;
                try { dbg.enterSubRule(227);

                loop227:
                do {
                    int alt227=2;
                    try { dbg.enterDecision(227, decisionCanBacktrack[227]);

                    int LA227_0 = input.LA(1);

                    if ( (LA227_0==NAMESPACE_SYM||(LA227_0>=IDENT && LA227_0<=MEDIA_SYM)||(LA227_0>=AND && LA227_0<=LPAREN)||(LA227_0>=RPAREN && LA227_0<=LINE_COMMENT)) ) {
                        alt227=1;
                    }


                    } finally {dbg.exitDecision(227);}

                    switch (alt227) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(565,4);
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

                dbg.location(565,33);
                match(input,COLON,FOLLOW_COLON_in_synpred1_Css32031); if (state.failed) return ;
                dbg.location(565,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt228=0;
                try { dbg.enterSubRule(228);

                loop228:
                do {
                    int alt228=2;
                    try { dbg.enterDecision(228, decisionCanBacktrack[228]);

                    int LA228_0 = input.LA(1);

                    if ( (LA228_0==NAMESPACE_SYM||(LA228_0>=IDENT && LA228_0<=MEDIA_SYM)||(LA228_0>=AND && LA228_0<=LINE_COMMENT)) ) {
                        alt228=1;
                    }


                    } finally {dbg.exitDecision(228);}

                    switch (alt228) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(565,39);
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
                	    if ( cnt228 >= 1 ) break loop228;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(228, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt228++;
                } while (true);
                } finally {dbg.exitSubRule(228);}

                dbg.location(565,62);
                match(input,SEMI,FOLLOW_SEMI_in_synpred1_Css32043); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:69: scss_declaration_interpolation_expression COLON
                {
                dbg.location(565,69);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred1_Css32047);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(565,111);
                match(input,COLON,FOLLOW_COLON_in_synpred1_Css32049); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:3: ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )
        int alt232=2;
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

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE
                {
                dbg.location(567,4);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:4: (~ ( LBRACE | SEMI | RBRACE | COLON ) )+
                int cnt230=0;
                try { dbg.enterSubRule(230);

                loop230:
                do {
                    int alt230=2;
                    try { dbg.enterDecision(230, decisionCanBacktrack[230]);

                    int LA230_0 = input.LA(1);

                    if ( (LA230_0==NAMESPACE_SYM||(LA230_0>=IDENT && LA230_0<=MEDIA_SYM)||(LA230_0>=AND && LA230_0<=LPAREN)||(LA230_0>=RPAREN && LA230_0<=LINE_COMMENT)) ) {
                        alt230=1;
                    }


                    } finally {dbg.exitDecision(230);}

                    switch (alt230) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:4: ~ ( LBRACE | SEMI | RBRACE | COLON )
                	    {
                	    dbg.location(567,4);
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

                dbg.location(567,33);
                match(input,COLON,FOLLOW_COLON_in_synpred2_Css32079); if (state.failed) return ;
                dbg.location(567,39);
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:39: (~ ( SEMI | LBRACE | RBRACE ) )+
                int cnt231=0;
                try { dbg.enterSubRule(231);

                loop231:
                do {
                    int alt231=2;
                    try { dbg.enterDecision(231, decisionCanBacktrack[231]);

                    int LA231_0 = input.LA(1);

                    if ( (LA231_0==NAMESPACE_SYM||(LA231_0>=IDENT && LA231_0<=MEDIA_SYM)||(LA231_0>=AND && LA231_0<=LINE_COMMENT)) ) {
                        alt231=1;
                    }


                    } finally {dbg.exitDecision(231);}

                    switch (alt231) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:39: ~ ( SEMI | LBRACE | RBRACE )
                	    {
                	    dbg.location(567,39);
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
                	    if ( cnt231 >= 1 ) break loop231;
                	    if (state.backtracking>0) {state.failed=true; return ;}
                            EarlyExitException eee =
                                new EarlyExitException(231, input);
                            dbg.recognitionException(eee);

                            throw eee;
                    }
                    cnt231++;
                } while (true);
                } finally {dbg.exitSubRule(231);}

                dbg.location(567,62);
                match(input,LBRACE,FOLLOW_LBRACE_in_synpred2_Css32091); if (state.failed) return ;

                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:71: scss_declaration_interpolation_expression COLON
                {
                dbg.location(567,71);
                pushFollow(FOLLOW_scss_declaration_interpolation_expression_in_synpred2_Css32095);
                scss_declaration_interpolation_expression();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(567,113);
                match(input,COLON,FOLLOW_COLON_in_synpred2_Css32097); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(569,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:18: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt233=0;
        try { dbg.enterSubRule(233);

        loop233:
        do {
            int alt233=2;
            try { dbg.enterDecision(233, decisionCanBacktrack[233]);

            int LA233_0 = input.LA(1);

            if ( (LA233_0==NAMESPACE_SYM||(LA233_0>=IDENT && LA233_0<=MEDIA_SYM)||(LA233_0>=AND && LA233_0<=LINE_COMMENT)) ) {
                alt233=1;
            }


            } finally {dbg.exitDecision(233);}

            switch (alt233) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(569,18);
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
        	    if ( cnt233 >= 1 ) break loop233;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(233, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt233++;
        } while (true);
        } finally {dbg.exitSubRule(233);}

        dbg.location(569,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred3_Css32137); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(575,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:16: (~ ( RBRACE ) )+
        int cnt234=0;
        try { dbg.enterSubRule(234);

        loop234:
        do {
            int alt234=2;
            try { dbg.enterDecision(234, decisionCanBacktrack[234]);

            int LA234_0 = input.LA(1);

            if ( ((LA234_0>=NAMESPACE_SYM && LA234_0<=LBRACE)||(LA234_0>=AND && LA234_0<=LINE_COMMENT)) ) {
                alt234=1;
            }


            } finally {dbg.exitDecision(234);}

            switch (alt234) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:16: ~ ( RBRACE )
        	    {
        	    dbg.location(575,16);
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
        	    if ( cnt234 >= 1 ) break loop234;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(234, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt234++;
        } while (true);
        } finally {dbg.exitSubRule(234);}

        dbg.location(575,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred4_Css32224); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:9: ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:11: (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(581,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:11: (~ ( HASH_SYMBOL | LBRACE ) )*
        try { dbg.enterSubRule(235);

        loop235:
        do {
            int alt235=2;
            try { dbg.enterDecision(235, decisionCanBacktrack[235]);

            int LA235_0 = input.LA(1);

            if ( ((LA235_0>=NAMESPACE_SYM && LA235_0<=MEDIA_SYM)||(LA235_0>=RBRACE && LA235_0<=MINUS)||(LA235_0>=HASH && LA235_0<=LINE_COMMENT)) ) {
                alt235=1;
            }


            } finally {dbg.exitDecision(235);}

            switch (alt235) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:11: ~ ( HASH_SYMBOL | LBRACE )
        	    {
        	    dbg.location(581,11);
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
        	    break loop235;
            }
        } while (true);
        } finally {dbg.exitSubRule(235);}

        dbg.location(581,38);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred5_Css32282); if (state.failed) return ;
        dbg.location(581,50);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Css32284); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:19: esPred
        {
        dbg.location(594,19);
        pushFollow(FOLLOW_esPred_in_synpred6_Css32382);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:6: esPred
        {
        dbg.location(596,6);
        pushFollow(FOLLOW_esPred_in_synpred7_Css32403);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(610,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:8: ( IDENT | STAR )?
        int alt236=2;
        try { dbg.enterSubRule(236);
        try { dbg.enterDecision(236, decisionCanBacktrack[236]);

        int LA236_0 = input.LA(1);

        if ( (LA236_0==IDENT||LA236_0==STAR) ) {
            alt236=1;
        }
        } finally {dbg.exitDecision(236);}

        switch (alt236) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(610,8);
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
        } finally {dbg.exitSubRule(236);}

        dbg.location(610,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred8_Css32517); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:9: ( (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )* HASH_SYMBOL LBRACE
        {
        dbg.location(708,11);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:11: (~ ( HASH_SYMBOL | COLON | SEMI | RBRACE ) )*
        try { dbg.enterSubRule(237);

        loop237:
        do {
            int alt237=2;
            try { dbg.enterDecision(237, decisionCanBacktrack[237]);

            int LA237_0 = input.LA(1);

            if ( (LA237_0==NAMESPACE_SYM||(LA237_0>=IDENT && LA237_0<=LBRACE)||(LA237_0>=AND && LA237_0<=LPAREN)||(LA237_0>=RPAREN && LA237_0<=MINUS)||(LA237_0>=HASH && LA237_0<=LINE_COMMENT)) ) {
                alt237=1;
            }


            } finally {dbg.exitDecision(237);}

            switch (alt237) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:11: ~ ( HASH_SYMBOL | COLON | SEMI | RBRACE )
        	    {
        	    dbg.location(708,11);
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
        	    break loop237;
            }
        } while (true);
        } finally {dbg.exitSubRule(237);}

        dbg.location(708,51);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred9_Css33475); if (state.failed) return ;
        dbg.location(708,63);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred9_Css33477); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:12: expressionPredicate
        {
        dbg.location(723,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred10_Css33563);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

    // $ANTLR start synpred11_Css3
    public final void synpred11_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:17: term
        {
        dbg.location(895,17);
        pushFollow(FOLLOW_term_in_synpred11_Css34802);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Css3

    // $ANTLR start synpred12_Css3
    public final void synpred12_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1038:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1038,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred12_Css35875); if (state.failed) return ;
        dbg.location(1038,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Css35877); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Css3

    // $ANTLR start synpred13_Css3
    public final void synpred13_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1045:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1045,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred13_Css35999); if (state.failed) return ;
        dbg.location(1045,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred13_Css36001); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Css3

    // $ANTLR start synpred14_Css3
    public final void synpred14_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:13: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1056:14: HASH_SYMBOL LBRACE
        {
        dbg.location(1056,14);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred14_Css36133); if (state.failed) return ;
        dbg.location(1056,26);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred14_Css36135); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Css3

    // $ANTLR start synpred15_Css3
    public final void synpred15_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:17: ( HASH_SYMBOL LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1063:18: HASH_SYMBOL LBRACE
        {
        dbg.location(1063,18);
        match(input,HASH_SYMBOL,FOLLOW_HASH_SYMBOL_in_synpred15_Css36253); if (state.failed) return ;
        dbg.location(1063,30);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred15_Css36255); if (state.failed) return ;

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


    protected DFA22 dfa22 = new DFA22(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA67 dfa67 = new DFA67(this);
    protected DFA93 dfa93 = new DFA93(this);
    protected DFA99 dfa99 = new DFA99(this);
    protected DFA104 dfa104 = new DFA104(this);
    protected DFA107 dfa107 = new DFA107(this);
    protected DFA125 dfa125 = new DFA125(this);
    protected DFA135 dfa135 = new DFA135(this);
    protected DFA139 dfa139 = new DFA139(this);
    protected DFA142 dfa142 = new DFA142(this);
    protected DFA148 dfa148 = new DFA148(this);
    protected DFA169 dfa169 = new DFA169(this);
    protected DFA173 dfa173 = new DFA173(this);
    protected DFA188 dfa188 = new DFA188(this);
    protected DFA193 dfa193 = new DFA193(this);
    protected DFA213 dfa213 = new DFA213(this);
    protected DFA216 dfa216 = new DFA216(this);
    protected DFA215 dfa215 = new DFA215(this);
    protected DFA229 dfa229 = new DFA229(this);
    protected DFA232 dfa232 = new DFA232(this);
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
    static final String DFA46_eotS =
        "\31\uffff";
    static final String DFA46_eofS =
        "\31\uffff";
    static final String DFA46_minS =
        "\2\6\1\uffff\1\6\4\uffff\1\6\2\uffff\1\5\1\6\1\uffff\1\6\1\5\1\6"+
        "\3\5\1\6\1\5\1\6\2\5";
    static final String DFA46_maxS =
        "\1\126\1\122\1\uffff\1\122\4\uffff\1\122\2\uffff\2\122\1\uffff\2"+
        "\122\1\130\2\122\1\131\1\130\1\131\1\130\2\122";
    static final String DFA46_acceptS =
        "\2\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\2\uffff\1"+
        "\2\13\uffff";
    static final String DFA46_specialS =
        "\31\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\2\5\uffff\1\3\5\uffff\1\2\1\uffff\1\2\1\uffff\1\10\1\uffff"+
            "\1\7\3\uffff\1\7\1\uffff\1\4\1\5\1\6\24\uffff\3\2\1\1\4\2\1"+
            "\uffff\1\2\25\uffff\1\11\1\2\1\12",
            "\1\13\6\uffff\1\2\4\uffff\1\2\1\uffff\1\2\2\uffff\1\2\35\uffff"+
            "\4\2\30\uffff\2\2",
            "",
            "\1\15\6\uffff\1\15\2\uffff\4\15\1\11\2\uffff\1\14\71\uffff"+
            "\2\14",
            "",
            "",
            "",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\16\71\uffff\2\11",
            "",
            "",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\20\1\2\2\uffff"+
            "\1\17\32\uffff\13\2\1\uffff\1\2\22\uffff\2\17",
            "\1\15\6\uffff\1\15\2\uffff\4\15\1\11\2\uffff\1\14\71\uffff"+
            "\2\14",
            "",
            "\2\7\5\uffff\1\7\6\uffff\1\11\2\uffff\1\16\71\uffff\2\11",
            "\1\12\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\20\1\2\2\uffff"+
            "\1\17\32\uffff\13\2\1\uffff\1\2\22\uffff\2\17",
            "\3\12\3\uffff\1\21\5\uffff\1\12\2\uffff\1\23\1\21\6\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\3"+
            "\uffff\1\22\2\uffff\2\2",
            "\1\24\5\uffff\1\24\10\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\24\5\uffff\1\24\10\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\12\7\uffff\1\2\11\uffff\1\25\71\uffff\2\25\6\uffff\1\2",
            "\3\12\3\uffff\1\27\5\uffff\1\12\3\uffff\1\27\1\26\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\26\1\uffff\1\30\2\uffff\2\2",
            "\1\12\7\uffff\1\2\11\uffff\1\25\71\uffff\2\25\6\uffff\1\2",
            "\3\12\3\uffff\1\27\5\uffff\1\12\3\uffff\1\27\1\26\5\uffff\1"+
            "\12\24\uffff\1\12\2\uffff\1\12\1\uffff\1\12\17\uffff\12\12\2"+
            "\26\1\uffff\1\30\2\uffff\2\2",
            "\1\24\5\uffff\1\24\10\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12",
            "\1\24\5\uffff\1\24\10\uffff\1\2\1\23\1\uffff\1\12\71\uffff"+
            "\2\12"
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "400:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration | {...}? cp_mixin_call );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA67_eotS =
        "\4\uffff";
    static final String DFA67_eofS =
        "\4\uffff";
    static final String DFA67_minS =
        "\2\13\2\uffff";
    static final String DFA67_maxS =
        "\2\122\2\uffff";
    static final String DFA67_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA67_specialS =
        "\4\uffff}>";
    static final String[] DFA67_transitionS = {
            "\1\3\1\uffff\1\2\11\uffff\1\1\71\uffff\2\1",
            "\1\3\1\uffff\1\2\11\uffff\1\1\71\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA67_eot = DFA.unpackEncodedString(DFA67_eotS);
    static final short[] DFA67_eof = DFA.unpackEncodedString(DFA67_eofS);
    static final char[] DFA67_min = DFA.unpackEncodedStringToUnsignedChars(DFA67_minS);
    static final char[] DFA67_max = DFA.unpackEncodedStringToUnsignedChars(DFA67_maxS);
    static final short[] DFA67_accept = DFA.unpackEncodedString(DFA67_acceptS);
    static final short[] DFA67_special = DFA.unpackEncodedString(DFA67_specialS);
    static final short[][] DFA67_transition;

    static {
        int numStates = DFA67_transitionS.length;
        DFA67_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA67_transition[i] = DFA.unpackEncodedString(DFA67_transitionS[i]);
        }
    }

    class DFA67 extends DFA {

        public DFA67(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 67;
            this.eot = DFA67_eot;
            this.eof = DFA67_eof;
            this.min = DFA67_min;
            this.max = DFA67_max;
            this.accept = DFA67_accept;
            this.special = DFA67_special;
            this.transition = DFA67_transition;
        }
        public String getDescription() {
            return "()* loopback of 465:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA93_eotS =
        "\6\uffff";
    static final String DFA93_eofS =
        "\6\uffff";
    static final String DFA93_minS =
        "\2\6\2\uffff\2\6";
    static final String DFA93_maxS =
        "\1\125\1\122\2\uffff\2\122";
    static final String DFA93_acceptS =
        "\2\uffff\1\1\1\2\2\uffff";
    static final String DFA93_specialS =
        "\6\uffff}>";
    static final String[] DFA93_transitionS = {
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

    static final short[] DFA93_eot = DFA.unpackEncodedString(DFA93_eotS);
    static final short[] DFA93_eof = DFA.unpackEncodedString(DFA93_eofS);
    static final char[] DFA93_min = DFA.unpackEncodedStringToUnsignedChars(DFA93_minS);
    static final char[] DFA93_max = DFA.unpackEncodedStringToUnsignedChars(DFA93_maxS);
    static final short[] DFA93_accept = DFA.unpackEncodedString(DFA93_acceptS);
    static final short[] DFA93_special = DFA.unpackEncodedString(DFA93_specialS);
    static final short[][] DFA93_transition;

    static {
        int numStates = DFA93_transitionS.length;
        DFA93_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA93_transition[i] = DFA.unpackEncodedString(DFA93_transitionS[i]);
        }
    }

    class DFA93 extends DFA {

        public DFA93(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 93;
            this.eot = DFA93_eot;
            this.eof = DFA93_eof;
            this.min = DFA93_min;
            this.max = DFA93_max;
            this.accept = DFA93_accept;
            this.special = DFA93_special;
            this.transition = DFA93_transition;
        }
        public String getDescription() {
            return "542:9: ( ({...}? cp_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA99_eotS =
        "\24\uffff";
    static final String DFA99_eofS =
        "\24\uffff";
    static final String DFA99_minS =
        "\1\6\7\0\1\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA99_maxS =
        "\1\126\7\0\1\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA99_acceptS =
        "\10\uffff\1\5\1\uffff\4\3\1\uffff\2\3\1\4\1\1\1\2";
    static final String DFA99_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\10\4\uffff\1\11\5\uffff}>";
    static final String[] DFA99_transitionS = {
            "\1\3\5\uffff\1\6\1\uffff\1\10\3\uffff\1\5\1\uffff\1\13\1\uffff"+
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
            return "()* loopback of 561:13: ( ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON )=> declaration SEMI ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON )=> scss_nested_properties ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | {...}? cp_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA99_0 = input.LA(1);

                         
                        int index99_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA99_0==STAR) ) {s = 1;}

                        else if ( (LA99_0==HASH_SYMBOL) ) {s = 2;}

                        else if ( (LA99_0==IDENT) ) {s = 3;}

                        else if ( (LA99_0==DOT) ) {s = 4;}

                        else if ( (LA99_0==GEN) ) {s = 5;}

                        else if ( (LA99_0==MEDIA_SYM||LA99_0==AT_IDENT) ) {s = 6;}

                        else if ( (LA99_0==SASS_VAR) ) {s = 7;}

                        else if ( (LA99_0==RBRACE) ) {s = 8;}

                        else if ( (LA99_0==HASH) ) {s = 9;}

                        else if ( (LA99_0==SASS_MIXIN) && (synpred3_Css3())) {s = 10;}

                        else if ( (LA99_0==COLON) && (synpred3_Css3())) {s = 11;}

                        else if ( (LA99_0==PIPE) && (synpred3_Css3())) {s = 12;}

                        else if ( (LA99_0==LESS_AND) && (synpred3_Css3())) {s = 13;}

                        else if ( (LA99_0==MINUS) ) {s = 14;}

                        else if ( (LA99_0==LBRACKET) && (synpred3_Css3())) {s = 15;}

                        else if ( (LA99_0==DCOLON) && (synpred3_Css3())) {s = 16;}

                        else if ( (LA99_0==SASS_INCLUDE) ) {s = 17;}

                         
                        input.seek(index99_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA99_1 = input.LA(1);

                         
                        int index99_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA99_2 = input.LA(1);

                         
                        int index99_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA99_3 = input.LA(1);

                         
                        int index99_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred2_Css3()) ) {s = 19;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA99_4 = input.LA(1);

                         
                        int index99_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( ((synpred3_Css3()||((synpred3_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()")))) ) {s = 16;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 17;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA99_5 = input.LA(1);

                         
                        int index99_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred2_Css3()) ) {s = 19;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA99_6 = input.LA(1);

                         
                        int index99_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred1_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 18;}

                        else if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 19;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 8;}

                         
                        input.seek(index99_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA99_7 = input.LA(1);

                         
                        int index99_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred1_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 18;}

                        else if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 19;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 8;}

                         
                        input.seek(index99_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA99_9 = input.LA(1);

                         
                        int index99_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA99_14 = input.LA(1);

                         
                        int index99_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 18;}

                        else if ( (synpred3_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index99_14);
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
    static final String DFA104_eotS =
        "\17\uffff";
    static final String DFA104_eofS =
        "\17\uffff";
    static final String DFA104_minS =
        "\2\6\2\0\1\uffff\2\6\5\uffff\1\0\1\uffff\1\0";
    static final String DFA104_maxS =
        "\1\76\1\122\2\0\1\uffff\2\122\5\uffff\1\0\1\uffff\1\0";
    static final String DFA104_acceptS =
        "\4\uffff\1\2\2\uffff\5\1\1\uffff\1\1\1\uffff";
    static final String DFA104_specialS =
        "\1\4\1\0\1\3\1\1\1\uffff\1\5\1\2\5\uffff\1\7\1\uffff\1\6}>";
    static final String[] DFA104_transitionS = {
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

    static final short[] DFA104_eot = DFA.unpackEncodedString(DFA104_eotS);
    static final short[] DFA104_eof = DFA.unpackEncodedString(DFA104_eofS);
    static final char[] DFA104_min = DFA.unpackEncodedStringToUnsignedChars(DFA104_minS);
    static final char[] DFA104_max = DFA.unpackEncodedStringToUnsignedChars(DFA104_maxS);
    static final short[] DFA104_accept = DFA.unpackEncodedString(DFA104_acceptS);
    static final short[] DFA104_special = DFA.unpackEncodedString(DFA104_specialS);
    static final short[][] DFA104_transition;

    static {
        int numStates = DFA104_transitionS.length;
        DFA104_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA104_transition[i] = DFA.unpackEncodedString(DFA104_transitionS[i]);
        }
    }

    class DFA104 extends DFA {

        public DFA104(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 104;
            this.eot = DFA104_eot;
            this.eof = DFA104_eof;
            this.min = DFA104_min;
            this.max = DFA104_max;
            this.accept = DFA104_accept;
            this.special = DFA104_special;
            this.transition = DFA104_transition;
        }
        public String getDescription() {
            return "578:1: selectorsGroup : ( ( (~ ( HASH_SYMBOL | LBRACE ) )* HASH_SYMBOL LBRACE )=> scss_selector_interpolation_expression ( ws )? | selector ( COMMA ( ws )? selector )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA104_1 = input.LA(1);

                         
                        int index104_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA104_1==LBRACE) && (synpred5_Css3())) {s = 8;}

                        else if ( (LA104_1==NAME) ) {s = 4;}

                        else if ( (LA104_1==WS||(LA104_1>=NL && LA104_1<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA104_1==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA104_1==IDENT||LA104_1==COLON||LA104_1==MINUS||(LA104_1>=HASH && LA104_1<=DOT)) && (synpred5_Css3())) {s = 11;}

                         
                        input.seek(index104_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA104_3 = input.LA(1);

                         
                        int index104_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index104_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA104_6 = input.LA(1);

                         
                        int index104_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA104_6==WS||(LA104_6>=NL && LA104_6<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA104_6==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA104_6==IDENT) ) {s = 14;}

                        else if ( (LA104_6==LBRACE) && (synpred5_Css3())) {s = 13;}

                        else if ( (LA104_6==COLON||LA104_6==MINUS||(LA104_6>=HASH && LA104_6<=DOT)) && (synpred5_Css3())) {s = 11;}

                        else if ( ((LA104_6>=NOT && LA104_6<=GEN)) ) {s = 4;}

                         
                        input.seek(index104_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA104_2 = input.LA(1);

                         
                        int index104_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index104_2);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA104_0 = input.LA(1);

                         
                        int index104_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA104_0==HASH_SYMBOL) ) {s = 1;}

                        else if ( (LA104_0==IDENT) ) {s = 2;}

                        else if ( (LA104_0==HASH) ) {s = 3;}

                        else if ( (LA104_0==GEN||(LA104_0>=LBRACKET && LA104_0<=PIPE)||LA104_0==LESS_AND) ) {s = 4;}

                        else if ( (LA104_0==DOT) ) {s = 5;}

                        else if ( (LA104_0==COLON) ) {s = 6;}

                        else if ( (LA104_0==MINUS) && (synpred5_Css3())) {s = 7;}

                         
                        input.seek(index104_0);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA104_5 = input.LA(1);

                         
                        int index104_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA104_5==IDENT) ) {s = 12;}

                        else if ( (LA104_5==WS||(LA104_5>=NL && LA104_5<=COMMENT)) && (synpred5_Css3())) {s = 9;}

                        else if ( (LA104_5==HASH_SYMBOL) && (synpred5_Css3())) {s = 10;}

                        else if ( (LA104_5==GEN) ) {s = 4;}

                        else if ( (LA104_5==COLON||LA104_5==MINUS||(LA104_5>=HASH && LA104_5<=DOT)) && (synpred5_Css3())) {s = 11;}

                        else if ( (LA104_5==LBRACE) && (synpred5_Css3())) {s = 13;}

                         
                        input.seek(index104_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA104_14 = input.LA(1);

                         
                        int index104_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index104_14);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA104_12 = input.LA(1);

                         
                        int index104_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index104_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 104, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA107_eotS =
        "\21\uffff";
    static final String DFA107_eofS =
        "\21\uffff";
    static final String DFA107_minS =
        "\1\6\7\uffff\5\0\4\uffff";
    static final String DFA107_maxS =
        "\1\76\7\uffff\5\0\4\uffff";
    static final String DFA107_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA107_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA107_transitionS = {
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

    static final short[] DFA107_eot = DFA.unpackEncodedString(DFA107_eotS);
    static final short[] DFA107_eof = DFA.unpackEncodedString(DFA107_eofS);
    static final char[] DFA107_min = DFA.unpackEncodedStringToUnsignedChars(DFA107_minS);
    static final char[] DFA107_max = DFA.unpackEncodedStringToUnsignedChars(DFA107_maxS);
    static final short[] DFA107_accept = DFA.unpackEncodedString(DFA107_acceptS);
    static final short[] DFA107_special = DFA.unpackEncodedString(DFA107_specialS);
    static final short[][] DFA107_transition;

    static {
        int numStates = DFA107_transitionS.length;
        DFA107_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA107_transition[i] = DFA.unpackEncodedString(DFA107_transitionS[i]);
        }
    }

    class DFA107 extends DFA {

        public DFA107(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 107;
            this.eot = DFA107_eot;
            this.eof = DFA107_eof;
            this.min = DFA107_min;
            this.max = DFA107_max;
            this.accept = DFA107_accept;
            this.special = DFA107_special;
            this.transition = DFA107_transition;
        }
        public String getDescription() {
            return "()* loopback of 594:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA107_8 = input.LA(1);

                         
                        int index107_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index107_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA107_9 = input.LA(1);

                         
                        int index107_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index107_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA107_10 = input.LA(1);

                         
                        int index107_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index107_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA107_11 = input.LA(1);

                         
                        int index107_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index107_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA107_12 = input.LA(1);

                         
                        int index107_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index107_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 107, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA125_eotS =
        "\4\uffff";
    static final String DFA125_eofS =
        "\4\uffff";
    static final String DFA125_minS =
        "\2\6\2\uffff";
    static final String DFA125_maxS =
        "\2\122\2\uffff";
    static final String DFA125_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA125_specialS =
        "\4\uffff}>";
    static final String[] DFA125_transitionS = {
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1\1"+
            "\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "\1\3\4\uffff\1\3\1\uffff\1\3\4\uffff\1\3\1\2\2\3\1\uffff\1"+
            "\1\32\uffff\3\3\1\uffff\7\3\1\uffff\1\3\22\uffff\2\1",
            "",
            ""
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
            return "694:21: ( ( ws )? LPAREN ( ws )? ( expression | STAR )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA135_eotS =
        "\17\uffff";
    static final String DFA135_eofS =
        "\17\uffff";
    static final String DFA135_minS =
        "\2\6\10\0\1\uffff\1\6\2\0\1\uffff";
    static final String DFA135_maxS =
        "\2\124\10\0\1\uffff\1\124\2\0\1\uffff";
    static final String DFA135_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA135_specialS =
        "\2\uffff\1\2\1\4\1\11\1\7\1\10\1\3\1\5\1\1\2\uffff\1\6\1\0\1\uffff}>";
    static final String[] DFA135_transitionS = {
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
            return "721:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA135_13 = input.LA(1);

                         
                        int index135_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA135_9 = input.LA(1);

                         
                        int index135_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index135_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA135_2 = input.LA(1);

                         
                        int index135_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA135_7 = input.LA(1);

                         
                        int index135_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA135_3 = input.LA(1);

                         
                        int index135_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_3);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA135_8 = input.LA(1);

                         
                        int index135_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred10_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index135_8);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA135_12 = input.LA(1);

                         
                        int index135_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA135_5 = input.LA(1);

                         
                        int index135_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_5);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA135_6 = input.LA(1);

                         
                        int index135_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_6);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA135_4 = input.LA(1);

                         
                        int index135_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index135_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 135, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA139_eotS =
        "\12\uffff";
    static final String DFA139_eofS =
        "\12\uffff";
    static final String DFA139_minS =
        "\1\5\1\uffff\1\6\1\uffff\1\6\1\5\1\6\1\5\2\23";
    static final String DFA139_maxS =
        "\1\124\1\uffff\1\124\1\uffff\2\124\1\6\1\124\2\122";
    static final String DFA139_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA139_specialS =
        "\12\uffff}>";
    static final String[] DFA139_transitionS = {
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

    static final short[] DFA139_eot = DFA.unpackEncodedString(DFA139_eotS);
    static final short[] DFA139_eof = DFA.unpackEncodedString(DFA139_eofS);
    static final char[] DFA139_min = DFA.unpackEncodedStringToUnsignedChars(DFA139_minS);
    static final char[] DFA139_max = DFA.unpackEncodedStringToUnsignedChars(DFA139_maxS);
    static final short[] DFA139_accept = DFA.unpackEncodedString(DFA139_acceptS);
    static final short[] DFA139_special = DFA.unpackEncodedString(DFA139_specialS);
    static final short[][] DFA139_transition;

    static {
        int numStates = DFA139_transitionS.length;
        DFA139_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA139_transition[i] = DFA.unpackEncodedString(DFA139_transitionS[i]);
        }
    }

    class DFA139 extends DFA {

        public DFA139(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 139;
            this.eot = DFA139_eot;
            this.eof = DFA139_eof;
            this.min = DFA139_min;
            this.max = DFA139_max;
            this.accept = DFA139_accept;
            this.special = DFA139_special;
            this.transition = DFA139_transition;
        }
        public String getDescription() {
            return "()* loopback of 781:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA142_eotS =
        "\13\uffff";
    static final String DFA142_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA142_minS =
        "\1\6\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA142_maxS =
        "\1\124\2\uffff\1\124\5\uffff\1\124\1\uffff";
    static final String DFA142_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA142_specialS =
        "\13\uffff}>";
    static final String[] DFA142_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\5\uffff\1\4\3\uffff\1\7\6\uffff\1\1"+
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
            return "786:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA148_eotS =
        "\10\uffff";
    static final String DFA148_eofS =
        "\10\uffff";
    static final String DFA148_minS =
        "\1\6\1\uffff\3\6\1\uffff\2\23";
    static final String DFA148_maxS =
        "\1\124\1\uffff\2\124\1\6\1\uffff\2\122";
    static final String DFA148_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA148_specialS =
        "\10\uffff}>";
    static final String[] DFA148_transitionS = {
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
            return "814:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA169_eotS =
        "\36\uffff";
    static final String DFA169_eofS =
        "\36\uffff";
    static final String DFA169_minS =
        "\1\5\1\uffff\2\6\10\uffff\1\6\10\0\1\6\10\0";
    static final String DFA169_maxS =
        "\1\124\1\uffff\2\124\10\uffff\1\124\10\0\1\124\10\0";
    static final String DFA169_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA169_specialS =
        "\1\17\14\uffff\1\20\1\16\1\12\1\4\1\1\1\7\1\10\1\13\1\uffff\1\5"+
        "\1\2\1\6\1\0\1\11\1\3\1\14\1\15}>";
    static final String[] DFA169_transitionS = {
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
            return "()* loopback of 895:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA169_25 = input.LA(1);

                         
                        int index169_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_25);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA169_17 = input.LA(1);

                         
                        int index169_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_17);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA169_23 = input.LA(1);

                         
                        int index169_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_23);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA169_27 = input.LA(1);

                         
                        int index169_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_27);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA169_16 = input.LA(1);

                         
                        int index169_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_16);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA169_22 = input.LA(1);

                         
                        int index169_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_22);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA169_24 = input.LA(1);

                         
                        int index169_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_24);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA169_18 = input.LA(1);

                         
                        int index169_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_18);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA169_19 = input.LA(1);

                         
                        int index169_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_19);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA169_26 = input.LA(1);

                         
                        int index169_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_26);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA169_15 = input.LA(1);

                         
                        int index169_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_15);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA169_20 = input.LA(1);

                         
                        int index169_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_20);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA169_28 = input.LA(1);

                         
                        int index169_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_28);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA169_29 = input.LA(1);

                         
                        int index169_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_29);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA169_14 = input.LA(1);

                         
                        int index169_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA169_0 = input.LA(1);

                         
                        int index169_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA169_0==SEMI||LA169_0==COMMA||(LA169_0>=LBRACE && LA169_0<=RBRACE)||LA169_0==RPAREN||LA169_0==SOLIDUS||LA169_0==STAR||LA169_0==IMPORTANT_SYM||LA169_0==SASS_DEFAULT) ) {s = 1;}

                        else if ( (LA169_0==PLUS) ) {s = 2;}

                        else if ( (LA169_0==MINUS) ) {s = 3;}

                        else if ( (LA169_0==PERCENTAGE||(LA169_0>=NUMBER && LA169_0<=DIMENSION)) && (synpred11_Css3())) {s = 4;}

                        else if ( (LA169_0==STRING) && (synpred11_Css3())) {s = 5;}

                        else if ( (LA169_0==IDENT) && (synpred11_Css3())) {s = 6;}

                        else if ( (LA169_0==GEN) && (synpred11_Css3())) {s = 7;}

                        else if ( (LA169_0==URI) && (synpred11_Css3())) {s = 8;}

                        else if ( (LA169_0==HASH) && (synpred11_Css3())) {s = 9;}

                        else if ( (LA169_0==MEDIA_SYM||LA169_0==AT_IDENT) && (synpred11_Css3())) {s = 10;}

                        else if ( (LA169_0==SASS_VAR) && (synpred11_Css3())) {s = 11;}

                         
                        input.seek(index169_0);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA169_13 = input.LA(1);

                         
                        int index169_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index169_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 169, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA173_eotS =
        "\13\uffff";
    static final String DFA173_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA173_minS =
        "\1\6\2\uffff\1\23\5\uffff\1\23\1\uffff";
    static final String DFA173_maxS =
        "\1\124\2\uffff\1\122\5\uffff\1\122\1\uffff";
    static final String DFA173_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA173_specialS =
        "\13\uffff}>";
    static final String[] DFA173_transitionS = {
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

    static final short[] DFA173_eot = DFA.unpackEncodedString(DFA173_eotS);
    static final short[] DFA173_eof = DFA.unpackEncodedString(DFA173_eofS);
    static final char[] DFA173_min = DFA.unpackEncodedStringToUnsignedChars(DFA173_minS);
    static final char[] DFA173_max = DFA.unpackEncodedStringToUnsignedChars(DFA173_maxS);
    static final short[] DFA173_accept = DFA.unpackEncodedString(DFA173_acceptS);
    static final short[] DFA173_special = DFA.unpackEncodedString(DFA173_specialS);
    static final short[][] DFA173_transition;

    static {
        int numStates = DFA173_transitionS.length;
        DFA173_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA173_transition[i] = DFA.unpackEncodedString(DFA173_transitionS[i]);
        }
    }

    class DFA173 extends DFA {

        public DFA173(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 173;
            this.eot = DFA173_eot;
            this.eof = DFA173_eof;
            this.min = DFA173_min;
            this.max = DFA173_max;
            this.accept = DFA173_accept;
            this.special = DFA173_special;
            this.transition = DFA173_transition;
        }
        public String getDescription() {
            return "902:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA188_eotS =
        "\4\uffff";
    static final String DFA188_eofS =
        "\4\uffff";
    static final String DFA188_minS =
        "\2\5\2\uffff";
    static final String DFA188_maxS =
        "\2\122\2\uffff";
    static final String DFA188_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA188_specialS =
        "\4\uffff}>";
    static final String[] DFA188_transitionS = {
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "\1\3\15\uffff\1\2\3\uffff\1\1\71\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA188_eot = DFA.unpackEncodedString(DFA188_eotS);
    static final short[] DFA188_eof = DFA.unpackEncodedString(DFA188_eofS);
    static final char[] DFA188_min = DFA.unpackEncodedStringToUnsignedChars(DFA188_minS);
    static final char[] DFA188_max = DFA.unpackEncodedStringToUnsignedChars(DFA188_maxS);
    static final short[] DFA188_accept = DFA.unpackEncodedString(DFA188_acceptS);
    static final short[] DFA188_special = DFA.unpackEncodedString(DFA188_specialS);
    static final short[][] DFA188_transition;

    static {
        int numStates = DFA188_transitionS.length;
        DFA188_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA188_transition[i] = DFA.unpackEncodedString(DFA188_transitionS[i]);
        }
    }

    class DFA188 extends DFA {

        public DFA188(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 188;
            this.eot = DFA188_eot;
            this.eof = DFA188_eof;
            this.min = DFA188_min;
            this.max = DFA188_max;
            this.accept = DFA188_accept;
            this.special = DFA188_special;
            this.transition = DFA188_transition;
        }
        public String getDescription() {
            return "950:5: ( ( ws )? LPAREN ( cp_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA193_eotS =
        "\5\uffff";
    static final String DFA193_eofS =
        "\5\uffff";
    static final String DFA193_minS =
        "\1\5\1\14\1\uffff\1\14\1\uffff";
    static final String DFA193_maxS =
        "\1\25\1\130\1\uffff\1\130\1\uffff";
    static final String DFA193_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA193_specialS =
        "\5\uffff}>";
    static final String[] DFA193_transitionS = {
            "\1\1\5\uffff\1\1\11\uffff\1\2",
            "\1\4\11\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
            "",
            "\1\4\11\uffff\1\4\1\3\71\uffff\2\3\1\uffff\1\4\2\uffff\2\2",
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
            return "()* loopback of 970:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA213_eotS =
        "\14\uffff";
    static final String DFA213_eofS =
        "\14\uffff";
    static final String DFA213_minS =
        "\2\6\1\uffff\2\6\2\16\1\6\1\16\1\uffff\1\6\1\uffff";
    static final String DFA213_maxS =
        "\1\70\1\122\1\uffff\2\126\4\122\1\uffff\1\122\1\uffff";
    static final String DFA213_acceptS =
        "\2\uffff\1\2\6\uffff\1\1\1\uffff\1\1";
    static final String DFA213_specialS =
        "\5\uffff\1\3\1\0\1\4\1\1\1\uffff\1\2\1\uffff}>";
    static final String[] DFA213_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            ""
    };

    static final short[] DFA213_eot = DFA.unpackEncodedString(DFA213_eotS);
    static final short[] DFA213_eof = DFA.unpackEncodedString(DFA213_eofS);
    static final char[] DFA213_min = DFA.unpackEncodedStringToUnsignedChars(DFA213_minS);
    static final char[] DFA213_max = DFA.unpackEncodedStringToUnsignedChars(DFA213_maxS);
    static final short[] DFA213_accept = DFA.unpackEncodedString(DFA213_acceptS);
    static final short[] DFA213_special = DFA.unpackEncodedString(DFA213_specialS);
    static final short[][] DFA213_transition;

    static {
        int numStates = DFA213_transitionS.length;
        DFA213_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA213_transition[i] = DFA.unpackEncodedString(DFA213_transitionS[i]);
        }
    }

    class DFA213 extends DFA {

        public DFA213(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 213;
            this.eot = DFA213_eot;
            this.eof = DFA213_eof;
            this.min = DFA213_min;
            this.max = DFA213_max;
            this.accept = DFA213_accept;
            this.special = DFA213_special;
            this.transition = DFA213_transition;
        }
        public String getDescription() {
            return "1037:9: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA213_6 = input.LA(1);

                         
                        int index213_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA213_6==WS||(LA213_6>=NL && LA213_6<=COMMENT)) ) {s = 8;}

                        else if ( (LA213_6==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA213_6==COLON) ) {s = 2;}

                         
                        input.seek(index213_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA213_8 = input.LA(1);

                         
                        int index213_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA213_8==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA213_8==WS||(LA213_8>=NL && LA213_8<=COMMENT)) ) {s = 8;}

                        else if ( (LA213_8==COLON) ) {s = 2;}

                         
                        input.seek(index213_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA213_10 = input.LA(1);

                         
                        int index213_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA213_10==LPAREN) && (synpred12_Css3())) {s = 11;}

                        else if ( (LA213_10==WS||(LA213_10>=NL && LA213_10<=COMMENT)) ) {s = 10;}

                        else if ( (LA213_10==IDENT||LA213_10==COMMA||LA213_10==LBRACE||LA213_10==GEN||LA213_10==COLON||(LA213_10>=PLUS && LA213_10<=PIPE)||LA213_10==LESS_AND) ) {s = 2;}

                         
                        input.seek(index213_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA213_5 = input.LA(1);

                         
                        int index213_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA213_5==WS||(LA213_5>=NL && LA213_5<=COMMENT)) ) {s = 8;}

                        else if ( (LA213_5==RBRACE) && (synpred12_Css3())) {s = 9;}

                        else if ( (LA213_5==COLON) ) {s = 2;}

                         
                        input.seek(index213_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA213_7 = input.LA(1);

                         
                        int index213_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA213_7==WS||(LA213_7>=NL && LA213_7<=COMMENT)) ) {s = 10;}

                        else if ( (LA213_7==LPAREN) && (synpred12_Css3())) {s = 11;}

                        else if ( (LA213_7==IDENT||LA213_7==COMMA||LA213_7==LBRACE||LA213_7==GEN||LA213_7==COLON||(LA213_7>=PLUS && LA213_7<=PIPE)||LA213_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index213_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 213, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA216_eotS =
        "\4\uffff";
    static final String DFA216_eofS =
        "\4\uffff";
    static final String DFA216_minS =
        "\2\6\2\uffff";
    static final String DFA216_maxS =
        "\2\122\2\uffff";
    static final String DFA216_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA216_specialS =
        "\4\uffff}>";
    static final String[] DFA216_transitionS = {
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
            "\1\3\6\uffff\1\2\6\uffff\1\3\2\uffff\1\1\35\uffff\4\3\30\uffff"+
            "\2\1",
            "",
            ""
    };

    static final short[] DFA216_eot = DFA.unpackEncodedString(DFA216_eotS);
    static final short[] DFA216_eof = DFA.unpackEncodedString(DFA216_eofS);
    static final char[] DFA216_min = DFA.unpackEncodedStringToUnsignedChars(DFA216_minS);
    static final char[] DFA216_max = DFA.unpackEncodedStringToUnsignedChars(DFA216_maxS);
    static final short[] DFA216_accept = DFA.unpackEncodedString(DFA216_acceptS);
    static final short[] DFA216_special = DFA.unpackEncodedString(DFA216_specialS);
    static final short[][] DFA216_transition;

    static {
        int numStates = DFA216_transitionS.length;
        DFA216_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA216_transition[i] = DFA.unpackEncodedString(DFA216_transitionS[i]);
        }
    }

    class DFA216 extends DFA {

        public DFA216(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 216;
            this.eot = DFA216_eot;
            this.eof = DFA216_eof;
            this.min = DFA216_min;
            this.max = DFA216_max;
            this.accept = DFA216_accept;
            this.special = DFA216_special;
            this.transition = DFA216_transition;
        }
        public String getDescription() {
            return "()* loopback of 1042:9: ( ( ws )? ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA215_eotS =
        "\14\uffff";
    static final String DFA215_eofS =
        "\14\uffff";
    static final String DFA215_minS =
        "\2\6\1\uffff\2\6\2\16\1\6\1\16\1\uffff\1\6\1\uffff";
    static final String DFA215_maxS =
        "\1\70\1\122\1\uffff\2\126\4\122\1\uffff\1\122\1\uffff";
    static final String DFA215_acceptS =
        "\2\uffff\1\2\6\uffff\1\1\1\uffff\1\1";
    static final String DFA215_specialS =
        "\5\uffff\1\3\1\0\1\4\1\1\1\uffff\1\2\1\uffff}>";
    static final String[] DFA215_transitionS = {
            "\1\2\15\uffff\1\2\40\uffff\1\2\1\1\2\2",
            "\1\2\6\uffff\1\3\6\uffff\1\2\2\uffff\1\2\35\uffff\4\2\30\uffff"+
            "\2\2",
            "",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\7\5\uffff\1\5\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\1\uffff"+
            "\1\5\1\4\35\uffff\10\2\1\uffff\1\2\22\uffff\2\4\1\uffff\1\6"+
            "\2\2",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            "\1\11\5\uffff\1\2\2\uffff\1\10\71\uffff\2\10",
            "",
            "\1\2\4\uffff\1\2\1\uffff\1\2\4\uffff\1\2\1\13\1\2\2\uffff\1"+
            "\12\32\uffff\13\2\1\uffff\1\2\22\uffff\2\12",
            ""
    };

    static final short[] DFA215_eot = DFA.unpackEncodedString(DFA215_eotS);
    static final short[] DFA215_eof = DFA.unpackEncodedString(DFA215_eofS);
    static final char[] DFA215_min = DFA.unpackEncodedStringToUnsignedChars(DFA215_minS);
    static final char[] DFA215_max = DFA.unpackEncodedStringToUnsignedChars(DFA215_maxS);
    static final short[] DFA215_accept = DFA.unpackEncodedString(DFA215_acceptS);
    static final short[] DFA215_special = DFA.unpackEncodedString(DFA215_specialS);
    static final short[][] DFA215_transition;

    static {
        int numStates = DFA215_transitionS.length;
        DFA215_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA215_transition[i] = DFA.unpackEncodedString(DFA215_transitionS[i]);
        }
    }

    class DFA215 extends DFA {

        public DFA215(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 215;
            this.eot = DFA215_eot;
            this.eof = DFA215_eof;
            this.min = DFA215_min;
            this.max = DFA215_max;
            this.accept = DFA215_accept;
            this.special = DFA215_special;
            this.transition = DFA215_transition;
        }
        public String getDescription() {
            return "1044:13: ( ( HASH_SYMBOL LBRACE )=> scss_interpolation_expression_var | ( IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA215_6 = input.LA(1);

                         
                        int index215_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA215_6==WS||(LA215_6>=NL && LA215_6<=COMMENT)) ) {s = 8;}

                        else if ( (LA215_6==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA215_6==COLON) ) {s = 2;}

                         
                        input.seek(index215_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA215_8 = input.LA(1);

                         
                        int index215_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA215_8==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA215_8==WS||(LA215_8>=NL && LA215_8<=COMMENT)) ) {s = 8;}

                        else if ( (LA215_8==COLON) ) {s = 2;}

                         
                        input.seek(index215_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA215_10 = input.LA(1);

                         
                        int index215_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA215_10==LPAREN) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA215_10==WS||(LA215_10>=NL && LA215_10<=COMMENT)) ) {s = 10;}

                        else if ( (LA215_10==IDENT||LA215_10==COMMA||LA215_10==LBRACE||LA215_10==GEN||LA215_10==COLON||(LA215_10>=PLUS && LA215_10<=PIPE)||LA215_10==LESS_AND) ) {s = 2;}

                         
                        input.seek(index215_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA215_5 = input.LA(1);

                         
                        int index215_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA215_5==WS||(LA215_5>=NL && LA215_5<=COMMENT)) ) {s = 8;}

                        else if ( (LA215_5==RBRACE) && (synpred13_Css3())) {s = 9;}

                        else if ( (LA215_5==COLON) ) {s = 2;}

                         
                        input.seek(index215_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA215_7 = input.LA(1);

                         
                        int index215_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA215_7==WS||(LA215_7>=NL && LA215_7<=COMMENT)) ) {s = 10;}

                        else if ( (LA215_7==LPAREN) && (synpred13_Css3())) {s = 11;}

                        else if ( (LA215_7==IDENT||LA215_7==COMMA||LA215_7==LBRACE||LA215_7==GEN||LA215_7==COLON||(LA215_7>=PLUS && LA215_7<=PIPE)||LA215_7==LESS_AND) ) {s = 2;}

                         
                        input.seek(index215_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 215, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA229_eotS =
        "\11\uffff";
    static final String DFA229_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA229_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA229_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA229_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA229_specialS =
        "\11\uffff}>";
    static final String[] DFA229_transitionS = {
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

    static final short[] DFA229_eot = DFA.unpackEncodedString(DFA229_eotS);
    static final short[] DFA229_eof = DFA.unpackEncodedString(DFA229_eofS);
    static final char[] DFA229_min = DFA.unpackEncodedStringToUnsignedChars(DFA229_minS);
    static final char[] DFA229_max = DFA.unpackEncodedStringToUnsignedChars(DFA229_maxS);
    static final short[] DFA229_accept = DFA.unpackEncodedString(DFA229_acceptS);
    static final short[] DFA229_special = DFA.unpackEncodedString(DFA229_specialS);
    static final short[][] DFA229_transition;

    static {
        int numStates = DFA229_transitionS.length;
        DFA229_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA229_transition[i] = DFA.unpackEncodedString(DFA229_transitionS[i]);
        }
    }

    class DFA229 extends DFA {

        public DFA229(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 229;
            this.eot = DFA229_eot;
            this.eof = DFA229_eof;
            this.min = DFA229_min;
            this.max = DFA229_max;
            this.accept = DFA229_accept;
            this.special = DFA229_special;
            this.transition = DFA229_transition;
        }
        public String getDescription() {
            return "565:3: synpred1_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ SEMI | scss_declaration_interpolation_expression COLON );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA232_eotS =
        "\11\uffff";
    static final String DFA232_eofS =
        "\5\uffff\1\4\3\uffff";
    static final String DFA232_minS =
        "\3\4\2\uffff\4\4";
    static final String DFA232_maxS =
        "\3\u0081\2\uffff\4\u0081";
    static final String DFA232_acceptS =
        "\3\uffff\1\1\1\2\4\uffff";
    static final String DFA232_specialS =
        "\11\uffff}>";
    static final String[] DFA232_transitionS = {
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
            return "567:3: synpred2_Css3 : ( (~ ( LBRACE | SEMI | RBRACE | COLON ) )+ COLON (~ ( SEMI | LBRACE | RBRACE ) )+ LBRACE | scss_declaration_interpolation_expression COLON );";
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
    public static final BitSet FOLLOW_MEDIA_SYM_in_media436 = new BitSet(new long[]{0x00000000008F2040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_media438 = new BitSet(new long[]{0x00000000000F2040L});
    public static final BitSet FOLLOW_mediaQueryList_in_media441 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media451 = new BitSet(new long[]{0x5FE0000151D44040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_media453 = new BitSet(new long[]{0x5FE0000151544040L,0x0000000000200000L});
    public static final BitSet FOLLOW_rule_in_media472 = new BitSet(new long[]{0x5FE0000151D44040L,0x0000000000260000L});
    public static final BitSet FOLLOW_page_in_media476 = new BitSet(new long[]{0x5FE0000151D44040L,0x0000000000260000L});
    public static final BitSet FOLLOW_fontFace_in_media480 = new BitSet(new long[]{0x5FE0000151D44040L,0x0000000000260000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media484 = new BitSet(new long[]{0x5FE0000151D44040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_media488 = new BitSet(new long[]{0x5FE0000151544040L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACE_in_media502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList518 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList522 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList524 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList527 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery546 = new BitSet(new long[]{0x0000000000870040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery548 = new BitSet(new long[]{0x0000000000070040L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery555 = new BitSet(new long[]{0x0000000000808002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery557 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery562 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery564 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery567 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery575 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery579 = new BitSet(new long[]{0x00000000008F0040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery581 = new BitSet(new long[]{0x00000000000F0040L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery584 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression639 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression641 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression644 = new BitSet(new long[]{0x0000000000B00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression646 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression651 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_mediaExpression653 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_mediaExpression656 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression661 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body695 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_body697 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_bodyItem722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_call_in_bodyItem810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule877 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule879 = new BitSet(new long[]{0x00000000008020C0L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule884 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule886 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule901 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule913 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document939 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document941 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document946 = new BitSet(new long[]{0x0000000000802800L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document948 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_COMMA_in_moz_document954 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document956 = new BitSet(new long[]{0x000000000E800100L,0x0000000000060000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document959 = new BitSet(new long[]{0x0000000000802800L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_moz_document961 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document968 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_moz_document970 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_body_in_moz_document975 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes1021 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1023 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes1026 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1028 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes1033 = new BitSet(new long[]{0x0000000020804040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1035 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes1042 = new BitSet(new long[]{0x0000000020804040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes1044 = new BitSet(new long[]{0x0000000020004040L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes1051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock1064 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1066 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock1071 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock1074 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1077 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1081 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1099 = new BitSet(new long[]{0x0000000000800802L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1111 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1114 = new BitSet(new long[]{0x0000000020800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1116 = new BitSet(new long[]{0x0000000020000040L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1119 = new BitSet(new long[]{0x0000000000800802L,0x0000000000060000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1148 = new BitSet(new long[]{0x0000000000902040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1150 = new BitSet(new long[]{0x0000000000102040L});
    public static final BitSet FOLLOW_IDENT_in_page1155 = new BitSet(new long[]{0x0000000000902000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1157 = new BitSet(new long[]{0x0000000000102000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1164 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1166 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_page1179 = new BitSet(new long[]{0x09E1FFFE00C45060L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1181 = new BitSet(new long[]{0x09E1FFFE00445060L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1236 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1238 = new BitSet(new long[]{0x0000000000804020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1240 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_SEMI_in_page1246 = new BitSet(new long[]{0x09E1FFFE00C45060L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_page1248 = new BitSet(new long[]{0x09E1FFFE00445060L,0x0000000000100000L});
    public static final BitSet FOLLOW_declaration_in_page1252 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_margin_in_page1254 = new BitSet(new long[]{0x0000000000804020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_page1256 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_RBRACE_in_page1271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1292 = new BitSet(new long[]{0x0000000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1294 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1297 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1299 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1310 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1312 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1315 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1319 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1350 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fontFace1352 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1363 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_fontFace1365 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1368 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1372 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1397 = new BitSet(new long[]{0x0000000000802000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_margin1399 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1402 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_margin1404 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1407 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_margin1409 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1640 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1692 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1703 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1705 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1714 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_combinator1716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property1777 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_property1781 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_property1787 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_property1790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_mixin_declaration_in_rule1834 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1867 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1890 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_rule1892 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule1895 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_rule1909 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1919 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations2053 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_declarations2055 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2057 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_scss_nested_properties_in_declarations2101 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2103 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_rule_in_declarations2140 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2142 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_cp_mixin_call_in_declarations2181 = new BitSet(new long[]{0x5FE00001D1D41042L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_declarations2183 = new BitSet(new long[]{0x5FE00001D1541042L,0x0000000000700000L});
    public static final BitSet FOLLOW_declaration_in_declarations2227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_selector_interpolation_expression_in_selectorsGroup2288 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2305 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2308 = new BitSet(new long[]{0x5FE0000000940040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2310 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2313 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2340 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_combinator_in_selector2343 = new BitSet(new long[]{0x5FE0000000140040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2345 = new BitSet(new long[]{0x5FFC000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2378 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2385 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2387 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2406 = new BitSet(new long[]{0x5FE0000000940042L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2408 = new BitSet(new long[]{0x5FE0000000140042L,0x0000000000200000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2520 = new BitSet(new long[]{0x5800000000040040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2526 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2546 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2550 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_cssId2655 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2685 = new BitSet(new long[]{0x0000000000040040L});
    public static final BitSet FOLLOW_set_in_cssClass2687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2759 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2766 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2769 = new BitSet(new long[]{0x1800000000800040L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2780 = new BitSet(new long[]{0x8000000000800000L,0x000000000006003FL});
    public static final BitSet FOLLOW_ws_in_slAttribute2782 = new BitSet(new long[]{0x8000000000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_set_in_slAttribute2824 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_slAttribute3004 = new BitSet(new long[]{0x00000000008000C0L,0x0000000000060000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute3023 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060020L});
    public static final BitSet FOLLOW_ws_in_slAttribute3041 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3086 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3160 = new BitSet(new long[]{0x0000000000060040L});
    public static final BitSet FOLLOW_set_in_pseudo3224 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3281 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3284 = new BitSet(new long[]{0x08A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_pseudo3286 = new BitSet(new long[]{0x08A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_pseudo3291 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_STAR_in_pseudo3295 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3379 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_pseudo3381 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3384 = new BitSet(new long[]{0x5FE0000000B40040L,0x0000000000260000L});
    public static final BitSet FOLLOW_ws_in_pseudo3386 = new BitSet(new long[]{0x5FE0000000340040L,0x0000000000200000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3389 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3436 = new BitSet(new long[]{0x09E0000000441040L,0x0000000000100000L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_declaration3482 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_property_in_declaration3503 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_declaration3516 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_declaration3518 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3521 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_prio_in_declaration3524 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_declaration3526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue3609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3647 = new BitSet(new long[]{0xF7FDFFFFFFBFDFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_syncTo_SEMI3794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3870 = new BitSet(new long[]{0x00A6000020C419C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_operator_in_expression3875 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_expression3877 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_expression3882 = new BitSet(new long[]{0x00A6000020C419C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_unaryOperator_in_term3907 = new BitSet(new long[]{0x0080000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_term3909 = new BitSet(new long[]{0x00800000204411C0L,0x000000000011FF80L});
    public static final BitSet FOLLOW_set_in_term3933 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_term4133 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_term4141 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_term4149 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_term4157 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_term4165 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_term4173 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_term4183 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_term4195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4211 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_function4213 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_function4218 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4220 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_expression_in_function4230 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_fnAttribute_in_function4248 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_COMMA_in_function4251 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_function4253 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttribute_in_function4256 = new BitSet(new long[]{0x0000000000200800L});
    public static final BitSet FOLLOW_RPAREN_in_function4277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4325 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_functionName4327 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4331 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4334 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_functionName4336 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4359 = new BitSet(new long[]{0x8000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4361 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4364 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4366 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4384 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4387 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4389 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4442 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4490 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4492 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4495 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4497 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4500 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4529 = new BitSet(new long[]{0x0000000000900000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4531 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4534 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4536 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4539 = new BitSet(new long[]{0x0000000000000020L,0x0000000000080000L});
    public static final BitSet FOLLOW_SASS_DEFAULT_in_cp_variable_declaration4542 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4544 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable4582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable4614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression4638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4658 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp4672 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4674 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4677 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp4690 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4692 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4695 = new BitSet(new long[]{0x0024000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4728 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp4741 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4743 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4746 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp4760 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4762 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4765 = new BitSet(new long[]{0x0802000000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4798 = new BitSet(new long[]{0x00A4000020C411C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4805 = new BitSet(new long[]{0x00A4000020C411C2L,0x000000000017FF80L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp4819 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4821 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp4824 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp4826 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term4866 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_STRING_in_cp_term5066 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term5074 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_GEN_in_cp_term5082 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_URI_in_cp_term5090 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term5098 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_function_in_cp_term5106 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term5114 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_term5126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_declaration5157 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5159 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5161 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5164 = new BitSet(new long[]{0x0000000000601000L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5166 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5169 = new BitSet(new long[]{0x0000000000800002L,0x0000000002060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5171 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_cp_mixin_declaration5175 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_MIXIN_in_cp_mixin_declaration5194 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5196 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_declaration5198 = new BitSet(new long[]{0x0000000000880002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5200 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_declaration5204 = new BitSet(new long[]{0x0000000000601000L,0x0000000001900000L});
    public static final BitSet FOLLOW_less_args_list_in_cp_mixin_declaration5206 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_declaration5209 = new BitSet(new long[]{0x0000000000800002L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_declaration5211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cp_mixin_call5253 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5255 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_SASS_INCLUDE_in_cp_mixin_call5277 = new BitSet(new long[]{0x0000000000800000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5279 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_cp_mixin_name_in_cp_mixin_call5281 = new BitSet(new long[]{0x0000000000880020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5294 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_cp_mixin_call5297 = new BitSet(new long[]{0x00A4000020E411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_mixin_call_args_in_cp_mixin_call5299 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_cp_mixin_call5302 = new BitSet(new long[]{0x0000000000800020L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call5306 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SEMI_in_cp_mixin_call5309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cp_mixin_name5338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5374 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_cp_mixin_call_args5378 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_cp_mixin_call_args5386 = new BitSet(new long[]{0x00A4000020C411C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_term_in_cp_mixin_call_args5389 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5431 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5435 = new BitSet(new long[]{0x0000000000C01000L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5445 = new BitSet(new long[]{0x0000000000401000L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5448 = new BitSet(new long[]{0x0000000000000822L});
    public static final BitSet FOLLOW_set_in_less_args_list5454 = new BitSet(new long[]{0x0000000000800000L,0x0000000001860000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5464 = new BitSet(new long[]{0x0000000000000000L,0x0000000001800000L});
    public static final BitSet FOLLOW_set_in_less_args_list5467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg5521 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5525 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_arg5527 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg5530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded5556 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5558 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5561 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded5565 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5573 = new BitSet(new long[]{0x00000000008A0000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5576 = new BitSet(new long[]{0x0000000000008802L});
    public static final BitSet FOLLOW_NOT_in_less_condition5606 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5608 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition5617 = new BitSet(new long[]{0x0000000000C01040L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_condition5619 = new BitSet(new long[]{0x0000000000401040L,0x0000000000100000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition5645 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5647 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition5678 = new BitSet(new long[]{0x8008000000A00000L,0x000000001C060000L});
    public static final BitSet FOLLOW_ws_in_less_condition5681 = new BitSet(new long[]{0x8008000000800000L,0x000000001C060000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition5684 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_less_condition5686 = new BitSet(new long[]{0x00A4000020CC11C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition5689 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition5718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition5744 = new BitSet(new long[]{0x0000000000880000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5746 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition5749 = new BitSet(new long[]{0x0000000000C01000L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5751 = new BitSet(new long[]{0x0000000000401000L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition5754 = new BitSet(new long[]{0x0000000000A00000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5756 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition5759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name5781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression5880 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression5908 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_selector_interpolation_expression5965 = new BitSet(new long[]{0x01E0000000100040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_selector_interpolation_expression6004 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_selector_interpolation_expression6040 = new BitSet(new long[]{0x01E0000000900042L,0x0000000000060000L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6138 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6166 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_declaration_interpolation_expression6219 = new BitSet(new long[]{0x01E0000000000040L});
    public static final BitSet FOLLOW_scss_interpolation_expression_var_in_scss_declaration_interpolation_expression6258 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_scss_declaration_interpolation_expression6294 = new BitSet(new long[]{0x01E0000000800042L,0x0000000000060000L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_scss_interpolation_expression_var6367 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_interpolation_expression_var6369 = new BitSet(new long[]{0x0000000000C01040L,0x0000000000160000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var6371 = new BitSet(new long[]{0x0000000000401040L,0x0000000000100000L});
    public static final BitSet FOLLOW_cp_variable_in_scss_interpolation_expression_var6376 = new BitSet(new long[]{0x0000000000804000L,0x0000000000060000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_scss_interpolation_expression_var6380 = new BitSet(new long[]{0x0000000000804000L,0x0000000000060000L});
    public static final BitSet FOLLOW_ws_in_scss_interpolation_expression_var6384 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_interpolation_expression_var6387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_scss_nested_properties6431 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_scss_nested_properties6433 = new BitSet(new long[]{0x00A4000020CC31C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties6435 = new BitSet(new long[]{0x00A4000020CC31C0L,0x000000000017FF80L});
    public static final BitSet FOLLOW_propertyValue_in_scss_nested_properties6438 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_scss_nested_properties6441 = new BitSet(new long[]{0x5FE00001D1D45040L,0x0000000000760000L});
    public static final BitSet FOLLOW_ws_in_scss_nested_properties6443 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_syncToFollow_in_scss_nested_properties6446 = new BitSet(new long[]{0x5FE00001D1545040L,0x0000000000700000L});
    public static final BitSet FOLLOW_declarations_in_scss_nested_properties6448 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_scss_nested_properties6450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css32019 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred1_Css32031 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred1_Css32033 = new BitSet(new long[]{0xFFFFFFFFFFFF9FF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_SEMI_in_synpred1_Css32043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred1_Css32047 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred1_Css32049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred2_Css32067 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Css32079 = new BitSet(new long[]{0xFFFFFFFFFFFF9FD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_synpred2_Css32081 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred2_Css32091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scss_declaration_interpolation_expression_in_synpred2_Css32095 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Css32097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css32127 = new BitSet(new long[]{0xFFFFFFFFFFFFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_LBRACE_in_synpred3_Css32137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred4_Css32218 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_RBRACE_in_synpred4_Css32224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred5_Css32270 = new BitSet(new long[]{0xFFFFFFFFFFFFDFF0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred5_Css32282 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Css32284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred6_Css32382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred7_Css32403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_Css32508 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred8_Css32517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred9_Css33457 = new BitSet(new long[]{0xFFFFFFFFFFEFBFD0L,0xFFFFFFFFFFFFFFFFL,0x0000000000000003L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred9_Css33475 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred9_Css33477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred10_Css33563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred11_Css34802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred12_Css35875 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Css35877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred13_Css35999 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred13_Css36001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred14_Css36133 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred14_Css36135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_SYMBOL_in_synpred15_Css36253 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_synpred15_Css36255 = new BitSet(new long[]{0x0000000000000002L});

}