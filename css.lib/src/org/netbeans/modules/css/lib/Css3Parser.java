// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-02-14 16:11:52

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
        "invalidRule", "mediaQueryOperator", "less_mixin_call_args", "function", 
        "syncTo_RBRACE", "syncToDeclarationsRule", "selectorsGroup", "slAttribute", 
        "less_additionExp", "synpred6_Css3", "namespace", "synpred7_Css3", 
        "charSetValue", "declarations", "mediaExpression", "declarationPredicate", 
        "less_mixin_declaration", "counterStyle", "syncToFollow", "fontFace", 
        "unaryOperator", "vendorAtRule", "less_mixin_guarded", "namespaces", 
        "synpred2_Css3", "less_condition", "simpleSelectorSequence", "mediaType", 
        "rule", "less_arg", "mediaFeature", "slAttributeName", "body", "page", 
        "ws", "cssId", "mediaQueryList", "synpred5_Css3", "less_term", "selector", 
        "elementSubsequent", "esPred", "namespacePrefix", "property", "less_condition_operator", 
        "prio", "webkitKeyframesBlock", "less_fn_name", "margin", "fnAttribute", 
        "imports", "resourceIdentifier", "fnAttributeName", "term", "expression", 
        "styleSheet", "pseudoPage", "synpred4_Css3", "fnAttributeValue", 
        "less_multiplyExp", "webkitKeyframes", "less_variable_declaration", 
        "namespacePrefixName", "charSet", "combinator", "moz_document", 
        "less_function_in_condition", "propertyValue", "cssClass", "lastDeclarationPredicate", 
        "declaration", "media", "rulePredicate", "atRuleId", "bodyItem", 
        "webkitKeyframeSelectors", "expressionPredicate", "importItem", 
        "hexColor", "pseudo", "less_mixin_call", "mediaQuery", "less_variable", 
        "operator", "elementName", "generic_at_rule", "less_atomExp", "slAttributeValue", 
        "synpred3_Css3", "synpred1_Css3", "margin_sym", "less_expression", 
        "functionName", "moz_document_function", "typeSelector", "nsPred", 
        "less_args_list"
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
            true, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:1: styleSheet : ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(309, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:5: ( ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:6: ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF
            {
            dbg.location(311,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:6: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:6: ws
                    {
                    dbg.location(311,6);
                    pushFollow(FOLLOW_ws_in_styleSheet125);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(312,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:6: ( charSet ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:8: charSet ( ws )?
                    {
                    dbg.location(312,8);
                    pushFollow(FOLLOW_charSet_in_styleSheet135);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(312,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:16: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:16: ws
                            {
                            dbg.location(312,16);
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

            dbg.location(313,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:313:9: ( imports )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:313:9: imports
                    {
                    dbg.location(313,9);
                    pushFollow(FOLLOW_imports_in_styleSheet151);
                    imports();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(4);}

            dbg.location(314,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:9: ( namespaces )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:9: namespaces
                    {
                    dbg.location(314,9);
                    pushFollow(FOLLOW_namespaces_in_styleSheet162);
                    namespaces();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(5);}

            dbg.location(315,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:9: ( body )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:9: body
                    {
                    dbg.location(315,9);
                    pushFollow(FOLLOW_body_in_styleSheet174);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(6);}

            dbg.location(316,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet182); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(317, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:1: namespaces : ( namespace ( ws )? )+ ;
    public final void namespaces() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaces");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(319, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:2: ( ( namespace ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:2: ( namespace ( ws )? )+
            {
            dbg.location(321,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:2: ( namespace ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:4: namespace ( ws )?
            	    {
            	    dbg.location(321,4);
            	    pushFollow(FOLLOW_namespace_in_namespaces199);
            	    namespace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(321,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:14: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:14: ws
            	            {
            	            dbg.location(321,14);
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
        dbg.location(322, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:1: namespace : NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(324, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:3: ( NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:5: NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';'
            {
            dbg.location(325,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace217); if (state.failed) return ;
            dbg.location(325,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:19: ws
                    {
                    dbg.location(325,19);
                    pushFollow(FOLLOW_ws_in_namespace219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(325,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:23: ( namespacePrefixName ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:24: namespacePrefixName ( ws )?
                    {
                    dbg.location(325,24);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespace223);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(325,44);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:44: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:44: ws
                            {
                            dbg.location(325,44);
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

            dbg.location(325,50);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace230);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(325,69);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:69: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:69: ws
                    {
                    dbg.location(325,69);
                    pushFollow(FOLLOW_ws_in_namespace232);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}

            dbg.location(325,73);
            match(input,SEMI,FOLLOW_SEMI_in_namespace235); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(326, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:1: namespacePrefixName : IDENT ;
    public final void namespacePrefixName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefixName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(328, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:5: IDENT
            {
            dbg.location(329,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespacePrefixName248); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(330, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(332, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(333,3);
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
        dbg.location(334, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:1: charSet : CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(336, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:5: ( CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:9: CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI
            {
            dbg.location(337,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet286); if (state.failed) return ;
            dbg.location(337,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:21: ws
                    {
                    dbg.location(337,21);
                    pushFollow(FOLLOW_ws_in_charSet288);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(13);}

            dbg.location(337,25);
            pushFollow(FOLLOW_charSetValue_in_charSet291);
            charSetValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(337,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:38: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:38: ws
                    {
                    dbg.location(337,38);
                    pushFollow(FOLLOW_ws_in_charSet293);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(14);}

            dbg.location(337,42);
            match(input,SEMI,FOLLOW_SEMI_in_charSet296); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(338, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:1: charSetValue : STRING ;
    public final void charSetValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSetValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(340, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:2: ( STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:4: STRING
            {
            dbg.location(341,4);
            match(input,STRING,FOLLOW_STRING_in_charSetValue310); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "charSetValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSetValue"


    // $ANTLR start "imports"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:1: imports : ( importItem ( ws )? )+ ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(344, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:2: ( ( importItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:2: ( importItem ( ws )? )+
            {
            dbg.location(346,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:2: ( importItem ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:4: importItem ( ws )?
            	    {
            	    dbg.location(346,4);
            	    pushFollow(FOLLOW_importItem_in_imports324);
            	    importItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(346,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:15: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:15: ws
            	            {
            	            dbg.location(346,15);
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
        dbg.location(347, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:1: importItem : IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI ;
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(349, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:9: IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI
            {
            dbg.location(350,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem347); if (state.failed) return ;
            dbg.location(350,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:20: ws
                    {
                    dbg.location(350,20);
                    pushFollow(FOLLOW_ws_in_importItem349);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(350,24);
            pushFollow(FOLLOW_resourceIdentifier_in_importItem352);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(350,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:43: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:43: ws
                    {
                    dbg.location(350,43);
                    pushFollow(FOLLOW_ws_in_importItem354);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(350,47);
            pushFollow(FOLLOW_mediaQueryList_in_importItem357);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(350,62);
            match(input,SEMI,FOLLOW_SEMI_in_importItem359); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(351, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(352, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE
            {
            dbg.location(353,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media375); if (state.failed) return ;
            dbg.location(353,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:17: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:17: ws
                    {
                    dbg.location(353,17);
                    pushFollow(FOLLOW_ws_in_media377);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(353,21);
            pushFollow(FOLLOW_mediaQueryList_in_media380);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(354,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media390); if (state.failed) return ;
            dbg.location(354,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:16: ws
                    {
                    dbg.location(354,16);
                    pushFollow(FOLLOW_ws_in_media392);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}

            dbg.location(355,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:13: ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:15: ( rule | page | fontFace | vendorAtRule ) ( ws )?
            	    {
            	    dbg.location(355,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:15: ( rule | page | fontFace | vendorAtRule )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:17: rule
            	            {
            	            dbg.location(355,17);
            	            pushFollow(FOLLOW_rule_in_media411);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:24: page
            	            {
            	            dbg.location(355,24);
            	            pushFollow(FOLLOW_page_in_media415);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:31: fontFace
            	            {
            	            dbg.location(355,31);
            	            pushFollow(FOLLOW_fontFace_in_media419);
            	            fontFace();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 4 :
            	            dbg.enterAlt(4);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:42: vendorAtRule
            	            {
            	            dbg.location(355,42);
            	            pushFollow(FOLLOW_vendorAtRule_in_media423);
            	            vendorAtRule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(355,57);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:57: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:57: ws
            	            {
            	            dbg.location(355,57);
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

            dbg.location(356,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media441); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(357, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(359, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(360,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(360,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList457);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(360,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:17: ( COMMA ( ws )? mediaQuery )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(360,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList461); if (state.failed) return ;
                    	    dbg.location(360,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:25: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:25: ws
                    	            {
                    	            dbg.location(360,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList463);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(360,29);
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
        dbg.location(361, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(363, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(364,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:4: ( mediaQueryOperator ( ws )? )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(364,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery485);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(364,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:24: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:24: ws
                                    {
                                    dbg.location(364,24);
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

                    dbg.location(364,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery494);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(364,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:42: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:42: ws
                            {
                            dbg.location(364,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery496);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(29);}

                    dbg.location(364,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:46: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(364,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery501); if (state.failed) return ;
                    	    dbg.location(364,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:52: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:52: ws
                    	            {
                    	            dbg.location(364,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery503);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(364,56);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(365,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery514);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(365,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:20: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(365,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery518); if (state.failed) return ;
                    	    dbg.location(365,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:26: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:26: ws
                    	            {
                    	            dbg.location(365,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery520);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(365,30);
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
        dbg.location(366, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(368, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(369,3);
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
        dbg.location(370, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(372, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(373,2);
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
        dbg.location(374, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:1: mediaExpression : '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(376, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:5: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:7: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )?
            {
            dbg.location(377,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression578); if (state.failed) return ;
            dbg.location(377,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:11: ws
                    {
                    dbg.location(377,11);
                    pushFollow(FOLLOW_ws_in_mediaExpression580);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(377,15);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression583);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(377,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:28: ws
                    {
                    dbg.location(377,28);
                    pushFollow(FOLLOW_ws_in_mediaExpression585);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(377,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:32: ( ':' ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:34: ':' ( ws )? expression
                    {
                    dbg.location(377,34);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression590); if (state.failed) return ;
                    dbg.location(377,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:38: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:38: ws
                            {
                            dbg.location(377,38);
                            pushFollow(FOLLOW_ws_in_mediaExpression592);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(377,42);
                    pushFollow(FOLLOW_expression_in_mediaExpression595);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(377,56);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression600); if (state.failed) return ;
            dbg.location(377,60);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:60: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:60: ws
                    {
                    dbg.location(377,60);
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
        dbg.location(378, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:4: IDENT
            {
            dbg.location(381,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature618); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:2: ( bodyItem ( ws )? )+
            {
            dbg.location(385,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:2: ( bodyItem ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:4: bodyItem ( ws )?
            	    {
            	    dbg.location(385,4);
            	    pushFollow(FOLLOW_bodyItem_in_body634);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(385,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:13: ws
            	            {
            	            dbg.location(385,13);
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
        dbg.location(386, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? => less_variable_declaration );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? => less_variable_declaration )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:6: rule
                    {
                    dbg.location(390,6);
                    pushFollow(FOLLOW_rule_in_bodyItem661);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:11: media
                    {
                    dbg.location(391,11);
                    pushFollow(FOLLOW_media_in_bodyItem673);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:11: page
                    {
                    dbg.location(392,11);
                    pushFollow(FOLLOW_page_in_bodyItem685);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:11: counterStyle
                    {
                    dbg.location(393,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem697);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:11: fontFace
                    {
                    dbg.location(394,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem709);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:395:11: vendorAtRule
                    {
                    dbg.location(395,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem721);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:396:11: {...}? => less_variable_declaration
                    {
                    dbg.location(396,11);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "bodyItem", "isLessSource()");
                    }
                    dbg.location(396,30);
                    pushFollow(FOLLOW_less_variable_declaration_in_bodyItem735);
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
        dbg.location(397, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:1: ( moz_document | webkitKeyframes | generic_at_rule )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:3: moz_document
                    {
                    dbg.location(406,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule758);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:18: webkitKeyframes
                    {
                    dbg.location(406,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule762);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:36: generic_at_rule
                    {
                    dbg.location(406,36);
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
        dbg.location(406, 51);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(408, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(409,2);
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
        dbg.location(411, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(414,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule802); if (state.failed) return ;
            dbg.location(414,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:16: WS
            	    {
            	    dbg.location(414,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule804); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(414,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:20: ( atRuleId ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:22: atRuleId ( WS )*
                    {
                    dbg.location(414,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule809);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(414,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:31: WS
                    	    {
                    	    dbg.location(414,31);
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

            dbg.location(415,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule826); if (state.failed) return ;
            dbg.location(416,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule838);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(417,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule848); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(418, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(419, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(421,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document864); if (state.failed) return ;
            dbg.location(421,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:19: ws
                    {
                    dbg.location(421,19);
                    pushFollow(FOLLOW_ws_in_moz_document866);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(421,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:25: moz_document_function ( ws )?
            {
            dbg.location(421,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document871);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(421,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:47: ws
                    {
                    dbg.location(421,47);
                    pushFollow(FOLLOW_ws_in_moz_document873);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}


            }

            dbg.location(421,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(421,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document879); if (state.failed) return ;
            	    dbg.location(421,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:60: ws
            	            {
            	            dbg.location(421,60);
            	            pushFollow(FOLLOW_ws_in_moz_document881);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(421,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document884);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(421,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:86: ws
            	            {
            	            dbg.location(421,86);
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

            dbg.location(422,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document893); if (state.failed) return ;
            dbg.location(422,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:9: ws
                    {
                    dbg.location(422,9);
                    pushFollow(FOLLOW_ws_in_moz_document895);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(423,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:3: ( body )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:3: body
                    {
                    dbg.location(423,3);
                    pushFollow(FOLLOW_body_in_moz_document900);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(424,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document905); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "moz_document");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document"


    // $ANTLR start "moz_document_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(427, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(428,2);
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
        dbg.location(430, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(433, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(435,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes946); if (state.failed) return ;
            dbg.location(435,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:23: ws
                    {
                    dbg.location(435,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes948);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(435,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes951);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(435,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:36: ws
                    {
                    dbg.location(435,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes953);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(436,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes958); if (state.failed) return ;
            dbg.location(436,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:9: ws
                    {
                    dbg.location(436,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes960);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(437,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:3: ( webkitKeyframesBlock ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(437,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes967);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(437,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:26: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:26: ws
            	            {
            	            dbg.location(437,26);
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

            dbg.location(438,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes976); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(443,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock989);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(443,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:26: ws
                    {
                    dbg.location(443,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock991);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(445,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock996); if (state.failed) return ;
            dbg.location(445,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:10: ws
                    {
                    dbg.location(445,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock999);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(445,14);
            pushFollow(FOLLOW_syncToFollow_in_webkitKeyframesBlock1002);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1006);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(447,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1009); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframesBlock");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframesBlock"


    // $ANTLR start "webkitKeyframeSelectors"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(450, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(452,2);
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

            dbg.location(452,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(452,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:27: ws
            	            {
            	            dbg.location(452,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1036);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(452,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1039); if (state.failed) return ;
            	    dbg.location(452,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:37: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:37: ws
            	            {
            	            dbg.location(452,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1041);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(62);}

            	    dbg.location(452,41);
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
        dbg.location(453, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(455, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(456,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1073); if (state.failed) return ;
            dbg.location(456,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:16: ws
                    {
                    dbg.location(456,16);
                    pushFollow(FOLLOW_ws_in_page1075);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(456,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:20: ( IDENT ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:22: IDENT ( ws )?
                    {
                    dbg.location(456,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1080); if (state.failed) return ;
                    dbg.location(456,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:28: ws
                            {
                            dbg.location(456,28);
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

            dbg.location(456,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:35: ( pseudoPage ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:36: pseudoPage ( ws )?
                    {
                    dbg.location(456,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1089);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(456,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:47: ws
                            {
                            dbg.location(456,47);
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

            dbg.location(457,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1104); if (state.failed) return ;
            dbg.location(457,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:16: ws
                    {
                    dbg.location(457,16);
                    pushFollow(FOLLOW_ws_in_page1106);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(461,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:13: ( declaration | margin ( ws )? )?
            int alt71=3;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT||LA71_0==GEN||LA71_0==STAR) ) {
                alt71=1;
            }
            else if ( (LA71_0==MEDIA_SYM||LA71_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                alt71=1;
            }
            else if ( ((LA71_0>=TOPLEFTCORNER_SYM && LA71_0<=RIGHTBOTTOM_SYM)) ) {
                alt71=2;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:14: declaration
                    {
                    dbg.location(461,14);
                    pushFollow(FOLLOW_declaration_in_page1161);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:26: margin ( ws )?
                    {
                    dbg.location(461,26);
                    pushFollow(FOLLOW_margin_in_page1163);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(461,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:33: ws
                            {
                            dbg.location(461,33);
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

            dbg.location(461,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(461,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1171); if (state.failed) return ;
            	    dbg.location(461,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:45: ws
            	            {
            	            dbg.location(461,45);
            	            pushFollow(FOLLOW_ws_in_page1173);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(461,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:49: ( declaration | margin ( ws )? )?
            	    int alt74=3;
            	    try { dbg.enterSubRule(74);
            	    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            	    int LA74_0 = input.LA(1);

            	    if ( (LA74_0==IDENT||LA74_0==GEN||LA74_0==STAR) ) {
            	        alt74=1;
            	    }
            	    else if ( (LA74_0==MEDIA_SYM||LA74_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
            	        alt74=1;
            	    }
            	    else if ( ((LA74_0>=TOPLEFTCORNER_SYM && LA74_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt74=2;
            	    }
            	    } finally {dbg.exitDecision(74);}

            	    switch (alt74) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:50: declaration
            	            {
            	            dbg.location(461,50);
            	            pushFollow(FOLLOW_declaration_in_page1177);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:62: margin ( ws )?
            	            {
            	            dbg.location(461,62);
            	            pushFollow(FOLLOW_margin_in_page1179);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(461,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:69: ws
            	                    {
            	                    dbg.location(461,69);
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

            dbg.location(462,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1196); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(463, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(465, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(466,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1217); if (state.failed) return ;
            dbg.location(466,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:25: ws
                    {
                    dbg.location(466,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(466,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1222); if (state.failed) return ;
            dbg.location(466,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:35: ws
                    {
                    dbg.location(466,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1224);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(467,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1235); if (state.failed) return ;
            dbg.location(467,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:16: ws
                    {
                    dbg.location(467,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1237);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(467,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1240);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(468,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1244);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(469,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1254); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(470, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(472, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(473,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1275); if (state.failed) return ;
            dbg.location(473,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:21: ws
                    {
                    dbg.location(473,21);
                    pushFollow(FOLLOW_ws_in_fontFace1277);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(474,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1288); if (state.failed) return ;
            dbg.location(474,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:16: ws
                    {
                    dbg.location(474,16);
                    pushFollow(FOLLOW_ws_in_fontFace1290);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(474,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1293);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(475,3);
            pushFollow(FOLLOW_declarations_in_fontFace1297);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(476,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1307); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(477, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(479, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(480,4);
            pushFollow(FOLLOW_margin_sym_in_margin1322);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(480,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:15: ws
                    {
                    dbg.location(480,15);
                    pushFollow(FOLLOW_ws_in_margin1324);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(480,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1327); if (state.failed) return ;
            dbg.location(480,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:26: ws
                    {
                    dbg.location(480,26);
                    pushFollow(FOLLOW_ws_in_margin1329);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(480,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1332);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(480,53);
            pushFollow(FOLLOW_declarations_in_margin1334);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(480,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1336); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(481, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(483, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(484,2);
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
        dbg.location(501, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(503, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:7: COLON IDENT
            {
            dbg.location(504,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1565); if (state.failed) return ;
            dbg.location(504,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1567); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:1: operator : ( SOLIDUS | COMMA );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(507, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:5: ( SOLIDUS | COMMA )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(508,5);
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
        dbg.location(510, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(512, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:7: PLUS ( ws )?
                    {
                    dbg.location(513,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1617); if (state.failed) return ;
                    dbg.location(513,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:12: ws
                            {
                            dbg.location(513,12);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:7: GREATER ( ws )?
                    {
                    dbg.location(514,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1628); if (state.failed) return ;
                    dbg.location(514,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:15: ws
                            {
                            dbg.location(514,15);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:7: TILDE ( ws )?
                    {
                    dbg.location(515,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1639); if (state.failed) return ;
                    dbg.location(515,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:13: ws
                            {
                            dbg.location(515,13);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:5: 
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
        dbg.location(517, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:519:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(519, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(520,5);
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
        dbg.location(522, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:1: property : ( IDENT | GEN | {...}? => less_variable ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(524, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:5: ( ( IDENT | GEN | {...}? => less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:7: ( IDENT | GEN | {...}? => less_variable ) ( ws )?
            {
            dbg.location(525,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:7: ( IDENT | GEN | {...}? => less_variable )
            int alt87=3;
            try { dbg.enterSubRule(87);
            try { dbg.enterDecision(87, decisionCanBacktrack[87]);

            int LA87_0 = input.LA(1);

            if ( (LA87_0==IDENT) ) {
                alt87=1;
            }
            else if ( (LA87_0==GEN) ) {
                alt87=2;
            }
            else if ( (LA87_0==MEDIA_SYM||LA87_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                alt87=3;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:8: IDENT
                    {
                    dbg.location(525,8);
                    match(input,IDENT,FOLLOW_IDENT_in_property1702); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:16: GEN
                    {
                    dbg.location(525,16);
                    match(input,GEN,FOLLOW_GEN_in_property1706); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:22: {...}? => less_variable
                    {
                    dbg.location(525,22);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "property", "isLessSource()");
                    }
                    dbg.location(525,41);
                    pushFollow(FOLLOW_less_variable_in_property1712);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(525,56);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:56: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:56: ws
                    {
                    dbg.location(525,56);
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
        dbg.location(526, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:1: rule : ( selectorsGroup | {...}? => less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(528, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:5: ( ( selectorsGroup | {...}? => less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:9: ( selectorsGroup | {...}? => less_mixin_declaration ) LBRACE ( ws )? syncToFollow declarations RBRACE
            {
            dbg.location(529,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:9: ( selectorsGroup | {...}? => less_mixin_declaration )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:11: selectorsGroup
                    {
                    dbg.location(529,11);
                    pushFollow(FOLLOW_selectorsGroup_in_rule1742);
                    selectorsGroup();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:28: {...}? => less_mixin_declaration
                    {
                    dbg.location(529,28);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "rule", "isLessSource()");
                    }
                    dbg.location(529,47);
                    pushFollow(FOLLOW_less_mixin_declaration_in_rule1748);
                    less_mixin_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(89);}

            dbg.location(531,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1761); if (state.failed) return ;
            dbg.location(531,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:531:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:531:16: ws
                    {
                    dbg.location(531,16);
                    pushFollow(FOLLOW_ws_in_rule1763);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(531,20);
            pushFollow(FOLLOW_syncToFollow_in_rule1766);
            syncToFollow();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(532,13);
            pushFollow(FOLLOW_declarations_in_rule1780);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(533,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1790); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well   
                    
        }
        finally {
        }
        dbg.location(534, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:1: declarations : ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call ) ( ws )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(542, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:5: ( ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call ) ( ws )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:10: ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call ) ( ws )? )*
            {
            dbg.location(544,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:10: ( ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call ) ( ws )? )*
            try { dbg.enterSubRule(93);

            loop93:
            do {
                int alt93=2;
                try { dbg.enterDecision(93, decisionCanBacktrack[93]);

                int LA93_0 = input.LA(1);

                if ( (LA93_0==IDENT||LA93_0==GEN||LA93_0==COLON||(LA93_0>=HASH && LA93_0<=PIPE)||LA93_0==LESS_AND||LA93_0==122) ) {
                    alt93=1;
                }
                else if ( (LA93_0==MEDIA_SYM||LA93_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                    alt93=1;
                }


                } finally {dbg.exitDecision(93);}

                switch (alt93) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call ) ( ws )?
            	    {
            	    dbg.location(545,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call )
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:17: ( rulePredicate )=> rule
            	            {
            	            dbg.location(546,34);
            	            pushFollow(FOLLOW_rule_in_declarations1868);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:17: ( ( declarationPredicate )=> declaration SEMI )
            	            {
            	            dbg.location(548,17);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:17: ( ( declarationPredicate )=> declaration SEMI )
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:19: ( declarationPredicate )=> declaration SEMI
            	            {
            	            dbg.location(548,43);
            	            pushFollow(FOLLOW_declaration_in_declarations1911);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(548,55);
            	            match(input,SEMI,FOLLOW_SEMI_in_declarations1913); if (state.failed) return ;

            	            }


            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:17: {...}? => less_mixin_call
            	            {
            	            dbg.location(550,17);
            	            if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
            	                if (state.backtracking>0) {state.failed=true; return ;}
            	                throw new FailedPredicateException(input, "declarations", "isLessSource()");
            	            }
            	            dbg.location(550,36);
            	            pushFollow(FOLLOW_less_mixin_call_in_declarations1953);
            	            less_mixin_call();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(91);}

            	    dbg.location(552,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:13: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:13: ws
            	            {
            	            dbg.location(552,13);
            	            pushFollow(FOLLOW_ws_in_declarations1993);
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
        dbg.location(556, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:1: rulePredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE ;
    public final void rulePredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rulePredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(558, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: (~ ( LBRACE | SEMI | RBRACE ) )+ LBRACE
            {
            dbg.location(562,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: (~ ( LBRACE | SEMI | RBRACE ) )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(562,7);
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

            dbg.location(562,37);
            match(input,LBRACE,FOLLOW_LBRACE_in_rulePredicate2075); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "rulePredicate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "rulePredicate"


    // $ANTLR start "declarationPredicate"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:1: declarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI ;
    public final void declarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(565, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:5: (~ ( LBRACE | SEMI | RBRACE ) )+ SEMI
            {
            dbg.location(569,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:5: (~ ( LBRACE | SEMI | RBRACE ) )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(569,7);
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

            dbg.location(569,37);
            match(input,SEMI,FOLLOW_SEMI_in_declarationPredicate2135); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(570, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:572:1: lastDeclarationPredicate options {k=1; } : (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE ;
    public final void lastDeclarationPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "lastDeclarationPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(572, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:5: ( (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:5: (~ ( LBRACE | SEMI | RBRACE ) )+ RBRACE
            {
            dbg.location(576,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:5: (~ ( LBRACE | SEMI | RBRACE ) )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:7: ~ ( LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(576,7);
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

            dbg.location(576,37);
            match(input,RBRACE,FOLLOW_RBRACE_in_lastDeclarationPredicate2195); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(577, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(579, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(580,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup2216);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(580,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:16: ( COMMA ( ws )? selector )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(580,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup2219); if (state.failed) return ;
            	    dbg.location(580,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:23: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:23: ws
            	            {
            	            dbg.location(580,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup2221);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(97);}

            	    dbg.location(580,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup2224);
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
        dbg.location(581, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(583, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(584,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector2247);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(584,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:30: ( combinator simpleSelectorSequence )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(584,31);
            	    pushFollow(FOLLOW_combinator_in_selector2250);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(584,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector2252);
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
        dbg.location(585, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(588, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:2: ( ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* ) | ( ( ( esPred )=> elementSubsequent ( ws )? )+ ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    {
                    dbg.location(591,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:2: ( typeSelector ( ( esPred )=> elementSubsequent ( ws )? )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:4: typeSelector ( ( esPred )=> elementSubsequent ( ws )? )*
                    {
                    dbg.location(591,4);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence2285);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(591,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:17: ( ( esPred )=> elementSubsequent ( ws )? )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:18: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(591,28);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2292);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(591,46);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:46: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:46: ws
                    	            {
                    	            dbg.location(591,46);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2294);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    {
                    dbg.location(593,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:2: ( ( ( esPred )=> elementSubsequent ( ws )? )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:4: ( ( esPred )=> elementSubsequent ( ws )? )+
                    {
                    dbg.location(593,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:4: ( ( esPred )=> elementSubsequent ( ws )? )+
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:5: ( esPred )=> elementSubsequent ( ws )?
                    	    {
                    	    dbg.location(593,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence2313);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(593,33);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:33: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:33: ws
                    	            {
                    	            dbg.location(593,33);
                    	            pushFollow(FOLLOW_ws_in_simpleSelectorSequence2315);
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
        dbg.location(594, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(601, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(602,5);
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
        dbg.location(603, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(605, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(607,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:6: ( ( nsPred )=> namespacePrefix )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(607,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2418);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(607,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:37: elementName ( ws )?
            {
            dbg.location(607,37);
            pushFollow(FOLLOW_elementName_in_typeSelector2424);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(607,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:49: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:49: ws
                    {
                    dbg.location(607,49);
                    pushFollow(FOLLOW_ws_in_typeSelector2426);
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
        dbg.location(609, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(612, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(614,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:3: ( IDENT | STAR )?
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
                    dbg.location(614,3);
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

            dbg.location(614,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred2456); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "nsPred");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "nsPred"


    // $ANTLR start "namespacePrefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(617, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(618,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( namespacePrefixName | STAR )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:7: namespacePrefixName
                    {
                    dbg.location(618,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2471);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:29: STAR
                    {
                    dbg.location(618,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2475); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(108);}

            dbg.location(618,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2479); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(619, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(622, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:5: ( ( cssId | cssClass | slAttribute | pseudo ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:5: ( cssId | cssClass | slAttribute | pseudo )
            {
            dbg.location(624,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:5: ( cssId | cssClass | slAttribute | pseudo )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:6: cssId
                    {
                    dbg.location(625,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2513);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:8: cssClass
                    {
                    dbg.location(626,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2522);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:11: slAttribute
                    {
                    dbg.location(627,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2534);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:11: pseudo
                    {
                    dbg.location(628,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2546);
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
        dbg.location(630, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(633, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:5: ( HASH | ( '#' NAME ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:7: HASH
                    {
                    dbg.location(634,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2574); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:14: ( '#' NAME )
                    {
                    dbg.location(634,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:16: '#' NAME
                    {
                    dbg.location(634,16);
                    match(input,122,FOLLOW_122_in_cssId2580); if (state.failed) return ;
                    dbg.location(634,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2582); if (state.failed) return ;

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
        dbg.location(635, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(641, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:7: DOT ( IDENT | GEN )
            {
            dbg.location(642,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2610); if (state.failed) return ;
            dbg.location(642,11);
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
        dbg.location(643, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:1: elementName : ( ( IDENT | GEN | LESS_AND ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(650, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:5: ( ( IDENT | GEN | LESS_AND ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(651,5);
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
        dbg.location(652, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(654, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(655,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2684); if (state.failed) return ;
            dbg.location(656,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:6: ( namespacePrefix )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:6: namespacePrefix
                    {
                    dbg.location(656,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2691);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(656,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:23: ws
                    {
                    dbg.location(656,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2694);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}

            dbg.location(657,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2705);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(657,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:25: ws
                    {
                    dbg.location(657,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2707);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}

            dbg.location(659,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(660,17);
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

                    dbg.location(668,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:17: ws
                            {
                            dbg.location(668,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2929);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(114);}

                    dbg.location(669,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2948);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(670,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:17: ws
                            {
                            dbg.location(670,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2966);
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

            dbg.location(673,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2995); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(674, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(681, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:4: IDENT
            {
            dbg.location(682,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName3011); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(683, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(685, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:2: ( IDENT | STRING )
            {
            dbg.location(687,2);
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
        dbg.location(691, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(693, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(694,7);
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

            dbg.location(695,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(696,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:697:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(697,21);
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

                    dbg.location(698,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(699,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:25: ws
                                    {
                                    dbg.location(699,25);
                                    pushFollow(FOLLOW_ws_in_pseudo3206);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(117);}

                            dbg.location(699,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3209); if (state.failed) return ;
                            dbg.location(699,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:36: ws
                                    {
                                    dbg.location(699,36);
                                    pushFollow(FOLLOW_ws_in_pseudo3211);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(118);}

                            dbg.location(699,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:40: ( expression | '*' )?
                            int alt119=3;
                            try { dbg.enterSubRule(119);
                            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

                            int LA119_0 = input.LA(1);

                            if ( ((LA119_0>=IDENT && LA119_0<=URI)||LA119_0==GEN||LA119_0==PERCENTAGE||LA119_0==PLUS||(LA119_0>=MINUS && LA119_0<=HASH)||(LA119_0>=NUMBER && LA119_0<=DIMENSION)) ) {
                                alt119=1;
                            }
                            else if ( (LA119_0==MEDIA_SYM||LA119_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                                alt119=1;
                            }
                            else if ( (LA119_0==STAR) ) {
                                alt119=2;
                            }
                            } finally {dbg.exitDecision(119);}

                            switch (alt119) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:42: expression
                                    {
                                    dbg.location(699,42);
                                    pushFollow(FOLLOW_expression_in_pseudo3216);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:55: '*'
                                    {
                                    dbg.location(699,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo3220); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(119);}

                            dbg.location(699,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3225); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(703,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(703,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo3304); if (state.failed) return ;
                    dbg.location(703,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:23: ws
                            {
                            dbg.location(703,23);
                            pushFollow(FOLLOW_ws_in_pseudo3306);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}

                    dbg.location(703,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo3309); if (state.failed) return ;
                    dbg.location(703,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:34: ws
                            {
                            dbg.location(703,34);
                            pushFollow(FOLLOW_ws_in_pseudo3311);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(122);}

                    dbg.location(703,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:38: ( simpleSelectorSequence )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:38: simpleSelectorSequence
                            {
                            dbg.location(703,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo3314);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(123);}

                    dbg.location(703,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo3317); if (state.failed) return ;

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
        dbg.location(705, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(707, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:5: ( STAR )? property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(710,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:5: ( STAR )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:5: STAR
                    {
                    dbg.location(710,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3361); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(710,11);
            pushFollow(FOLLOW_property_in_declaration3364);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(710,20);
            match(input,COLON,FOLLOW_COLON_in_declaration3366); if (state.failed) return ;
            dbg.location(710,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:26: ws
                    {
                    dbg.location(710,26);
                    pushFollow(FOLLOW_ws_in_declaration3368);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(710,30);
            pushFollow(FOLLOW_propertyValue_in_declaration3371);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(710,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:44: ( prio )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:44: prio
                    {
                    dbg.location(710,44);
                    pushFollow(FOLLOW_prio_in_declaration3373);
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
        dbg.location(711, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:1: propertyValue : ( ( expressionPredicate )=> expression | {...}? => less_expression );
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(719, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:2: ( ( expressionPredicate )=> expression | {...}? => less_expression )
            int alt128=2;
            try { dbg.enterDecision(128, decisionCanBacktrack[128]);

            try {
                isCyclicDecision = true;
                alt128 = dfa128.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(128);}

            switch (alt128) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:9: ( expressionPredicate )=> expression
                    {
                    dbg.location(721,32);
                    pushFollow(FOLLOW_expression_in_propertyValue3409);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:9: {...}? => less_expression
                    {
                    dbg.location(723,9);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "propertyValue", "isLessSource()");
                    }
                    dbg.location(723,28);
                    pushFollow(FOLLOW_less_expression_in_propertyValue3433);
                    less_expression();

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
        dbg.location(726, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:1: expressionPredicate options {k=1; } : (~ ( PLUS | MINUS | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) ;
    public final void expressionPredicate() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expressionPredicate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(729, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:5: ( (~ ( PLUS | MINUS | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:5: (~ ( PLUS | MINUS | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+ ( SEMI | RBRACE )
            {
            dbg.location(732,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:5: (~ ( PLUS | MINUS | STAR | SOLIDUS | LBRACE | SEMI | RBRACE ) )+
            int cnt129=0;
            try { dbg.enterSubRule(129);

            loop129:
            do {
                int alt129=2;
                try { dbg.enterDecision(129, decisionCanBacktrack[129]);

                int LA129_0 = input.LA(1);

                if ( ((LA129_0>=NAMESPACE_SYM && LA129_0<=CHARSET_SYM)||(LA129_0>=IMPORT_SYM && LA129_0<=MEDIA_SYM)||(LA129_0>=COMMA && LA129_0<=COLON)||(LA129_0>=GREATER && LA129_0<=TILDE)||(LA129_0>=HASH && LA129_0<=DCOLON)||(LA129_0>=PIPE && LA129_0<=124)) ) {
                    alt129=1;
                }


                } finally {dbg.exitDecision(129);}

                switch (alt129) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:7: ~ ( PLUS | MINUS | STAR | SOLIDUS | LBRACE | SEMI | RBRACE )
            	    {
            	    dbg.location(732,7);
            	    if ( (input.LA(1)>=NAMESPACE_SYM && input.LA(1)<=CHARSET_SYM)||(input.LA(1)>=IMPORT_SYM && input.LA(1)<=MEDIA_SYM)||(input.LA(1)>=COMMA && input.LA(1)<=COLON)||(input.LA(1)>=GREATER && input.LA(1)<=TILDE)||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||(input.LA(1)>=PIPE && input.LA(1)<=124) ) {
            	        input.consume();
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

            dbg.location(732,69);
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
        dbg.location(733, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                //why sync to DOT? - LESS allows class rules nested
                syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(737, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:6: 
            {
            }

        }
        finally {
        }
        dbg.location(743, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(745, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:6: 
            {
            }

        }
        finally {
        }
        dbg.location(750, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
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
            dbg.exitRule(getGrammarFileName(), "syncToFollow");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToFollow"


    // $ANTLR start "prio"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:760:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(760, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:7: IMPORTANT_SYM
            {
            dbg.location(761,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3632); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:764:1: expression : term ( ( operator ( ws )? )? term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(764, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:5: ( term ( ( operator ( ws )? )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:7: term ( ( operator ( ws )? )? term )*
            {
            dbg.location(765,7);
            pushFollow(FOLLOW_term_in_expression3653);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(765,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:12: ( ( operator ( ws )? )? term )*
            try { dbg.enterSubRule(132);

            loop132:
            do {
                int alt132=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:14: ( operator ( ws )? )? term
            	    {
            	    dbg.location(765,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:14: ( operator ( ws )? )?
            	    int alt131=2;
            	    try { dbg.enterSubRule(131);
            	    try { dbg.enterDecision(131, decisionCanBacktrack[131]);

            	    int LA131_0 = input.LA(1);

            	    if ( (LA131_0==COMMA||LA131_0==SOLIDUS) ) {
            	        alt131=1;
            	    }
            	    } finally {dbg.exitDecision(131);}

            	    switch (alt131) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:15: operator ( ws )?
            	            {
            	            dbg.location(765,15);
            	            pushFollow(FOLLOW_operator_in_expression3658);
            	            operator();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(765,24);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:24: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:765:24: ws
            	                    {
            	                    dbg.location(765,24);
            	                    pushFollow(FOLLOW_ws_in_expression3660);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(130);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(131);}

            	    dbg.location(765,30);
            	    pushFollow(FOLLOW_term_in_expression3665);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop132;
                }
            } while (true);
            } finally {dbg.exitSubRule(132);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(766, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? => less_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(768, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? => less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? => less_variable ) ( ws )?
            {
            dbg.location(769,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:7: ( unaryOperator ( ws )? )?
            int alt134=2;
            try { dbg.enterSubRule(134);
            try { dbg.enterDecision(134, decisionCanBacktrack[134]);

            int LA134_0 = input.LA(1);

            if ( (LA134_0==PLUS||LA134_0==MINUS) ) {
                alt134=1;
            }
            } finally {dbg.exitDecision(134);}

            switch (alt134) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:9: unaryOperator ( ws )?
                    {
                    dbg.location(769,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3690);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(769,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:23: ws
                            {
                            dbg.location(769,23);
                            pushFollow(FOLLOW_ws_in_term3692);
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

            dbg.location(770,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? => less_variable )
            int alt135=8;
            try { dbg.enterSubRule(135);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(771,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:784:7: STRING
                    {
                    dbg.location(784,7);
                    match(input,STRING,FOLLOW_STRING_in_term3916); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:7: IDENT
                    {
                    dbg.location(785,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3924); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:7: GEN
                    {
                    dbg.location(786,7);
                    match(input,GEN,FOLLOW_GEN_in_term3932); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:7: URI
                    {
                    dbg.location(787,7);
                    match(input,URI,FOLLOW_URI_in_term3940); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:7: hexColor
                    {
                    dbg.location(788,7);
                    pushFollow(FOLLOW_hexColor_in_term3948);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:789:7: function
                    {
                    dbg.location(789,7);
                    pushFollow(FOLLOW_function_in_term3956);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:7: {...}? => less_variable
                    {
                    dbg.location(790,7);
                    if ( !(evalPredicate(isLessSource(),"isLessSource()")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "term", "isLessSource()");
                    }
                    dbg.location(790,26);
                    pushFollow(FOLLOW_less_variable_in_term3966);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(135);}

            dbg.location(792,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:5: ws
                    {
                    dbg.location(792,5);
                    pushFollow(FOLLOW_ws_in_term3978);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(793, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(795, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(796,5);
            pushFollow(FOLLOW_functionName_in_function3994);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(796,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:18: ws
                    {
                    dbg.location(796,18);
                    pushFollow(FOLLOW_ws_in_function3996);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(797,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function4001); if (state.failed) return ;
            dbg.location(797,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:10: ws
                    {
                    dbg.location(797,10);
                    pushFollow(FOLLOW_ws_in_function4003);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(138);}

            dbg.location(798,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt141=2;
            try { dbg.enterSubRule(141);
            try { dbg.enterDecision(141, decisionCanBacktrack[141]);

            try {
                isCyclicDecision = true;
                alt141 = dfa141.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(141);}

            switch (alt141) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:4: expression
                    {
                    dbg.location(799,4);
                    pushFollow(FOLLOW_expression_in_function4013);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(801,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(802,5);
                    pushFollow(FOLLOW_fnAttribute_in_function4031);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(802,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(140);

                    loop140:
                    do {
                        int alt140=2;
                        try { dbg.enterDecision(140, decisionCanBacktrack[140]);

                        int LA140_0 = input.LA(1);

                        if ( (LA140_0==COMMA) ) {
                            alt140=1;
                        }


                        } finally {dbg.exitDecision(140);}

                        switch (alt140) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(802,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function4034); if (state.failed) return ;
                    	    dbg.location(802,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:24: ws
                    	            {
                    	            dbg.location(802,24);
                    	            pushFollow(FOLLOW_ws_in_function4036);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(139);}

                    	    dbg.location(802,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function4039);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop140;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(140);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(141);}

            dbg.location(805,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function4060); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(806, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:1: functionName : IDENT ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(812, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:817:4: IDENT
            {
            dbg.location(817,4);
            match(input,IDENT,FOLLOW_IDENT_in_functionName4109); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(818, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:820:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(820, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(821,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute4129);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(821,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:20: ws
                    {
                    dbg.location(821,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute4131);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(142);}

            dbg.location(821,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute4134); if (state.failed) return ;
            dbg.location(821,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:29: ws
                    {
                    dbg.location(821,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute4136);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}

            dbg.location(821,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute4139);
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
        dbg.location(822, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(824, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:4: IDENT ( DOT IDENT )*
            {
            dbg.location(825,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4154); if (state.failed) return ;
            dbg.location(825,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:10: ( DOT IDENT )*
            try { dbg.enterSubRule(144);

            loop144:
            do {
                int alt144=2;
                try { dbg.enterDecision(144, decisionCanBacktrack[144]);

                int LA144_0 = input.LA(1);

                if ( (LA144_0==DOT) ) {
                    alt144=1;
                }


                } finally {dbg.exitDecision(144);}

                switch (alt144) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:11: DOT IDENT
            	    {
            	    dbg.location(825,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName4157); if (state.failed) return ;
            	    dbg.location(825,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName4159); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop144;
                }
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
        dbg.location(826, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(828, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:829:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:829:4: expression
            {
            dbg.location(829,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue4173);
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
        dbg.location(830, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:832:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(832, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:7: HASH
            {
            dbg.location(833,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor4191); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(834, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(836, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:7: ( WS | NL | COMMENT )+
            {
            dbg.location(837,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:837:7: ( WS | NL | COMMENT )+
            int cnt145=0;
            try { dbg.enterSubRule(145);

            loop145:
            do {
                int alt145=2;
                try { dbg.enterDecision(145, decisionCanBacktrack[145]);

                int LA145_0 = input.LA(1);

                if ( (LA145_0==WS||(LA145_0>=NL && LA145_0<=COMMENT)) ) {
                    alt145=1;
                }


                } finally {dbg.exitDecision(145);}

                switch (alt145) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(837,7);
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(838, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:1: less_variable_declaration : less_variable ( ws )? COLON ( ws )? less_expression SEMI ;
    public final void less_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(843, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:5: ( less_variable ( ws )? COLON ( ws )? less_expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:7: less_variable ( ws )? COLON ( ws )? less_expression SEMI
            {
            dbg.location(844,7);
            pushFollow(FOLLOW_less_variable_in_less_variable_declaration4249);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(844,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:21: ws
                    {
                    dbg.location(844,21);
                    pushFollow(FOLLOW_ws_in_less_variable_declaration4251);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(146);}

            dbg.location(844,25);
            match(input,COLON,FOLLOW_COLON_in_less_variable_declaration4254); if (state.failed) return ;
            dbg.location(844,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:31: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:844:31: ws
                    {
                    dbg.location(844,31);
                    pushFollow(FOLLOW_ws_in_less_variable_declaration4256);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(147);}

            dbg.location(844,35);
            pushFollow(FOLLOW_less_expression_in_less_variable_declaration4259);
            less_expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(844,51);
            match(input,SEMI,FOLLOW_SEMI_in_less_variable_declaration4261); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(845, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:1: less_variable : ( AT_IDENT | MEDIA_SYM );
    public final void less_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(848, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:5: ( AT_IDENT | MEDIA_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(849,5);
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
        dbg.location(850, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_variable"


    // $ANTLR start "less_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:853:1: less_expression : less_additionExp ;
    public final void less_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(853, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:5: ( less_additionExp )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:854:10: less_additionExp
            {
            dbg.location(854,10);
            pushFollow(FOLLOW_less_additionExp_in_less_expression4305);
            less_additionExp();

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
        dbg.location(855, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_expression"


    // $ANTLR start "less_additionExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:857:1: less_additionExp : less_multiplyExp ( PLUS ( ws )? less_multiplyExp | MINUS ( ws )? less_multiplyExp )* ;
    public final void less_additionExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_additionExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(857, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:5: ( less_multiplyExp ( PLUS ( ws )? less_multiplyExp | MINUS ( ws )? less_multiplyExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:10: less_multiplyExp ( PLUS ( ws )? less_multiplyExp | MINUS ( ws )? less_multiplyExp )*
            {
            dbg.location(858,10);
            pushFollow(FOLLOW_less_multiplyExp_in_less_additionExp4325);
            less_multiplyExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(859,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:10: ( PLUS ( ws )? less_multiplyExp | MINUS ( ws )? less_multiplyExp )*
            try { dbg.enterSubRule(150);

            loop150:
            do {
                int alt150=3;
                try { dbg.enterDecision(150, decisionCanBacktrack[150]);

                int LA150_0 = input.LA(1);

                if ( (LA150_0==PLUS) ) {
                    alt150=1;
                }
                else if ( (LA150_0==MINUS) ) {
                    alt150=2;
                }


                } finally {dbg.exitDecision(150);}

                switch (alt150) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:12: PLUS ( ws )? less_multiplyExp
            	    {
            	    dbg.location(859,12);
            	    match(input,PLUS,FOLLOW_PLUS_in_less_additionExp4339); if (state.failed) return ;
            	    dbg.location(859,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:17: ws
            	            {
            	            dbg.location(859,17);
            	            pushFollow(FOLLOW_ws_in_less_additionExp4341);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(148);}

            	    dbg.location(859,21);
            	    pushFollow(FOLLOW_less_multiplyExp_in_less_additionExp4344);
            	    less_multiplyExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:12: MINUS ( ws )? less_multiplyExp
            	    {
            	    dbg.location(860,12);
            	    match(input,MINUS,FOLLOW_MINUS_in_less_additionExp4357); if (state.failed) return ;
            	    dbg.location(860,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:18: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:860:18: ws
            	            {
            	            dbg.location(860,18);
            	            pushFollow(FOLLOW_ws_in_less_additionExp4359);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(149);}

            	    dbg.location(860,22);
            	    pushFollow(FOLLOW_less_multiplyExp_in_less_additionExp4362);
            	    less_multiplyExp();

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
        dbg.location(862, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_additionExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_additionExp"


    // $ANTLR start "less_multiplyExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:1: less_multiplyExp : less_atomExp ( STAR ( ws )? less_atomExp | SOLIDUS ( ws )? less_atomExp )* ;
    public final void less_multiplyExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_multiplyExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(864, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:5: ( less_atomExp ( STAR ( ws )? less_atomExp | SOLIDUS ( ws )? less_atomExp )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:10: less_atomExp ( STAR ( ws )? less_atomExp | SOLIDUS ( ws )? less_atomExp )*
            {
            dbg.location(865,10);
            pushFollow(FOLLOW_less_atomExp_in_less_multiplyExp4395);
            less_atomExp();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(866,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:10: ( STAR ( ws )? less_atomExp | SOLIDUS ( ws )? less_atomExp )*
            try { dbg.enterSubRule(153);

            loop153:
            do {
                int alt153=3;
                try { dbg.enterDecision(153, decisionCanBacktrack[153]);

                int LA153_0 = input.LA(1);

                if ( (LA153_0==STAR) ) {
                    alt153=1;
                }
                else if ( (LA153_0==SOLIDUS) ) {
                    alt153=2;
                }


                } finally {dbg.exitDecision(153);}

                switch (alt153) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:12: STAR ( ws )? less_atomExp
            	    {
            	    dbg.location(866,12);
            	    match(input,STAR,FOLLOW_STAR_in_less_multiplyExp4408); if (state.failed) return ;
            	    dbg.location(866,17);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:17: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:17: ws
            	            {
            	            dbg.location(866,17);
            	            pushFollow(FOLLOW_ws_in_less_multiplyExp4410);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(151);}

            	    dbg.location(866,21);
            	    pushFollow(FOLLOW_less_atomExp_in_less_multiplyExp4413);
            	    less_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    dbg.enterAlt(2);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:12: SOLIDUS ( ws )? less_atomExp
            	    {
            	    dbg.location(867,12);
            	    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_less_multiplyExp4427); if (state.failed) return ;
            	    dbg.location(867,20);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:20: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:20: ws
            	            {
            	            dbg.location(867,20);
            	            pushFollow(FOLLOW_ws_in_less_multiplyExp4429);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(152);}

            	    dbg.location(867,24);
            	    pushFollow(FOLLOW_less_atomExp_in_less_multiplyExp4432);
            	    less_atomExp();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(869, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_multiplyExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_multiplyExp"


    // $ANTLR start "less_atomExp"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:871:1: less_atomExp : ( term ( ( term )=> term )* | LPAREN ( ws )? less_additionExp RPAREN ( ws )? );
    public final void less_atomExp() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_atomExp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(871, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:5: ( term ( ( term )=> term )* | LPAREN ( ws )? less_additionExp RPAREN ( ws )? )
            int alt157=2;
            try { dbg.enterDecision(157, decisionCanBacktrack[157]);

            int LA157_0 = input.LA(1);

            if ( ((LA157_0>=IDENT && LA157_0<=URI)||LA157_0==GEN||LA157_0==PERCENTAGE||LA157_0==PLUS||(LA157_0>=MINUS && LA157_0<=HASH)||(LA157_0>=NUMBER && LA157_0<=DIMENSION)) ) {
                alt157=1;
            }
            else if ( (LA157_0==MEDIA_SYM||LA157_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                alt157=1;
            }
            else if ( (LA157_0==LPAREN) ) {
                alt157=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:10: term ( ( term )=> term )*
                    {
                    dbg.location(872,10);
                    pushFollow(FOLLOW_term_in_less_atomExp4465);
                    term();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(872,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:15: ( ( term )=> term )*
                    try { dbg.enterSubRule(154);

                    loop154:
                    do {
                        int alt154=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:16: ( term )=> term
                    	    {
                    	    dbg.location(872,24);
                    	    pushFollow(FOLLOW_term_in_less_atomExp4472);
                    	    term();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:10: LPAREN ( ws )? less_additionExp RPAREN ( ws )?
                    {
                    dbg.location(873,10);
                    match(input,LPAREN,FOLLOW_LPAREN_in_less_atomExp4486); if (state.failed) return ;
                    dbg.location(873,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:17: ws
                            {
                            dbg.location(873,17);
                            pushFollow(FOLLOW_ws_in_less_atomExp4488);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(155);}

                    dbg.location(873,21);
                    pushFollow(FOLLOW_less_additionExp_in_less_atomExp4491);
                    less_additionExp();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(873,38);
                    match(input,RPAREN,FOLLOW_RPAREN_in_less_atomExp4493); if (state.failed) return ;
                    dbg.location(873,45);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:45: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:45: ws
                            {
                            dbg.location(873,45);
                            pushFollow(FOLLOW_ws_in_less_atomExp4495);
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
            dbg.exitRule(getGrammarFileName(), "less_atomExp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_atomExp"


    // $ANTLR start "less_term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:1: less_term : ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? ;
    public final void less_term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(877, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:5: ( ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )?
            {
            dbg.location(879,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:879:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )
            int alt158=8;
            try { dbg.enterSubRule(158);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(880,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:893:7: STRING
                    {
                    dbg.location(893,7);
                    match(input,STRING,FOLLOW_STRING_in_less_term4733); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:7: IDENT
                    {
                    dbg.location(894,7);
                    match(input,IDENT,FOLLOW_IDENT_in_less_term4741); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:895:7: GEN
                    {
                    dbg.location(895,7);
                    match(input,GEN,FOLLOW_GEN_in_less_term4749); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:7: URI
                    {
                    dbg.location(896,7);
                    match(input,URI,FOLLOW_URI_in_less_term4757); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:7: hexColor
                    {
                    dbg.location(897,7);
                    pushFollow(FOLLOW_hexColor_in_less_term4765);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:898:7: function
                    {
                    dbg.location(898,7);
                    pushFollow(FOLLOW_function_in_less_term4773);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:7: less_variable
                    {
                    dbg.location(899,7);
                    pushFollow(FOLLOW_less_variable_in_less_term4781);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(158);}

            dbg.location(901,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:5: ws
                    {
                    dbg.location(901,5);
                    pushFollow(FOLLOW_ws_in_less_term4793);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(159);}


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
            dbg.exitRule(getGrammarFileName(), "less_term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_term"


    // $ANTLR start "less_mixin_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:924:1: less_mixin_declaration : cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? ;
    public final void less_mixin_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(924, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:5: ( cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:5: cssClass ( ws )? LPAREN ( less_args_list )? RPAREN ( ws )? ( less_mixin_guarded ( ws )? )?
            {
            dbg.location(926,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_declaration4835);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(926,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:14: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:14: ws
                    {
                    dbg.location(926,14);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4837);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(926,18);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_declaration4840); if (state.failed) return ;
            dbg.location(926,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:25: ( less_args_list )?
            int alt161=2;
            try { dbg.enterSubRule(161);
            try { dbg.enterDecision(161, decisionCanBacktrack[161]);

            int LA161_0 = input.LA(1);

            if ( (LA161_0==MEDIA_SYM||LA161_0==AT_IDENT||(LA161_0>=123 && LA161_0<=124)) ) {
                alt161=1;
            }
            } finally {dbg.exitDecision(161);}

            switch (alt161) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:25: less_args_list
                    {
                    dbg.location(926,25);
                    pushFollow(FOLLOW_less_args_list_in_less_mixin_declaration4842);
                    less_args_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(161);}

            dbg.location(926,41);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_declaration4845); if (state.failed) return ;
            dbg.location(926,48);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:48: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:48: ws
                    {
                    dbg.location(926,48);
                    pushFollow(FOLLOW_ws_in_less_mixin_declaration4847);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(162);}

            dbg.location(926,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:52: ( less_mixin_guarded ( ws )? )?
            int alt164=2;
            try { dbg.enterSubRule(164);
            try { dbg.enterDecision(164, decisionCanBacktrack[164]);

            int LA164_0 = input.LA(1);

            if ( (LA164_0==LESS_WHEN) ) {
                alt164=1;
            }
            } finally {dbg.exitDecision(164);}

            switch (alt164) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:53: less_mixin_guarded ( ws )?
                    {
                    dbg.location(926,53);
                    pushFollow(FOLLOW_less_mixin_guarded_in_less_mixin_declaration4851);
                    less_mixin_guarded();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(926,72);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:72: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:926:72: ws
                            {
                            dbg.location(926,72);
                            pushFollow(FOLLOW_ws_in_less_mixin_declaration4853);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(163);}


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
        dbg.location(927, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:931:1: less_mixin_call : cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )? ;
    public final void less_mixin_call() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(931, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:932:5: ( cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:5: cssClass ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )? ( ( ws )? SEMI )?
            {
            dbg.location(933,5);
            pushFollow(FOLLOW_cssClass_in_less_mixin_call4879);
            cssClass();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(933,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:14: ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )?
            int alt167=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:15: ( ws )? LPAREN ( less_mixin_call_args )? RPAREN
                    {
                    dbg.location(933,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:15: ws
                            {
                            dbg.location(933,15);
                            pushFollow(FOLLOW_ws_in_less_mixin_call4882);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(165);}

                    dbg.location(933,19);
                    match(input,LPAREN,FOLLOW_LPAREN_in_less_mixin_call4885); if (state.failed) return ;
                    dbg.location(933,26);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:26: ( less_mixin_call_args )?
                    int alt166=2;
                    try { dbg.enterSubRule(166);
                    try { dbg.enterDecision(166, decisionCanBacktrack[166]);

                    int LA166_0 = input.LA(1);

                    if ( ((LA166_0>=IDENT && LA166_0<=URI)||LA166_0==GEN||LA166_0==PERCENTAGE||LA166_0==PLUS||(LA166_0>=MINUS && LA166_0<=HASH)||(LA166_0>=NUMBER && LA166_0<=DIMENSION)) ) {
                        alt166=1;
                    }
                    else if ( (LA166_0==MEDIA_SYM||LA166_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {
                        alt166=1;
                    }
                    } finally {dbg.exitDecision(166);}

                    switch (alt166) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:26: less_mixin_call_args
                            {
                            dbg.location(933,26);
                            pushFollow(FOLLOW_less_mixin_call_args_in_less_mixin_call4887);
                            less_mixin_call_args();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(166);}

                    dbg.location(933,48);
                    match(input,RPAREN,FOLLOW_RPAREN_in_less_mixin_call4890); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(167);}

            dbg.location(933,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:57: ( ( ws )? SEMI )?
            int alt169=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:58: ( ws )? SEMI
                    {
                    dbg.location(933,58);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:58: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:933:58: ws
                            {
                            dbg.location(933,58);
                            pushFollow(FOLLOW_ws_in_less_mixin_call4895);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(168);}

                    dbg.location(933,62);
                    match(input,SEMI,FOLLOW_SEMI_in_less_mixin_call4898); if (state.failed) return ;

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
        dbg.location(934, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:936:1: less_mixin_call_args : term ( ( COMMA | SEMI ) ( ws )? term )* ;
    public final void less_mixin_call_args() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_call_args");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(936, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:5: ( term ( ( COMMA | SEMI ) ( ws )? term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:5: term ( ( COMMA | SEMI ) ( ws )? term )*
            {
            dbg.location(940,5);
            pushFollow(FOLLOW_term_in_less_mixin_call_args4936);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(940,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:10: ( ( COMMA | SEMI ) ( ws )? term )*
            try { dbg.enterSubRule(171);

            loop171:
            do {
                int alt171=2;
                try { dbg.enterDecision(171, decisionCanBacktrack[171]);

                int LA171_0 = input.LA(1);

                if ( (LA171_0==SEMI||LA171_0==COMMA) ) {
                    alt171=1;
                }


                } finally {dbg.exitDecision(171);}

                switch (alt171) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:12: ( COMMA | SEMI ) ( ws )? term
            	    {
            	    dbg.location(940,12);
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

            	    dbg.location(940,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:940:27: ws
            	            {
            	            dbg.location(940,27);
            	            pushFollow(FOLLOW_ws_in_less_mixin_call_args4948);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(170);}

            	    dbg.location(940,31);
            	    pushFollow(FOLLOW_term_in_less_mixin_call_args4951);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(941, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:944:1: less_args_list : ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) );
    public final void less_args_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_args_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(944, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:945:5: ( ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? ) | ( '...' | '@rest...' ) )
            int alt176=2;
            try { dbg.enterDecision(176, decisionCanBacktrack[176]);

            int LA176_0 = input.LA(1);

            if ( (LA176_0==MEDIA_SYM||LA176_0==AT_IDENT) ) {
                alt176=1;
            }
            else if ( ((LA176_0>=123 && LA176_0<=124)) ) {
                alt176=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 176, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(176);}

            switch (alt176) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? )
                    {
                    dbg.location(948,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:5: ( less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:7: less_arg ( ( COMMA | SEMI ) ( ws )? less_arg )* ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )?
                    {
                    dbg.location(948,7);
                    pushFollow(FOLLOW_less_arg_in_less_args_list4993);
                    less_arg();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(948,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*
                    try { dbg.enterSubRule(173);

                    loop173:
                    do {
                        int alt173=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:18: ( COMMA | SEMI ) ( ws )? less_arg
                    	    {
                    	    dbg.location(948,18);
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

                    	    dbg.location(948,35);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:35: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:35: ws
                    	            {
                    	            dbg.location(948,35);
                    	            pushFollow(FOLLOW_ws_in_less_args_list5007);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(172);}

                    	    dbg.location(948,39);
                    	    pushFollow(FOLLOW_less_arg_in_less_args_list5010);
                    	    less_arg();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop173;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(173);}

                    dbg.location(948,50);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:50: ( ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' ) )?
                    int alt175=2;
                    try { dbg.enterSubRule(175);
                    try { dbg.enterDecision(175, decisionCanBacktrack[175]);

                    int LA175_0 = input.LA(1);

                    if ( (LA175_0==SEMI||LA175_0==COMMA) ) {
                        alt175=1;
                    }
                    } finally {dbg.exitDecision(175);}

                    switch (alt175) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:52: ( COMMA | SEMI ) ( ws )? ( '...' | '@rest...' )
                            {
                            dbg.location(948,52);
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

                            dbg.location(948,69);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:69: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:948:69: ws
                                    {
                                    dbg.location(948,69);
                                    pushFollow(FOLLOW_ws_in_less_args_list5026);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(174);}

                            dbg.location(948,73);
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
                    } finally {dbg.exitSubRule(175);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:950:5: ( '...' | '@rest...' )
                    {
                    dbg.location(950,5);
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
        dbg.location(951, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:954:1: less_arg : less_variable ( COLON ( ws )? less_expression )? ;
    public final void less_arg() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_arg");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(954, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:955:5: ( less_variable ( COLON ( ws )? less_expression )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:5: less_variable ( COLON ( ws )? less_expression )?
            {
            dbg.location(956,5);
            pushFollow(FOLLOW_less_variable_in_less_arg5083);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(956,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:19: ( COLON ( ws )? less_expression )?
            int alt178=2;
            try { dbg.enterSubRule(178);
            try { dbg.enterDecision(178, decisionCanBacktrack[178]);

            int LA178_0 = input.LA(1);

            if ( (LA178_0==COLON) ) {
                alt178=1;
            }
            } finally {dbg.exitDecision(178);}

            switch (alt178) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:21: COLON ( ws )? less_expression
                    {
                    dbg.location(956,21);
                    match(input,COLON,FOLLOW_COLON_in_less_arg5087); if (state.failed) return ;
                    dbg.location(956,27);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:27: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:956:27: ws
                            {
                            dbg.location(956,27);
                            pushFollow(FOLLOW_ws_in_less_arg5089);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(177);}

                    dbg.location(956,31);
                    pushFollow(FOLLOW_less_expression_in_less_arg5092);
                    less_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(178);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(957, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:961:1: less_mixin_guarded : LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* ;
    public final void less_mixin_guarded() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_mixin_guarded");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(961, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:962:5: ( LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:5: LESS_WHEN ( ws )? less_condition ( ( COMMA | AND ) ( ws )? less_condition )*
            {
            dbg.location(963,5);
            match(input,LESS_WHEN,FOLLOW_LESS_WHEN_in_less_mixin_guarded5118); if (state.failed) return ;
            dbg.location(963,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:15: ws
                    {
                    dbg.location(963,15);
                    pushFollow(FOLLOW_ws_in_less_mixin_guarded5120);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(179);}

            dbg.location(963,19);
            pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5123);
            less_condition();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(963,34);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:34: ( ( COMMA | AND ) ( ws )? less_condition )*
            try { dbg.enterSubRule(181);

            loop181:
            do {
                int alt181=2;
                try { dbg.enterDecision(181, decisionCanBacktrack[181]);

                int LA181_0 = input.LA(1);

                if ( ((LA181_0>=COMMA && LA181_0<=AND)) ) {
                    alt181=1;
                }


                } finally {dbg.exitDecision(181);}

                switch (alt181) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:36: ( COMMA | AND ) ( ws )? less_condition
            	    {
            	    dbg.location(963,36);
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

            	    dbg.location(963,50);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:50: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:963:50: ws
            	            {
            	            dbg.location(963,50);
            	            pushFollow(FOLLOW_ws_in_less_mixin_guarded5135);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(180);}

            	    dbg.location(963,54);
            	    pushFollow(FOLLOW_less_condition_in_less_mixin_guarded5138);
            	    less_condition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop181;
                }
            } while (true);
            } finally {dbg.exitSubRule(181);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(964, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:968:1: less_condition : ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN ;
    public final void less_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(968, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:969:5: ( ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:5: ( NOT ( ws )? )? LPAREN ( ws )? ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) ) RPAREN
            {
            dbg.location(970,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:5: ( NOT ( ws )? )?
            int alt183=2;
            try { dbg.enterSubRule(183);
            try { dbg.enterDecision(183, decisionCanBacktrack[183]);

            int LA183_0 = input.LA(1);

            if ( (LA183_0==NOT) ) {
                alt183=1;
            }
            } finally {dbg.exitDecision(183);}

            switch (alt183) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:6: NOT ( ws )?
                    {
                    dbg.location(970,6);
                    match(input,NOT,FOLLOW_NOT_in_less_condition5168); if (state.failed) return ;
                    dbg.location(970,10);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:10: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:970:10: ws
                            {
                            dbg.location(970,10);
                            pushFollow(FOLLOW_ws_in_less_condition5170);
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

            dbg.location(971,5);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_condition5179); if (state.failed) return ;
            dbg.location(971,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:12: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:971:12: ws
                    {
                    dbg.location(971,12);
                    pushFollow(FOLLOW_ws_in_less_condition5181);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(184);}

            dbg.location(972,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:972:9: ( less_function_in_condition ( ws )? | ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? ) )
            int alt189=2;
            try { dbg.enterSubRule(189);
            try { dbg.enterDecision(189, decisionCanBacktrack[189]);

            int LA189_0 = input.LA(1);

            if ( (LA189_0==IDENT) ) {
                alt189=1;
            }
            else if ( (LA189_0==MEDIA_SYM||LA189_0==AT_IDENT) ) {
                alt189=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 189, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(189);}

            switch (alt189) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:13: less_function_in_condition ( ws )?
                    {
                    dbg.location(973,13);
                    pushFollow(FOLLOW_less_function_in_condition_in_less_condition5207);
                    less_function_in_condition();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(973,40);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:40: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:973:40: ws
                            {
                            dbg.location(973,40);
                            pushFollow(FOLLOW_ws_in_less_condition5209);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(185);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:13: ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? )
                    {
                    dbg.location(975,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:13: ( less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:15: less_variable ( ( ws )? less_condition_operator ( ws )? less_expression )?
                    {
                    dbg.location(975,15);
                    pushFollow(FOLLOW_less_variable_in_less_condition5240);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(975,29);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:29: ( ( ws )? less_condition_operator ( ws )? less_expression )?
                    int alt188=2;
                    try { dbg.enterSubRule(188);
                    try { dbg.enterDecision(188, decisionCanBacktrack[188]);

                    int LA188_0 = input.LA(1);

                    if ( (LA188_0==WS||LA188_0==GREATER||LA188_0==OPEQ||(LA188_0>=NL && LA188_0<=COMMENT)||(LA188_0>=GREATER_OR_EQ && LA188_0<=LESS_OR_EQ)) ) {
                        alt188=1;
                    }
                    } finally {dbg.exitDecision(188);}

                    switch (alt188) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:30: ( ws )? less_condition_operator ( ws )? less_expression
                            {
                            dbg.location(975,30);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:30: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:30: ws
                                    {
                                    dbg.location(975,30);
                                    pushFollow(FOLLOW_ws_in_less_condition5243);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(186);}

                            dbg.location(975,34);
                            pushFollow(FOLLOW_less_condition_operator_in_less_condition5246);
                            less_condition_operator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(975,58);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:58: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:975:58: ws
                                    {
                                    dbg.location(975,58);
                                    pushFollow(FOLLOW_ws_in_less_condition5248);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(187);}

                            dbg.location(975,62);
                            pushFollow(FOLLOW_less_expression_in_less_condition5251);
                            less_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(188);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(189);}

            dbg.location(977,5);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_condition5280); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_condition");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_condition"


    // $ANTLR start "less_function_in_condition"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:981:1: less_function_in_condition : less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN ;
    public final void less_function_in_condition() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_function_in_condition");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(981, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:982:5: ( less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:5: less_fn_name ( ws )? LPAREN ( ws )? less_variable ( ws )? RPAREN
            {
            dbg.location(983,5);
            pushFollow(FOLLOW_less_fn_name_in_less_function_in_condition5306);
            less_fn_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(983,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:18: ws
                    {
                    dbg.location(983,18);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5308);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(190);}

            dbg.location(983,22);
            match(input,LPAREN,FOLLOW_LPAREN_in_less_function_in_condition5311); if (state.failed) return ;
            dbg.location(983,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:29: ws
                    {
                    dbg.location(983,29);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5313);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(191);}

            dbg.location(983,33);
            pushFollow(FOLLOW_less_variable_in_less_function_in_condition5316);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(983,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:983:47: ws
                    {
                    dbg.location(983,47);
                    pushFollow(FOLLOW_ws_in_less_function_in_condition5318);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(192);}

            dbg.location(983,51);
            match(input,RPAREN,FOLLOW_RPAREN_in_less_function_in_condition5321); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(984, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:987:1: less_fn_name : IDENT ;
    public final void less_fn_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_fn_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(987, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:988:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:989:5: IDENT
            {
            dbg.location(989,5);
            match(input,IDENT,FOLLOW_IDENT_in_less_fn_name5343); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "less_fn_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_fn_name"


    // $ANTLR start "less_condition_operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:992:1: less_condition_operator : ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ );
    public final void less_condition_operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_condition_operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(992, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:993:5: ( GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(993,5);
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
        dbg.location(995, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:17: ( rulePredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:18: rulePredicate
        {
        dbg.location(546,18);
        pushFollow(FOLLOW_rulePredicate_in_synpred1_Css31865);
        rulePredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:19: ( declarationPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:20: declarationPredicate
        {
        dbg.location(548,20);
        pushFollow(FOLLOW_declarationPredicate_in_synpred2_Css31908);
        declarationPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:18: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:19: esPred
        {
        dbg.location(591,19);
        pushFollow(FOLLOW_esPred_in_synpred3_Css32289);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:6: esPred
        {
        dbg.location(593,6);
        pushFollow(FOLLOW_esPred_in_synpred4_Css32310);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:8: nsPred
        {
        dbg.location(607,8);
        pushFollow(FOLLOW_nsPred_in_synpred5_Css32415);
        nsPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:9: ( expressionPredicate )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:10: expressionPredicate
        {
        dbg.location(721,10);
        pushFollow(FOLLOW_expressionPredicate_in_synpred6_Css33406);
        expressionPredicate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:16: ( term )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:872:17: term
        {
        dbg.location(872,17);
        pushFollow(FOLLOW_term_in_synpred7_Css34469);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

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
    protected DFA91 dfa91 = new DFA91(this);
    protected DFA101 dfa101 = new DFA101(this);
    protected DFA120 dfa120 = new DFA120(this);
    protected DFA128 dfa128 = new DFA128(this);
    protected DFA132 dfa132 = new DFA132(this);
    protected DFA135 dfa135 = new DFA135(this);
    protected DFA141 dfa141 = new DFA141(this);
    protected DFA154 dfa154 = new DFA154(this);
    protected DFA158 dfa158 = new DFA158(this);
    protected DFA167 dfa167 = new DFA167(this);
    protected DFA169 dfa169 = new DFA169(this);
    protected DFA173 dfa173 = new DFA173(this);
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
        "\2\uffff\1\2\4\uffff\1\0\1\3\2\uffff\1\1}>";
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
            return "388:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | {...}? => less_variable_declaration );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA42_7 = input.LA(1);

                         
                        int index42_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_7==WS) ) {s = 11;}

                        else if ( ((LA42_7>=IDENT && LA42_7<=STRING)||LA42_7==LBRACE) ) {s = 6;}

                        else if ( (LA42_7==COLON||(LA42_7>=NL && LA42_7<=COMMENT)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                         
                        input.seek(index42_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA42_11 = input.LA(1);

                         
                        int index42_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA42_11>=IDENT && LA42_11<=STRING)||LA42_11==LBRACE) ) {s = 6;}

                        else if ( (LA42_11==WS) ) {s = 11;}

                        else if ( (LA42_11==COLON||(LA42_11>=NL && LA42_11<=COMMENT)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                         
                        input.seek(index42_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA42_2 = input.LA(1);

                         
                        int index42_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_2==WS||(LA42_2>=NL && LA42_2<=COMMENT)) ) {s = 8;}

                        else if ( (LA42_2==IDENT||LA42_2==LBRACE||(LA42_2>=ONLY && LA42_2<=GEN)||LA42_2==LPAREN) ) {s = 9;}

                        else if ( (LA42_2==COLON) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                         
                        input.seek(index42_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA42_8 = input.LA(1);

                         
                        int index42_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_8==IDENT||LA42_8==LBRACE||(LA42_8>=ONLY && LA42_8<=GEN)||LA42_8==LPAREN) ) {s = 9;}

                        else if ( (LA42_8==WS||(LA42_8>=NL && LA42_8<=COMMENT)) ) {s = 8;}

                        else if ( (LA42_8==COLON) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                         
                        input.seek(index42_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 42, _s, input);
            error(nvae);
            throw nvae;
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
            return "()* loopback of 452:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
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
        "\3\uffff\1\0\1\1\1\uffff}>";
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
            return "529:9: ( selectorsGroup | {...}? => less_mixin_declaration )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA89_3 = input.LA(1);

                         
                        int index89_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA89_3==WS||(LA89_3>=NL && LA89_3<=COMMENT)) ) {s = 4;}

                        else if ( (LA89_3==IDENT||LA89_3==LBRACE||LA89_3==COMMA||LA89_3==GEN||LA89_3==COLON||(LA89_3>=PLUS && LA89_3<=TILDE)||(LA89_3>=HASH && LA89_3<=PIPE)||LA89_3==LESS_AND||LA89_3==122) ) {s = 1;}

                        else if ( (LA89_3==LPAREN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 5;}

                         
                        input.seek(index89_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA89_4 = input.LA(1);

                         
                        int index89_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA89_4==IDENT||LA89_4==LBRACE||LA89_4==COMMA||LA89_4==GEN||LA89_4==COLON||(LA89_4>=PLUS && LA89_4<=TILDE)||(LA89_4>=HASH && LA89_4<=PIPE)||LA89_4==LESS_AND||LA89_4==122) ) {s = 1;}

                        else if ( (LA89_4==WS||(LA89_4>=NL && LA89_4<=COMMENT)) ) {s = 4;}

                        else if ( (LA89_4==LPAREN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 5;}

                         
                        input.seek(index89_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 89, _s, input);
            error(nvae);
            throw nvae;
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
            return "545:13: ( ( rulePredicate )=> rule | ( ( declarationPredicate )=> declaration SEMI ) | {...}? => less_mixin_call )";
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

                        else if ( (LA91_0==DOT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 7;}

                        else if ( (LA91_0==LBRACKET) && (synpred1_Css3())) {s = 8;}

                        else if ( (LA91_0==COLON||LA91_0==DCOLON) && (synpred1_Css3())) {s = 9;}

                        else if ( (LA91_0==LESS_AND) && (synpred1_Css3())) {s = 10;}

                        else if ( (LA91_0==MEDIA_SYM||LA91_0==AT_IDENT) && ((synpred2_Css3()&&evalPredicate(isLessSource(),"isLessSource()")))) {s = 11;}

                         
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
                        if ( (((synpred1_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))||synpred1_Css3())) ) {s = 10;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 12;}

                         
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
            return "()* loopback of 591:17: ( ( esPred )=> elementSubsequent ( ws )? )*";
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
            return "698:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA128_eotS =
        "\15\uffff";
    static final String DFA128_eofS =
        "\15\uffff";
    static final String DFA128_minS =
        "\2\5\7\0\1\uffff\1\5\1\0\1\uffff";
    static final String DFA128_maxS =
        "\1\117\1\121\7\0\1\uffff\1\121\1\0\1\uffff";
    static final String DFA128_acceptS =
        "\11\uffff\1\2\2\uffff\1\1";
    static final String DFA128_specialS =
        "\1\2\1\3\1\11\1\12\1\7\1\5\1\10\1\4\1\0\1\uffff\1\6\1\1\1\uffff}>";
    static final String[] DFA128_transitionS = {
            "\1\4\1\3\1\6\3\uffff\1\10\6\uffff\1\5\1\10\6\uffff\1\2\25\uffff"+
            "\1\1\2\uffff\1\1\1\7\16\uffff\1\11\2\uffff\12\2",
            "\1\4\1\3\1\6\3\uffff\1\13\6\uffff\1\5\1\13\1\12\5\uffff\1\2"+
            "\31\uffff\1\7\21\uffff\12\2\2\12",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\4\1\3\1\6\3\uffff\1\13\6\uffff\1\5\1\13\1\12\5\uffff\1\2"+
            "\31\uffff\1\7\21\uffff\12\2\2\12",
            "\1\uffff",
            ""
    };

    static final short[] DFA128_eot = DFA.unpackEncodedString(DFA128_eotS);
    static final short[] DFA128_eof = DFA.unpackEncodedString(DFA128_eofS);
    static final char[] DFA128_min = DFA.unpackEncodedStringToUnsignedChars(DFA128_minS);
    static final char[] DFA128_max = DFA.unpackEncodedStringToUnsignedChars(DFA128_maxS);
    static final short[] DFA128_accept = DFA.unpackEncodedString(DFA128_acceptS);
    static final short[] DFA128_special = DFA.unpackEncodedString(DFA128_specialS);
    static final short[][] DFA128_transition;

    static {
        int numStates = DFA128_transitionS.length;
        DFA128_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA128_transition[i] = DFA.unpackEncodedString(DFA128_transitionS[i]);
        }
    }

    class DFA128 extends DFA {

        public DFA128(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 128;
            this.eot = DFA128_eot;
            this.eof = DFA128_eof;
            this.min = DFA128_min;
            this.max = DFA128_max;
            this.accept = DFA128_accept;
            this.special = DFA128_special;
            this.transition = DFA128_transition;
        }
        public String getDescription() {
            return "719:1: propertyValue : ( ( expressionPredicate )=> expression | {...}? => less_expression );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA128_8 = input.LA(1);

                         
                        int index128_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred6_Css3()&&evalPredicate(isLessSource(),"isLessSource()"))) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA128_11 = input.LA(1);

                         
                        int index128_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA128_0 = input.LA(1);

                         
                        int index128_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA128_0==PLUS||LA128_0==MINUS) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 1;}

                        else if ( (LA128_0==PERCENTAGE||(LA128_0>=NUMBER && LA128_0<=DIMENSION)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 2;}

                        else if ( (LA128_0==STRING) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 3;}

                        else if ( (LA128_0==IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                        else if ( (LA128_0==GEN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 5;}

                        else if ( (LA128_0==URI) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 6;}

                        else if ( (LA128_0==HASH) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 7;}

                        else if ( (LA128_0==MEDIA_SYM||LA128_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 8;}

                        else if ( (LA128_0==LPAREN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 9;}

                         
                        input.seek(index128_0);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA128_1 = input.LA(1);

                         
                        int index128_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA128_1==WS||(LA128_1>=NL && LA128_1<=COMMENT)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                        else if ( (LA128_1==PERCENTAGE||(LA128_1>=NUMBER && LA128_1<=DIMENSION)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 2;}

                        else if ( (LA128_1==STRING) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 3;}

                        else if ( (LA128_1==IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                        else if ( (LA128_1==GEN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 5;}

                        else if ( (LA128_1==URI) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 6;}

                        else if ( (LA128_1==HASH) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 7;}

                        else if ( (LA128_1==MEDIA_SYM||LA128_1==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 11;}

                         
                        input.seek(index128_1);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA128_7 = input.LA(1);

                         
                        int index128_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA128_5 = input.LA(1);

                         
                        int index128_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA128_10 = input.LA(1);

                         
                        int index128_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA128_10==PERCENTAGE||(LA128_10>=NUMBER && LA128_10<=DIMENSION)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 2;}

                        else if ( (LA128_10==STRING) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 3;}

                        else if ( (LA128_10==IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                        else if ( (LA128_10==GEN) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 5;}

                        else if ( (LA128_10==URI) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 6;}

                        else if ( (LA128_10==HASH) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 7;}

                        else if ( (LA128_10==MEDIA_SYM||LA128_10==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 11;}

                        else if ( (LA128_10==WS||(LA128_10>=NL && LA128_10<=COMMENT)) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 10;}

                         
                        input.seek(index128_10);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA128_4 = input.LA(1);

                         
                        int index128_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_4);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA128_6 = input.LA(1);

                         
                        int index128_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_6);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA128_2 = input.LA(1);

                         
                        int index128_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_2);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA128_3 = input.LA(1);

                         
                        int index128_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 12;}

                        else if ( (evalPredicate(isLessSource(),"isLessSource()")) ) {s = 9;}

                         
                        input.seek(index128_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 128, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA132_eotS =
        "\11\uffff";
    static final String DFA132_eofS =
        "\11\uffff";
    static final String DFA132_minS =
        "\1\5\1\uffff\1\5\2\uffff\2\5\1\uffff\1\5";
    static final String DFA132_maxS =
        "\1\117\1\uffff\1\121\2\uffff\2\121\1\uffff\1\121";
    static final String DFA132_acceptS =
        "\1\uffff\1\2\1\uffff\2\1\2\uffff\1\1\1\uffff";
    static final String DFA132_specialS =
        "\1\0\10\uffff}>";
    static final String[] DFA132_transitionS = {
            "\3\3\1\uffff\1\1\1\uffff\1\4\1\uffff\1\1\1\2\3\uffff\1\3\1\4"+
            "\6\uffff\1\3\24\uffff\2\3\2\uffff\2\3\17\uffff\2\1\12\3",
            "",
            "\1\6\2\7\3\uffff\1\7\6\uffff\2\7\1\5\5\uffff\1\7\25\uffff\1"+
            "\7\2\uffff\2\7\21\uffff\12\7\2\5",
            "",
            "",
            "\1\6\2\7\3\uffff\1\7\6\uffff\2\7\1\5\5\uffff\1\7\25\uffff\1"+
            "\7\2\uffff\2\7\21\uffff\12\7\2\5",
            "\3\7\1\uffff\1\7\1\uffff\1\7\1\uffff\2\7\3\uffff\2\7\1\10\5"+
            "\uffff\1\7\24\uffff\1\3\1\7\2\uffff\2\7\1\1\6\uffff\1\1\6\uffff"+
            "\15\7\2\10",
            "",
            "\3\7\1\uffff\1\7\1\uffff\1\7\1\uffff\2\7\3\uffff\2\7\1\10\5"+
            "\uffff\1\7\24\uffff\1\3\1\7\2\uffff\2\7\7\uffff\1\1\6\uffff"+
            "\15\7\2\10"
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
            return "()* loopback of 765:12: ( ( operator ( ws )? )? term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA132_0 = input.LA(1);

                         
                        int index132_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA132_0==SEMI||LA132_0==RBRACE||(LA132_0>=RPAREN && LA132_0<=IMPORTANT_SYM)) ) {s = 1;}

                        else if ( (LA132_0==COMMA) ) {s = 2;}

                        else if ( ((LA132_0>=IDENT && LA132_0<=URI)||LA132_0==GEN||LA132_0==PERCENTAGE||(LA132_0>=SOLIDUS && LA132_0<=PLUS)||(LA132_0>=MINUS && LA132_0<=HASH)||(LA132_0>=NUMBER && LA132_0<=DIMENSION)) ) {s = 3;}

                        else if ( (LA132_0==MEDIA_SYM||LA132_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                         
                        input.seek(index132_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 132, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA135_eotS =
        "\13\uffff";
    static final String DFA135_eofS =
        "\3\uffff\1\11\4\uffff\1\11\2\uffff";
    static final String DFA135_minS =
        "\1\5\2\uffff\1\5\4\uffff\1\5\2\uffff";
    static final String DFA135_maxS =
        "\1\117\2\uffff\1\121\4\uffff\1\121\2\uffff";
    static final String DFA135_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\uffff\1\3\1\7";
    static final String DFA135_specialS =
        "\1\0\12\uffff}>";
    static final String[] DFA135_transitionS = {
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
            return "770:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | {...}? => less_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA135_0 = input.LA(1);

                         
                        int index135_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA135_0==PERCENTAGE||(LA135_0>=NUMBER && LA135_0<=DIMENSION)) ) {s = 1;}

                        else if ( (LA135_0==STRING) ) {s = 2;}

                        else if ( (LA135_0==IDENT) ) {s = 3;}

                        else if ( (LA135_0==GEN) ) {s = 4;}

                        else if ( (LA135_0==URI) ) {s = 5;}

                        else if ( (LA135_0==HASH) ) {s = 6;}

                        else if ( (LA135_0==MEDIA_SYM||LA135_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 7;}

                         
                        input.seek(index135_0);
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
    static final String DFA141_eotS =
        "\7\uffff";
    static final String DFA141_eofS =
        "\7\uffff";
    static final String DFA141_minS =
        "\1\5\1\uffff\1\5\1\uffff\1\5\2\uffff";
    static final String DFA141_maxS =
        "\1\117\1\uffff\1\121\1\uffff\1\121\2\uffff";
    static final String DFA141_acceptS =
        "\1\uffff\1\1\1\uffff\1\1\1\uffff\1\1\1\2";
    static final String DFA141_specialS =
        "\1\0\6\uffff}>";
    static final String[] DFA141_transitionS = {
            "\1\2\2\1\3\uffff\1\3\6\uffff\1\1\1\3\6\uffff\1\1\25\uffff\1"+
            "\1\2\uffff\2\1\21\uffff\12\1",
            "",
            "\3\5\3\uffff\1\5\2\uffff\1\5\3\uffff\2\5\1\4\5\uffff\1\5\24"+
            "\uffff\2\5\2\uffff\2\5\1\6\6\uffff\1\6\6\uffff\2\5\1\uffff\12"+
            "\5\2\4",
            "",
            "\3\5\3\uffff\1\5\2\uffff\1\5\3\uffff\2\5\1\4\5\uffff\1\5\24"+
            "\uffff\2\5\2\uffff\2\5\7\uffff\1\6\6\uffff\2\5\1\uffff\12\5"+
            "\2\4",
            "",
            ""
    };

    static final short[] DFA141_eot = DFA.unpackEncodedString(DFA141_eotS);
    static final short[] DFA141_eof = DFA.unpackEncodedString(DFA141_eofS);
    static final char[] DFA141_min = DFA.unpackEncodedStringToUnsignedChars(DFA141_minS);
    static final char[] DFA141_max = DFA.unpackEncodedStringToUnsignedChars(DFA141_maxS);
    static final short[] DFA141_accept = DFA.unpackEncodedString(DFA141_acceptS);
    static final short[] DFA141_special = DFA.unpackEncodedString(DFA141_specialS);
    static final short[][] DFA141_transition;

    static {
        int numStates = DFA141_transitionS.length;
        DFA141_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA141_transition[i] = DFA.unpackEncodedString(DFA141_transitionS[i]);
        }
    }

    class DFA141 extends DFA {

        public DFA141(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 141;
            this.eot = DFA141_eot;
            this.eof = DFA141_eof;
            this.min = DFA141_min;
            this.max = DFA141_max;
            this.accept = DFA141_accept;
            this.special = DFA141_special;
            this.transition = DFA141_transition;
        }
        public String getDescription() {
            return "798:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA141_0 = input.LA(1);

                         
                        int index141_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA141_0>=STRING && LA141_0<=URI)||LA141_0==GEN||LA141_0==PERCENTAGE||LA141_0==PLUS||(LA141_0>=MINUS && LA141_0<=HASH)||(LA141_0>=NUMBER && LA141_0<=DIMENSION)) ) {s = 1;}

                        else if ( (LA141_0==IDENT) ) {s = 2;}

                        else if ( (LA141_0==MEDIA_SYM||LA141_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 3;}

                         
                        input.seek(index141_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 141, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA154_eotS =
        "\33\uffff";
    static final String DFA154_eofS =
        "\33\uffff";
    static final String DFA154_minS =
        "\1\5\1\uffff\2\5\7\uffff\1\5\7\0\1\5\7\0";
    static final String DFA154_maxS =
        "\1\117\1\uffff\2\121\7\uffff\1\121\7\0\1\121\7\0";
    static final String DFA154_acceptS =
        "\1\uffff\1\2\2\uffff\7\1\20\uffff";
    static final String DFA154_specialS =
        "\1\14\13\uffff\1\5\1\15\1\1\1\10\1\13\1\16\1\6\1\uffff\1\7\1\11"+
        "\1\3\1\0\1\2\1\12\1\4}>";
    static final String[] DFA154_transitionS = {
            "\1\6\1\5\1\10\1\uffff\1\1\1\uffff\1\12\1\uffff\2\1\3\uffff\1"+
            "\7\1\12\6\uffff\1\4\24\uffff\1\1\1\2\2\uffff\1\3\1\11\3\uffff"+
            "\1\1\13\uffff\2\1\12\4",
            "",
            "\1\16\1\15\1\20\3\uffff\1\22\6\uffff\1\17\1\22\1\13\5\uffff"+
            "\1\14\25\uffff\1\1\2\uffff\1\1\1\21\16\uffff\1\1\2\uffff\12"+
            "\14\2\13",
            "\1\26\1\25\1\30\3\uffff\1\32\6\uffff\1\27\1\32\1\23\5\uffff"+
            "\1\24\25\uffff\1\1\2\uffff\1\1\1\31\16\uffff\1\1\2\uffff\12"+
            "\24\2\23",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\16\1\15\1\20\3\uffff\1\22\6\uffff\1\17\1\22\1\13\5\uffff"+
            "\1\14\25\uffff\1\1\2\uffff\1\1\1\21\16\uffff\1\1\2\uffff\12"+
            "\14\2\13",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\26\1\25\1\30\3\uffff\1\32\6\uffff\1\27\1\32\1\23\5\uffff"+
            "\1\24\25\uffff\1\1\2\uffff\1\1\1\31\16\uffff\1\1\2\uffff\12"+
            "\24\2\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "()* loopback of 872:15: ( ( term )=> term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA154_23 = input.LA(1);

                         
                        int index154_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_23);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA154_14 = input.LA(1);

                         
                        int index154_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA154_24 = input.LA(1);

                         
                        int index154_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_24);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA154_22 = input.LA(1);

                         
                        int index154_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_22);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA154_26 = input.LA(1);

                         
                        int index154_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_26);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA154_12 = input.LA(1);

                         
                        int index154_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_12);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA154_18 = input.LA(1);

                         
                        int index154_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_18);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA154_20 = input.LA(1);

                         
                        int index154_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_20);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA154_15 = input.LA(1);

                         
                        int index154_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_15);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA154_21 = input.LA(1);

                         
                        int index154_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_21);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA154_25 = input.LA(1);

                         
                        int index154_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_25);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA154_16 = input.LA(1);

                         
                        int index154_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_16);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA154_0 = input.LA(1);

                         
                        int index154_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA154_0==SEMI||(LA154_0>=RBRACE && LA154_0<=COMMA)||LA154_0==SOLIDUS||LA154_0==STAR||(LA154_0>=RPAREN && LA154_0<=IMPORTANT_SYM)) ) {s = 1;}

                        else if ( (LA154_0==PLUS) ) {s = 2;}

                        else if ( (LA154_0==MINUS) ) {s = 3;}

                        else if ( (LA154_0==PERCENTAGE||(LA154_0>=NUMBER && LA154_0<=DIMENSION)) && (synpred7_Css3())) {s = 4;}

                        else if ( (LA154_0==STRING) && (synpred7_Css3())) {s = 5;}

                        else if ( (LA154_0==IDENT) && (synpred7_Css3())) {s = 6;}

                        else if ( (LA154_0==GEN) && (synpred7_Css3())) {s = 7;}

                        else if ( (LA154_0==URI) && (synpred7_Css3())) {s = 8;}

                        else if ( (LA154_0==HASH) && (synpred7_Css3())) {s = 9;}

                        else if ( (LA154_0==MEDIA_SYM||LA154_0==AT_IDENT) && ((synpred7_Css3()&&evalPredicate(isLessSource(),"isLessSource()")))) {s = 10;}

                         
                        input.seek(index154_0);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA154_13 = input.LA(1);

                         
                        int index154_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA154_17 = input.LA(1);

                         
                        int index154_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 10;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index154_17);
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
        "\13\uffff";
    static final String DFA158_eofS =
        "\3\uffff\1\11\4\uffff\1\11\2\uffff";
    static final String DFA158_minS =
        "\1\5\2\uffff\1\24\4\uffff\1\24\2\uffff";
    static final String DFA158_maxS =
        "\1\117\2\uffff\1\121\4\uffff\1\121\2\uffff";
    static final String DFA158_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\uffff\1\3\1\7";
    static final String DFA158_specialS =
        "\13\uffff}>";
    static final String[] DFA158_transitionS = {
            "\1\3\1\2\1\5\3\uffff\1\7\6\uffff\1\4\1\7\6\uffff\1\1\31\uffff"+
            "\1\6\21\uffff\12\1",
            "",
            "",
            "\1\10\56\uffff\1\12\14\uffff\2\10",
            "",
            "",
            "",
            "",
            "\1\10\56\uffff\1\12\14\uffff\2\10",
            "",
            ""
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
            return "879:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA167_eotS =
        "\6\uffff";
    static final String DFA167_eofS =
        "\6\uffff";
    static final String DFA167_minS =
        "\2\5\4\uffff";
    static final String DFA167_maxS =
        "\2\172\4\uffff";
    static final String DFA167_acceptS =
        "\2\uffff\1\1\3\2";
    static final String DFA167_specialS =
        "\1\0\5\uffff}>";
    static final String[] DFA167_transitionS = {
            "\1\3\3\uffff\1\3\1\uffff\1\4\1\uffff\1\3\4\uffff\1\3\1\4\1\1"+
            "\31\uffff\1\3\5\uffff\6\3\1\uffff\1\3\7\uffff\1\2\14\uffff\2"+
            "\1\50\uffff\1\3",
            "\1\5\3\uffff\1\3\1\uffff\1\5\1\uffff\1\5\4\uffff\2\5\1\1\31"+
            "\uffff\1\5\5\uffff\6\5\1\uffff\1\5\7\uffff\1\2\14\uffff\2\1"+
            "\50\uffff\1\5",
            "",
            "",
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
            return "933:14: ( ( ws )? LPAREN ( less_mixin_call_args )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA167_0 = input.LA(1);

                         
                        int index167_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA167_0==WS||(LA167_0>=NL && LA167_0<=COMMENT)) ) {s = 1;}

                        else if ( (LA167_0==LPAREN) ) {s = 2;}

                        else if ( (LA167_0==IDENT||LA167_0==SEMI||LA167_0==RBRACE||LA167_0==GEN||LA167_0==COLON||(LA167_0>=HASH && LA167_0<=PIPE)||LA167_0==LESS_AND||LA167_0==122) ) {s = 3;}

                        else if ( (LA167_0==MEDIA_SYM||LA167_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                         
                        input.seek(index167_0);
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
    static final String DFA169_eotS =
        "\6\uffff";
    static final String DFA169_eofS =
        "\6\uffff";
    static final String DFA169_minS =
        "\2\5\4\uffff";
    static final String DFA169_maxS =
        "\2\172\4\uffff";
    static final String DFA169_acceptS =
        "\2\uffff\1\1\3\2";
    static final String DFA169_specialS =
        "\1\0\5\uffff}>";
    static final String[] DFA169_transitionS = {
            "\1\3\3\uffff\1\2\1\uffff\1\4\1\uffff\1\3\4\uffff\1\3\1\4\1\1"+
            "\31\uffff\1\3\5\uffff\6\3\1\uffff\1\3\24\uffff\2\1\50\uffff"+
            "\1\3",
            "\1\5\3\uffff\1\2\1\uffff\1\5\1\uffff\1\3\4\uffff\2\5\1\1\31"+
            "\uffff\1\5\5\uffff\6\5\1\uffff\1\5\24\uffff\2\1\50\uffff\1\5",
            "",
            "",
            "",
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
            return "933:57: ( ( ws )? SEMI )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA169_0 = input.LA(1);

                         
                        int index169_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA169_0==WS||(LA169_0>=NL && LA169_0<=COMMENT)) ) {s = 1;}

                        else if ( (LA169_0==SEMI) ) {s = 2;}

                        else if ( (LA169_0==IDENT||LA169_0==RBRACE||LA169_0==GEN||LA169_0==COLON||(LA169_0>=HASH && LA169_0<=PIPE)||LA169_0==LESS_AND||LA169_0==122) ) {s = 3;}

                        else if ( (LA169_0==MEDIA_SYM||LA169_0==AT_IDENT) && (evalPredicate(isLessSource(),"isLessSource()"))) {s = 4;}

                         
                        input.seek(index169_0);
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
        "\5\uffff";
    static final String DFA173_eofS =
        "\5\uffff";
    static final String DFA173_minS =
        "\1\11\1\13\1\uffff\1\13\1\uffff";
    static final String DFA173_maxS =
        "\1\104\1\174\1\uffff\1\174\1\uffff";
    static final String DFA173_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA173_specialS =
        "\5\uffff}>";
    static final String[] DFA173_transitionS = {
            "\1\1\4\uffff\1\1\65\uffff\1\2",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\51\uffff\2\2",
            "",
            "\1\4\7\uffff\1\4\1\3\73\uffff\2\3\51\uffff\2\2",
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
            return "()* loopback of 948:16: ( ( COMMA | SEMI ) ( ws )? less_arg )*";
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
    public static final BitSet FOLLOW_less_variable_declaration_in_bodyItem735 = new BitSet(new long[]{0x0000000000000002L});
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
    public static final BitSet FOLLOW_LBRACE_in_moz_document893 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_moz_document895 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
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
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock996 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock999 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToFollow_in_webkitKeyframesBlock1002 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
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
    public static final BitSet FOLLOW_LBRACE_in_page1104 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_page1106 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0400000000000000L});
    public static final BitSet FOLLOW_declaration_in_page1161 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1163 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1165 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1171 = new BitSet(new long[]{0x0BF07FFFFA3C2A20L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_page1173 = new BitSet(new long[]{0x0BF07FFFFA2C2A20L,0x0400000000000000L});
    public static final BitSet FOLLOW_declaration_in_page1177 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1179 = new BitSet(new long[]{0x0000000000102200L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_page1181 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1217 = new BitSet(new long[]{0x0000000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1219 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1222 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1224 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1235 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1237 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1240 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1244 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1275 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fontFace1277 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1288 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_fontFace1290 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1293 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_fontFace1297 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1322 = new BitSet(new long[]{0x0000000000101000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_margin1324 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1327 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_margin1329 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1332 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
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
    public static final BitSet FOLLOW_less_variable_in_property1712 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_property1715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1742 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_less_mixin_declaration_in_rule1748 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1761 = new BitSet(new long[]{0x0BF040003A3C2820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_rule1763 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_syncToFollow_in_rule1766 = new BitSet(new long[]{0x0BF040003A2C2820L,0x0400000000000000L});
    public static final BitSet FOLLOW_declarations_in_rule1780 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_declarations1868 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_declaration_in_declarations1911 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_declarations1913 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_less_mixin_call_in_declarations1953 = new BitSet(new long[]{0x0BF040003A3C0822L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_declarations1993 = new BitSet(new long[]{0x0BF040003A2C0822L,0x0400000000000000L});
    public static final BitSet FOLLOW_set_in_rulePredicate2058 = new BitSet(new long[]{0xFFFFFFFFFFFFDDF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_LBRACE_in_rulePredicate2075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_declarationPredicate2118 = new BitSet(new long[]{0xFFFFFFFFFFFFCFF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_SEMI_in_declarationPredicate2135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_lastDeclarationPredicate2178 = new BitSet(new long[]{0xFFFFFFFFFFFFEDF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_RBRACE_in_lastDeclarationPredicate2195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2216 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup2219 = new BitSet(new long[]{0x0BF0400000140020L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup2221 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup2224 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2247 = new BitSet(new long[]{0x0BF7400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector2250 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector2252 = new BitSet(new long[]{0x0BF7400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence2285 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2292 = new BitSet(new long[]{0x0BF0400000140022L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2294 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence2313 = new BitSet(new long[]{0x0BF0400000140022L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_simpleSelectorSequence2315 = new BitSet(new long[]{0x0BF0400000040022L,0x0400000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2418 = new BitSet(new long[]{0x0B00000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2424 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred2447 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred2456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2471 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2475 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_122_in_cssId2580 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2610 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2684 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2691 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2694 = new BitSet(new long[]{0x0300000000100020L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2705 = new BitSet(new long[]{0xF000000000100000L,0x0000000000030007L});
    public static final BitSet FOLLOW_ws_in_slAttribute2707 = new BitSet(new long[]{0xF000000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_set_in_slAttribute2749 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2929 = new BitSet(new long[]{0x0000000000100060L,0x0000000000030000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2948 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030004L});
    public static final BitSet FOLLOW_ws_in_slAttribute2966 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName3011 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue3025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo3085 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo3149 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3206 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3209 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_ws_in_pseudo3211 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_expression_in_pseudo3216 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_STAR_in_pseudo3220 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo3304 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_pseudo3306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo3309 = new BitSet(new long[]{0x0BF0400000140020L,0x0400000000030010L});
    public static final BitSet FOLLOW_ws_in_pseudo3311 = new BitSet(new long[]{0x0BF0400000040020L,0x0400000000000010L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo3314 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo3317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3361 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_property_in_declaration3364 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration3366 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_declaration3368 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3371 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_prio_in_declaration3373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_expression_in_propertyValue3433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_expressionPredicate3472 = new BitSet(new long[]{0xFEF67FFFFFFFEFF0L,0x1FFFFFFFFFFFFFFFL});
    public static final BitSet FOLLOW_set_in_expressionPredicate3505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3653 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_operator_in_expression3658 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_expression3660 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_expression3665 = new BitSet(new long[]{0x0BF9C0003E3C48E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_unaryOperator_in_term3690 = new BitSet(new long[]{0x0BF040003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_term3692 = new BitSet(new long[]{0x0BF040003E2C08E0L,0x040000000000FFC0L});
    public static final BitSet FOLLOW_set_in_term3716 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_STRING_in_term3916 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_IDENT_in_term3924 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_term3932 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_URI_in_term3940 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_hexColor_in_term3948 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_function_in_term3956 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_term3966 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_term3978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3994 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_function3996 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_function4001 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_function4003 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_expression_in_function4013 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_fnAttribute_in_function4031 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_function4034 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_function4036 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_fnAttribute_in_function4039 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_function4060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute4129 = new BitSet(new long[]{0x1000000000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4131 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute4134 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_fnAttribute4136 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute4139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4154 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName4157 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName4159 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue4173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor4191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws4212 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_less_variable_declaration4249 = new BitSet(new long[]{0x0000400000100000L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_variable_declaration4251 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_less_variable_declaration4254 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_variable_declaration4256 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_expression_in_less_variable_declaration4259 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_variable_declaration4261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_variable0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_additionExp_in_less_expression4305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_multiplyExp_in_less_additionExp4325 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_PLUS_in_less_additionExp4339 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_additionExp4341 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_multiplyExp_in_less_additionExp4344 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_MINUS_in_less_additionExp4357 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_additionExp4359 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_multiplyExp_in_less_additionExp4362 = new BitSet(new long[]{0x0009000000000002L});
    public static final BitSet FOLLOW_less_atomExp_in_less_multiplyExp4395 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_STAR_in_less_multiplyExp4408 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_multiplyExp4410 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_atomExp_in_less_multiplyExp4413 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_less_multiplyExp4427 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_multiplyExp4429 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_atomExp_in_less_multiplyExp4432 = new BitSet(new long[]{0x0100800000000002L});
    public static final BitSet FOLLOW_term_in_less_atomExp4465 = new BitSet(new long[]{0x0BF940003E3C08E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_less_atomExp4472 = new BitSet(new long[]{0x0BF940003E3C08E2L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_LPAREN_in_less_atomExp4486 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_atomExp4488 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_additionExp_in_less_atomExp4491 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_atomExp4493 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_atomExp4495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_term4533 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_STRING_in_less_term4733 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_IDENT_in_less_term4741 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_GEN_in_less_term4749 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_URI_in_less_term4757 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_hexColor_in_less_term4765 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_function_in_less_term4773 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_less_variable_in_less_term4781 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_term4793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_declaration4835 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4837 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_declaration4840 = new BitSet(new long[]{0x0BF040003A2C0820L,0x1C00000000000010L});
    public static final BitSet FOLLOW_less_args_list_in_less_mixin_declaration4842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_declaration4845 = new BitSet(new long[]{0x0000000000100002L,0x0000000000070000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4847 = new BitSet(new long[]{0x0000000000000002L,0x0000000000040000L});
    public static final BitSet FOLLOW_less_mixin_guarded_in_less_mixin_declaration4851 = new BitSet(new long[]{0x0000000000100002L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_declaration4853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_less_mixin_call4879 = new BitSet(new long[]{0x0000000000100202L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call4882 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_mixin_call4885 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFD0L});
    public static final BitSet FOLLOW_less_mixin_call_args_in_less_mixin_call4887 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_mixin_call4890 = new BitSet(new long[]{0x0000000000100202L,0x0000000000030000L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call4895 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_mixin_call4898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args4936 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_mixin_call_args4940 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_ws_in_less_mixin_call_args4948 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC0L});
    public static final BitSet FOLLOW_term_in_less_mixin_call_args4951 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list4993 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_args_list4997 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5007 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_arg_in_less_args_list5010 = new BitSet(new long[]{0x0000000000004202L});
    public static final BitSet FOLLOW_set_in_less_args_list5016 = new BitSet(new long[]{0x0000000000100000L,0x1800000000030000L});
    public static final BitSet FOLLOW_ws_in_less_args_list5026 = new BitSet(new long[]{0x0000000000000000L,0x1800000000000000L});
    public static final BitSet FOLLOW_set_in_less_args_list5029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_args_list5051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_variable_in_less_arg5083 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_COLON_in_less_arg5087 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_arg5089 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_expression_in_less_arg5092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_WHEN_in_less_mixin_guarded5118 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5120 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5123 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_set_in_less_mixin_guarded5127 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_mixin_guarded5135 = new BitSet(new long[]{0x0000000000120000L,0x0000000000030008L});
    public static final BitSet FOLLOW_less_condition_in_less_mixin_guarded5138 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_NOT_in_less_condition5168 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_condition5170 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_condition5179 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_condition5181 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_function_in_condition_in_less_condition5207 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_condition5209 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_less_variable_in_less_condition5240 = new BitSet(new long[]{0x1002000000100000L,0x00000000003B0010L});
    public static final BitSet FOLLOW_ws_in_less_condition5243 = new BitSet(new long[]{0x1002000000100000L,0x00000000003B0000L});
    public static final BitSet FOLLOW_less_condition_operator_in_less_condition5246 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_ws_in_less_condition5248 = new BitSet(new long[]{0x0BF940003E3C08E0L,0x040000000003FFC8L});
    public static final BitSet FOLLOW_less_expression_in_less_condition5251 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_condition5280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_fn_name_in_less_function_in_condition5306 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030008L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5308 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LPAREN_in_less_function_in_condition5311 = new BitSet(new long[]{0x0BF040003A3C0820L,0x0400000000030000L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5313 = new BitSet(new long[]{0x0BF040003A2C0820L,0x0400000000000000L});
    public static final BitSet FOLLOW_less_variable_in_less_function_in_condition5316 = new BitSet(new long[]{0x0000000000100000L,0x0000000000030010L});
    public static final BitSet FOLLOW_ws_in_less_function_in_condition5318 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_less_function_in_condition5321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_less_fn_name5343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_less_condition_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rulePredicate_in_synpred1_Css31865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declarationPredicate_in_synpred2_Css31908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred3_Css32289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred4_Css32310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred5_Css32415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionPredicate_in_synpred6_Css33406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_synpred7_Css34469 = new BitSet(new long[]{0x0000000000000002L});

}