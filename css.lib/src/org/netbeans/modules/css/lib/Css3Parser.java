// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-02-22 12:52:55

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "LESS_AND", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "SASS_VAR", "LESS_WHEN", "GREATER_OR_EQ", "LESS", "LESS_OR_EQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'", "'...'", "'@rest...'"
    };
    public static final int EOF=-1;
    public static final int T__123=123;
    public static final int T__124=124;
    public static final int T__125=125;
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
    public static final int SASS_VAR=82;
    public static final int LESS_WHEN=83;
    public static final int GREATER_OR_EQ=84;
    public static final int LESS=85;
    public static final int LESS_OR_EQ=86;
    public static final int HEXCHAR=87;
    public static final int NONASCII=88;
    public static final int UNICODE=89;
    public static final int ESCAPE=90;
    public static final int NMSTART=91;
    public static final int NMCHAR=92;
    public static final int URL=93;
    public static final int A=94;
    public static final int B=95;
    public static final int C=96;
    public static final int D=97;
    public static final int E=98;
    public static final int F=99;
    public static final int G=100;
    public static final int H=101;
    public static final int I=102;
    public static final int J=103;
    public static final int K=104;
    public static final int L=105;
    public static final int M=106;
    public static final int N=107;
    public static final int O=108;
    public static final int P=109;
    public static final int Q=110;
    public static final int R=111;
    public static final int S=112;
    public static final int T=113;
    public static final int U=114;
    public static final int V=115;
    public static final int W=116;
    public static final int X=117;
    public static final int Y=118;
    public static final int Z=119;
    public static final int CDO=120;
    public static final int CDC=121;
    public static final int INVALID=122;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "mediaType", "importItem", "lastDeclarationPredicate", 
        "cssClass", "simpleSelectorSequence", "cp_variable", "operator", 
        "atRuleId", "term", "pseudo", "less_condition", "cp_additionExp", 
        "selectorsGroup", "webkitKeyframesBlock", "slAttributeValue", "synpred5_Css3", 
        "cp_variable_declaration", "elementName", "webkitKeyframeSelectors", 
        "imports", "margin_sym", "media", "mediaQueryList", "mediaFeature", 
        "styleSheet", "charSet", "selector", "declarations", "cp_term", 
        "resourceIdentifier", "less_function_in_condition", "moz_document_function", 
        "margin", "pseudoPage", "syncToDeclarationsRule", "synpred6_Css3", 
        "cp_expression", "body", "rulePredicate", "slAttribute", "less_mixin_call", 
        "typeSelector", "less_arg", "ws", "cp_atomExp", "namespace", "less_args_list", 
        "expressionPredicate", "property", "bodyItem", "counterStyle", "declaration", 
        "mediaQuery", "mediaExpression", "less_mixin_guarded", "mediaQueryOperator", 
        "expression", "fnAttribute", "function", "propertyValue", "cp_multiplyExp", 
        "namespacePrefixName", "less_mixin_call_args", "charSetValue", "less_condition_operator", 
        "generic_at_rule", "declarationPredicate", "syncTo_RBRACE", "unaryOperator", 
        "synpred2_Css3", "synpred1_Css3", "rule", "synpred7_Css3", "namespaces", 
        "fnAttributeValue", "fnAttributeName", "synpred4_Css3", "page", 
        "fontFace", "synpred8_Css3", "hexColor", "webkitKeyframes", "combinator", 
        "esPred", "synpred3_Css3", "slAttributeName", "less_fn_name", "functionName", 
        "prio", "cssId", "moz_document", "vendorAtRule", "less_mixin_declaration", 
        "syncToFollow", "namespacePrefix", "elementSubsequent"
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
            false, false, true, false, false, false, false, false, false, 
            false, false, true, false, true, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, true, false, false, false, false, 
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

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=AT_IDENT)||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==LESS_AND||LA6_0==SASS_VAR||LA6_0==123) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:1: namespace : NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(332, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:3: ( NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:5: NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';'
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

                if ( (LA23_0==IDENT||(LA23_0>=GEN && LA23_0<=AT_IDENT)||LA23_0==MOZ_DOCUMENT_SYM||LA23_0==WEBKIT_KEYFRAMES_SYM||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==LESS_AND||LA23_0==123) ) {
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
            	    case HASH:
            	    case DOT:
            	    case LBRACKET:
            	    case DCOLON:
            	    case STAR:
            	    case PIPE:
            	    case LESS_AND:
            	    case 123:
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

            if ( (LA26_0==IDENT||(LA26_0>=ONLY && LA26_0<=GEN)||LA26_0==LPAREN) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:1: mediaExpression : '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:5: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:7: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )?
            {
            dbg.location(385,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression578); if (state.failed) return ;
            dbg.location(385,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:11: ws
                    {
                    dbg.location(385,11);
                    pushFollow(FOLLOW_ws_in_mediaExpression580);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(385,15);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression583);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(385,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:28: ws
                    {
                    dbg.location(385,28);
                    pushFollow(FOLLOW_ws_in_mediaExpression585);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(385,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:32: ( ':' ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:34: ':' ( ws )? expression
                    {
                    dbg.location(385,34);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression590); if (state.failed) return ;
                    dbg.location(385,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:38: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:38: ws
                            {
                            dbg.location(385,38);
                            pushFollow(FOLLOW_ws_in_mediaExpression592);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(385,42);
                    pushFollow(FOLLOW_expression_in_mediaExpression595);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(385,56);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression600); if (state.failed) return ;
            dbg.location(385,60);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:60: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:60: ws
                    {
                    dbg.location(385,60);
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

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=AT_IDENT)||LA41_0==MOZ_DOCUMENT_SYM||LA41_0==WEBKIT_KEYFRAMES_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==LESS_AND||LA41_0==SASS_VAR||LA41_0==123) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration )
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

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(405, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:1: ( moz_document | webkitKeyframes | generic_at_rule )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:3: moz_document
                    {
                    dbg.location(414,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule758);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:18: webkitKeyframes
                    {
                    dbg.location(414,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule762);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:36: generic_at_rule
                    {
                    dbg.location(414,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule766);
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
        dbg.location(414, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(416, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(417,2);
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
        dbg.location(419, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(422,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule802); if (state.failed) return ;
            dbg.location(422,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: WS
            	    {
            	    dbg.location(422,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule804); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(422,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:20: ( atRuleId ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:22: atRuleId ( WS )*
                    {
                    dbg.location(422,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule809);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(422,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:31: WS
                    	    {
                    	    dbg.location(422,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule811); if (state.failed) return ;

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

            dbg.location(423,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule826); if (state.failed) return ;
            dbg.location(424,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule838);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(425,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule848); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(426, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(427, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(429,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document864); if (state.failed) return ;
            dbg.location(429,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:19: ws
                    {
                    dbg.location(429,19);
                    pushFollow(FOLLOW_ws_in_moz_document866);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(429,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:25: moz_document_function ( ws )?
            {
            dbg.location(429,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document871);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(429,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:47: ws
                    {
                    dbg.location(429,47);
                    pushFollow(FOLLOW_ws_in_moz_document873);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}


            }

            dbg.location(429,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(429,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document879); if (state.failed) return ;
            	    dbg.location(429,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:60: ws
            	            {
            	            dbg.location(429,60);
            	            pushFollow(FOLLOW_ws_in_moz_document881);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(429,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document884);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(429,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:86: ws
            	            {
            	            dbg.location(429,86);
            	            pushFollow(FOLLOW_ws_in_moz_document886);
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

            dbg.location(430,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document893); if (state.failed) return ;
            dbg.location(430,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:9: ws
                    {
                    dbg.location(430,9);
                    pushFollow(FOLLOW_ws_in_moz_document895);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(431,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:3: ( body )?
            int alt53=2;
            try { dbg.enterSubRule(53);
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            int LA53_0 = input.LA(1);

            if ( (LA53_0==IDENT||LA53_0==MEDIA_SYM||(LA53_0>=GEN && LA53_0<=AT_IDENT)||LA53_0==MOZ_DOCUMENT_SYM||LA53_0==WEBKIT_KEYFRAMES_SYM||(LA53_0>=PAGE_SYM && LA53_0<=FONT_FACE_SYM)||LA53_0==COLON||(LA53_0>=HASH && LA53_0<=PIPE)||LA53_0==LESS_AND||LA53_0==SASS_VAR||LA53_0==123) ) {
                alt53=1;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:3: body
                    {
                    dbg.location(431,3);
                    pushFollow(FOLLOW_body_in_moz_document900);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(432,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document905); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(433, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(435, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(436,2);
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
        dbg.location(438, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(443,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes946); if (state.failed) return ;
            dbg.location(443,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:23: ws
                    {
                    dbg.location(443,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes948);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(443,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes951);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(443,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:36: ws
                    {
                    dbg.location(443,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes953);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(444,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes958); if (state.failed) return ;
            dbg.location(444,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:9: ws
                    {
                    dbg.location(444,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes960);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(445,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:3: ( webkitKeyframesBlock ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(445,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes967);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(445,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:26: ws
            	            {
            	            dbg.location(445,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes969);
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

            dbg.location(446,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes976); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(451,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock989);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(451,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:26: ws
                    {
                    dbg.location(451,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock991);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(453,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock996); if (state.failed) return ;
            dbg.location(453,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:453:10: ws
                    {
                    dbg.location(453,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock999);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(453,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1002);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(454,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1006);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(455,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1009); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframesBlock");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframesBlock"


    // $ANTLR start "webkitKeyframeSelectors"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:458:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(458, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(460,2);
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

            dbg.location(460,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(460,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:27: ws
            	            {
            	            dbg.location(460,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1036);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(460,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1039); if (state.failed) return ;
            	    dbg.location(460,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:37: ws
            	            {
            	            dbg.location(460,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1041);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(62);}

            	    dbg.location(460,41);
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
        dbg.location(461, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(463, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(464,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1073); if (state.failed) return ;
            dbg.location(464,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:16: ws
                    {
                    dbg.location(464,16);
                    pushFollow(FOLLOW_ws_in_page1075);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(464,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:20: ( IDENT ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:22: IDENT ( ws )?
                    {
                    dbg.location(464,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1080); if (state.failed) return ;
                    dbg.location(464,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:28: ws
                            {
                            dbg.location(464,28);
                            pushFollow(FOLLOW_ws_in_page1082);
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

            dbg.location(464,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:35: ( pseudoPage ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:36: pseudoPage ( ws )?
                    {
                    dbg.location(464,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1089);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(464,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:47: ws
                            {
                            dbg.location(464,47);
                            pushFollow(FOLLOW_ws_in_page1091);
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

            dbg.location(465,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1104); if (state.failed) return ;
            dbg.location(465,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:16: ws
                    {
                    dbg.location(465,16);
                    pushFollow(FOLLOW_ws_in_page1106);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(469,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:13: ( declaration | margin ( ws )? )?
            int alt71=3;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT||LA71_0==MEDIA_SYM||(LA71_0>=GEN && LA71_0<=AT_IDENT)||LA71_0==STAR||LA71_0==SASS_VAR) ) {
                alt71=1;
            }
            else if ( ((LA71_0>=TOPLEFTCORNER_SYM && LA71_0<=RIGHTBOTTOM_SYM)) ) {
                alt71=2;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:14: declaration
                    {
                    dbg.location(469,14);
                    pushFollow(FOLLOW_declaration_in_page1161);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:26: margin ( ws )?
                    {
                    dbg.location(469,26);
                    pushFollow(FOLLOW_margin_in_page1163);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(469,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:33: ws
                            {
                            dbg.location(469,33);
                            pushFollow(FOLLOW_ws_in_page1165);
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

            dbg.location(469,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(469,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1171); if (state.failed) return ;
            	    dbg.location(469,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:45: ws
            	            {
            	            dbg.location(469,45);
            	            pushFollow(FOLLOW_ws_in_page1173);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(469,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:49: ( declaration | margin ( ws )? )?
            	    int alt74=3;
            	    try { dbg.enterSubRule(74);
            	    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            	    int LA74_0 = input.LA(1);

            	    if ( (LA74_0==IDENT||LA74_0==MEDIA_SYM||(LA74_0>=GEN && LA74_0<=AT_IDENT)||LA74_0==STAR||LA74_0==SASS_VAR) ) {
            	        alt74=1;
            	    }
            	    else if ( ((LA74_0>=TOPLEFTCORNER_SYM && LA74_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt74=2;
            	    }
            	    } finally {dbg.exitDecision(74);}

            	    switch (alt74) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:50: declaration
            	            {
            	            dbg.location(469,50);
            	            pushFollow(FOLLOW_declaration_in_page1177);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:62: margin ( ws )?
            	            {
            	            dbg.location(469,62);
            	            pushFollow(FOLLOW_margin_in_page1179);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(469,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:69: ws
            	                    {
            	                    dbg.location(469,69);
            	                    pushFollow(FOLLOW_ws_in_page1181);
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

            dbg.location(470,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1196); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(473, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(474,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1217); if (state.failed) return ;
            dbg.location(474,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:25: ws
                    {
                    dbg.location(474,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(474,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1222); if (state.failed) return ;
            dbg.location(474,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:35: ws
                    {
                    dbg.location(474,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1224);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(475,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1235); if (state.failed) return ;
            dbg.location(475,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:16: ws
                    {
                    dbg.location(475,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1237);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(475,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1240);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(476,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1244);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(477,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1254); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(478, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(480, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(481,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1275); if (state.failed) return ;
            dbg.location(481,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:21: ws
                    {
                    dbg.location(481,21);
                    pushFollow(FOLLOW_ws_in_fontFace1277);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(482,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1288); if (state.failed) return ;
            dbg.location(482,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:16: ws
                    {
                    dbg.location(482,16);
                    pushFollow(FOLLOW_ws_in_fontFace1290);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(482,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1293);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(483,3);
            pushFollow(FOLLOW_declarations_in_fontFace1297);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(484,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1307); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(485, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(487, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(488,4);
            pushFollow(FOLLOW_margin_sym_in_margin1322);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(488,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:15: ws
                    {
                    dbg.location(488,15);
                    pushFollow(FOLLOW_ws_in_margin1324);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(488,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1327); if (state.failed) return ;
            dbg.location(488,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:26: ws
                    {
                    dbg.location(488,26);
                    pushFollow(FOLLOW_ws_in_margin1329);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(488,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1332);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(488,53);
            pushFollow(FOLLOW_declarations_in_margin1334);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(488,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1336); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(489, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(491, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(492,2);
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
        dbg.location(509, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(511, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:7: COLON IDENT
            {
            dbg.location(512,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1565); if (state.failed) return ;
            dbg.location(512,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1567); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(513, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(515, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(516,5);
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
        dbg.location(518, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(520, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
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
            case 123:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:7: PLUS ( ws )?
                    {
                    dbg.location(521,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1617); if (state.failed) return ;
                    dbg.location(521,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:12: ws
                            {
                            dbg.location(521,12);
                            pushFollow(FOLLOW_ws_in_combinator1619);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:7: GREATER ( ws )?
                    {
                    dbg.location(522,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1628); if (state.failed) return ;
                    dbg.location(522,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:15: ws
                            {
                            dbg.location(522,15);
                            pushFollow(FOLLOW_ws_in_combinator1630);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:7: TILDE ( ws )?
                    {
                    dbg.location(523,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1639); if (state.failed) return ;
                    dbg.location(523,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:13: ws
                            {
                            dbg.location(523,13);
                            pushFollow(FOLLOW_ws_in_combinator1641);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:5: 
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
        dbg.location(525, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(527, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(528,5);
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
        dbg.location(530, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:532:1: property : ( IDENT | GEN | {...}? cp_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(532, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:5: ( ( IDENT | GEN | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:7: ( IDENT | GEN | {...}? cp_variable ) ( ws )?
            {
            dbg.location(533,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:7: ( IDENT | GEN | {...}? cp_variable )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:8: IDENT
                    {
                    dbg.location(533,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property1702); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:16: GEN
                    {
                    dbg.location(533,16);
                    match(input,GEN,FOLLOW_GEN_in_property1706); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:22: {...}? cp_variable
                    {
                    dbg.location(533,22);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isCssPreprocessorSource()");
                    }
                    dbg.location(533,51);
                    pushFollow(FOLLOW_cp_variable_in_property1712);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(533,64);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:64: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:64: ws
                    {
                    dbg.location(533,64);
                    pushFollow(FOLLOW_ws_in_property1715);
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
        dbg.location(534, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:1: rule : ( ({...}? less_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(536, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:5: ( ( ({...}? less_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:9: ( ({...}? less_mixin_declaration ) | ( selectorsGroup ) ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(537,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:9: ( ({...}? less_mixin_declaration ) | ( selectorsGroup ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:13: ({...}? less_mixin_declaration )
                    {
                    dbg.location(538,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:13: ({...}? less_mixin_declaration )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:15: {...}? less_mixin_declaration
                    {
                    dbg.location(538,15);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isCssPreprocessorSource()");
                    }
                    dbg.location(538,44);
                    pushFollow(FOLLOW_less_mixin_declaration_in_rule1759);
                    less_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:13: ( selectorsGroup )
                    {
                    dbg.location(540,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:13: ( selectorsGroup )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:15: selectorsGroup
                    {
                    dbg.location(540,15);
                    pushFollow(FOLLOW_selectorsGroup_in_rule1792);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(543,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1815); if (state.failed) return ;
            dbg.location(543,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:16: ws
                    {
                    dbg.location(543,16);
                    pushFollow(FOLLOW_ws_in_rule1817);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(543,20);
            pushFollow(FOLLOW_syncToFollow_in_rule1820);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(544,13);
            pushFollow(FOLLOW_declarations_in_rule1834);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(545,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1844); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(546, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:1: declarations : ( ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )? | {...}? less_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(554, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:5: ( ( ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )? | {...}? less_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:13: ( ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )? | {...}? less_mixin_call ( ws )? )* ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            {
            dbg.location(556,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:13: ( ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )? | {...}? less_mixin_call ( ws )? )*
            try { dbg.enterSubRule(94);

            loop94:
            do {
                int alt94=4;
                try { dbg.enterDecision(94, decisionCanBacktrack[94]);

                try {
                    isCyclicDecision = true;
                    alt94 = dfa94.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(94);}

                switch (alt94) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )?
            	    {
            	    dbg.location(557,50);
            	    pushFollow(FOLLOW_rule_in_declarations1920);
            	    rule();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(557,55);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:55: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:55: ws
            	            {
            	            dbg.location(557,55);
            	            pushFollow(FOLLOW_ws_in_declarations1922);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:3: ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )?
            	    {
            	    dbg.location(559,34);
            	    pushFollow(FOLLOW_declaration_in_declarations1945);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(559,46);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1947); if (state.failed) return ;
            	    dbg.location(559,51);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:51: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:51: ws
            	            {
            	            dbg.location(559,51);
            	            pushFollow(FOLLOW_ws_in_declarations1949);
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:17: {...}? less_mixin_call ( ws )?
            	    {
            	    dbg.location(561,17);
            	    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        throw new FailedPredicateException(input, "declarations", "isCssPreprocessorSource()");
            	    }
            	    dbg.location(561,46);
            	    pushFollow(FOLLOW_less_mixin_call_in_declarations1988);
            	    less_mixin_call();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(561,62);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:62: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:62: ws
            	            {
            	            dbg.location(561,62);
            	            pushFollow(FOLLOW_ws_in_declarations1990);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(93);}


            	    }
            	    break;

            	default :
            	    break loop94;
                }
            } while (true);
            } finally {dbg.exitSubRule(94);}

            dbg.location(563,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:13: ( ( (~ ( RBRACE ) )+ RBRACE )=> declaration )?
            int alt95=2;
            try { dbg.enterSubRule(95);
            try { dbg.enterDecision(95, decisionCanBacktrack[95]);

            int LA95_0 = input.LA(1);

            if ( (LA95_0==STAR) && (synpred3_Css3())) {
                alt95=1;
            }
            else if ( (LA95_0==IDENT) && (synpred3_Css3())) {
                alt95=1;
            }
            else if ( (LA95_0==GEN) && (synpred3_Css3())) {
                alt95=1;
            }
            else if ( (LA95_0==MEDIA_SYM||LA95_0==AT_IDENT) && (synpred3_Css3())) {
                alt95=1;
            }
            else if ( (LA95_0==SASS_VAR) && (synpred3_Css3())) {
                alt95=1;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:14: ( (~ ( RBRACE ) )+ RBRACE )=> declaration
                    {
                    dbg.location(563,36);
                    pushFollow(FOLLOW_declaration_in_declarations2032);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(564, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:1: rulePredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE ;
    public final void rulePredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rulePredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(585, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:5: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
            {
            dbg.location(589,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt96=0;
            try { dbg.enterSubRule(96);

            loop96:
            do {
                int alt96=2;
                try { dbg.enterDecision(96, decisionCanBacktrack[96]);

                int LA96_0 = input.LA(1);

                if ( ((LA96_0>=NAMESPACE_SYM && LA96_0<=CHARSET_SYM)||(LA96_0>=IMPORT_SYM && LA96_0<=MEDIA_SYM)||(LA96_0>=COMMA && LA96_0<=125)) ) {
                    alt96=1;
                }


                } finally {dbg.exitDecision(96);}

                switch (alt96) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(589,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
            	        input.consume();
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

            dbg.location(589,36);
            match(input,LBRACE,FOLLOW_LBRACE_in_rulePredicate2112); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(590, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:1: declarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI ;
    public final void declarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(592, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI
            {
            dbg.location(596,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt97=0;
            try { dbg.enterSubRule(97);

            loop97:
            do {
                int alt97=2;
                try { dbg.enterDecision(97, decisionCanBacktrack[97]);

                int LA97_0 = input.LA(1);

                if ( ((LA97_0>=NAMESPACE_SYM && LA97_0<=CHARSET_SYM)||(LA97_0>=IMPORT_SYM && LA97_0<=MEDIA_SYM)||(LA97_0>=COMMA && LA97_0<=125)) ) {
                    alt97=1;
                }


                } finally {dbg.exitDecision(97);}

                switch (alt97) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(596,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
            	        input.consume();
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
            	    if ( cnt97 >= 1 ) break loop97;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(97, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt97++;
            } while (true);
            } finally {dbg.exitSubRule(97);}

            dbg.location(596,37);
            match(input,SEMI,FOLLOW_SEMI_in_declarationPredicate2172); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(597, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:599:1: lastDeclarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE ;
    public final void lastDeclarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "lastDeclarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(599, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:5: (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE
            {
            dbg.location(603,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:5: (~ ( LBRACE | SEMI | RBRACE ) )+
            int cnt98=0;
            try { dbg.enterSubRule(98);

            loop98:
            do {
                int alt98=2;
                try { dbg.enterDecision(98, decisionCanBacktrack[98]);

                int LA98_0 = input.LA(1);

                if ( ((LA98_0>=NAMESPACE_SYM && LA98_0<=CHARSET_SYM)||(LA98_0>=IMPORT_SYM && LA98_0<=MEDIA_SYM)||(LA98_0>=COMMA && LA98_0<=125)) ) {
                    alt98=1;
                }


                } finally {dbg.exitDecision(98);}

                switch (alt98) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(603,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
            	        input.consume();
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
            	    if ( cnt98 >= 1 ) break loop98;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(98, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt98++;
            } while (true);
            } finally {dbg.exitSubRule(98);}

            dbg.location(603,37);
            match(input,RBRACE,FOLLOW_RBRACE_in_lastDeclarationPredicate2232); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(604, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(606, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(607,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup2253);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(607,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(100);

            loop100:
            do {
                int alt100=2;
                try { dbg.enterDecision(100, decisionCanBacktrack[100]);

                int LA100_0 = input.LA(1);

                if ( (LA100_0==COMMA) ) {
                    alt100=1;
                }


                } finally {dbg.exitDecision(100);}

                switch (alt100) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(607,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2256); if (state.failed) return ;
            	    dbg.location(607,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:23: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:23: ws
            	            {
            	            dbg.location(607,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup2258);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(99);}

            	    dbg.location(607,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup2261);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop100;
                }
            } while (true);
            } finally {dbg.exitSubRule(100);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(608, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(610, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(611,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2284);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(611,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(101);

            loop101:
            do {
                int alt101=2;
                try { dbg.enterDecision(101, decisionCanBacktrack[101]);

                int LA101_0 = input.LA(1);

                if ( (LA101_0==IDENT||LA101_0==GEN||LA101_0==COLON||(LA101_0>=PLUS && LA101_0<=TILDE)||(LA101_0>=HASH && LA101_0<=PIPE)||LA101_0==LESS_AND||LA101_0==123) ) {
                    alt101=1;
                }


                } finally {dbg.exitDecision(101);}

                switch (alt101) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(611,31);
            	    pushFollow(FOLLOW_combinator_in_selector2287);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(611,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2289);
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
        dbg.location(612, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(615, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
            int alt106=2;
            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

            int LA106_0 = input.LA(1);

            if ( (LA106_0==IDENT||LA106_0==GEN||(LA106_0>=STAR && LA106_0<=PIPE)||LA106_0==LESS_AND) ) {
                alt106=1;
            }
            else if ( (LA106_0==COLON||(LA106_0>=HASH && LA106_0<=DCOLON)||LA106_0==123) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(618,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(618,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2322);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(618,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:17: ( ( esPred )=> elementSubsequent ( ws )? )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(618,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2329);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(618,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:46: ws
                    	            {
                    	            dbg.location(618,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2331);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(620,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(620,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:4: ( ( esPred )=> elementSubsequent ( ws )? )+
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

                            if ( (synpred5_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case 123:
                            {
                            int LA105_3 = input.LA(2);

                            if ( (synpred5_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA105_4 = input.LA(2);

                            if ( (synpred5_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA105_5 = input.LA(2);

                            if ( (synpred5_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA105_6 = input.LA(2);

                            if ( (synpred5_Css3()) ) {
                                alt105=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(105);}

                        switch (alt105) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(620,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2350);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(620,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:33: ws
                    	            {
                    	            dbg.location(620,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2352);
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
        dbg.location(621, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(628, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(629,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==123 ) {
                input.consume();
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
        dbg.location(630, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:1: typeSelector options {k=2; } : ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(632, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:3: ( ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(634,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:6: ( ( ( IDENT | STAR )? PIPE )=> namespacePrefix )?
            int alt107=2;
            try { dbg.enterSubRule(107);
            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

            int LA107_0 = input.LA(1);

            if ( (LA107_0==IDENT) ) {
                int LA107_1 = input.LA(2);

                if ( (synpred6_Css3()) ) {
                    alt107=1;
                }
            }
            else if ( (LA107_0==STAR) ) {
                int LA107_2 = input.LA(2);

                if ( (synpred6_Css3()) ) {
                    alt107=1;
                }
            }
            else if ( (LA107_0==PIPE) && (synpred6_Css3())) {
                alt107=1;
            }
            } finally {dbg.exitDecision(107);}

            switch (alt107) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:7: ( ( IDENT | STAR )? PIPE )=> namespacePrefix
                    {
                    dbg.location(634,31);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2464);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(107);}

            dbg.location(634,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:49: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:51: elementName ( ws )?
            {
            dbg.location(634,51);
            pushFollow(FOLLOW_elementName_in_typeSelector2470);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(634,63);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:63: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:63: ws
                    {
                    dbg.location(634,63);
                    pushFollow(FOLLOW_ws_in_typeSelector2472);
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
        dbg.location(635, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(637, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(638,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:5: ( namespacePrefixName | STAR )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:7: namespacePrefixName
                    {
                    dbg.location(638,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2490);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:29: STAR
                    {
                    dbg.location(638,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2494); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(109);}

            dbg.location(638,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2498); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(639, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(642, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(644,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt110=4;
            try { dbg.enterSubRule(110);
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            switch ( input.LA(1) ) {
            case HASH:
            case 123:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:6: cssId
                    {
                    dbg.location(645,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2532);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:8: cssClass
                    {
                    dbg.location(646,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2541);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:11: slAttribute
                    {
                    dbg.location(647,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2553);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:11: pseudo
                    {
                    dbg.location(648,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2565);
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
        dbg.location(650, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(653, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:5: ( HASH | ( '#' NAME ) )
            int alt111=2;
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( (LA111_0==HASH) ) {
                alt111=1;
            }
            else if ( (LA111_0==123) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:7: HASH
                    {
                    dbg.location(654,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2593); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:14: ( '#' NAME )
                    {
                    dbg.location(654,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:16: '#' NAME
                    {
                    dbg.location(654,16);
                    match(input,123,FOLLOW_123_in_cssId2599); if (state.failed) return ;
                    dbg.location(654,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2601); if (state.failed) return ;

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
        dbg.location(655, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(661, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:7: DOT ( IDENT | GEN )
            {
            dbg.location(662,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2629); if (state.failed) return ;
            dbg.location(662,11);
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
        dbg.location(663, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:1: elementName : ( ( IDENT | GEN | LESS_AND ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(670, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:5: ( ( IDENT | GEN | LESS_AND ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(671,5);
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
        dbg.location(672, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(674, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(675,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2703); if (state.failed) return ;
            dbg.location(676,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:6: ( namespacePrefix )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:6: namespacePrefix
                    {
                    dbg.location(676,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2710);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}

            dbg.location(676,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:23: ws
                    {
                    dbg.location(676,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2713);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}

            dbg.location(677,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2724);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(677,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:25: ws
                    {
                    dbg.location(677,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2726);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(114);}

            dbg.location(679,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(680,17);
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

                    dbg.location(688,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:17: ws
                            {
                            dbg.location(688,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2948);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(115);}

                    dbg.location(689,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2967);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(690,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:17: ws
                            {
                            dbg.location(690,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2985);
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

            dbg.location(693,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute3014); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(694, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(701, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:4: IDENT
            {
            dbg.location(702,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3030); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(703, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(705, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:2: ( IDENT | STRING )
            {
            dbg.location(707,2);
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
        dbg.location(711, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(713, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(714,7);
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

            dbg.location(715,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(716,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(717,21);
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

                    dbg.location(718,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(719,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:25: ws
                                    {
                                    dbg.location(719,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3225);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(118);}

                            dbg.location(719,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3228); if (state.failed) return ;
                            dbg.location(719,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:36: ws
                                    {
                                    dbg.location(719,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3230);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(119);}

                            dbg.location(719,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:40: ( expression | '*' )?
                            int alt120=3;
                            try { dbg.enterSubRule(120);
                            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                            int LA120_0 = input.LA(1);

                            if ( ((LA120_0>=IDENT && LA120_0<=URI)||LA120_0==MEDIA_SYM||(LA120_0>=GEN && LA120_0<=AT_IDENT)||LA120_0==PERCENTAGE||LA120_0==PLUS||(LA120_0>=MINUS && LA120_0<=HASH)||(LA120_0>=NUMBER && LA120_0<=DIMENSION)||LA120_0==SASS_VAR) ) {
                                alt120=1;
                            }
                            else if ( (LA120_0==STAR) ) {
                                alt120=2;
                            }
                            } finally {dbg.exitDecision(120);}

                            switch (alt120) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:42: expression
                                    {
                                    dbg.location(719,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3235);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:55: '*'
                                    {
                                    dbg.location(719,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3239); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(120);}

                            dbg.location(719,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3244); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(723,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(723,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3323); if (state.failed) return ;
                    dbg.location(723,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:23: ws
                            {
                            dbg.location(723,23);
                            pushFollow(FOLLOW_ws_in_pseudo3325);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(122);}

                    dbg.location(723,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3328); if (state.failed) return ;
                    dbg.location(723,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:34: ws
                            {
                            dbg.location(723,34);
                            pushFollow(FOLLOW_ws_in_pseudo3330);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(123);}

                    dbg.location(723,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:38: ( simpleSelectorSequence )?
                    int alt124=2;
                    try { dbg.enterSubRule(124);
                    try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                    int LA124_0 = input.LA(1);

                    if ( (LA124_0==IDENT||LA124_0==GEN||LA124_0==COLON||(LA124_0>=HASH && LA124_0<=PIPE)||LA124_0==LESS_AND||LA124_0==123) ) {
                        alt124=1;
                    }
                    } finally {dbg.exitDecision(124);}

                    switch (alt124) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:38: simpleSelectorSequence
                            {
                            dbg.location(723,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3333);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(124);}

                    dbg.location(723,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3336); if (state.failed) return ;

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
        dbg.location(725, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(727, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:728:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:5: ( STAR )? property COLON ( ws )? propertyValue ( prio ( ws )? )?
            {
            dbg.location(730,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:5: ( STAR )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:5: STAR
                    {
                    dbg.location(730,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3380); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(730,11);
            pushFollow(FOLLOW_property_in_declaration3383);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(730,20);
            match(input,COLON,FOLLOW_COLON_in_declaration3385); if (state.failed) return ;
            dbg.location(730,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:26: ws
                    {
                    dbg.location(730,26);
                    pushFollow(FOLLOW_ws_in_declaration3387);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}

            dbg.location(730,30);
            pushFollow(FOLLOW_propertyValue_in_declaration3390);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(730,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:44: ( prio ( ws )? )?
            int alt129=2;
            try { dbg.enterSubRule(129);
            try { dbg.enterDecision(129, decisionCanBacktrack[129]);

            int LA129_0 = input.LA(1);

            if ( (LA129_0==IMPORTANT_SYM) ) {
                alt129=1;
            }
            } finally {dbg.exitDecision(129);}

            switch (alt129) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:45: prio ( ws )?
                    {
                    dbg.location(730,45);
                    pushFollow(FOLLOW_prio_in_declaration3393);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(730,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:50: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:50: ws
                            {
                            dbg.location(730,50);
                            pushFollow(FOLLOW_ws_in_declaration3395);
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
        dbg.location(731, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(739, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:2: ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) )
            int alt130=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:9: ( ( expressionPredicate )=> expression )
                    {
                    dbg.location(741,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:9: ( ( expressionPredicate )=> expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:11: ( expressionPredicate )=> expression
                    {
                    dbg.location(741,34);
                    pushFollow(FOLLOW_expression_in_propertyValue3435);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:9: ({...}? cp_expression )
                    {
                    dbg.location(751,9);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:9: ({...}? cp_expression )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:11: {...}? cp_expression
                    {
                    dbg.location(751,11);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isCssPreprocessorSource()");
                    }
                    dbg.location(751,40);
                    pushFollow(FOLLOW_cp_expression_in_propertyValue3478);
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
        dbg.location(752, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:1: expressionPredicate options {k=1; } : (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(755, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:757:5: ( (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(758,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:5: (~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt131=0;
            try { dbg.enterSubRule(131);

            loop131:
            do {
                int alt131=2;
                try { dbg.enterDecision(131, decisionCanBacktrack[131]);

                int LA131_0 = input.LA(1);

                if ( ((LA131_0>=NAMESPACE_SYM && LA131_0<=CHARSET_SYM)||(LA131_0>=IMPORT_SYM && LA131_0<=MEDIA_SYM)||(LA131_0>=COMMA && LA131_0<=GEN)||(LA131_0>=WS && LA131_0<=COLON)||(LA131_0>=PLUS && LA131_0<=DCOLON)||(LA131_0>=PIPE && LA131_0<=125)) ) {
                    alt131=1;
                }


                } finally {dbg.exitDecision(131);}

                switch (alt131) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:758:7: ~ ( AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(758,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=GEN)||(input.LA(1)>=WS && input.LA(1)<=COLON)||(input.LA(1)>=PLUS && input.LA(1)<=DCOLON)||(input.LA(1)>=PIPE && input.LA(1)<=125) ) {
            	        input.consume();
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

            dbg.location(758,65);
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
        dbg.location(759, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(763, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:6: 
            {
            }

        }
        finally {
        }
        dbg.location(769, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(771, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:776:6: 
            {
            }

        }
        finally {
        }
        dbg.location(776, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:779:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(779, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:783:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:6: 
            {
            }

        }
        finally {
        }
        dbg.location(784, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(786, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:7: IMPORTANT_SYM
            {
            dbg.location(787,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3672); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(788, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(790, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(791,7);
            pushFollow(FOLLOW_term_in_expression3693);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(791,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(134);

            loop134:
            do {
                int alt134=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(791,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:14: ( operator ( ws )? )?
            	    int alt133=2;
            	    try { dbg.enterSubRule(133);
            	    try { dbg.enterDecision(133, decisionCanBacktrack[133]);

            	    int LA133_0 = input.LA(1);

            	    if ( (LA133_0==COMMA||LA133_0==SOLIDUS) ) {
            	        alt133=1;
            	    }
            	    } finally {dbg.exitDecision(133);}

            	    switch (alt133) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:15: operator ( ws )?
            	            {
            	            dbg.location(791,15);
            	            pushFollow(FOLLOW_operator_in_expression3698);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(791,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:24: ws
            	                    {
            	                    dbg.location(791,24);
            	                    pushFollow(FOLLOW_ws_in_expression3700);
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

            	    dbg.location(791,30);
            	    pushFollow(FOLLOW_term_in_expression3705);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop134;
                }
            } while (true);
            } finally {dbg.exitSubRule(134);}


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
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:794:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(794, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable ) ( ws )?
            {
            dbg.location(795,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:7: ( unaryOperator ( ws )? )?
            int alt136=2;
            try { dbg.enterSubRule(136);
            try { dbg.enterDecision(136, decisionCanBacktrack[136]);

            int LA136_0 = input.LA(1);

            if ( (LA136_0==PLUS||LA136_0==MINUS) ) {
                alt136=1;
            }
            } finally {dbg.exitDecision(136);}

            switch (alt136) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:9: unaryOperator ( ws )?
                    {
                    dbg.location(795,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3730);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(795,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:23: ws
                            {
                            dbg.location(795,23);
                            pushFollow(FOLLOW_ws_in_term3732);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(135);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}

            dbg.location(796,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )
            int alt137=8;
            try { dbg.enterSubRule(137);
            try { dbg.enterDecision(137, decisionCanBacktrack[137]);

            try {
                isCyclicDecision = true;
                alt137 = dfa137.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(797,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:7: STRING
                    {
                    dbg.location(810,7);
                    match(input,STRING,FOLLOW_STRING_in_term3956); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:7: IDENT
                    {
                    dbg.location(811,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3964); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:7: GEN
                    {
                    dbg.location(812,7);
                    match(input,GEN,FOLLOW_GEN_in_term3972); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:7: URI
                    {
                    dbg.location(813,7);
                    match(input,URI,FOLLOW_URI_in_term3980); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:7: hexColor
                    {
                    dbg.location(814,7);
                    pushFollow(FOLLOW_hexColor_in_term3988);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:7: function
                    {
                    dbg.location(815,7);
                    pushFollow(FOLLOW_function_in_term3996);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:7: {...}? cp_variable
                    {
                    dbg.location(816,7);
                    if ( !(evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isCssPreprocessorSource()");
                    }
                    dbg.location(816,36);
                    pushFollow(FOLLOW_cp_variable_in_term4006);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(818,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:5: ws
                    {
                    dbg.location(818,5);
                    pushFollow(FOLLOW_ws_in_term4018);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(819, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(821, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(822,5);
            pushFollow(FOLLOW_functionName_in_function4034);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(822,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:18: ws
                    {
                    dbg.location(822,18);
                    pushFollow(FOLLOW_ws_in_function4036);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(139);}

            dbg.location(823,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4041); if (state.failed) return ;
            dbg.location(823,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:10: ws
                    {
                    dbg.location(823,10);
                    pushFollow(FOLLOW_ws_in_function4043);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(824,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt143=2;
            try { dbg.enterSubRule(143);
            try { dbg.enterDecision(143, decisionCanBacktrack[143]);

            try {
                isCyclicDecision = true;
                alt143 = dfa143.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(143);}

            switch (alt143) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:4: expression
                    {
                    dbg.location(825,4);
                    pushFollow(FOLLOW_expression_in_function4053);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(827,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:827:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(828,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4071);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(828,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(142);

                    loop142:
                    do {
                        int alt142=2;
                        try { dbg.enterDecision(142, decisionCanBacktrack[142]);

                        int LA142_0 = input.LA(1);

                        if ( (LA142_0==COMMA) ) {
                            alt142=1;
                        }


                        } finally {dbg.exitDecision(142);}

                        switch (alt142) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(828,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4074); if (state.failed) return ;
                    	    dbg.location(828,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:24: ws
                    	            {
                    	            dbg.location(828,24);
                    	            pushFollow(FOLLOW_ws_in_function4076);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(141);}

                    	    dbg.location(828,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4079);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop142;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(142);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}

            dbg.location(831,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4100); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(832, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(838, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(842,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:4: ( IDENT COLON )?
            int alt144=2;
            try { dbg.enterSubRule(144);
            try { dbg.enterDecision(144, decisionCanBacktrack[144]);

            int LA144_0 = input.LA(1);

            if ( (LA144_0==IDENT) ) {
                int LA144_1 = input.LA(2);

                if ( (LA144_1==COLON) ) {
                    alt144=1;
                }
            }
            } finally {dbg.exitDecision(144);}

            switch (alt144) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:5: IDENT COLON
                    {
                    dbg.location(842,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName4148); if (state.failed) return ;
                    dbg.location(842,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName4150); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(144);}

            dbg.location(842,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4154); if (state.failed) return ;
            dbg.location(842,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:25: ( DOT IDENT )*
            try { dbg.enterSubRule(145);

            loop145:
            do {
                int alt145=2;
                try { dbg.enterDecision(145, decisionCanBacktrack[145]);

                int LA145_0 = input.LA(1);

                if ( (LA145_0==DOT) ) {
                    alt145=1;
                }


                } finally {dbg.exitDecision(145);}

                switch (alt145) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:842:26: DOT IDENT
            	    {
            	    dbg.location(842,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName4157); if (state.failed) return ;
            	    dbg.location(842,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName4159); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop145;
                }
            } while (true);
            } finally {dbg.exitSubRule(145);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(844, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:846:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(846, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(847,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4182);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(847,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:20: ws
                    {
                    dbg.location(847,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4184);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(146);}

            dbg.location(847,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4187); if (state.failed) return ;
            dbg.location(847,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:29: ws
                    {
                    dbg.location(847,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4189);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(147);}

            dbg.location(847,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4192);
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
        dbg.location(848, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:850:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(850, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:4: IDENT ( DOT IDENT )*
            {
            dbg.location(851,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4207); if (state.failed) return ;
            dbg.location(851,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:10: ( DOT IDENT )*
            try { dbg.enterSubRule(148);

            loop148:
            do {
                int alt148=2;
                try { dbg.enterDecision(148, decisionCanBacktrack[148]);

                int LA148_0 = input.LA(1);

                if ( (LA148_0==DOT) ) {
                    alt148=1;
                }


                } finally {dbg.exitDecision(148);}

                switch (alt148) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:11: DOT IDENT
            	    {
            	    dbg.location(851,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4210); if (state.failed) return ;
            	    dbg.location(851,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4212); if (state.failed) return ;

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
        dbg.location(852, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(854, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:855:4: expression
            {
            dbg.location(855,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4226);
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
        dbg.location(856, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(858, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:7: HASH
            {
            dbg.location(859,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4244); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "hexColor");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "hexColor"


    // $ANTLR start "ws"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:862:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(862, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:7: ( WS | NL | COMMENT )+
            {
            dbg.location(863,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:7: ( WS | NL | COMMENT )+
            int cnt149=0;
            try { dbg.enterSubRule(149);

            loop149:
            do {
                int alt149=2;
                try { dbg.enterDecision(149, decisionCanBacktrack[149]);

                int LA149_0 = input.LA(1);

                if ( (LA149_0==WS||(LA149_0>=NL && LA149_0<=COMMENT)) ) {
                    alt149=1;
                }


                } finally {dbg.exitDecision(149);}

                switch (alt149) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(863,7);
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
            	    if ( cnt149 >= 1 ) break loop149;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(149, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt149++;
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
        dbg.location(864, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:1: cp_variable_declaration : cp_variable ( ws )? COLON ( ws )? cp_expression SEMI ;
    public final void cp_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(869, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:5: ( cp_variable ( ws )? COLON ( ws )? cp_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:7: cp_variable ( ws )? COLON ( ws )? cp_expression SEMI
            {
            dbg.location(870,7);
            pushFollow(FOLLOW_cp_variable_in_cp_variable_declaration4302);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(870,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:19: ws
                    {
                    dbg.location(870,19);
                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4304);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(150);}

            dbg.location(870,23);
            match(input,COLON,FOLLOW_COLON_in_cp_variable_declaration4307); if (state.failed) return ;
            dbg.location(870,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:29: ws
                    {
                    dbg.location(870,29);
                    pushFollow(FOLLOW_ws_in_cp_variable_declaration4309);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(151);}

            dbg.location(870,33);
            pushFollow(FOLLOW_cp_expression_in_cp_variable_declaration4312);
            cp_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(870,47);
            match(input,SEMI,FOLLOW_SEMI_in_cp_variable_declaration4314); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_variable_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable_declaration"


    // $ANTLR start "cp_variable"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:1: cp_variable : ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) );
    public final void cp_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(874, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:5: ({...}? ( AT_IDENT | MEDIA_SYM ) | {...}? ( SASS_VAR ) )
            int alt152=2;
            try { dbg.enterDecision(152, decisionCanBacktrack[152]);

            int LA152_0 = input.LA(1);

            if ( (LA152_0==MEDIA_SYM||LA152_0==AT_IDENT) ) {
                alt152=1;
            }
            else if ( (LA152_0==SASS_VAR) ) {
                alt152=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 152, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(152);}

            switch (alt152) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:876:9: {...}? ( AT_IDENT | MEDIA_SYM )
                    {
                    dbg.location(876,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isLessSource()");
                    }
                    dbg.location(876,27);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:9: {...}? ( SASS_VAR )
                    {
                    dbg.location(878,9);
                    if ( !(evalPredicate(isScssSource(),"isScssSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "cp_variable", "isScssSource()");
                    }
                    dbg.location(878,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:27: ( SASS_VAR )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:29: SASS_VAR
                    {
                    dbg.location(878,29);
                    match(input,SASS_VAR,FOLLOW_SASS_VAR_in_cp_variable4375); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cp_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_variable"


    // $ANTLR start "cp_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:1: cp_expression : cp_additionExp ;
    public final void cp_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(882, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:5: ( cp_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:883:10: cp_additionExp
            {
            dbg.location(883,10);
            pushFollow(FOLLOW_cp_additionExp_in_cp_expression4398);
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
        dbg.location(884, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:1: cp_additionExp : cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* ;
    public final void cp_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(886, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:5: ( cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:10: cp_multiplyExp ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            {
            dbg.location(887,10);
            pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4418);
            cp_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(888,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:10: ( PLUS ( ws )? cp_multiplyExp | MINUS ( ws )? cp_multiplyExp )*
            try { dbg.enterSubRule(155);

            loop155:
            do {
                int alt155=3;
                try { dbg.enterDecision(155, decisionCanBacktrack[155]);

                int LA155_0 = input.LA(1);

                if ( (LA155_0==PLUS) ) {
                    alt155=1;
                }
                else if ( (LA155_0==MINUS) ) {
                    alt155=2;
                }


                } finally {dbg.exitDecision(155);}

                switch (alt155) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:12: PLUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(888,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_cp_additionExp4432); if (state.failed) return ;
            	    dbg.location(888,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:17: ws
            	            {
            	            dbg.location(888,17);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4434);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(153);}

            	    dbg.location(888,21);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4437);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:12: MINUS ( ws )? cp_multiplyExp
            	    {
            	    dbg.location(889,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_cp_additionExp4450); if (state.failed) return ;
            	    dbg.location(889,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:18: ws
            	            {
            	            dbg.location(889,18);
            	            pushFollow(FOLLOW_ws_in_cp_additionExp4452);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(154);}

            	    dbg.location(889,22);
            	    pushFollow(FOLLOW_cp_multiplyExp_in_cp_additionExp4455);
            	    cp_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop155;
                }
            } while (true);
            } finally {dbg.exitSubRule(155);}


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
            dbg.exitRule(getGrammarFileName(), "cp_additionExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_additionExp"


    // $ANTLR start "cp_multiplyExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:1: cp_multiplyExp : cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* ;
    public final void cp_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(893, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:5: ( cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:10: cp_atomExp ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            {
            dbg.location(894,10);
            pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4488);
            cp_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(895,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:10: ( STAR ( ws )? cp_atomExp | SOLIDUS ( ws )? cp_atomExp )*
            try { dbg.enterSubRule(158);

            loop158:
            do {
                int alt158=3;
                try { dbg.enterDecision(158, decisionCanBacktrack[158]);

                int LA158_0 = input.LA(1);

                if ( (LA158_0==STAR) ) {
                    alt158=1;
                }
                else if ( (LA158_0==SOLIDUS) ) {
                    alt158=2;
                }


                } finally {dbg.exitDecision(158);}

                switch (alt158) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:12: STAR ( ws )? cp_atomExp
            	    {
            	    dbg.location(895,12);
            	    match(input,STAR,FOLLOW_STAR_in_cp_multiplyExp4501); if (state.failed) return ;
            	    dbg.location(895,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:17: ws
            	            {
            	            dbg.location(895,17);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4503);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(156);}

            	    dbg.location(895,21);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4506);
            	    cp_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:12: SOLIDUS ( ws )? cp_atomExp
            	    {
            	    dbg.location(896,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_cp_multiplyExp4520); if (state.failed) return ;
            	    dbg.location(896,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:20: ws
            	            {
            	            dbg.location(896,20);
            	            pushFollow(FOLLOW_ws_in_cp_multiplyExp4522);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(157);}

            	    dbg.location(896,24);
            	    pushFollow(FOLLOW_cp_atomExp_in_cp_multiplyExp4525);
            	    cp_atomExp();

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
        dbg.location(898, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:900:1: cp_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? );
    public final void cp_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(900, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:5: ( term ( ( term )=> term )* | LPAREN ( ws )? cp_additionExp RPAREN ( ws )? )
            int alt162=2;
            try { dbg.enterDecision(162, decisionCanBacktrack[162]);

            int LA162_0 = input.LA(1);

            if ( ((LA162_0>=IDENT && LA162_0<=URI)||LA162_0==MEDIA_SYM||(LA162_0>=GEN && LA162_0<=AT_IDENT)||LA162_0==PERCENTAGE||LA162_0==PLUS||(LA162_0>=MINUS && LA162_0<=HASH)||(LA162_0>=NUMBER && LA162_0<=DIMENSION)||LA162_0==SASS_VAR) ) {
                alt162=1;
            }
            else if ( (LA162_0==LPAREN) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:10: term ( ( term )=> term )*
                    {
                    dbg.location(901,10);
                    pushFollow(FOLLOW_term_in_cp_atomExp4558);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(901,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(159);

                    loop159:
                    do {
                        int alt159=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:16: ( term )=> term
                    	    {
                    	    dbg.location(901,24);
                    	    pushFollow(FOLLOW_term_in_cp_atomExp4565);
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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:10: LPAREN ( ws )? cp_additionExp RPAREN ( ws )?
                    {
                    dbg.location(902,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_cp_atomExp4579); if (state.failed) return ;
                    dbg.location(902,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:17: ws
                            {
                            dbg.location(902,17);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4581);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(160);}

                    dbg.location(902,21);
                    pushFollow(FOLLOW_cp_additionExp_in_cp_atomExp4584);
                    cp_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(902,36);
                    match(input,RPAREN,FOLLOW_RPAREN_in_cp_atomExp4586); if (state.failed) return ;
                    dbg.location(902,43);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:43: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:902:43: ws
                            {
                            dbg.location(902,43);
                            pushFollow(FOLLOW_ws_in_cp_atomExp4588);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(161);}


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
        dbg.location(903, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:1: cp_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? ;
    public final void cp_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cp_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(906, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:907:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable ) ( ws )?
            {
            dbg.location(908,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:908:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )
            int alt163=8;
            try { dbg.enterSubRule(163);
            try { dbg.enterDecision(163, decisionCanBacktrack[163]);

            try {
                isCyclicDecision = true;
                alt163 = dfa163.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(163);}

            switch (alt163) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(909,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:922:7: STRING
                    {
                    dbg.location(922,7);
                    match(input,STRING,FOLLOW_STRING_in_cp_term4826); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:923:7: IDENT
                    {
                    dbg.location(923,7);
                    match(input,IDENT,FOLLOW_IDENT_in_cp_term4834); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:7: GEN
                    {
                    dbg.location(924,7);
                    match(input,GEN,FOLLOW_GEN_in_cp_term4842); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:7: URI
                    {
                    dbg.location(925,7);
                    match(input,URI,FOLLOW_URI_in_cp_term4850); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:7: hexColor
                    {
                    dbg.location(926,7);
                    pushFollow(FOLLOW_hexColor_in_cp_term4858);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:7: function
                    {
                    dbg.location(927,7);
                    pushFollow(FOLLOW_function_in_cp_term4866);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:928:7: cp_variable
                    {
                    dbg.location(928,7);
                    pushFollow(FOLLOW_cp_variable_in_cp_term4874);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(163);}

            dbg.location(930,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:930:5: ws
                    {
                    dbg.location(930,5);
                    pushFollow(FOLLOW_ws_in_cp_term4886);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(164);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(931, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cp_term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cp_term"


    // $ANTLR start "less_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:953:1: less_mixin_declaration : cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? ;
    public final void less_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(953, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:5: ( cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:5: cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
            {
            dbg.location(955,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_declaration4928);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(955,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:14: ws
                    {
                    dbg.location(955,14);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4930);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(165);}

            dbg.location(955,18);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_declaration4933); if (state.failed) return ;
            dbg.location(955,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:25: ( less_args_list )?
            int alt166=2;
            try { dbg.enterSubRule(166);
            try { dbg.enterDecision(166, decisionCanBacktrack[166]);

            int LA166_0 = input.LA(1);

            if ( (LA166_0==MEDIA_SYM||LA166_0==AT_IDENT||LA166_0==SASS_VAR||(LA166_0>=124 && LA166_0<=125)) ) {
                alt166=1;
            }
            } finally {dbg.exitDecision(166);}

            switch (alt166) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:25: less_args_list
                    {
                    dbg.location(955,25);
                    pushFollow(FOLLOW_less_args_list_in_less_mixin_declaration4935);
                    less_args_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(166);}

            dbg.location(955,41);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_declaration4938); if (state.failed) return ;
            dbg.location(955,48);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:48: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:48: ws
                    {
                    dbg.location(955,48);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4940);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(167);}

            dbg.location(955,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:52: ( less_mixin_guarded ( ws )? )?
            int alt169=2;
            try { dbg.enterSubRule(169);
            try { dbg.enterDecision(169, decisionCanBacktrack[169]);

            int LA169_0 = input.LA(1);

            if ( (LA169_0==LESS_WHEN) ) {
                alt169=1;
            }
            } finally {dbg.exitDecision(169);}

            switch (alt169) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:53: less_mixin_guarded ( ws )?
                    {
                    dbg.location(955,53);
                    pushFollow(FOLLOW_less_mixin_guarded_in_less_mixin_declaration4944);
                    less_mixin_guarded();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(955,72);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:72: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:72: ws
                            {
                            dbg.location(955,72);
                            pushFollow(FOLLOW_ws_in_less_mixin_declaration4946);
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
            dbg.exitRule(getGrammarFileName(), "less_mixin_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_declaration"


    // $ANTLR start "less_mixin_call"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:960:1: less_mixin_call : cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )? ;
    public final void less_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(960, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:5: ( cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:5: cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )?
            {
            dbg.location(962,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_call4972);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(962,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:14: ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )?
            int alt172=2;
            try { dbg.enterSubRule(172);
            try { dbg.enterDecision(172, decisionCanBacktrack[172]);

            try {
                isCyclicDecision = true;
                alt172 = dfa172.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(172);}

            switch (alt172) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:15: ( ws )? LPAREN ( less_mixin_call_args )? RPAREN
                    {
                    dbg.location(962,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:15: ws
                            {
                            dbg.location(962,15);
                            pushFollow(FOLLOW_ws_in_less_mixin_call4975);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(170);}

                    dbg.location(962,19);
                    match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_call4978); if (state.failed) return ;
                    dbg.location(962,26);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:26: ( less_mixin_call_args )?
                    int alt171=2;
                    try { dbg.enterSubRule(171);
                    try { dbg.enterDecision(171, decisionCanBacktrack[171]);

                    int LA171_0 = input.LA(1);

                    if ( ((LA171_0>=IDENT && LA171_0<=URI)||LA171_0==MEDIA_SYM||(LA171_0>=GEN && LA171_0<=AT_IDENT)||LA171_0==PERCENTAGE||LA171_0==PLUS||(LA171_0>=MINUS && LA171_0<=HASH)||(LA171_0>=NUMBER && LA171_0<=DIMENSION)||LA171_0==SASS_VAR) ) {
                        alt171=1;
                    }
                    } finally {dbg.exitDecision(171);}

                    switch (alt171) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:26: less_mixin_call_args
                            {
                            dbg.location(962,26);
                            pushFollow(FOLLOW_less_mixin_call_args_in_less_mixin_call4980);
                            less_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(171);}

                    dbg.location(962,48);
                    match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_call4983); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(172);}

            dbg.location(962,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:57: ( ( ws )? SEMI )?
            int alt174=2;
            try { dbg.enterSubRule(174);
            try { dbg.enterDecision(174, decisionCanBacktrack[174]);

            try {
                isCyclicDecision = true;
                alt174 = dfa174.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(174);}

            switch (alt174) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:58: ( ws )? SEMI
                    {
                    dbg.location(962,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:58: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:58: ws
                            {
                            dbg.location(962,58);
                            pushFollow(FOLLOW_ws_in_less_mixin_call4988);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(173);}

                    dbg.location(962,62);
                    match(input,SEMI,FOLLOW_SEMI_in_less_mixin_call4991); if (state.failed) return ;

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
        dbg.location(963, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:965:1: less_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void less_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(965, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:966:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(969,5);
            pushFollow(FOLLOW_term_in_less_mixin_call_args5029);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(969,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(176);

            loop176:
            do {
                int alt176=2;
                try { dbg.enterDecision(176, decisionCanBacktrack[176]);

                int LA176_0 = input.LA(1);

                if ( (LA176_0==SEMI||LA176_0==COMMA) ) {
                    alt176=1;
                }


                } finally {dbg.exitDecision(176);}

                switch (alt176) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(969,12);
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

            	    dbg.location(969,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:27: ws
            	            {
            	            dbg.location(969,27);
            	            pushFollow(FOLLOW_ws_in_less_mixin_call_args5041);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(175);}

            	    dbg.location(969,31);
            	    pushFollow(FOLLOW_term_in_less_mixin_call_args5044);
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
            dbg.exitRule(getGrammarFileName(), "less_mixin_call_args");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_mixin_call_args"


    // $ANTLR start "less_args_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(973, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:974:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) )
            int alt181=2;
            try { dbg.enterDecision(181, decisionCanBacktrack[181]);

            int LA181_0 = input.LA(1);

            if ( (LA181_0==MEDIA_SYM||LA181_0==AT_IDENT||LA181_0==SASS_VAR) ) {
                alt181=1;
            }
            else if ( ((LA181_0>=124 && LA181_0<=125)) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? )
                    {
                    dbg.location(977,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )?
                    {
                    dbg.location(977,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list5086);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(977,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(977,18);
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

                    	    dbg.location(977,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:35: ws
                    	            {
                    	            dbg.location(977,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5100);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(177);}

                    	    dbg.location(977,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5103);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop178;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(178);}

                    dbg.location(977,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:50: ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )?
                    int alt180=2;
                    try { dbg.enterSubRule(180);
                    try { dbg.enterDecision(180, decisionCanBacktrack[180]);

                    int LA180_0 = input.LA(1);

                    if ( (LA180_0==SEMI||LA180_0==COMMA) ) {
                        alt180=1;
                    }
                    } finally {dbg.exitDecision(180);}

                    switch (alt180) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:52: ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' )
                            {
                            dbg.location(977,52);
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

                            dbg.location(977,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:977:69: ws
                                    {
                                    dbg.location(977,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5119);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(179);}

                            dbg.location(977,73);
                            if ( (input.LA(1)>=124 && input.LA(1)<=125) ) {
                                input.consume();
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
                    } finally {dbg.exitSubRule(180);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:979:5: ( '...' | '@rest...' )
                    {
                    dbg.location(979,5);
                    if ( (input.LA(1)>=124 && input.LA(1)<=125) ) {
                        input.consume();
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
        dbg.location(980, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:1: less_arg : cp_variable ( COLON ( ws )? cp_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(983, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:984:5: ( cp_variable ( COLON ( ws )? cp_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:5: cp_variable ( COLON ( ws )? cp_expression )?
            {
            dbg.location(985,5);
            pushFollow(FOLLOW_cp_variable_in_less_arg5176);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(985,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:17: ( COLON ( ws )? cp_expression )?
            int alt183=2;
            try { dbg.enterSubRule(183);
            try { dbg.enterDecision(183, decisionCanBacktrack[183]);

            int LA183_0 = input.LA(1);

            if ( (LA183_0==COLON) ) {
                alt183=1;
            }
            } finally {dbg.exitDecision(183);}

            switch (alt183) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:19: COLON ( ws )? cp_expression
                    {
                    dbg.location(985,19);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5180); if (state.failed) return ;
                    dbg.location(985,25);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:25: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:985:25: ws
                            {
                            dbg.location(985,25);
                            pushFollow(FOLLOW_ws_in_less_arg5182);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(182);}

                    dbg.location(985,29);
                    pushFollow(FOLLOW_cp_expression_in_less_arg5185);
                    cp_expression();

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
        dbg.location(986, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:990:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(990, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:991:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(992,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded5211); if (state.failed) return ;
            dbg.location(992,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:15: ws
                    {
                    dbg.location(992,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded5213);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(184);}

            dbg.location(992,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5216);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(992,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(186);

            loop186:
            do {
                int alt186=2;
                try { dbg.enterDecision(186, decisionCanBacktrack[186]);

                int LA186_0 = input.LA(1);

                if ( ((LA186_0>=COMMA && LA186_0<=AND)) ) {
                    alt186=1;
                }


                } finally {dbg.exitDecision(186);}

                switch (alt186) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(992,36);
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

            	    dbg.location(992,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:50: ws
            	            {
            	            dbg.location(992,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded5228);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(185);}

            	    dbg.location(992,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5231);
            	    less_condition();

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
        dbg.location(993, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:997:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(997, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:998:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) ) RPAREN
            {
            dbg.location(999,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:5: ( NOT ( ws )? )?
            int alt188=2;
            try { dbg.enterSubRule(188);
            try { dbg.enterDecision(188, decisionCanBacktrack[188]);

            int LA188_0 = input.LA(1);

            if ( (LA188_0==NOT) ) {
                alt188=1;
            }
            } finally {dbg.exitDecision(188);}

            switch (alt188) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:6: NOT ( ws )?
                    {
                    dbg.location(999,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition5261); if (state.failed) return ;
                    dbg.location(999,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:999:10: ws
                            {
                            dbg.location(999,10);
                            pushFollow(FOLLOW_ws_in_less_condition5263);
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

            dbg.location(1000,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition5272); if (state.failed) return ;
            dbg.location(1000,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1000:12: ws
                    {
                    dbg.location(1000,12);
                    pushFollow(FOLLOW_ws_in_less_condition5274);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(189);}

            dbg.location(1001,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1001:9: ( less_function_in_condition ( ws )? | ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? ) )
            int alt194=2;
            try { dbg.enterSubRule(194);
            try { dbg.enterDecision(194, decisionCanBacktrack[194]);

            int LA194_0 = input.LA(1);

            if ( (LA194_0==IDENT) ) {
                alt194=1;
            }
            else if ( (LA194_0==MEDIA_SYM||LA194_0==AT_IDENT||LA194_0==SASS_VAR) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(1002,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition5300);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1002,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:40: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1002:40: ws
                            {
                            dbg.location(1002,40);
                            pushFollow(FOLLOW_ws_in_less_condition5302);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(190);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    {
                    dbg.location(1004,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:13: ( cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:15: cp_variable ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    {
                    dbg.location(1004,15);
                    pushFollow(FOLLOW_cp_variable_in_less_condition5333);
                    cp_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(1004,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:27: ( ( ws )? less_condition_operator ( ws )? cp_expression )?
                    int alt193=2;
                    try { dbg.enterSubRule(193);
                    try { dbg.enterDecision(193, decisionCanBacktrack[193]);

                    int LA193_0 = input.LA(1);

                    if ( (LA193_0==WS||LA193_0==GREATER||LA193_0==OPEQ||(LA193_0>=NL && LA193_0<=COMMENT)||(LA193_0>=GREATER_OR_EQ && LA193_0<=LESS_OR_EQ)) ) {
                        alt193=1;
                    }
                    } finally {dbg.exitDecision(193);}

                    switch (alt193) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:28: ( ws )? less_condition_operator ( ws )? cp_expression
                            {
                            dbg.location(1004,28);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:28: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:28: ws
                                    {
                                    dbg.location(1004,28);
                                    pushFollow(FOLLOW_ws_in_less_condition5336);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(191);}

                            dbg.location(1004,32);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition5339);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(1004,56);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:56: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1004:56: ws
                                    {
                                    dbg.location(1004,56);
                                    pushFollow(FOLLOW_ws_in_less_condition5341);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(192);}

                            dbg.location(1004,60);
                            pushFollow(FOLLOW_cp_expression_in_less_condition5344);
                            cp_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(193);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(194);}

            dbg.location(1006,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition5373); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(1007, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1010:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1010, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1011:5: ( less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:5: less_fn_name ( ws )? LPAREN ( ws )? cp_variable ( ws )? RPAREN
            {
            dbg.location(1012,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition5399);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1012,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:18: ws
                    {
                    dbg.location(1012,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5401);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(195);}

            dbg.location(1012,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition5404); if (state.failed) return ;
            dbg.location(1012,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:29: ws
                    {
                    dbg.location(1012,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5406);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(196);}

            dbg.location(1012,33);
            pushFollow(FOLLOW_cp_variable_in_less_function_in_condition5409);
            cp_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(1012,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:45: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1012:45: ws
                    {
                    dbg.location(1012,45);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5411);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(197);}

            dbg.location(1012,49);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition5414); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_function_in_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_function_in_condition"


    // $ANTLR start "less_fn_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1016:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1016, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1017:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1018:5: IDENT
            {
            dbg.location(1018,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name5436); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_fn_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_fn_name"


    // $ANTLR start "less_condition_operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1021:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(1021, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1022:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(1022,5);
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
        dbg.location(1024, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:17: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:18: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
        {
        dbg.location(557,18);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:18: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt198=0;
        try { dbg.enterSubRule(198);

        loop198:
        do {
            int alt198=2;
            try { dbg.enterDecision(198, decisionCanBacktrack[198]);

            int LA198_0 = input.LA(1);

            if ( ((LA198_0>=NAMESPACE_SYM && LA198_0<=CHARSET_SYM)||(LA198_0>=IMPORT_SYM && LA198_0<=MEDIA_SYM)||(LA198_0>=COMMA && LA198_0<=125)) ) {
                alt198=1;
            }


            } finally {dbg.exitDecision(198);}

            switch (alt198) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:18: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(557,18);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
        	        input.consume();
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
        	    if ( cnt198 >= 1 ) break loop198;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(198, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt198++;
        } while (true);
        } finally {dbg.exitSubRule(198);}

        dbg.location(557,41);
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Css31917); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:3: ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:4: (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI
        {
        dbg.location(559,4);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:4: (~ ( LBRACE | SEMI | RBRACE ) )+
        int cnt199=0;
        try { dbg.enterSubRule(199);

        loop199:
        do {
            int alt199=2;
            try { dbg.enterDecision(199, decisionCanBacktrack[199]);

            int LA199_0 = input.LA(1);

            if ( ((LA199_0>=NAMESPACE_SYM && LA199_0<=CHARSET_SYM)||(LA199_0>=IMPORT_SYM && LA199_0<=MEDIA_SYM)||(LA199_0>=COMMA && LA199_0<=125)) ) {
                alt199=1;
            }


            } finally {dbg.exitDecision(199);}

            switch (alt199) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:4: ~ ( LBRACE | SEMI | RBRACE )
        	    {
        	    dbg.location(559,4);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
        	        input.consume();
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
        	    if ( cnt199 >= 1 ) break loop199;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(199, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt199++;
        } while (true);
        } finally {dbg.exitSubRule(199);}

        dbg.location(559,27);
        match(input,SEMI,FOLLOW_SEMI_in_synpred2_Css31942); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:14: ( (~ ( RBRACE ) )+ RBRACE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:16: (~ ( RBRACE ) )+ RBRACE
        {
        dbg.location(563,16);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:16: (~ ( RBRACE ) )+
        int cnt200=0;
        try { dbg.enterSubRule(200);

        loop200:
        do {
            int alt200=2;
            try { dbg.enterDecision(200, decisionCanBacktrack[200]);

            int LA200_0 = input.LA(1);

            if ( ((LA200_0>=NAMESPACE_SYM && LA200_0<=LBRACE)||(LA200_0>=COMMA && LA200_0<=125)) ) {
                alt200=1;
            }


            } finally {dbg.exitDecision(200);}

            switch (alt200) {
        	case 1 :
        	    dbg.enterAlt(1);

        	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:16: ~ ( RBRACE )
        	    {
        	    dbg.location(563,16);
        	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=LBRACE)||(input.LA(1)>=COMMA && input.LA(1)<=125) ) {
        	        input.consume();
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
        	    if ( cnt200 >= 1 ) break loop200;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(200, input);
                    dbg.recognitionException(eee);

                    throw eee;
            }
            cnt200++;
        } while (true);
        } finally {dbg.exitSubRule(200);}

        dbg.location(563,27);
        match(input,RBRACE,FOLLOW_RBRACE_in_synpred3_Css32029); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:19: esPred
        {
        dbg.location(618,19);
        pushFollow(FOLLOW_esPred_in_synpred4_Css32326);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:6: esPred
        {
        dbg.location(620,6);
        pushFollow(FOLLOW_esPred_in_synpred5_Css32347);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:7: ( ( IDENT | STAR )? PIPE )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:8: ( IDENT | STAR )? PIPE
        {
        dbg.location(634,8);
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:8: ( IDENT | STAR )?
        int alt201=2;
        try { dbg.enterSubRule(201);
        try { dbg.enterDecision(201, decisionCanBacktrack[201]);

        int LA201_0 = input.LA(1);

        if ( (LA201_0==IDENT||LA201_0==STAR) ) {
            alt201=1;
        }
        } finally {dbg.exitDecision(201);}

        switch (alt201) {
            case 1 :
                dbg.enterAlt(1);

                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                {
                dbg.location(634,8);
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
        } finally {dbg.exitSubRule(201);}

        dbg.location(634,24);
        match(input,PIPE,FOLLOW_PIPE_in_synpred6_Css32461); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:11: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:12: expressionPredicate
        {
        dbg.location(741,12);
        pushFollow(FOLLOW_expressionPredicate_in_synpred7_Css33432);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:17: term
        {
        dbg.location(901,17);
        pushFollow(FOLLOW_term_in_synpred8_Css34562);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

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


    protected DFA42 dfa42 = new DFA42(this);
    protected DFA63 dfa63 = new DFA63(this);
    protected DFA89 dfa89 = new DFA89(this);
    protected DFA94 dfa94 = new DFA94(this);
    protected DFA103 dfa103 = new DFA103(this);
    protected DFA121 dfa121 = new DFA121(this);
    protected DFA130 dfa130 = new DFA130(this);
    protected DFA134 dfa134 = new DFA134(this);
    protected DFA137 dfa137 = new DFA137(this);
    protected DFA143 dfa143 = new DFA143(this);
    protected DFA159 dfa159 = new DFA159(this);
    protected DFA163 dfa163 = new DFA163(this);
    protected DFA172 dfa172 = new DFA172(this);
    protected DFA174 dfa174 = new DFA174(this);
    protected DFA178 dfa178 = new DFA178(this);
    static final String DFA42_eotS =
        "\14\uffff";
    static final String DFA42_eofS =
        "\14\uffff";
    static final String DFA42_minS =
        "\1\5\1\uffff\1\5\4\uffff\1\5\1\uffff\1\5\1\uffff\1\5";
    static final String DFA42_maxS =
        "\1\173\1\uffff\1\121\4\uffff\1\121\1\uffff\1\121\1\uffff\1\121";
    static final String DFA42_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\4\1\5\1\6\1\uffff\1\7\1\uffff\1\2\1\uffff";
    static final String DFA42_specialS =
        "\14\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\1\5\uffff\1\2\6\uffff\1\1\1\7\1\uffff\1\6\3\uffff\1\6\1\uffff"+
            "\1\3\1\4\1\5\20\uffff\1\1\5\uffff\6\1\1\uffff\1\1\26\uffff\1"+
            "\10\50\uffff\1\1",
            "",
            "\1\12\6\uffff\1\12\3\uffff\3\12\1\uffff\1\11\31\uffff\1\10"+
            "\24\uffff\1\12\14\uffff\2\11",
            "",
            "",
            "",
            "",
            "\2\6\5\uffff\1\6\7\uffff\1\13\31\uffff\1\10\41\uffff\2\10",
            "",
            "\1\12\6\uffff\1\12\3\uffff\3\12\1\uffff\1\11\31\uffff\1\10"+
            "\24\uffff\1\12\14\uffff\2\11",
            "",
            "\2\6\5\uffff\1\6\7\uffff\1\13\31\uffff\1\10\41\uffff\2\10"
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
            return "396:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? cp_variable_declaration );";
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
            return "()* loopback of 460:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
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
        "\2\5\1\uffff\2\5\1\uffff";
    static final String DFA89_maxS =
        "\1\173\1\22\1\uffff\2\173\1\uffff";
    static final String DFA89_acceptS =
        "\2\uffff\1\2\2\uffff\1\1";
    static final String DFA89_specialS =
        "\6\uffff}>";
    static final String[] DFA89_transitionS = {
            "\1\2\14\uffff\1\2\33\uffff\1\2\5\uffff\1\2\1\1\4\2\1\uffff\1"+
            "\2\77\uffff\1\2",
            "\1\3\14\uffff\1\3",
            "",
            "\1\2\6\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\1\4\31\uffff"+
            "\1\2\1\uffff\3\2\1\uffff\6\2\1\uffff\1\2\7\uffff\1\5\14\uffff"+
            "\2\4\51\uffff\1\2",
            "\1\2\6\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\1\4\31\uffff"+
            "\1\2\1\uffff\3\2\1\uffff\6\2\1\uffff\1\2\7\uffff\1\5\14\uffff"+
            "\2\4\51\uffff\1\2",
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
            return "537:9: ( ({...}? less_mixin_declaration ) | ( selectorsGroup ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA94_eotS =
        "\20\uffff";
    static final String DFA94_eofS =
        "\20\uffff";
    static final String DFA94_minS =
        "\1\5\5\0\1\uffff\1\0\10\uffff";
    static final String DFA94_maxS =
        "\1\173\5\0\1\uffff\1\0\10\uffff";
    static final String DFA94_acceptS =
        "\6\uffff\1\4\1\uffff\6\1\1\2\1\3";
    static final String DFA94_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\uffff\1\6\10\uffff}>";
    static final String[] DFA94_transitionS = {
            "\1\2\5\uffff\1\4\1\uffff\1\6\4\uffff\1\3\1\4\32\uffff\1\15\5"+
            "\uffff\1\12\1\7\1\14\1\15\1\1\1\10\1\uffff\1\11\26\uffff\1\5"+
            "\50\uffff\1\13",
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
            "",
            "",
            ""
    };

    static final short[] DFA94_eot = DFA.unpackEncodedString(DFA94_eotS);
    static final short[] DFA94_eof = DFA.unpackEncodedString(DFA94_eofS);
    static final char[] DFA94_min = DFA.unpackEncodedStringToUnsignedChars(DFA94_minS);
    static final char[] DFA94_max = DFA.unpackEncodedStringToUnsignedChars(DFA94_maxS);
    static final short[] DFA94_accept = DFA.unpackEncodedString(DFA94_acceptS);
    static final short[] DFA94_special = DFA.unpackEncodedString(DFA94_specialS);
    static final short[][] DFA94_transition;

    static {
        int numStates = DFA94_transitionS.length;
        DFA94_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA94_transition[i] = DFA.unpackEncodedString(DFA94_transitionS[i]);
        }
    }

    class DFA94 extends DFA {

        public DFA94(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 94;
            this.eot = DFA94_eot;
            this.eof = DFA94_eof;
            this.min = DFA94_min;
            this.max = DFA94_max;
            this.accept = DFA94_accept;
            this.special = DFA94_special;
            this.transition = DFA94_transition;
        }
        public String getDescription() {
            return "()* loopback of 556:13: ( ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )=> rule ( ws )? | ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )=> declaration SEMI ( ws )? | {...}? less_mixin_call ( ws )? )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA94_0 = input.LA(1);

                         
                        int index94_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA94_0==STAR) ) {s = 1;}

                        else if ( (LA94_0==IDENT) ) {s = 2;}

                        else if ( (LA94_0==GEN) ) {s = 3;}

                        else if ( (LA94_0==MEDIA_SYM||LA94_0==AT_IDENT) ) {s = 4;}

                        else if ( (LA94_0==SASS_VAR) ) {s = 5;}

                        else if ( (LA94_0==RBRACE) ) {s = 6;}

                        else if ( (LA94_0==DOT) ) {s = 7;}

                        else if ( (LA94_0==PIPE) && (synpred1_Css3())) {s = 8;}

                        else if ( (LA94_0==LESS_AND) && (synpred1_Css3())) {s = 9;}

                        else if ( (LA94_0==HASH) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA94_0==123) && (synpred1_Css3())) {s = 11;}

                        else if ( (LA94_0==LBRACKET) && (synpred1_Css3())) {s = 12;}

                        else if ( (LA94_0==COLON||LA94_0==DCOLON) && (synpred1_Css3())) {s = 13;}

                         
                        input.seek(index94_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA94_1 = input.LA(1);

                         
                        int index94_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index94_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA94_2 = input.LA(1);

                         
                        int index94_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index94_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA94_3 = input.LA(1);

                         
                        int index94_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 13;}

                        else if ( (synpred2_Css3()) ) {s = 14;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index94_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA94_4 = input.LA(1);

                         
                        int index94_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 6;}

                         
                        input.seek(index94_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA94_5 = input.LA(1);

                         
                        int index94_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred2_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 6;}

                         
                        input.seek(index94_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA94_7 = input.LA(1);

                         
                        int index94_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred1_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))||synpred1_Css3())) ) {s = 13;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 15;}

                         
                        input.seek(index94_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 94, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA103_eotS =
        "\21\uffff";
    static final String DFA103_eofS =
        "\21\uffff";
    static final String DFA103_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA103_maxS =
        "\1\173\7\uffff\5\0\4\uffff";
    static final String DFA103_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA103_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA103_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\33\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\1\10\1\12\1\13\1\14\2\1\1\uffff\1\1\10\uffff\1"+
            "\1\66\uffff\1\11",
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
            return "()* loopback of 618:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
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
                        if ( (synpred4_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA103_9 = input.LA(1);

                         
                        int index103_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA103_10 = input.LA(1);

                         
                        int index103_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA103_11 = input.LA(1);

                         
                        int index103_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index103_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA103_12 = input.LA(1);

                         
                        int index103_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 16;}

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
        "\2\5\2\uffff";
    static final String DFA121_maxS =
        "\2\173\2\uffff";
    static final String DFA121_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA121_specialS =
        "\4\uffff}>";
    static final String[] DFA121_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\1\uffff\1\3\7\uffff\1\2\1\3\13"+
            "\uffff\2\1\51\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\1\uffff\1\3\7\uffff\1\2\1\3\13"+
            "\uffff\2\1\51\uffff\1\3",
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
            return "718:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA130_eotS =
        "\17\uffff";
    static final String DFA130_eofS =
        "\17\uffff";
    static final String DFA130_minS =
        "\2\5\10\0\1\uffff\1\5\2\0\1\uffff";
    static final String DFA130_maxS =
        "\2\122\10\0\1\uffff\1\122\2\0\1\uffff";
    static final String DFA130_acceptS =
        "\12\uffff\1\2\3\uffff\1\1";
    static final String DFA130_specialS =
        "\2\uffff\1\2\1\0\1\1\1\6\1\10\1\3\1\11\1\5\2\uffff\1\7\1\4\1\uffff}>";
    static final String[] DFA130_transitionS = {
            "\1\4\1\3\1\6\3\uffff\1\10\6\uffff\1\5\1\10\6\uffff\1\2\25\uffff"+
            "\1\1\2\uffff\1\1\1\7\16\uffff\1\12\2\uffff\12\2\2\uffff\1\11",
            "\1\4\1\3\1\6\3\uffff\1\14\6\uffff\1\5\1\14\1\13\5\uffff\1\2"+
            "\31\uffff\1\7\21\uffff\12\2\2\13\1\15",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\4\1\3\1\6\3\uffff\1\14\6\uffff\1\5\1\14\1\13\5\uffff\1\2"+
            "\31\uffff\1\7\21\uffff\12\2\2\13\1\15",
            "\1\uffff",
            "\1\uffff",
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
            return "739:1: propertyValue : ( ( ( expressionPredicate )=> expression ) | ({...}? cp_expression ) );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA130_3 = input.LA(1);

                         
                        int index130_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA130_4 = input.LA(1);

                         
                        int index130_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_4);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA130_2 = input.LA(1);

                         
                        int index130_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA130_7 = input.LA(1);

                         
                        int index130_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA130_13 = input.LA(1);

                         
                        int index130_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_13);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA130_9 = input.LA(1);

                         
                        int index130_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred7_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isScssSource(),"isScssSource()"))) ) {s = 10;}

                         
                        input.seek(index130_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA130_5 = input.LA(1);

                         
                        int index130_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_5);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA130_12 = input.LA(1);

                         
                        int index130_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA130_6 = input.LA(1);

                         
                        int index130_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 14;}

                        else if ( (evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")) ) {s = 10;}

                         
                        input.seek(index130_6);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA130_8 = input.LA(1);

                         
                        int index130_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred7_Css3()&&evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()"))&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 14;}

                        else if ( ((evalPredicate(isCssPreprocessorSource(),"isCssPreprocessorSource()")&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 10;}

                         
                        input.seek(index130_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 130, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA134_eotS =
        "\12\uffff";
    static final String DFA134_eofS =
        "\12\uffff";
    static final String DFA134_minS =
        "\1\5\1\uffff\1\5\1\uffff\4\5\2\24";
    static final String DFA134_maxS =
        "\1\122\1\uffff\1\122\1\uffff\2\122\1\5\1\122\2\121";
    static final String DFA134_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA134_specialS =
        "\12\uffff}>";
    static final String[] DFA134_transitionS = {
            "\3\3\1\uffff\1\1\1\uffff\1\3\1\uffff\1\1\1\2\3\uffff\2\3\6\uffff"+
            "\1\3\24\uffff\2\3\2\uffff\2\3\17\uffff\2\1\12\3\2\uffff\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\6\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1"+
            "\3\2\uffff\2\3\21\uffff\12\3\2\4\1\3",
            "",
            "\1\5\2\3\3\uffff\1\3\6\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1"+
            "\3\2\uffff\2\3\21\uffff\12\3\2\4\1\3",
            "\3\3\1\uffff\1\3\1\uffff\1\3\1\uffff\2\3\3\uffff\2\3\1\7\5"+
            "\uffff\1\3\23\uffff\3\3\2\uffff\2\3\1\6\6\uffff\1\1\6\uffff"+
            "\15\3\2\7\1\3",
            "\1\10",
            "\3\3\1\uffff\1\3\1\uffff\1\3\1\uffff\2\3\3\uffff\2\3\1\7\5"+
            "\uffff\1\3\24\uffff\2\3\2\uffff\2\3\7\uffff\1\1\6\uffff\15\3"+
            "\2\7\1\3",
            "\1\11\40\uffff\1\6\6\uffff\1\1\6\uffff\1\3\14\uffff\2\11",
            "\1\11\47\uffff\1\1\6\uffff\1\3\14\uffff\2\11"
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
            return "()* loopback of 791:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA137_eotS =
        "\13\uffff";
    static final String DFA137_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA137_minS =
        "\1\5\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA137_maxS =
        "\1\122\2\uffff\1\122\5\uffff\1\122\1\uffff";
    static final String DFA137_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA137_specialS =
        "\13\uffff}>";
    static final String[] DFA137_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\6\uffff\1\4\1\7\6\uffff\1\1\31\uffff"+
            "\1\6\21\uffff\12\1\2\uffff\1\7",
            "",
            "",
            "\3\12\1\uffff\1\12\1\uffff\1\12\1\uffff\2\12\3\uffff\2\12\1"+
            "\11\5\uffff\1\12\23\uffff\1\10\2\12\2\uffff\2\12\1\10\2\uffff"+
            "\1\12\12\uffff\1\10\14\12\2\11\1\12",
            "",
            "",
            "",
            "",
            "",
            "\3\12\1\uffff\1\12\1\uffff\1\12\1\uffff\2\12\3\uffff\2\12\1"+
            "\11\5\uffff\1\12\24\uffff\2\12\2\uffff\2\12\3\uffff\1\12\12"+
            "\uffff\1\10\14\12\2\11\1\12",
            ""
    };

    static final short[] DFA137_eot = DFA.unpackEncodedString(DFA137_eotS);
    static final short[] DFA137_eof = DFA.unpackEncodedString(DFA137_eofS);
    static final char[] DFA137_min = DFA.unpackEncodedStringToUnsignedChars(DFA137_minS);
    static final char[] DFA137_max = DFA.unpackEncodedStringToUnsignedChars(DFA137_maxS);
    static final short[] DFA137_accept = DFA.unpackEncodedString(DFA137_acceptS);
    static final short[] DFA137_special = DFA.unpackEncodedString(DFA137_specialS);
    static final short[][] DFA137_transition;

    static {
        int numStates = DFA137_transitionS.length;
        DFA137_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA137_transition[i] = DFA.unpackEncodedString(DFA137_transitionS[i]);
        }
    }

    class DFA137 extends DFA {

        public DFA137(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 137;
            this.eot = DFA137_eot;
            this.eof = DFA137_eof;
            this.min = DFA137_min;
            this.max = DFA137_max;
            this.accept = DFA137_accept;
            this.special = DFA137_special;
            this.transition = DFA137_transition;
        }
        public String getDescription() {
            return "796:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA143_eotS =
        "\10\uffff";
    static final String DFA143_eofS =
        "\10\uffff";
    static final String DFA143_minS =
        "\1\5\1\uffff\3\5\1\uffff\2\24";
    static final String DFA143_maxS =
        "\1\122\1\uffff\2\122\1\5\1\uffff\2\121";
    static final String DFA143_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA143_specialS =
        "\10\uffff}>";
    static final String[] DFA143_transitionS = {
            "\1\2\2\1\3\uffff\1\1\6\uffff\2\1\6\uffff\1\1\25\uffff\1\1\2"+
            "\uffff\2\1\21\uffff\12\1\2\uffff\1\1",
            "",
            "\3\1\3\uffff\1\1\2\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\23"+
            "\uffff\3\1\2\uffff\2\1\1\4\6\uffff\1\5\6\uffff\2\1\1\uffff\12"+
            "\1\2\3\1\1",
            "\3\1\3\uffff\1\1\2\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\24"+
            "\uffff\2\1\2\uffff\2\1\7\uffff\1\5\6\uffff\2\1\1\uffff\12\1"+
            "\2\3\1\1",
            "\1\6",
            "",
            "\1\7\40\uffff\1\4\6\uffff\1\5\6\uffff\1\1\14\uffff\2\7",
            "\1\7\47\uffff\1\5\6\uffff\1\1\14\uffff\2\7"
    };

    static final short[] DFA143_eot = DFA.unpackEncodedString(DFA143_eotS);
    static final short[] DFA143_eof = DFA.unpackEncodedString(DFA143_eofS);
    static final char[] DFA143_min = DFA.unpackEncodedStringToUnsignedChars(DFA143_minS);
    static final char[] DFA143_max = DFA.unpackEncodedStringToUnsignedChars(DFA143_maxS);
    static final short[] DFA143_accept = DFA.unpackEncodedString(DFA143_acceptS);
    static final short[] DFA143_special = DFA.unpackEncodedString(DFA143_specialS);
    static final short[][] DFA143_transition;

    static {
        int numStates = DFA143_transitionS.length;
        DFA143_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA143_transition[i] = DFA.unpackEncodedString(DFA143_transitionS[i]);
        }
    }

    class DFA143 extends DFA {

        public DFA143(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 143;
            this.eot = DFA143_eot;
            this.eof = DFA143_eof;
            this.min = DFA143_min;
            this.max = DFA143_max;
            this.accept = DFA143_accept;
            this.special = DFA143_special;
            this.transition = DFA143_transition;
        }
        public String getDescription() {
            return "824:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA159_eotS =
        "\36\uffff";
    static final String DFA159_eofS =
        "\36\uffff";
    static final String DFA159_minS =
        "\1\5\1\uffff\2\5\10\uffff\1\5\10\0\1\5\10\0";
    static final String DFA159_maxS =
        "\1\122\1\uffff\2\122\10\uffff\1\122\10\0\1\122\10\0";
    static final String DFA159_acceptS =
        "\1\uffff\1\2\2\uffff\10\1\22\uffff";
    static final String DFA159_specialS =
        "\1\11\14\uffff\1\0\1\2\1\6\1\13\1\15\1\20\1\3\1\5\1\uffff\1\1\1"+
        "\14\1\4\1\16\1\17\1\10\1\7\1\12}>";
    static final String[] DFA159_transitionS = {
            "\1\6\1\5\1\10\1\uffff\1\1\1\uffff\1\12\1\uffff\2\1\3\uffff\1"+
            "\7\1\12\6\uffff\1\4\24\uffff\1\1\1\2\2\uffff\1\3\1\11\3\uffff"+
            "\1\1\13\uffff\2\1\12\4\2\uffff\1\13",
            "",
            "\1\17\1\16\1\21\3\uffff\1\23\6\uffff\1\20\1\23\1\14\5\uffff"+
            "\1\15\25\uffff\1\1\2\uffff\1\1\1\22\16\uffff\1\1\2\uffff\12"+
            "\15\2\14\1\24",
            "\1\30\1\27\1\32\3\uffff\1\34\6\uffff\1\31\1\34\1\25\5\uffff"+
            "\1\26\25\uffff\1\1\2\uffff\1\1\1\33\16\uffff\1\1\2\uffff\12"+
            "\26\2\25\1\35",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17\1\16\1\21\3\uffff\1\23\6\uffff\1\20\1\23\1\14\5\uffff"+
            "\1\15\25\uffff\1\1\2\uffff\1\1\1\22\16\uffff\1\1\2\uffff\12"+
            "\15\2\14\1\24",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\30\1\27\1\32\3\uffff\1\34\6\uffff\1\31\1\34\1\25\5\uffff"+
            "\1\26\25\uffff\1\1\2\uffff\1\1\1\33\16\uffff\1\1\2\uffff\12"+
            "\26\2\25\1\35",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
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
            return "()* loopback of 901:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA159_13 = input.LA(1);

                         
                        int index159_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA159_22 = input.LA(1);

                         
                        int index159_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_22);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA159_14 = input.LA(1);

                         
                        int index159_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_14);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA159_19 = input.LA(1);

                         
                        int index159_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_19);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA159_24 = input.LA(1);

                         
                        int index159_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_24);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA159_20 = input.LA(1);

                         
                        int index159_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_20);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA159_15 = input.LA(1);

                         
                        int index159_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_15);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA159_28 = input.LA(1);

                         
                        int index159_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_28);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA159_27 = input.LA(1);

                         
                        int index159_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_27);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA159_0 = input.LA(1);

                         
                        int index159_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA159_0==SEMI||(LA159_0>=RBRACE && LA159_0<=COMMA)||LA159_0==SOLIDUS||LA159_0==STAR||(LA159_0>=RPAREN && LA159_0<=IMPORTANT_SYM)) ) {s = 1;}

                        else if ( (LA159_0==PLUS) ) {s = 2;}

                        else if ( (LA159_0==MINUS) ) {s = 3;}

                        else if ( (LA159_0==PERCENTAGE||(LA159_0>=NUMBER && LA159_0<=DIMENSION)) && (synpred8_Css3())) {s = 4;}

                        else if ( (LA159_0==STRING) && (synpred8_Css3())) {s = 5;}

                        else if ( (LA159_0==IDENT) && (synpred8_Css3())) {s = 6;}

                        else if ( (LA159_0==GEN) && (synpred8_Css3())) {s = 7;}

                        else if ( (LA159_0==URI) && (synpred8_Css3())) {s = 8;}

                        else if ( (LA159_0==HASH) && (synpred8_Css3())) {s = 9;}

                        else if ( (LA159_0==MEDIA_SYM||LA159_0==AT_IDENT) && (synpred8_Css3())) {s = 10;}

                        else if ( (LA159_0==SASS_VAR) && (synpred8_Css3())) {s = 11;}

                         
                        input.seek(index159_0);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA159_29 = input.LA(1);

                         
                        int index159_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_29);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA159_16 = input.LA(1);

                         
                        int index159_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_16);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA159_23 = input.LA(1);

                         
                        int index159_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_23);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA159_17 = input.LA(1);

                         
                        int index159_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_17);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA159_25 = input.LA(1);

                         
                        int index159_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_25);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA159_26 = input.LA(1);

                         
                        int index159_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_26);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA159_18 = input.LA(1);

                         
                        int index159_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 11;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index159_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 159, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA163_eotS =
        "\13\uffff";
    static final String DFA163_eofS =
        "\3\uffff\1\12\5\uffff\1\12\1\uffff";
    static final String DFA163_minS =
        "\1\5\2\uffff\1\24\5\uffff\1\24\1\uffff";
    static final String DFA163_maxS =
        "\1\122\2\uffff\1\121\5\uffff\1\121\1\uffff";
    static final String DFA163_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA163_specialS =
        "\13\uffff}>";
    static final String[] DFA163_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\6\uffff\1\4\1\7\6\uffff\1\1\31\uffff"+
            "\1\6\21\uffff\12\1\2\uffff\1\7",
            "",
            "",
            "\1\11\31\uffff\1\10\6\uffff\1\10\15\uffff\1\10\14\uffff\2\11",
            "",
            "",
            "",
            "",
            "",
            "\1\11\56\uffff\1\10\14\uffff\2\11",
            ""
    };

    static final short[] DFA163_eot = DFA.unpackEncodedString(DFA163_eotS);
    static final short[] DFA163_eof = DFA.unpackEncodedString(DFA163_eofS);
    static final char[] DFA163_min = DFA.unpackEncodedStringToUnsignedChars(DFA163_minS);
    static final char[] DFA163_max = DFA.unpackEncodedStringToUnsignedChars(DFA163_maxS);
    static final short[] DFA163_accept = DFA.unpackEncodedString(DFA163_acceptS);
    static final short[] DFA163_special = DFA.unpackEncodedString(DFA163_specialS);
    static final short[][] DFA163_transition;

    static {
        int numStates = DFA163_transitionS.length;
        DFA163_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA163_transition[i] = DFA.unpackEncodedString(DFA163_transitionS[i]);
        }
    }

    class DFA163 extends DFA {

        public DFA163(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 163;
            this.eot = DFA163_eot;
            this.eof = DFA163_eof;
            this.min = DFA163_min;
            this.max = DFA163_max;
            this.accept = DFA163_accept;
            this.special = DFA163_special;
            this.transition = DFA163_transition;
        }
        public String getDescription() {
            return "908:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | cp_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA172_eotS =
        "\4\uffff";
    static final String DFA172_eofS =
        "\4\uffff";
    static final String DFA172_minS =
        "\2\5\2\uffff";
    static final String DFA172_maxS =
        "\2\173\2\uffff";
    static final String DFA172_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA172_specialS =
        "\4\uffff}>";
    static final String[] DFA172_transitionS = {
            "\1\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\4\uffff\2\3\1\1\31"+
            "\uffff\1\3\5\uffff\6\3\1\uffff\1\3\7\uffff\1\2\14\uffff\2\1"+
            "\1\3\50\uffff\1\3",
            "\1\3\3\uffff\1\3\1\uffff\1\3\1\uffff\1\3\4\uffff\2\3\1\1\31"+
            "\uffff\1\3\5\uffff\6\3\1\uffff\1\3\7\uffff\1\2\14\uffff\2\1"+
            "\1\3\50\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA172_eot = DFA.unpackEncodedString(DFA172_eotS);
    static final short[] DFA172_eof = DFA.unpackEncodedString(DFA172_eofS);
    static final char[] DFA172_min = DFA.unpackEncodedStringToUnsignedChars(DFA172_minS);
    static final char[] DFA172_max = DFA.unpackEncodedStringToUnsignedChars(DFA172_maxS);
    static final short[] DFA172_accept = DFA.unpackEncodedString(DFA172_acceptS);
    static final short[] DFA172_special = DFA.unpackEncodedString(DFA172_specialS);
    static final short[][] DFA172_transition;

    static {
        int numStates = DFA172_transitionS.length;
        DFA172_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA172_transition[i] = DFA.unpackEncodedString(DFA172_transitionS[i]);
        }
    }

    class DFA172 extends DFA {

        public DFA172(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 172;
            this.eot = DFA172_eot;
            this.eof = DFA172_eof;
            this.min = DFA172_min;
            this.max = DFA172_max;
            this.accept = DFA172_accept;
            this.special = DFA172_special;
            this.transition = DFA172_transition;
        }
        public String getDescription() {
            return "962:14: ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA174_eotS =
        "\4\uffff";
    static final String DFA174_eofS =
        "\4\uffff";
    static final String DFA174_minS =
        "\2\5\2\uffff";
    static final String DFA174_maxS =
        "\2\173\2\uffff";
    static final String DFA174_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA174_specialS =
        "\4\uffff}>";
    static final String[] DFA174_transitionS = {
            "\1\3\3\uffff\1\2\1\uffff\1\3\1\uffff\1\3\4\uffff\2\3\1\1\31"+
            "\uffff\1\3\5\uffff\6\3\1\uffff\1\3\24\uffff\2\1\1\3\50\uffff"+
            "\1\3",
            "\1\3\3\uffff\1\2\1\uffff\1\3\1\uffff\1\3\4\uffff\2\3\1\1\31"+
            "\uffff\1\3\5\uffff\6\3\1\uffff\1\3\24\uffff\2\1\1\3\50\uffff"+
            "\1\3",
            "",
            ""
    };

    static final short[] DFA174_eot = DFA.unpackEncodedString(DFA174_eotS);
    static final short[] DFA174_eof = DFA.unpackEncodedString(DFA174_eofS);
    static final char[] DFA174_min = DFA.unpackEncodedStringToUnsignedChars(DFA174_minS);
    static final char[] DFA174_max = DFA.unpackEncodedStringToUnsignedChars(DFA174_maxS);
    static final short[] DFA174_accept = DFA.unpackEncodedString(DFA174_acceptS);
    static final short[] DFA174_special = DFA.unpackEncodedString(DFA174_specialS);
    static final short[][] DFA174_transition;

    static {
        int numStates = DFA174_transitionS.length;
        DFA174_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA174_transition[i] = DFA.unpackEncodedString(DFA174_transitionS[i]);
        }
    }

    class DFA174 extends DFA {

        public DFA174(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 174;
            this.eot = DFA174_eot;
            this.eof = DFA174_eof;
            this.min = DFA174_min;
            this.max = DFA174_max;
            this.accept = DFA174_accept;
            this.special = DFA174_special;
            this.transition = DFA174_transition;
        }
        public String getDescription() {
            return "962:57: ( ( ws )? SEMI )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA178_eotS =
        "\5\uffff";
    static final String DFA178_eofS =
        "\5\uffff";
    static final String DFA178_minS =
        "\1\11\1\13\1\uffff\1\13\1\uffff";
    static final String DFA178_maxS =
        "\1\104\1\175\1\uffff\1\175\1\uffff";
    static final String DFA178_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA178_specialS =
        "\5\uffff}>";
    static final String[] DFA178_transitionS = {
            "\1\1\4\uffff\1\1\65\uffff\1\2",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\1\4\51\uffff\2\2",
            "",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\1\4\51\uffff\2\2",
            ""
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
            return "()* loopback of 977:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x0BF040003A2C0D30L,0x0800000000040000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x0BF040003A3C0C30L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x0BF040003A2C0C30L,0x0800000000040000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x0BF040003A2C0830L,0x0800000000040000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0800000000040000L});
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
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0800000000030000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x0BF040002A2C2020L,0x0800000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0800000000030000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0800000000030000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0800000000030000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media423 = new BitSet(new long[]{0x0BF040002A3C2020L,0x0800000000030000L});
    public static final BitSet FOLLOW_ws_in_media427 = new BitSet(new long[]{0x0BF040002A2C2020L,0x0800000000000000L});
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
    public static final BitSet FOLLOW_COLON_in_mediaExpression590 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_mediaExpression592 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_expression_in_mediaExpression595 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression600 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body634 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_body636 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0800000000040000L});
    public static final BitSet FOLLOW_rule_in_bodyItem661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_declaration_in_bodyItem735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule802 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule804 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule809 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule811 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule826 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule838 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document864 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document866 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document871 = new BitSet(new long[]{0x0000000000105000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document873 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document879 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document881 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000030000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document884 = new BitSet(new long[]{0x0000000000105000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document886 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document893 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_moz_document895 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_body_in_moz_document900 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes946 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes948 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes951 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes953 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes958 = new BitSet(new long[]{0x0000000004102020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes960 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes967 = new BitSet(new long[]{0x0000000004102020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes969 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock989 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock991 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock996 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock999 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1002 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1006 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1024 = new BitSet(new long[]{0x0000000000104002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1036 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1039 = new BitSet(new long[]{0x0000000004100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1041 = new BitSet(new long[]{0x0000000004000020L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1044 = new BitSet(new long[]{0x0000000000104002L,0x0000000000030000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1073 = new BitSet(new long[]{0x0000400000101020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1075 = new BitSet(new long[]{0x0000400000001020L});
    public static final BitSet FOLLOW_IDENT_in_page1080 = new BitSet(new long[]{0x0000400000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1082 = new BitSet(new long[]{0x0000400000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1089 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1091 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page1104 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_page1106 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0800000000040000L});
    public static final BitSet FOLLOW_declaration_in_page1161 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1163 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1165 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1171 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_page1173 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0800000000040000L});
    public static final BitSet FOLLOW_declaration_in_page1177 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1179 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1181 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1217 = new BitSet(new long[]{0x0000000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1219 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1222 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1224 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1235 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1237 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1240 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1244 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1275 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fontFace1277 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1288 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_fontFace1290 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1293 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1297 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1322 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_margin1324 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1327 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_margin1329 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1332 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_declarations_in_margin1334 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1565 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1617 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1628 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1639 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_combinator1641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property1702 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_property1706 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_cp_variable_in_property1712 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_property1715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_mixin_declaration_in_rule1759 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1792 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1815 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_rule1817 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule1820 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0800000000040000L});
    public static final BitSet FOLLOW_declarations_in_rule1834 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_declarations1920 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_declarations1922 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0800000000040000L});
    public static final BitSet FOLLOW_declaration_in_declarations1945 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_declarations1947 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_declarations1949 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0800000000040000L});
    public static final BitSet FOLLOW_less_mixin_call_in_declarations1988 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_declarations1990 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0800000000040000L});
    public static final BitSet FOLLOW_declaration_in_declarations2032 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_rulePredicate2096 = new BitSet(new long[]{0xFFFFFFFFFFFFDDF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_LBRACE_in_rulePredicate2112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_declarationPredicate2155 = new BitSet(new long[]{0xFFFFFFFFFFFFCFF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_SEMI_in_declarationPredicate2172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_lastDeclarationPredicate2215 = new BitSet(new long[]{0xFFFFFFFFFFFFEDF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_RBRACE_in_lastDeclarationPredicate2232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2253 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2256 = new BitSet(new long[]{0x0BF0400000140020L,0x0800000000030000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2258 = new BitSet(new long[]{0x0BF0400000040020L,0x0800000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2261 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2284 = new BitSet(new long[]{0x0BF7400000040022L,0x0800000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector2287 = new BitSet(new long[]{0x0BF0400000040020L,0x0800000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2289 = new BitSet(new long[]{0x0BF7400000040022L,0x0800000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2322 = new BitSet(new long[]{0x0BF0400000040022L,0x0800000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2329 = new BitSet(new long[]{0x0BF0400000140022L,0x0800000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2331 = new BitSet(new long[]{0x0BF0400000040022L,0x0800000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2350 = new BitSet(new long[]{0x0BF0400000140022L,0x0800000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2352 = new BitSet(new long[]{0x0BF0400000040022L,0x0800000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2464 = new BitSet(new long[]{0x0B00000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2470 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2490 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2494 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_123_in_cssId2599 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2601 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2629 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2703 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2710 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2713 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2724 = new BitSet(new long[]{0xF000000000100000L,0x0000000000030007L});
    public static final BitSet FOLLOW_ws_in_slAttribute2726 = new BitSet(new long[]{0xF000000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_set_in_slAttribute2768 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2948 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2967 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030004L});
    public static final BitSet FOLLOW_ws_in_slAttribute2985 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute3014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3104 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo3168 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3225 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3228 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFD0L});
    public static final BitSet FOLLOW_ws_in_pseudo3230 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFD0L});
    public static final BitSet FOLLOW_expression_in_pseudo3235 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_STAR_in_pseudo3239 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3323 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3328 = new BitSet(new long[]{0x0BF0400000140020L,0x0800000000030010L});
    public static final BitSet FOLLOW_ws_in_pseudo3330 = new BitSet(new long[]{0x0BF0400000040020L,0x0800000000000010L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3333 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3380 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0800000000040000L});
    public static final BitSet FOLLOW_property_in_declaration3383 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration3385 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_declaration3387 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3390 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_prio_in_declaration3393 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_declaration3395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_expression_in_propertyValue3478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3516 = new BitSet(new long[]{0xFEFF7FFFFFF7EFF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_set_in_expressionPredicate3545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3693 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_operator_in_expression3698 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_expression3700 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_term_in_expression3705 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_unaryOperator_in_term3730 = new BitSet(new long[]{0x0BF040003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_term3732 = new BitSet(new long[]{0x0BF040003E2C08E0L,0x080000000004FFC0L});
    public static final BitSet FOLLOW_set_in_term3756 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_STRING_in_term3956 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_IDENT_in_term3964 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_term3972 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_URI_in_term3980 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_hexColor_in_term3988 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_function_in_term3996 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_cp_variable_in_term4006 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_term4018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function4034 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_function4036 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_function4041 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_function4043 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_expression_in_function4053 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_fnAttribute_in_function4071 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_function4074 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_function4076 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_fnAttribute_in_function4079 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_function4100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4148 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_functionName4150 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName4154 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName4157 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName4159 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4182 = new BitSet(new long[]{0x1000000000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4184 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4187 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4189 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4207 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4210 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4212 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4265 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_variable_declaration4302 = new BitSet(new long[]{0x0000400000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4304 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_cp_variable_declaration4307 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_variable_declaration4309 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_expression_in_cp_variable_declaration4312 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_cp_variable_declaration4314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_variable4343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SASS_VAR_in_cp_variable4375 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_expression4398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4418 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_PLUS_in_cp_additionExp4432 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4434 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4437 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cp_additionExp4450 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_additionExp4452 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_multiplyExp_in_cp_additionExp4455 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4488 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_STAR_in_cp_multiplyExp4501 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4503 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4506 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_cp_multiplyExp4520 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_multiplyExp4522 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_atomExp_in_cp_multiplyExp4525 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4558 = new BitSet(new long[]{0x0BF940003E3C08E2L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_term_in_cp_atomExp4565 = new BitSet(new long[]{0x0BF940003E3C08E2L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_LPAREN_in_cp_atomExp4579 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4581 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_additionExp_in_cp_atomExp4584 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_cp_atomExp4586 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_cp_atomExp4588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cp_term4626 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_STRING_in_cp_term4826 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_IDENT_in_cp_term4834 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_cp_term4842 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_URI_in_cp_term4850 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_hexColor_in_cp_term4858 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_function_in_cp_term4866 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_cp_variable_in_cp_term4874 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_cp_term4886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_declaration4928 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4930 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_declaration4933 = new BitSet(new long[]{0x0BF040003A2C0820L,0x3800000000040010L});
    public static final BitSet FOLLOW_less_args_list_in_less_mixin_declaration4935 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_declaration4938 = new BitSet(new long[]{0x0000000000100002L,0x00000000000B0000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4940 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_less_mixin_declaration4944 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_call4972 = new BitSet(new long[]{0x0000000000100202L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call4975 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_call4978 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFD0L});
    public static final BitSet FOLLOW_less_mixin_call_args_in_less_mixin_call4980 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_call4983 = new BitSet(new long[]{0x0000000000100202L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call4988 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_mixin_call4991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args5029 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_mixin_call_args5033 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call_args5041 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC0L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args5044 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5086 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_args_list5090 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5100 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0800000000040000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5103 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_args_list5109 = new BitSet(new long[]{0x0000000000100000L,0x3000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5119 = new BitSet(new long[]{0x0000000000000000L,0x3000000000000000L});
    public static final BitSet FOLLOW_set_in_less_args_list5122 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cp_variable_in_less_arg5176 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5180 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_less_arg5182 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_expression_in_less_arg5185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded5211 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5213 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5216 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded5220 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5228 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5231 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_NOT_in_less_condition5261 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_condition5263 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition5272 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_less_condition5274 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0800000000040000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition5300 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_condition5302 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_cp_variable_in_less_condition5333 = new BitSet(new long[]{0x1002000000100000L,0x0000000000730010L});
    public static final BitSet FOLLOW_ws_in_less_condition5336 = new BitSet(new long[]{0x1002000000100000L,0x0000000000730000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition5339 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_ws_in_less_condition5341 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x080000000007FFC8L});
    public static final BitSet FOLLOW_cp_expression_in_less_condition5344 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition5373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition5399 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5401 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition5404 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0800000000070000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5406 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0800000000040000L});
    public static final BitSet FOLLOW_cp_variable_in_less_function_in_condition5409 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5411 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition5414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name5436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred1_Css31907 = new BitSet(new long[]{0xFFFFFFFFFFFFDDF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Css31917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred2_Css31932 = new BitSet(new long[]{0xFFFFFFFFFFFFCFF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_SEMI_in_synpred2_Css31942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred3_Css32023 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x3FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_RBRACE_in_synpred3_Css32029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred4_Css32326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred5_Css32347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred6_Css32452 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_synpred6_Css32461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred7_Css33432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred8_Css34562 = new BitSet(new long[]{0x0000000000000002L});

}